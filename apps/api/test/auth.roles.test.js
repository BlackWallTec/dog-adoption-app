import { expect, test } from "vite-plus/test";
import {
  cookieOf,
  credentials,
  post,
  request,
  tokenFor,
  userStore,
} from "./helpers/auth-server.js";

test.each(["Root", "Administrador"])("el acceso web admite el rol %s", async (rol) => {
  const current = await userStore.fetchByEmail();
  userStore.fetchByEmail.mockResolvedValue({ ...current, rol });
  expect((await post("/api/perfiles/login", credentials)).status).toBe(200);
});

test("un cliente no obtiene cookie de acceso administrativo", async () => {
  const current = await userStore.fetchByEmail();
  userStore.fetchByEmail.mockResolvedValue({ ...current, rol: "Cliente" });
  const response = await post("/api/perfiles/login", credentials);
  expect(response.status).toBe(403);
  expect(response.headers.get("set-cookie")).toBeNull();
});

test("el login movil conserva su token para clientes", async () => {
  const current = await userStore.fetchByEmail();
  userStore.fetchByEmail.mockResolvedValue({ ...current, rol: "Cliente" });
  const response = await post("/api/perfiles/login", credentials, { "x-client": "mobile" });
  expect(response.status).toBe(200);
  const { user, token } = await response.json();
  expect(user.rol).toBe("Cliente");
  expect(token).toBeTypeOf("string");
  expect(response.headers.get("set-cookie")).toBeNull();
  expect(
    (
      await post("/api/perfiles/me", undefined, {
        "x-client": "mobile",
        Authorization: `Bearer ${token}`,
      })
    ).status,
  ).toBe(200);
});

test.each(["ausente", "alterada", "vencida"])("rechaza una sesion %s", async (kind) => {
  const headers =
    kind === "ausente"
      ? {}
      : {
          Cookie: `token=${kind === "alterada" ? "token-invalido" : tokenFor("Administrador", -1)}`,
        };
  expect((await post("/api/perfiles/me", undefined, headers)).status).toBe(401);
});

test("un JWT de cliente tampoco restaura el panel web", async () => {
  expect(
    (
      await post("/api/perfiles/me", undefined, {
        Cookie: `token=${tokenFor("Cliente")}`,
      })
    ).status,
  ).toBe(403);
});

test("la consulta administrativa exige autenticacion y rol", async () => {
  expect((await request("/api/catalogo/admin/perros")).status).toBe(401);
  expect(
    (
      await request("/api/catalogo/admin/perros", {
        headers: {
          "x-client": "mobile",
          Authorization: `Bearer ${tokenFor("Cliente")}`,
        },
      })
    ).status,
  ).toBe(403);
  const login = await post("/api/perfiles/login", credentials);
  const response = await request("/api/catalogo/admin/perros", {
    headers: { Cookie: cookieOf(login) },
  });
  expect(response.status).toBe(200);
  expect(await response.json()).toEqual({ success: true, data: [] });
});
