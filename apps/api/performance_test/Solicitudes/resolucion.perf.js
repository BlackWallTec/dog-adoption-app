import http from "k6/http";
import { check, sleep } from "k6";

// Comandos para correr pruebas (asegúrate de tener creada la carpeta performance_test/performance_reports):
// Carga:  k6 run -e BASE_URL=http://localhost:3000 -e TIPO=carga -e TOKEN="TU_TOKEN" performance_test/Solicitudes/resolucion.perf.js
// Estres: k6 run --summary-mode=full -e BASE_URL=http://localhost:3000 -e TIPO=estres -e TOKEN="TU_TOKEN" performance_test/Solicitudes/resolucion.perf.js

// ─── CONFIGURACIÓN DE TU API DE SOLICITUDES ─────────────────────────────────
const BASE_URL = __ENV.BASE_URL || "http://localhost:3000";
const SOLICITUD_ID = __ENV.SOLICITUD_ID || "f3c2672c-59b6-40e2-b95c-8b148e9cae03";
const RUTA = __ENV.RUTA || `/api/solicitudes/admin/solicitudes/${SOLICITUD_ID}/resolucion`;
const TOKEN =
  __ENV.TOKEN ||
  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1dWlkIjoiNmZjNmE2YmEtODExNi00OGM3LTllY2UtZWQwYTk0ZDcyZjEwIiwicm9sIjoiUm9vdCIsImlhdCI6MTc5NTU0MjM5OSwiZXhwIjoxNzk1NTQ1OTk5fQ.4TjeJufpRkw6KVMYFxR5RC--SWZYeKCf4Y6pLRVtBiY";

const TIPO = (__ENV.TIPO || "carga").toLowerCase(); // 'carga' | 'estres'
const STATE = __ENV.STATE || "en_revision"; // 'en_revision' | 'aprobada' | 'rechazada' | 'periodo_prueba'
const COMMENT = __ENV.COMMENT || "Prueba de rendimiento k6: no cumple con espacio cerrado.";
const P95 = Number(__ENV.P95 || 1500);
const VUS = Number(__ENV.VUS || 50);
// ────────────────────────────────────────────────────────────────────────────

// Configuración dinámica según el tipo de prueba elegido
const isEstres = TIPO === "estres";
const scenarios = {};
const thresholds = {};

if (!isEstres) {
  // PERFIL 1: CARGA (Rampa -> Meseta a 50 VUs -> Bajada con pausa de lectura)
  scenarios.carga_esperada = {
    executor: "ramping-vus",
    stages: [
      { duration: "15s", target: VUS },
      { duration: "30s", target: VUS },
      { duration: "10s", target: 0 },
    ],
  };
  thresholds.http_req_duration = [`p(95)<${P95}`];
  thresholds.http_req_failed = ["rate<0.01"];
} else {
  // PERFIL 2: ESTRÉS ESCALONADO (Escalones hasta 50 VUs sin pausa + escalón de recuperación)
  const NIVELES = (__ENV.NIVELES || "10,25,40,50").split(",").map(Number);
  const DUR = Number(__ENV.DUR || 20);
  const PAUSA = Number(__ENV.PAUSA || 10);
  const PARO = Math.max(1, PAUSA - 3);

  thresholds.http_req_failed = [
    { threshold: "rate<0.25", abortOnFail: true, delayAbortEval: "10s" },
  ];

  NIVELES.forEach((n, i) => {
    const nombre = `vus_${String(n).padStart(5, "0")}`;
    scenarios[nombre] = {
      executor: "constant-vus",
      vus: n,
      duration: `${DUR}s`,
      startTime: `${i * (DUR + PAUSA)}s`,
      gracefulStop: `${PARO}s`,
    };
    thresholds[`http_req_duration{scenario:${nombre}}`] = ["p(95)<9999999"];
    thresholds[`http_req_failed{scenario:${nombre}}`] = ["rate<2"];
  });

  scenarios.recuperacion = {
    executor: "constant-vus",
    vus: 5,
    duration: `${DUR}s`,
    startTime: `${NIVELES.length * (DUR + PAUSA)}s`,
    gracefulStop: `${PARO}s`,
  };
  thresholds["http_req_duration{scenario:recuperacion}"] = ["p(95)<9999999"];
  thresholds["http_req_failed{scenario:recuperacion}"] = ["rate<2"];
}

