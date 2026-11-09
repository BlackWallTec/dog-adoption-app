import { afterAll, beforeAll, beforeEach, expect, test } from "vite-plus/test";
import { createRegistrationDb } from "./helpers/registration-db.js";
import bcrypt from "bcryptjs";

let db, hash;
const sql = "SELECT public.perfiles_registrar_cliente($1,$2,$3,$4,$5,$6,$7,$8,$9,$10) AS id";
const args = (overrides = {}) =>
  Object.values({
    nombre: "Ana",
    apellidos: "Prueba",
    email: " ana@GMAIL.com ",
    hash,
    telefono: "5512345678",
    birth: "2000-06-15",
    calle: "Calle de prueba",
    numero: 12,
    cp: "06000",
    privacy: true,
    ...overrides,
  });
beforeAll(async () => {
  db = await createRegistrationDb();
  hash = await bcrypt.hash("clave-prueba", 4);
}, 30000);
beforeEach(async () => {
  await db.exec("RESET ROLE; TRUNCATE public.usuario CASCADE;");
});
afterAll(async () => {
  if (db) await db.close();
});

test("la migracion registra las cuatro relaciones y el consentimiento en PostgreSQL", async () => {
  await db.exec("SET ROLE service_role");
  const {
    rows: [{ id }],
  } = await db.query(sql, args());
  const {
    rows: [user],
  } = await db.query(
    `
    SELECT u.rol, u.contrasena, d.correo, d.codigo_postal, a.numero_exterior, p.aviso_version
    FROM public.usuario u JOIN public.cliente c USING (id_usuario)
    JOIN public.datos_confidenciales d USING (id_usuario)
    JOIN public.direcciones_fisicas a USING (id_usuario)
    JOIN public.perfiles_consentimiento p USING (id_usuario)
    WHERE u.id_usuario = $1`,
    [id],
  );
  expect(user).toMatchObject({
    rol: "Cliente",
    correo: "ana@gmail.com",
    codigo_postal: "06000",
    numero_exterior: 12,
    aviso_version: "registro-web-v1",
  });
  expect(await bcrypt.compare("clave-prueba", user.contrasena)).toBe(true);
});

test("un correo repetido no deja un segundo usuario ni permite cambiar mayusculas", async () => {
  await db.query(sql, args());
  await expect(db.query(sql, args({ email: "ANA@GMAIL.COM" }))).rejects.toMatchObject({
    code: "23505",
  });
  expect((await db.query("SELECT count(*)::int AS total FROM public.usuario")).rows[0].total).toBe(
    1,
  );
});

test("un fallo al guardar la direccion revierte tambien usuario, cliente y correo", async () => {
  await db.exec(
    "ALTER TABLE public.direcciones_fisicas ADD CONSTRAINT prueba_direccion CHECK (numero_exterior <> 999)",
  );
  try {
    await expect(db.query(sql, args({ numero: 999 }))).rejects.toMatchObject({ code: "23514" });
    for (const table of ["usuario", "cliente", "datos_confidenciales", "perfiles_consentimiento"]) {
      expect(
        (await db.query(`SELECT count(*)::int AS total FROM public.${table}`)).rows[0].total,
      ).toBe(0);
    }
  } finally {
    await db.exec("ALTER TABLE public.direcciones_fisicas DROP CONSTRAINT prueba_direccion");
  }
});

test.each(["anon", "authenticated"])(
  "el rol %s no puede invocar directamente la funcion de registro",
  async (role) => {
    await db.exec(`SET ROLE ${role}`);
    await expect(db.query(sql, args())).rejects.toMatchObject({ code: "42501" });
  },
);

test.each([{ birth: "2999-01-01" }, { privacy: false }, { hash: "texto-plano" }])(
  "la funcion tambien rechaza datos de registro incompatibles: %j",
  async (change) => {
    await expect(db.query(sql, args(change))).rejects.toMatchObject({ code: "22023" });
    expect(
      (await db.query("SELECT count(*)::int AS total FROM public.usuario")).rows[0].total,
    ).toBe(0);
  },
);
