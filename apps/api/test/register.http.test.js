import { expect, test } from "vite-plus/test";
import bcrypt from "bcryptjs";
import { cookieOf, post, request, userStore } from "./helpers/auth-server.js";
import { registration } from "./helpers/registration-data.js";

test("crear cuenta inicia sesion sin correo de confirmacion y permite volver a entrar", async () => {
  const response = await post("/api/perfiles/register", { ...registration, rol: "Root" });
  expect(response.status).toBe(201);
  expect(response.headers.get("set-cookie")).toContain("HttpOnly");
  const result = await response.json();
  expect(result).toEqual({
    user: {
      id: "cliente-nuevo",
      email: registration.email,
      rol: "Cliente",
    },
  });
  const [saved, hash] = userStore.register.mock.calls[0];
  expect(saved.rol).toBeUndefined();
  expect(hash).toMatch(/^\$2[ab]\$12\$/);
  expect(await bcrypt.compare(registration.passwd, hash)).toBe(true);
  expect(JSON.stringify(result)).not.toContain(hash);
  expect(result.token).toBeUndefined();
  const session = await post("/api/perfiles/me", undefined, { Cookie: cookieOf(response) });
  expect((await session.json()).user).toEqual(result.user);
  await post("/api/perfiles/logout", undefined, { Cookie: cookieOf(response) });
  userStore.fetchByEmail.mockResolvedValue({
    id_usuario: result.user.id,
    rol: "Cliente",
    contrasena: hash,
  });
  const login = await post("/api/perfiles/login", {
    email: registration.email.toUpperCase(),
    passwd: registration.passwd,
  });
  expect(login.status).toBe(200);
  expect((await login.json()).user).toEqual(result.user);
  expect(
    (
      await request("/api/catalogo/admin/perros", {
        headers: { Cookie: cookieOf(login) },
      })
    ).status,
  ).toBe(403);
});

test.each([
  [{ nombre: "" }, "nombre"],
  [{ email: "incorrecto" }, "email"],
  [{ privacidad: false }, "privacidad"],
  [{ fechaNacimiento: "2999-01-01" }, "fechaNacimiento"],
  [{ passwd: "corta" }, "passwd"],
  [{ confirmPasswd: "otra" }, "confirmPasswd"],
])("rechaza el campo invalido %j antes de guardar", async (change, field) => {
  const response = await post("/api/perfiles/register", { ...registration, ...change });
  expect(response.status).toBe(400);
  expect((await response.json()).fields[field]).toBeTypeOf("string");
  expect(userStore.register.mock.calls).toHaveLength(0);
  expect(response.headers.get("set-cookie")).toBeNull();
});

test("un correo duplicado devuelve 409 y marca solamente el correo", async () => {
  userStore.register.mockRejectedValue({ code: "23505", details: "dato privado" });
  const response = await post("/api/perfiles/register", registration);
  expect(response.status).toBe(409);
  expect(Object.keys((await response.json()).fields)).toEqual(["email"]);
  expect(response.headers.get("set-cookie")).toBeNull();
});

test("un fallo de base de datos no anuncia una cuenta creada", async () => {
  userStore.register.mockRejectedValue({ code: "PGRST202", details: "dato privado" });
  const response = await post("/api/perfiles/register", registration);
  expect(response.status).toBe(500);
  expect(await response.text()).not.toContain("dato privado");
  expect(response.headers.get("set-cookie")).toBeNull();
});

test("el perfil requiere sesion y consulta exclusivamente el identificador del JWT", async () => {
  expect((await request("/api/perfiles/profile")).status).toBe(401);
  expect(userStore.fetchProfile.mock.calls).toHaveLength(0);
  const login = await post("/api/perfiles/register", registration);
  const response = await request("/api/perfiles/profile?id_usuario=otra-persona", {
    headers: { Cookie: cookieOf(login) },
  });
  expect(response.status).toBe(200);
  expect(userStore.fetchProfile.mock.calls).toEqual([["cliente-nuevo"]]);
  expect(response.headers.get("cache-control")).toBe("no-store");
});
