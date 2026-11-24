// Prueba de ESTRÉS con k6: subir por escalones muy por encima de lo esperado para
// encontrar dónde se rompe, cómo se degrada y si se recupera.
//
// Cada escalón es un escenario aparte, así que el resumen imprime el p95 Y el
// porcentaje de errores de CADA uno. Así se ve en qué escalón empezó a romperse.
//
// Levanta el servidor SIN --watch y, en otra terminal:
//
//   k6 run estres.js
//   k6 run --summary-mode=full estres.js     además, una sección por escalón
//
// Dura poco más de 2 minutos (4 escalones de 20 s, con pausas, más el escalón de
// recuperación). Al final baja a pocos usuarios para medir si el sistema se recupera.
//
// Otras opciones:
//   -e PROGRAMA=<uuid>                otro programa de prueba
//   -e NIVELES=10,20,40,80            otros escalones
//   -e DUR=30                         más segundos por escalón
//   -e BASE_URL=http://localhost:3001
//
// CAMBIOS RESPECTO AL SCRIPT:
//   - Autenticación: cada VU usa su token de tokens.json (x-client: mobile + Bearer).
//   - Escritura: inscripción POST + cancelación PATCH, igual que carga.js.
//   - Escalones: 10, 20, 40 y 80 usuarios. El máximo esperado es 20; los escalones
//     de arriba son la sobrecarga que queremos encontrar.
//   - 409 SIN_CUPO y 409 YA_INSCRITO se cuentan aparte, con sus propios contadores.
//   - Requiere 100 cuentas de prueba (N_CUENTAS=100 al crear y generar tokens).
import http from "k6/http";
import { check, sleep } from "k6";
import { SharedArray } from "k6/data";
import { Counter } from "k6/metrics";

// ─── CAMBIA ESTO POR LO DE TU PROYECTO ──────────────────────────────────────
// CAMBIO: el endpoint es la inscripción, igual que en carga.js. No hay CUERPO.
// El programa debe tener cupo_maximo igual o mayor al escalón más alto; si no,
// los 409 SIN_CUPO dominan la prueba.
const PROGRAMA = __ENV.PROGRAMA || "08b3a405-8921-42a3-9b2c-260a44f1c284";
const RUTA = `/api/voluntarios/programas/${PROGRAMA}/inscripciones`;
// ────────────────────────────────────────────────────────────────────────────

const BASE_URL = __ENV.BASE_URL || "http://localhost:3000";
const NIVELES = (__ENV.NIVELES || "10,20,40,80")
  .split(",")
  .map((n) => Number(n.trim()))
  .filter((n) => Number.isInteger(n) && n > 0);
const DUR = Number(__ENV.DUR || 20); // segundos por escalón
const PAUSA = Number(__ENV.PAUSA || 10); // segundos entre escalones
const RECUPERACION = Number(__ENV.RECUPERACION || 10); // usuarios del escalón final

// CAMBIO: usuarios de prueba generados con generar-tokens.mjs
const usuarios = new SharedArray("usuarios", () => JSON.parse(open("./tokens.json")));

// CAMBIO: contadores para separar el cupo lleno y las colisiones de cuenta
const sinCupo = new Counter("inscripcion_sin_cupo");
const yaInscrito = new Counter("inscripcion_ya_inscrito");

if (NIVELES.length === 0) throw new Error("NIVELES no tiene números válidos");

// gracefulStop menor que la pausa: bajo saturación las peticiones tardan, y hay que
// dejarlas terminar antes del siguiente escalón para no contarlas como errores.
const PARO = Math.max(1, PAUSA - 3);
const scenarios = {};
const thresholds = {
  // Único umbral real: corta la prueba si se desploma de verdad (1 de cada 4 falla).
  // Es una válvula de seguridad, no el criterio del punto de quiebre.
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
  // Estos dos nunca fallan: están para que el resumen imprima, por escalón,
  // la latencia y el porcentaje de errores. Ahí se ve el punto de quiebre.
  thresholds[`http_req_duration{scenario:${nombre}}`] = ["p(95)<9999999"];
  thresholds[`http_req_failed{scenario:${nombre}}`] = ["rate<2"];
});

// Escalón final con pocos usuarios: ¿la latencia regresa a lo normal?
const inicioRecuperacion = NIVELES.length * (DUR + PAUSA);
scenarios.recuperacion = {
  executor: "constant-vus",
  vus: RECUPERACION,
  duration: `${DUR}s`,
  startTime: `${inicioRecuperacion}s`,
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
  // CAMBIO: cada VU toma su usuario. Con más VUs que cuentas, dos VUs comparten
  // cuenta y uno recibe 409 YA_INSCRITO, por eso se cuenta aparte.
  const { token } = usuarios[(__VU - 1) % usuarios.length];
  const headers = { "x-client": "mobile", Authorization: `Bearer ${token}` };

  const alta = http.post(`${BASE_URL}${RUTA}`, null, {
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

  // CAMBIO: cancelamos solo si se inscribió, igual que en carga.js
  if (alta.status === 201) {
    const baja = http.patch(`${BASE_URL}${RUTA}`, null, {
      headers,
      tags: { ruta: "cancelacion" },
    });
    check(baja, { "PATCH → 200": (r) => r.status === 200 });
  }

  // Sin pausa: cada usuario virtual dispara lo más rápido que puede
}
