import { expect, test, vi } from "vite-plus/test";
import { createPerfilesApi, PerfilesApiError } from "./perfiles.api.js";

const user = { id: "usuario-prueba", email: "ana@example.test", rol: "Administrador" };
const reply = (status, data) => ({
  ok: status >= 200 && status < 300,
  status,
  json: async () => data,
});

test("el login conserva la contrasena y usa cookies sin pedir un JWT al navegador", async () => {
  const fetchImpl = vi.fn(async () => reply(200, { user }));
  const api = createPerfilesApi({ fetchImpl });
  await expect(
    api.login({ email: " ANA@EXAMPLE.TEST ", passwd: " clave con espacios " }),
  ).resolves.toEqual({ user });
  const [path, options] = fetchImpl.mock.calls[0];
  expect(path).toBe("/api/perfiles/login");
  expect(options.credentials).toBe("include");
  expect(JSON.parse(options.body)).toEqual({ email: user.email, passwd: " clave con espacios " });
  expect(options.headers.Authorization).toBeUndefined();
});

test.each([400, 401, 403, 429, 500])(
  "traduce el error HTTP %i sin exponer detalles internos",
  async (status) => {
    const api = createPerfilesApi({
      fetchImpl: async () => reply(status, { error: "detalle privado" }),
    });
    await expect(api.login({ email: user.email, passwd: "clave" })).rejects.toMatchObject({
      status,
    });
    try {
      await api.getSession();
    } catch (error) {
      expect(error).toBeInstanceOf(PerfilesApiError);
      expect(error.message).not.toContain("detalle privado");
    }
  },
);

test("un fallo de red no se presenta como un inicio de sesion exitoso", async () => {
  const api = createPerfilesApi({
    fetchImpl: async () => {
      throw new TypeError("sin red");
    },
  });
  await expect(api.getSession()).rejects.toMatchObject({ status: 0 });
});

test.each([{}, { user: { id: "id" } }, { user: null }])(
  "rechaza una sesion incompleta: %j",
  async (data) => {
    const api = createPerfilesApi({ fetchImpl: async () => reply(200, data) });
    await expect(api.getSession()).rejects.toMatchObject({ status: 502 });
  },
);

test("no confirma el cierre de sesion ante una respuesta incompleta", async () => {
  const api = createPerfilesApi({ fetchImpl: async () => reply(200, {}) });
  await expect(api.logout()).rejects.toMatchObject({ status: 502 });
});
