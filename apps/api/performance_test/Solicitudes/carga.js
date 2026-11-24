// Prueba de CARGA con k6 — Módulo Solicitudes, HU02 (ver progreso de solicitud)
// Pregunta: ¿con el tráfico esperado (50 usuarios) el endpoint cumple el objetivo?
//
// Antes de correr:
//   1. en un aterminal cd apps/api  →  pnpm start
//   2. en otra tokens.json en esta misma carpeta
//
// Correr (desde apps/api):
//   k6 run performance_test/Solicitudes/carga.js            prueba de carga (50 VUs)
//   k6 run -e VUS=1 performance_test/Solicitudes/carga.js   latencia base (1 usuario)
//
//   - Autenticacion: cada VU usa un token de tokens.json (x-client: mobile + Bearer),
//     porque /api/solicitudes/cliente/* pasa por verificarToken.
//   - Solo lectura (GET): no escribe en la BD.
import http from "k6/http";
import { check, sleep } from "k6";
import { SharedArray } from "k6/data";

// Endpoint de la HU02: lista las solicitudes enviadas del usuario del token.
const RUTA = "/api/solicitudes/cliente/mis-solicitudes";
// Objetivo de rendimiento del equipo: p95 menor a 200 ms.
const P95 = 200;
// 50 usuarios. Se puede bajar a 1 para la latencia base.
const VUS = Number(__ENV.VUS || 50);
// ────────────────────────────────────────────────────────────────────────────

const BASE_URL = __ENV.BASE_URL || "http://localhost:3000";

// Tokens de las cuentas de prueba. SharedArray carga el archivo una sola vez.
const usuarios = new SharedArray("usuarios", () => JSON.parse(open("./tokens.json")));

export const options = {
  stages: [
    { duration: "15s", target: VUS },
    { duration: "30s", target: VUS },
    { duration: "10s", target: 0 },
  ],
  thresholds: {
    http_req_duration: [`p(95)<${P95}`], // el 95 % responde en menos de P95 ms
    http_req_failed: ["rate<0.01"], // menos de 1 % de errores
  },
  summaryTrendStats: ["min", "med", "avg", "p(90)", "p(95)", "p(99)", "max"],
};

export default function () {
  // Cada VU toma un token. Si hay más VUs que cuentas, algunos comparten cuenta;
  // como solo es lectura, no pasa nada.
  const { token } = usuarios[(__VU - 1) % usuarios.length];

  const res = http.get(`${BASE_URL}${RUTA}`, {
    headers: { "x-client": "mobile", Authorization: `Bearer ${token}` },
  });

  check(res, {
    "GET → 200": (r) => r.status === 200,
    "trae al menos 1 solicitud": (r) => {
      try {
        const data = r.json("data");
        return Array.isArray(data) && data.length > 0;
      } catch {
        return false;
      }
    },
  });

  sleep(1); // el usuario "lee" su pantalla de progreso antes de volver a consultar
}
