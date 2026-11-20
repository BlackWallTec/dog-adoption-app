import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import express from "express";
import request from "supertest";
import jwt from "jsonwebtoken";
import ROLES from "../../src/config/roles.js";
import rolesCompatibles from "../../src/modules/perfiles/http/modelosPerfiles/roles.model.js";
import { BAD_REQUEST, ERROR_CODES, SERVICE_UNAVAILABLE } from "@qarino/shared/errores.js";

// Se ejecuta el middleware real con Redis simulado: las pruebas no consumen comandos de Upstash.
const dobles = vi.hoisted(() => ({
  limitar: vi.fn(),
  solicitar: vi.fn(),
  consumir: vi.fn(),
  servicio: vi.fn(),
  login: vi.fn(),
  registro: vi.fn(),
  altaAdmin: vi.fn(),
}));
vi.mock("@upstash/redis", () => ({ Redis: { fromEnv: vi.fn(() => ({})) } }));
vi.mock("@upstash/ratelimit", () => ({
  Ratelimit: class {
    constructor({ prefix }) {
      this.prefix = prefix;
    }
    static slidingWindow(max, ventana) {
      return { max, ventana };
    }
    limit(id) {
      return dobles.limitar(id, this.prefix);
    }
  },
}));
vi.mock("../../src/modules/perfiles/services/correoMovil.service.js", () => ({
  correoMovilHabilitado: () => true,
  servicioCorreoMovil: dobles.servicio,
  verificarCorreoMovil: () => true,
}));
vi.mock("../../src/config/supabaseClient.js", () => ({ supabaseAdmin: {}, supabase: {} }));
vi.mock("../../src/modules/perfiles/http/auth.controller.js", () => ({
  postLogin: (_req, res) => {
    dobles.login();
    return res.json({ ok: true });
  },
  postLogout: (_req, res) => res.json({ ok: true }),
  getMe: (_req, res) => res.json({ ok: true }),
}));
vi.mock("../../src/modules/perfiles/http/register.controller.js", () => ({
  postRegister: (_req, res) => {
    dobles.registro();
    return res.status(201).json({ ok: true });
  },
  postRegisterAdmin: (_req, res) => {
    dobles.altaAdmin();
    return res.status(201).json({ ok: true });
  },
}));
const app = express();
app.use(express.json());
beforeAll(async () => {
  vi.stubEnv("UPSTASH_REDIS_REST_URL", "https://redis.example.invalid");
  vi.stubEnv("UPSTASH_REDIS_REST_TOKEN", "ficticio");
  vi.stubEnv("NODE_ENV", "production");
  vi.stubEnv("JWT_SECRET", "secreto-ficticio-de-limites");
  const { default: router } = await import("../../src/modules/perfiles/http/correoMovil.router.js");
  app.use("/correo", router);
  const { default: auth } = await import("../../src/modules/perfiles/http/auth.router.js");
  app.use("/auth", auth);
});
beforeEach(() => {
  dobles.limitar.mockReset().mockResolvedValue({ success: true, limit: 30, remaining: 29 });
  dobles.solicitar
    .mockReset()
    .mockResolvedValue({ message: "Si la cuenta corresponde, recibirás un código en tu correo." });
  dobles.consumir.mockReset().mockResolvedValue({ ok: true });
  dobles.servicio
    .mockReset()
    .mockReturnValue({ solicitar: dobles.solicitar, consumir: dobles.consumir });
  dobles.login.mockClear();
  dobles.registro.mockClear();
  dobles.altaAdmin.mockClear();
});

describe("Roles y límites de autenticación", () => {
  const token = (rol) => jwt.sign({ uuid: "usuario-prueba", rol }, process.env.JWT_SECRET);
  it("conserva las rutas de importación y los valores de los roles", async () => {
    const publico = await import("../../src/modules/perfiles/index.js");
    expect(publico.ROLES).toBe(ROLES);
    expect(rolesCompatibles).toBe(ROLES);
    expect(ROLES).toEqual({ ROOT: "Root", ADMIN: "Administrador", CLIENT: "Cliente" });
  });
  it.each([
    ["login", "login"],
    ["register", "registro"],
  ])("limita %s antes de ejecutar el controlador", async (ruta, prefijo) => {
    dobles.limitar.mockResolvedValueOnce({ success: false, limit: 10, remaining: 0 });
    const r = await request(app)
      .post(`/auth/${ruta}`)
      .set("x-forwarded-for", "192.0.2.10")
      .send({});
    expect(r.status).toBe(429);
    expect(r.body.code).toBe(ERROR_CODES.RATE_LIMITED);
    expect(dobles.limitar).toHaveBeenCalledWith("192.0.2.10", `rl:perfiles:${prefijo}`);
    expect(dobles.login).not.toHaveBeenCalled();
    expect(dobles.registro).not.toHaveBeenCalled();
  });
  it("permite login y registro cuando queda cuota", async () => {
    expect((await request(app).post("/auth/login").send({})).status).toBe(200);
    expect((await request(app).post("/auth/register").send({})).status).toBe(201);
    expect(dobles.login).toHaveBeenCalledOnce();
    expect(dobles.registro).toHaveBeenCalledOnce();
  });
  it("el alta administrativa exige Root antes de consumir cuota", async () => {
    expect((await request(app).post("/auth/register-admin").send({})).status).toBe(401);
    const r = await request(app)
      .post("/auth/register-admin")
      .set("x-client", "mobile")
      .set("Authorization", `Bearer ${token(ROLES.CLIENT)}`)
      .send({});
    expect(r.status).toBe(403);
    expect(dobles.limitar).not.toHaveBeenCalled();
    expect(dobles.altaAdmin).not.toHaveBeenCalled();
  });
  it("limita el alta administrativa por UUID sin quitar la protección de Root", async () => {
    dobles.limitar.mockResolvedValueOnce({ success: false, limit: 10, remaining: 0 });
    const bloqueada = await request(app)
      .post("/auth/register-admin")
      .set("x-client", "mobile")
      .set("Authorization", `Bearer ${token(ROLES.ROOT)}`)
      .send({});
    expect(bloqueada.status).toBe(429);
    expect(dobles.limitar).toHaveBeenCalledWith("usuario-prueba", "rl:perfiles:registro-admin");
    expect(dobles.altaAdmin).not.toHaveBeenCalled();
    const permitida = await request(app)
      .post("/auth/register-admin")
      .set("x-client", "mobile")
      .set("Authorization", `Bearer ${token(ROLES.ROOT)}`)
      .send({});
    expect(permitida.status).toBe(201);
    expect(dobles.altaAdmin).toHaveBeenCalledOnce();
  });
  it("no bloquea el cierre de sesión con la cuota de login agotada", async () => {
    dobles.limitar.mockResolvedValue({ success: false, limit: 10, remaining: 0 });
    expect((await request(app).post("/auth/logout")).status).toBe(200);
    expect(dobles.limitar).not.toHaveBeenCalled();
  });
});
afterAll(() => vi.unstubAllEnvs());

