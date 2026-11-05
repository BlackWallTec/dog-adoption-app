import { afterAll, beforeAll, beforeEach, vi } from "vite-plus/test";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import { randomBytes } from "node:crypto";
import { createApp } from "../../src/app.js";
import Usuario from "../../src/modules/perfiles/http/modelosPerfiles/user.model.js";

vi.mock("../../src/config/supabaseClient.js", () => ({ supabase: {}, supabaseAdmin: {} }));
vi.mock("../../src/modules/perfiles/http/modelosPerfiles/user.model.js", () => ({
  default: { fetchByEmail: vi.fn() },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/dog.model.js", () => ({
  default: { getDogs: vi.fn(async () => []) },
}));

export const userStore = Usuario;
export const credentials = { email: "ana@example.test", passwd: "clave-prueba" };
let server, baseUrl, hash;
const originalSecret = process.env.JWT_SECRET;

beforeAll(async () => {
  process.env.JWT_SECRET = randomBytes(32).toString("hex");
  hash = await bcrypt.hash(credentials.passwd, 4);
  server = createApp().listen(0, "127.0.0.1");
  await new Promise((resolve) => server.once("listening", resolve));
  baseUrl = `http://127.0.0.1:${server.address().port}`;
});
beforeEach(() => {
  userStore.fetchByEmail.mockReset().mockResolvedValue({
    id_usuario: "usuario-prueba",
    contrasena: hash,
    rol: "Administrador",
  });
});
afterAll(async () => {
  if (server) await new Promise((resolve) => server.close(resolve));
  if (originalSecret === undefined) delete process.env.JWT_SECRET;
  else process.env.JWT_SECRET = originalSecret;
});

export const request = (path, options) => fetch(baseUrl + path, options);
export const post = (path, body, headers = {}) =>
  request(path, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...headers },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
export const cookieOf = (response) => response.headers.get("set-cookie")?.split(";")[0];
export const tokenFor = (rol, expiresIn = "1h") =>
  jwt.sign({ uuid: "usuario-prueba", email: credentials.email, rol }, process.env.JWT_SECRET, {
    algorithm: "HS256",
    expiresIn,
  });
