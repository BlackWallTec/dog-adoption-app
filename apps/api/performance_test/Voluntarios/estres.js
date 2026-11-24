// Prueba de ESTRÉS con k6 — Módulo Voluntarios
// Pregunta: ¿en qué escalón se rompe o se degrada, y se recupera al bajar la carga?
//
// ⚠️ SOLO EN LOCAL. No correr contra el despliegue: Vercel, Supabase y el rate
// limiter tienen límite de acciones y comandos.
//
// Antes de correr:
//   1. En una terminal: cd apps/api  →  pnpm start   (sin --watch)
//   2. tokens.json en esta misma carpeta (crear-cuentas.mjs + generar-tokens.mjs)
//
// Correr (desde apps/api):
//   k6 run performance_test/Voluntarios/estres.js
//   k6 run --summary-mode=full performance_test/Voluntarios/estres.js   + sección por escalón
//   k6 run -e MODE=post performance_test/Voluntarios/estres.js          estrés de la inscripción
//
// Dura unos 2.5 minutos: 4 escalones de 20 s con pausas de 10 s, más el de recuperación.
// El reporte se guarda en performance_test/performance_reports/ (ignorado por git).
//
//   - Escalones: 50, 100, 300 y 500 usuarios. La carga esperada es 50; los escalones
//     de arriba son la sobrecarga que queremos encontrar.
//   - Autenticación: cada VU usa un token de tokens.json (x-client: mobile + Bearer).
//   - Por omisión solo lee (GET), mismo endpoint que carga.js: no escribe en la BD.
//     Si hay más VUs que cuentas, algunos comparten cuenta; como es lectura, no pasa nada.
//   - Con MODE=post: inscripción POST + cancelación PATCH. Escribe en Supabase, así que
//     avisa al equipo antes de correrlo. Requiere tantas cuentas como VUs y un programa
//     con cupo_maximo >= 500; si no, dominan los 409 YA_INSCRITO y SIN_CUPO, que se
//     cuentan aparte.
import http from "k6/http";
import { check } from "k6";
import { SharedArray } from "k6/data";
import { Counter } from "k6/metrics";
import { generarReporte } from "./reporte.js";

// Mismo endpoint de lectura que carga.js.
const RUTA = __ENV.RUTA || "/api/voluntarios/cliente/programas";
// Programa de prueba para MODE=post.
const PROGRAMA = __ENV.PROGRAMA || "08b3a405-8921-42a3-9b2c-260a44f1c284";
// Escalones de usuarios simultáneos.
const NIVELES = (__ENV.NIVELES || "50,100,300,500")
  .split(",")
  .map((n) => Number(n.trim()))
  .filter((n) => Number.isInteger(n) && n > 0);
// ────────────────────────────────────────────────────────────────────────────

const BASE_URL = __ENV.BASE_URL || "http://localhost:3000";
const MODE = (__ENV.MODE || "get").toLowerCase(); // get | post
const DUR = 20; // segundos por escalón
const PAUSA = 10; // segundos entre escalones
const RECUPERACION = 10; // usuarios del escalón final

if (!["get", "post"].includes(MODE)) {
  throw new Error(`MODE debe ser get o post (recibí "${MODE}")`);
}
if (NIVELES.length === 0) throw new Error("NIVELES no tiene números válidos");

const RUTA_POST = `/api/voluntarios/programas/${PROGRAMA}/inscripciones`;

const usuarios = new SharedArray("usuarios", () => JSON.parse(open("./tokens.json")));

// Contadores para separar el cupo lleno y las colisiones de cuenta (solo MODE=post)
const sinCupo = new Counter("inscripcion_sin_cupo");
const yaInscrito = new Counter("inscripcion_ya_inscrito");

// gracefulStop menor que la pausa: bajo saturación las peticiones tardan, y hay que
// dejarlas terminar antes del siguiente escalón para no contarlas como errores.
const PARO = Math.max(1, PAUSA - 3);

