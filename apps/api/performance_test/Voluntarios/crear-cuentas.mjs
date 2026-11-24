// crear-cuentas.mjs (Node 20.6+)
const BASE = process.env.BASE_URL || "http://localhost:3000";
const PASS = process.env.PASS_PRUEBA;
const CP_ID = process.env.CP_ID;
const N = Number(process.env.N_CUENTAS || 20);

if (!PASS || PASS.length < 8 || PASS.length > 72) {
  throw new Error("PASS_PRUEBA debe tener entre 8 y 72 caracteres");
}
if (!CP_ID) throw new Error("Falta CP_ID en el .env");

for (let i = 1; i <= N; i++) {
  const body = {
    nombre: "Prueba",
    apellidos: "Carga",
    correo: `qarino.prueba${i}@gmail.com`,
    telefono: `555${1000000 + i}`, // 10 dígitos
    calle: "Calle de prueba",
    numero_exterior: String(i),
    numero_interior: "", // string vacío, no null
    id_codigo_postal: CP_ID,
    fecha_nacimiento: "1995-01-01",
    contrasena: PASS,
    confirmContrasena: PASS,
    privacidad: true,
  };

  const res = await fetch(`${BASE}/api/perfiles/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const json = await res.json().catch(() => ({}));

  if (res.status === 201) {
    console.log(`OK    qarino.prueba${i}@gmail.com  ${json.user.uuid}`);
  } else {
    console.log(`FALLO qarino.prueba${i}@gmail.com  ${res.status}  ${JSON.stringify(json)}`);
  }
}
