import { writeFileSync } from "node:fs";

const BASE = process.env.BASE_URL || "http://localhost:3000";
const PASS = process.env.PASS_PRUEBA;
const N = Number(process.env.N_CUENTAS || 20);

const tokens = [];
for (let i = 1; i <= N; i++) {
  const correo = `qarino.prueba${i}@gmail.com`;
  const res = await fetch(`${BASE}/api/perfiles/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json", "x-client": "mobile" },
    body: JSON.stringify({ correo, contrasena: PASS }),
  });
  if (!res.ok) throw new Error(`Login falló para ${correo}: ${res.status}`);
  const { token } = await res.json();
  tokens.push({ correo, token });
}

writeFileSync("tokens.json", JSON.stringify(tokens, null, 2));
console.log(`tokens.json con ${tokens.length} cuentas`);