export const options = {
  scenarios,
  thresholds,
  summaryTrendStats: ["min", "med", "avg", "p(90)", "p(95)", "p(99)", "max"],
};

export default function () {
  const payload = JSON.stringify({
    state: STATE,
    comment: STATE === "rechazada" ? COMMENT : null,
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
      Cookie: `token=${TOKEN}`,
    },
  };

  const res = http.post(`${BASE_URL}${RUTA}`, payload, params);

  check(res, {
    "POST → 200 OK": (r) => r.status === 200,
    "Devuelve confirmación de resolución": (r) => r.body && r.body.includes("Resolución guardada"),
  });

  // En carga simulamos pausa del administrador; en estrés disparamos sin pausa
  if (!isEstres) {
    sleep(1);
  }
}

export function handleSummary(data) {
  const nombreArchivo = __ENV.REPORTE || `reporte_resolucion_${TIPO}_${Date.now()}.md`;
  const rutaSalida = `performance_test/performance_reports/${nombreArchivo}`;

  const duracion = data.metrics.http_req_duration?.values || {};
  const reqs = data.metrics.http_reqs?.values || {};
  const fallos = data.metrics.http_req_failed?.values || {};

  // Desglose por escalón (solo se llena cuando TIPO=estres)
  let tablaEscalones = "";
  if (TIPO === "estres") {
    tablaEscalones = `\n## Desglose por Escalón (Punto de Quiebre)\n| Escalón / Escenario | p95 Latencia | Tasa de Error |\n| :--- | :--- | :--- |\n`;
    Object.keys(data.metrics)
      .filter((k) => k.startsWith("http_req_duration{scenario:"))
      .sort()
      .forEach((metricKey) => {
        const escenario = metricKey.match(/scenario:(.*?)\}/)?.[1] || metricKey;
        const p95Esc = (data.metrics[metricKey]?.values?.["p(95)"] / 1000).toFixed(2);
        const failKey = `http_req_failed{scenario:${escenario}}`;
        const errEsc = ((data.metrics[failKey]?.values?.rate || 0) * 100).toFixed(2);
        tablaEscalones += `| **${escenario}** | ${p95Esc} s | ${errEsc}% |\n`;
      });
  }

  const markdown = `# Reporte de Rendimiento k6 — Resolución de Solicitudes (${TIPO.toUpperCase()})
- **Fecha:** ${new Date().toLocaleString("es-MX")}
- **Endpoint:** \`POST ${BASE_URL}${RUTA}\`
- **Estado enviado:** \`${STATE}\`
- **VUs Máximos:** ${data.metrics.vus_max?.values?.max || VUS}

## Métricas Globales
| Métrica | Valor |
| :--- | :--- |
| **Latencia Mínima (min)** | ${(duracion.min / 1000).toFixed(2)} s |
| **Latencia Mediana (p50)** | ${(duracion.med / 1000).toFixed(2)} s |
| **Latencia p95** | **${(duracion["p(95)"] / 1000).toFixed(2)} s** |
| **Latencia Máxima (max)** | ${(duracion.max / 1000).toFixed(2)} s |
| **Peticiones por segundo (RPS)** | ${reqs.rate?.toFixed(2)} req/s |
| **Total de peticiones** | ${reqs.count} |
| **Tasa de error global** | ${(fallos.rate * 100).toFixed(2)}% |
${tablaEscalones}`;

  return {
    stdout: JSON.stringify(data.metrics.http_req_duration.values, null, 2) + "\n",
    [rutaSalida]: markdown,
  };
}
