// Prueba de CARGA con k6: ¿aguanta el tráfico esperado cumpliendo el objetivo
// (Service Level Objective, SLO)?
//
// Ejecutar:
//   k6 run carga.js                          GET + POST, 20 usuarios
//   k6 run -e MODE=get  carga.js             solo la lectura
//   k6 run -e MODE=post carga.js             solo la escritura → ¿cuál ruta es la lenta?
//   k6 run -e MODE=get -e VUS=20 carga.js    solo la lectura, con 20 usuarios
//   k6 run -e BASE_URL=http://localhost:3000 carga.js
//
// CAMBIOS RESPECTO AL SCRIPT DEL PROFE (proyecto Qariño animal):
//   - Autenticación: cada VU usa su propio token de tokens.json (x-client: mobile + Bearer).
//   - Escritura: la inscripción va seguida de una cancelación (PATCH) para que el
//     siguiente ciclo pueda inscribirse otra vez.
//   - Usuarios: 20 por omisión, el máximo de concurrencia que esperamos.
import http from "k6/http";
import { check, sleep } from "k6";
import { SharedArray } from "k6/data";

// ─── CAMBIA ESTO POR LO DE TU PROYECTO ──────────────────────────────────────
// La ruta del endpoint que eligió tu equipo, con su query string si la necesita.
// CAMBIO: lectura = listado de programas con cupo disponible (requiere sesión).
const RUTA = __ENV.RUTA || "/api/voluntarios/cliente/programas";
// CAMBIO: id del programa de prueba. Debe tener cupo_maximo >= VUS y empezar en el futuro.
// Datos de prueba: programa 08b3a405-8921-42a3-9b2c-260a44f1c284 (BD de pruebas).
const PROGRAMA = __ENV.PROGRAMA || "08b3a405-8921-42a3-9b2c-260a44f1c284";
// El umbral de p95 que acordó tu equipo, en milisegundos. No tiene que ser 200.
const P95 = Number(__ENV.P95 || 200);
// CAMBIO: la inscripción no recibe cuerpo. El id del programa va en la ruta y el
// usuario sale del token, así que no hay CUERPO como en el script original.
// ────────────────────────────────────────────────────────────────────────────

const BASE_URL = __ENV.BASE_URL || "http://localhost:3000";
const MODE = (__ENV.MODE || "both").toLowerCase(); // get | post | both
const VUS = Number(__ENV.VUS || 20); // CAMBIO: 20 usuarios por omisión (antes 50)
const SLEEP = Number(__ENV.SLEEP ?? 1); // segundos de "lectura" entre acciones

// CAMBIO: usuarios de prueba creados con crear-cuentas.mjs y tokenizados con generar-tokens.mjs.
// SharedArray carga el archivo una sola vez para todos los VUs.
const usuarios = new SharedArray("usuarios", () => JSON.parse(open("./tokens.json")));

if (!["get", "post", "both"].includes(MODE)) {
  throw new Error(`MODE debe ser get, post o both (recibí "${MODE}")`);
}
const haceGet = MODE === "get" || MODE === "both";
const hacePost = MODE === "post" || MODE === "both";

// CAMBIO: la inscripción vive en /programas/:id/inscripciones, no en la ruta de lectura.
const RUTA_POST = `/api/voluntarios/programas/${PROGRAMA}/inscripciones`;

// Los dos primeros umbrales son el objetivo real: hacen que la prueba pase o falle.
// Los de cada ruta llevan un número enorme a propósito: nunca fallan, están solo
// para que el resumen IMPRIMA el p95 de esa ruta por separado.
const thresholds = {
  http_req_duration: [`p(95)<${P95}`], // el 95 % responde en menos de P95 ms
  http_req_failed: ["rate<0.01"], // menos de 1 % de errores
};
if (haceGet) thresholds["http_req_duration{ruta:lectura}"] = ["p(95)<999999"];
if (hacePost) thresholds["http_req_duration{ruta:escritura}"] = ["p(95)<999999"];
if (hacePost) thresholds["http_req_duration{ruta:cancelacion}"] = ["p(95)<999999"];

export const options = {
  stages: [
    { duration: "15s", target: VUS }, // rampa de subida
    { duration: "30s", target: VUS }, // carga sostenida (lo "normal" esperado)
    { duration: "10s", target: 0 }, // bajada
  ],
  thresholds,
  summaryTrendStats: ["min", "med", "avg", "p(90)", "p(95)", "p(99)", "max"],
};

export default function () {
  // CAMBIO: cada VU toma su propio usuario, para que no se inscriban todos con la misma cuenta.
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

    // CAMBIO: cancelamos para liberar el lugar. Si no, la siguiente iteración
    // de este mismo VU responde 409 YA_INSCRITO y cuenta como error.
    if (alta.status === 201) {
      const baja = http.patch(`${BASE_URL}${RUTA_POST}`, null, {
        headers,
        tags: { ruta: "cancelacion" },
      });
      check(baja, { "PATCH → 200": (r) => r.status === 200 });
    }
  }

  if (SLEEP > 0) sleep(SLEEP);
  // Sin pausa: cada usuario virtual dispara lo más rápido que puede
}