describe("Controlador de correo y middleware compartido", () => {
  it("consulta la configuración sin consumir cuota y evita caché", async () => {
    const r = await request(app).get("/correo/config");
    expect(r.body).toEqual({ habilitado: true });
    expect(r.headers["cache-control"]).toBe("no-store");
    expect(dobles.limitar).not.toHaveBeenCalled();
  });
  it("normaliza el correo con shared antes de solicitar el código", async () => {
    const r = await request(app).post("/correo/recuperar").send({ correo: " Prueba@GMAIL.com " });
    expect(r.status).toBe(202);
    expect(dobles.solicitar).toHaveBeenCalledWith("prueba@gmail.com", "recuperar");
    expect(r.headers["cache-control"]).toBe("no-store");
  });
  it.each([undefined, "invalido", "prueba@otro.com"])(
    "rechaza el correo %s con la validación compartida",
    async (correo) => {
      const r = await request(app).post("/correo/recuperar").send({ correo });
      expect(r.status).toBe(422);
      expect(r.body.code).toBe(ERROR_CODES.VALIDATION);
      expect(r.body.fields.correo).toBeTruthy();
      expect(dobles.servicio).not.toHaveBeenCalled();
    },
  );
  it("conserva el código estable del error de confirmación", async () => {
    dobles.consumir.mockRejectedValueOnce(
      new BAD_REQUEST("Código inválido", ERROR_CODES.INVALID_EMAIL_CODE),
    );
    const r = await request(app)
      .post("/correo/confirmar")
      .send({ correo: "prueba@gmail.com", codigo: "000000" });
    expect(r.status).toBe(400);
    expect(r.body).toEqual({ error: "Código inválido", code: ERROR_CODES.INVALID_EMAIL_CODE });
  });
  it("conserva el error de contraseña no guardada", async () => {
    dobles.consumir.mockRejectedValueOnce(
      new SERVICE_UNAVAILABLE("Reintenta", ERROR_CODES.PASSWORD_UPDATE_FAILED),
    );
    const r = await request(app)
      .post("/correo/restablecer")
      .send({ correo: "prueba@gmail.com", codigo: "012345" });
    expect(r.status).toBe(503);
    expect(r.body.code).toBe(ERROR_CODES.PASSWORD_UPDATE_FAILED);
  });
  it("informa indisponibilidad y oculta los errores inesperados", async () => {
    dobles.servicio.mockImplementationOnce(() => {
      throw new SERVICE_UNAVAILABLE("No disponible", ERROR_CODES.EMAIL_UNAVAILABLE);
    });
    const r = await request(app).post("/correo/recuperar").send({ correo: "prueba@gmail.com" });
    expect(r.status).toBe(503);
    expect(r.body.code).toBe(ERROR_CODES.EMAIL_UNAVAILABLE);
    dobles.servicio.mockImplementationOnce(() => {
      throw new Error("detalle privado");
    });
    const fallo = await request(app).post("/correo/recuperar").send({ correo: "prueba@gmail.com" });
    expect(fallo.status).toBe(500);
    expect(fallo.body).toEqual({ code: ERROR_CODES.INTERNAL, error: "Error interno del servidor" });
  });
  it.each(["reenviar", "recuperar", "confirmar", "restablecer"])(
    "limita %s con Upstash antes de atenderlo",
    async (ruta) => {
      dobles.limitar.mockResolvedValueOnce({ success: false, limit: 30, remaining: 0 });
      const r = await request(app).post(`/correo/${ruta}`).send({ correo: "prueba@gmail.com" });
      expect(r.status).toBe(429);
      expect(r.body.code).toBe(ERROR_CODES.RATE_LIMITED);
      expect(r.headers["x-ratelimit-remaining"]).toBe("0");
      expect(dobles.servicio).not.toHaveBeenCalled();
    },
  );
});
