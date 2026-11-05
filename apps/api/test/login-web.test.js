import { expect, test } from "vite-plus/test";
import { createPerfilesApi } from "../../web/JS/jsPerfiles/perfiles.api.js";
import { cookieOf, credentials, request } from "./helpers/auth-server.js";

test("el adaptador de la web completa login, restauracion y logout con Express", async () => {
  let cookie = "";
  const api = createPerfilesApi({
    fetchImpl: async (path, options) => {
      expect(options.credentials).toBe("include");
      const response = await request(path, {
        ...options,
        headers: { ...options.headers, ...(cookie ? { Cookie: cookie } : {}) },
      });
      if (response.headers.has("set-cookie")) cookie = cookieOf(response);
      return response;
    },
  });
  await expect(api.getSession()).rejects.toMatchObject({ status: 401 });
  const login = await api.login({ ...credentials, email: ` ${credentials.email.toUpperCase()} ` });
  expect(login.user.email).toBe(credentials.email);
  expect(login.user.rol).toBe("Administrador");
  expect((await api.getSession()).user).toEqual(login.user);
  await expect(api.logout()).resolves.toEqual({ ok: true });
  await expect(api.getSession()).rejects.toMatchObject({ status: 401 });
});
