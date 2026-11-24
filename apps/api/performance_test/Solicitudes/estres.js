// Prueba de estres con k6 — Módulo Solicitudes, HU02 (ver progreso de solicitud)
// Pregunta: ¿en qué escalón se rompe o se degrada, y se recupera al bajar la carga?
//
// Antes de correr:
//   1. en un aterminal cd apps/api  →  pnpm start
//   2. en otra tokens.json en esta misma carpeta
//
// Correr (desde apps/api):
//   k6 run performance_test/Solicitudes/estres.js
//   k6 run --summary-mode=full performance_test/Solicitudes/estres.js   + sección por escalón
//
// Dura unos 2.5 minutos: 4 escalones de 20 s con pausas de 10 s, más el de recuperación.
//
//   - Autenticación: cada VU usa un token de tokens.json (x-client: mobile + Bearer).
//   - Escalones: 50, 100, 300 y 500 usuarios.
//   - Mismo endpoint que carga.js, para poder comparar las dos pruebas.

import http from "k6/http";
import { check } from "k6";
import { SharedArray } from "k6/data";

// Mismo endpoint que carga.js.
const RUTA = "/api/solicitudes/cliente/mis-solicitudes";
// Escalones de usuarios simultáneos
const NIVELES = [50, 100, 300, 500];
// ────────────────────────────────────────────────────────────────────────────

const BASE_URL = __ENV.BASE_URL || "http://localhost:3000";
const DUR = 20; // segundos por escalon
const PAUSA = 10; // segundos entre escalones
const RECUPERACION = 10; // usuarios del escalon final

// gracefulStop menor que la pausa: bajo saturación las peticiones tardan, y hay que
// dejarlas terminar antes del siguiente escalón para no contarlas como errores.
const PARO = Math.max(1, PAUSA - 3);

const usuarios = new SharedArray("usuarios", () => JSON.parse(open("./tokens.json")));

const scenarios = {};
const thresholds = {
  // Valvula de seguridad: corta la prueba si 1 de cada 4 peticiones falla.
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
  // Estos nunca fallan: están para que el resumen imprima, por escalon,
  // la latencia y el porcentaje de errores. Ahí se ve el punto de quiebre.
  thresholds[`http_req_duration{scenario:${nombre}}`] = ["p(95)<9999999"];
  thresholds[`http_req_failed{scenario:${nombre}}`] = ["rate<2"];
});

// Escalon final con pocos usuarios: ¿la latencia regresa a lo normal?
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

export default function () {
  const { token } = usuarios[(__VU - 1) % usuarios.length];

  const res = http.get(`${BASE_URL}${RUTA}`, {
    headers: { "x-client": "mobile", Authorization: `Bearer ${token}` },
  });

  check(res, { "GET → 200": (r) => r.status === 200 });

  // Sin pausa: cada usuario virtual dispara lo mas rapido que puede.
}