const scenarios = {};
const thresholds = {
  // Válvula de seguridad: corta la prueba si 1 de cada 4 peticiones falla.
  // No es el criterio del punto de quiebre.
  http_req_failed: [{ threshold: "rate<0.25", abortOnFail: true, delayAbortEval: "10s" }],
};

NIVELES.forEach((n, i) => {
  const nombre = `vus_${String(n).padStart(5, "0")}`;
  scenarios[nombre] = {
    executor: "constant-vus",
    vus: n,
    duration: `${DUR}s`,
    startTime: `${i * (DUR + PAUSA)}s`,
    gracefulStop: `${PARO}s`,
  };
  // Estos nunca fallan: están para que el resumen imprima, por escalón,
  // la latencia y el porcentaje de errores. Ahí se ve el punto de quiebre.
  thresholds[`http_req_duration{scenario:${nombre}}`] = ["p(95)<9999999"];
  thresholds[`http_req_failed{scenario:${nombre}}`] = ["rate<2"];
});

// Escalón final con pocos usuarios: ¿la latencia regresa a lo normal?
scenarios.recuperacion = {
  executor: "constant-vus",
  vus: RECUPERACION,
  duration: `${DUR}s`,
  startTime: `${NIVELES.length * (DUR + PAUSA)}s`,
  gracefulStop: `${PARO}s`,
};
thresholds["http_req_duration{scenario:recuperacion}"] = ["p(95)<9999999"];
thresholds["http_req_failed{scenario:recuperacion}"] = ["rate<2"];

export const options = {
  scenarios,
  thresholds,
  summaryTrendStats: ["min", "med", "avg", "p(90)", "p(95)", "p(99)", "max"],
};

function leer(headers) {
  const res = http.get(`${BASE_URL}${RUTA}`, { headers, tags: { ruta: "lectura" } });
  check(res, { "GET → 200": (r) => r.status === 200 });
}

function inscribirYCancelar(headers) {
  const alta = http.post(`${BASE_URL}${RUTA_POST}`, null, {
    headers,
    tags: { ruta: "escritura" },
    responseCallback: http.expectedStatuses(201, 409),
  });
  check(alta, { "POST → 201 o 409": (r) => r.status === 201 || r.status === 409 });

  if (alta.status === 409) {
    const codigo = alta.json("code");
    if (codigo === "SIN_CUPO") sinCupo.add(1);
    if (codigo === "YA_INSCRITO") yaInscrito.add(1);
  }

  // Cancelamos solo si se inscribió, igual que en carga.js
  if (alta.status === 201) {
    const baja = http.patch(`${BASE_URL}${RUTA_POST}`, null, {
      headers,
      tags: { ruta: "cancelacion" },
    });
    check(baja, { "PATCH → 200": (r) => r.status === 200 });
  }
}

export default function () {
  const { token } = usuarios[(__VU - 1) % usuarios.length];
  const headers = { "x-client": "mobile", Authorization: `Bearer ${token}` };

  if (MODE === "get") leer(headers);
  else inscribirYCancelar(headers);

  // Sin pausa: cada usuario virtual dispara lo más rápido que puede.
}

export function handleSummary(data) {
  const notas = [
    `**Perfil:** escalones de ${NIVELES.join(", ")} VUs (${DUR} s cada uno, pausa de ${PAUSA} s) + recuperación con ${RECUPERACION} VUs`,
    `**Modo:** ${MODE}`,
  ];
  if (MODE === "post") {
    const total = (nombre) => data.metrics[nombre]?.values?.count || 0;
    notas.push(
      `**409 SIN_CUPO:** ${total("inscripcion_sin_cupo")} · **409 YA_INSCRITO:** ${total("inscripcion_ya_inscrito")} (cuentas: ${usuarios.length})`,
    );
  }

  return generarReporte(data, {
    tipo: "estres",
    baseUrl: BASE_URL,
    endpoints: MODE === "get" ? [`GET ${RUTA}`] : [`POST ${RUTA_POST}`, `PATCH ${RUTA_POST}`],
    etiqueta: "scenario",
    notas,
  });
}
