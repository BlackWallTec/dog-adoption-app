import { afterAll, beforeAll, expect, test } from "vite-plus/test";
import { createServer } from "vite-plus";
import { fileURLToPath } from "node:url";
import { apiOrigin, cookieOf, credentials } from "./helpers/auth-server.js";

let web, origin;
const previousTarget = process.env.API_PROXY_TARGET;
beforeAll(async () => {
  process.env.API_PROXY_TARGET = apiOrigin();
  web = await createServer({
    root: fileURLToPath(new URL("../../web", import.meta.url)),
    configFile: fileURLToPath(new URL("../../web/vite.config.js", import.meta.url)),
    server: { host: "127.0.0.1", port: 0, open: false },
    logLevel: "silent",
  });
  await web.listen();
  origin = `http://127.0.0.1:${web.httpServer.address().port}`;
});
afterAll(async () => {
  if (web) await web.close();
  if (previousTarget === undefined) delete process.env.API_PROXY_TARGET;
  else process.env.API_PROXY_TARGET = previousTarget;
});

test("Vite sirve el login y reenvia cookies de la API configurada", async () => {
  const page = await fetch(origin + "/HTML/htmlPerfiles/login.html");
  expect(page.status).toBe(200);
  expect(await page.text()).toContain('id="login-form"');
  const login = await fetch(origin + "/api/perfiles/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials),
  });
  expect(login.status).toBe(200);
  expect(login.headers.get("set-cookie")).toContain("HttpOnly");
  const me = await fetch(origin + "/api/perfiles/me", {
    method: "POST",
    headers: { Cookie: cookieOf(login) },
  });
  expect(me.status).toBe(200);
  expect((await me.json()).user.email).toBe(credentials.email);
});
