// Prueba de CARGA con k6 — Módulo Voluntarios
// Pregunta: ¿con el tráfico esperado (50 usuarios) el endpoint cumple el objetivo
// de rendimiento (p95 < 200 ms)?
//
// ⚠️ SOLO EN LOCAL. No correr contra el despliegue: Vercel, Supabase y el rate
// limiter tienen límite de acciones y comandos.
//
// Antes de correr:
//   1. En una terminal: cd apps/api  →  pnpm start   (sin --watch)
//   2. tokens.json en esta misma carpeta (crear-cuentas.mjs + generar-tokens.mjs)
//
// Correr (desde apps/api):
//   k6 run performance_test/Voluntarios/carga.js                 solo lectura (GET), 50 VUs
//   k6 run -e MODE=post performance_test/Voluntarios/carga.js    solo escritura (inscripción + cancelación)
//   k6 run -e MODE=both performance_test/Voluntarios/carga.js    lectura y escritura
//   k6 run -e VUS=1 performance_test/Voluntarios/carga.js        latencia base (1 usuario)
//
// El reporte se guarda en performance_test/performance_reports/ (ignorado por git).
//
//   - Autenticación: cada VU usa su propio token de tokens.json (x-client: mobile + Bearer).
//   - Por omisión solo lee (GET), igual que los demás módulos: no escribe en la BD.
//   - Con MODE=post, la inscripción va seguida de una cancelación (PATCH) para que el
//     siguiente ciclo pueda inscribirse otra vez. Escribe en Supabase.
import http from "k6/http";
import { check, sleep } from "k6";
import { SharedArray } from "k6/data";
import { generarReporte } from "./reporte.js";

// Lectura: listado de programas con cupo disponible (requiere sesión).
const RUTA = __ENV.RUTA || "/api/voluntarios/cliente/programas";
// Programa de prueba para la escritura. Debe tener cupo_maximo >= VUS y empezar en el futuro.
const PROGRAMA = __ENV.PROGRAMA || "08b3a405-8921-42a3-9b2c-260a44f1c284";
// Objetivo de rendimiento del equipo: p95 menor a 200 ms.
const P95 = 200;
// 50 usuarios, el tráfico esperado. Se puede bajar a 1 para la latencia base.
const VUS = Number(__ENV.VUS || 50);
// ────────────────────────────────────────────────────────────────────────────

const BASE_URL = __ENV.BASE_URL || "http://localhost:3000";
const MODE = (__ENV.MODE || "get").toLowerCase(); // get | post | both

// Usuarios de prueba creados con crear-cuentas.mjs y tokenizados con generar-tokens.mjs.
// SharedArray carga el archivo una sola vez para todos los VUs.
const usuarios = new SharedArray("usuarios", () => JSON.parse(open("./tokens.json")));

if (!["get", "post", "both"].includes(MODE)) {
  throw new Error(`MODE debe ser get, post o both (recibí "${MODE}")`);
}
const haceGet = MODE === "get" || MODE === "both";
const hacePost = MODE === "post" || MODE === "both";

// La inscripción vive en /programas/:id/inscripciones, no en la ruta de lectura.
const RUTA_POST = `/api/voluntarios/programas/${PROGRAMA}/inscripciones`;

// Los dos primeros umbrales son el objetivo real: hacen que la prueba pase o falle.
// Los de cada ruta nunca fallan: están para que el resumen y el reporte muestren
// la latencia y los errores de cada ruta por separado.
const thresholds = {
  http_req_duration: [`p(95)<${P95}`], // el 95 % responde en menos de P95 ms
  http_req_failed: ["rate<0.01"], // menos de 1 % de errores
};
const rutas = [];
if (haceGet) rutas.push("lectura");
if (hacePost) rutas.push("escritura", "cancelacion");
for (const ruta of rutas) {
  thresholds[`http_req_duration{ruta:${ruta}}`] = ["p(95)<999999"];
  thresholds[`http_req_failed{ruta:${ruta}}`] = ["rate<2"];
}

export const options = {
  stages: [
    { duration: "15s", target: VUS }, // rampa de subida
    { duration: "30s", target: VUS }, // meseta: carga sostenida (lo "normal" esperado)
    { duration: "10s", target: 0 }, // bajada
  ],
  thresholds,
  summaryTrendStats: ["min", "med", "avg", "p(90)", "p(95)", "p(99)", "max"],
};

export default function () {
  // Cada VU toma su propio usuario, para que no se inscriban todos con la misma cuenta.
  const { token } = usuarios[(__VU - 1) % usuarios.length];
  const headers = { "x-client": "mobile", Authorization: `Bearer ${token}` };

  if (haceGet) {
    const res = http.get(`${BASE_URL}${RUTA}`, {
      headers,
      tags: { ruta: "lectura" }, // ← esta etiqueta separa la ruta en el resumen
    });
    check(res, { "GET → 200": (r) => r.status === 200 });
  }

  if (hacePost) {
    const alta = http.post(`${BASE_URL}${RUTA_POST}`, null, {
      headers,
      tags: { ruta: "escritura" },
    });
    check(alta, { "POST → 201": (r) => r.status === 201 });

    // Cancelamos para liberar el lugar. Si no, la siguiente iteración de este
    // mismo VU responde 409 YA_INSCRITO y cuenta como error.
    if (alta.status === 201) {
      const baja = http.patch(`${BASE_URL}${RUTA_POST}`, null, {
        headers,
        tags: { ruta: "cancelacion" },
      });
      check(baja, { "PATCH → 200": (r) => r.status === 200 });
    }
  }

  sleep(1); // el usuario "lee" la pantalla antes de la siguiente acción
}

export function handleSummary(data) {
  const endpoints = [];
  if (haceGet) endpoints.push(`GET ${RUTA}`);
  if (hacePost) endpoints.push(`POST ${RUTA_POST}`, `PATCH ${RUTA_POST}`);

  return generarReporte(data, {
    tipo: "carga",
    baseUrl: BASE_URL,
    endpoints,
    etiqueta: "ruta",
    notas: [
      `**Perfil:** rampa 15 s → meseta 30 s con ${VUS} VUs → bajada 10 s`,
      `**Modo:** ${MODE}`,
    ],
  });
}
