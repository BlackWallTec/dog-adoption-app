import { afterAll, beforeAll, expect, test } from "vite-plus/test";
import { createRegistrationDb } from "./helpers/registration-db.js";
import { cookieOf, request, userStore } from "./helpers/auth-server.js";
import { registration } from "./helpers/registration-data.js";
import { createPerfilesApi } from "../../web/JS/jsPerfiles/perfiles.api.js";

let db;
beforeAll(async () => {
  db = await createRegistrationDb();
}, 30000);
afterAll(async () => {
  if (db) await db.close();
});

test("web, Express y PostgreSQL completan registro, datos propios y nuevo login", async () => {
  userStore.register.mockImplementation(async (v, hash) => {
    const {
      rows: [{ id }],
    } = await db.query(
      "SELECT public.perfiles_registrar_cliente($1,$2,$3,$4,$5,$6,$7,$8,$9,$10) AS id",
      [
        v.nombre,
        v.apellidos,
        v.email,
        hash,
        v.telefono,
        v.fechaNacimiento,
        v.calle,
        Number(v.numeroExterior),
        v.codigoPostal,
        v.privacidad,
      ],
    );
    return { id_usuario: id, rol: "Cliente" };
  });
  userStore.fetchByEmail.mockImplementation(async (email) => {
    const { rows } = await db.query(
      `SELECT u.id_usuario, u.rol, u.contrasena FROM public.usuario u
      JOIN public.datos_confidenciales d USING (id_usuario) WHERE d.correo=$1`,
      [email],
    );
    return rows[0] ?? null;
  });
  userStore.fetchProfile.mockImplementation(async (id) => {
    const { rows } = await db.query(
      "SELECT nombre, apellidos FROM public.usuario WHERE id_usuario=$1",
      [id],
    );
    return rows[0] ?? null;
  });
  let cookie = "";
  const api = createPerfilesApi({
    fetchImpl: async (path, options) => {
      const headers = new Headers(options.headers);
      if (cookie) headers.set("Cookie", cookie);
      const response = await request(path, {
        ...options,
        headers,
      });
      if (response.headers.has("set-cookie")) cookie = cookieOf(response);
      return response;
    },
  });
  const created = await api.register({
    ...registration,
    email: ` ${registration.email.toUpperCase()} `,
  });
  expect(created.user.rol).toBe("Cliente");
  expect((await api.getSession()).user).toEqual(created.user);
  expect((await api.getProfile()).profile).toEqual({ nombre: "Ana", apellidos: "Prueba" });
  await api.logout();
  await expect(api.getSession()).rejects.toMatchObject({ status: 401 });
  await expect(
    api.login({ email: registration.email, passwd: "equivocada" }),
  ).rejects.toMatchObject({ status: 401 });
  expect(
    (await api.login({ email: registration.email, passwd: registration.passwd })).user,
  ).toEqual(created.user);
  await expect(api.register(registration)).rejects.toMatchObject({
    status: 409,
    fields: { email: expect.any(String) },
  });
  expect((await db.query("SELECT count(*)::int AS total FROM public.usuario")).rows[0].total).toBe(
    1,
  );
}, 15000);
