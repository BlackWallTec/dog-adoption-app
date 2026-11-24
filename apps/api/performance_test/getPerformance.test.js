import http from "k6/http";
import { check, sleep } from "k6";

// Comandos para correr pruebas, desde la raíz del repo (dog-adoption-app)
// Carga:
// k6 run   -e BASE_URL=http://localhost:3000   -e TIPO=carga   -e RUTA=/api/catalogo/cliente/perros   -e DESCRIPCION="lista de perros disponibles"   apps/api/performance_test/getPerformance.test.js
//
// Estrés:
// k6 run --summary-mode=full -e BASE_URL=http://localhost:3000 -e TIPO=estres -e RUTA=/api/catalogo/cliente/perros -e DESCRIPCION="lista de perros" apps/api/performance_test/getPerformance.test.js

// ─── CONFIGURACIÓN DE LA API ─────────────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || "http://10.25.93.2:3000";
const RUTA = __ENV.RUTA || "/api/catalogo/cliente/perros";
const TIPO = (__ENV.TIPO || "carga").toLowerCase();
const DESCRIPCION = __ENV.DESCRIPCION || "lista de perros";
const VALIDACION = (__ENV.VALIDACION || "no_vacio").toLowerCase();

const P95 = Number(__ENV.P95 || 200);
const VUS = Number(__ENV.VUS || 50);
// ────────────────────────────────────────────────────────────────────────────

// Configuración dinámica según el tipo de prueba elegido
const isEstres = TIPO === "estres";
const scenarios = {};
const thresholds = {};

if (!isEstres) {
  // PERFIL 1: CARGA (Rampa -> Meseta -> Bajada)
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
  // PERFIL 2: ESTRÉS (Escalones + recuperación)
  const NIVELES = (__ENV.NIVELES || "50,100,300,500").split(",").map(Number);

  const DUR = Number(__ENV.DUR || 20);
  const PAUSA = Number(__ENV.PAUSA || 10);
  const PARO = Math.max(1, PAUSA - 3);

  thresholds.http_req_failed = [
    {
      threshold: "rate<0.25",
      abortOnFail: true,
      delayAbortEval: "10s",
    },
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
    vus: 10,
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
  const res = http.get(`${BASE_URL}${RUTA}`);

  const validaciones = {
    no_vacio: (r) => Boolean(r.body && r.body.length > 0),
    vacio_permitido: () => true,
  };

  const validarRespuesta = validaciones[VALIDACION] || validaciones.no_vacio;

  check(res, {
    "GET → 200 OK": (r) => r.status === 200,
    [`Respuesta: ${DESCRIPCION}`]: validarRespuesta,
  });

  // En carga simulamos lectura del usuario; en estrés disparamos sin pausa
  if (!isEstres) {
    sleep(1);
  }
}

// ─── GENERACIÓN DEL REPORTE ──────────────────────────────────────────────────
export function handleSummary(data) {
  const nombreArchivo = __ENV.REPORTE || `reporte_${TIPO}_${Date.now()}.md`;
  const rutaSalida = `./apps/api/performance_test/performance_reports/${nombreArchivo}`;

  const duracion = data.metrics.http_req_duration?.values || {};
  const reqs = data.metrics.http_reqs?.values || {};
  const fallos = data.metrics.http_req_failed?.values || {};

  // Desglose por escalón (solo TIPO=estres)
  let tablaEscalones = "";

  if (TIPO === "estres") {
    tablaEscalones = `
## Desglose por Escalón (Punto de Quiebre)
| Escalón / Escenario | p95 Latencia | Tasa de Error |
| :--- | :--- | :--- |
`;

    Object.keys(data.metrics)
      .filter((k) => k.startsWith("http_req_duration{scenario:"))
      .sort()
      .forEach((metricKey) => {
        const escenario = metricKey.match(/scenario:(.*?)\}/)?.[1] || metricKey;

        const p95Esc = ((data.metrics[metricKey]?.values?.["p(95)"] || 0) / 1000).toFixed(2);

        const failKey = `http_req_failed{scenario:${escenario}}`;

        const errEsc = ((data.metrics[failKey]?.values?.rate || 0) * 100).toFixed(2);

        tablaEscalones += `| **${escenario}** | ${p95Esc} s | ${errEsc}% |\n`;
      });
  }

  const markdown = `# Reporte de Rendimiento k6 (${TIPO.toUpperCase()})

- **Fecha:** ${new Date().toLocaleString("es-MX")}
- **Endpoint:** \`GET ${BASE_URL}${RUTA}\`
- **Respuesta esperada:** ${DESCRIPCION}
- **Validación de respuesta:** ${VALIDACION}
- **VUs Máximos:** ${data.metrics.vus_max?.values?.max || VUS}

## Métricas Globales

| Métrica | Valor |
| :--- | :--- |
| **Latencia Mínima (min)** | ${((duracion.min || 0) / 1000).toFixed(2)} s |
| **Latencia Mediana (p50)** | ${((duracion.med || 0) / 1000).toFixed(2)} s |
| **Latencia p95** | **${((duracion["p(95)"] || 0) / 1000).toFixed(2)} s** |
| **Latencia Máxima (max)** | ${((duracion.max || 0) / 1000).toFixed(2)} s |
| **Peticiones por segundo (RPS)** | ${(reqs.rate || 0).toFixed(2)} req/s |
| **Total de peticiones** | ${reqs.count || 0} |
| **Tasa de error global** | ${((fallos.rate || 0) * 100).toFixed(2)}% |

${tablaEscalones}`;

  return {
    stdout: JSON.stringify(duracion, null, 2) + "\n",
    [rutaSalida]: markdown,
  };
}
