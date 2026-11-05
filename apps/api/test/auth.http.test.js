import { expect, test } from "vite-plus/test";
import { cookieOf, credentials, post, userStore } from "./helpers/auth-server.js";

test("el login web crea una cookie HttpOnly y permite consultar la sesion", async () => {
  const response = await post("/api/perfiles/login", {
    ...credentials,
    email: ` ${credentials.email} `,
  });
  expect(response.status).toBe(200);
  expect(response.headers.get("set-cookie")).toContain("HttpOnly");
  expect(response.headers.get("set-cookie")).toContain("SameSite=Lax");
  expect(await response.json()).toEqual({
    user: {
      id: "usuario-prueba",
      email: credentials.email,
      rol: "Administrador",
    },
  });
  expect(userStore.fetchByEmail.mock.calls).toEqual([[credentials.email]]);
  const session = await post("/api/perfiles/me", undefined, { Cookie: cookieOf(response) });
  expect(session.status).toBe(200);
  expect((await session.json()).user.email).toBe(credentials.email);
});

test.each([undefined, {}, { email: "   ", passwd: "clave" }])(
  "los datos incompletos se rechazan sin consultar la base de datos: %j",
  async (body) => {
    const response = await post("/api/perfiles/login", body);
    expect(response.status).toBe(400);
    expect(userStore.fetchByEmail.mock.calls).toHaveLength(0);
  },
);

test("usuario inexistente y contrasena incorrecta devuelven el mismo error", async () => {
  const wrong = await post("/api/perfiles/login", { ...credentials, passwd: "incorrecta" });
  userStore.fetchByEmail.mockResolvedValue(null);
  const absent = await post("/api/perfiles/login", credentials);
  expect(wrong.status).toBe(401);
  expect(absent.status).toBe(401);
  expect(await absent.json()).toEqual(await wrong.json());
});

test("el cierre de sesion borra la cookie del navegador", async () => {
  const login = await post("/api/perfiles/login", credentials);
  const logout = await post("/api/perfiles/logout", undefined, { Cookie: cookieOf(login) });
  expect(logout.status).toBe(200);
  expect(await logout.json()).toEqual({ ok: true });
  expect(logout.headers.get("set-cookie")).toMatch(/^token=;/);
  expect(logout.headers.get("set-cookie")).toContain("Expires=Thu, 01 Jan 1970");
  expect((await post("/api/perfiles/me")).status).toBe(401);
});

test("el endpoint bloquea la cuenta despues de 25 credenciales incorrectas", async () => {
  const attempt = { email: "bloqueo@gmail.com", passwd: "incorrecta" };
  for (let i = 0; i < 25; i++)
    expect((await post("/api/perfiles/login", attempt)).status).toBe(401);
  const calls = userStore.fetchByEmail.mock.calls.length;
  const blocked = await post("/api/perfiles/login", attempt);
  expect(blocked.status).toBe(429);
  expect(blocked.headers.get("retry-after")).toBeTruthy();
  expect(userStore.fetchByEmail.mock.calls).toHaveLength(calls);
});
