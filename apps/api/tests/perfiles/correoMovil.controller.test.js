import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import express from "express";
import request from "supertest";
import { BAD_REQUEST, ERROR_CODES, SERVICE_UNAVAILABLE } from "@qarino/shared/errores.js";

// Se ejecuta el middleware real con Redis simulado: las pruebas no consumen comandos de Upstash.
const dobles = vi.hoisted(() => ({
  limitar: vi.fn(),
  solicitar: vi.fn(),
  consumir: vi.fn(),
  servicio: vi.fn(),
}));
vi.mock("@upstash/redis", () => ({ Redis: { fromEnv: vi.fn(() => ({})) } }));
vi.mock("@upstash/ratelimit", () => ({
  Ratelimit: class {
    static slidingWindow(max, ventana) {
      return { max, ventana };
    }
    limit(id) {
      return dobles.limitar(id);
    }
  },
}));
vi.mock("../../src/modules/perfiles/services/correoMovil.service.js", () => ({
  correoMovilHabilitado: () => true,
  servicioCorreoMovil: dobles.servicio,
}));
const app = express();
app.use(express.json());
beforeAll(async () => {
  vi.stubEnv("UPSTASH_REDIS_REST_URL", "https://redis.example.invalid");
  vi.stubEnv("UPSTASH_REDIS_REST_TOKEN", "ficticio");
  vi.stubEnv("NODE_ENV", "production");
  const { default: router } = await import("../../src/modules/perfiles/http/correoMovil.router.js");
  app.use("/correo", router);
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
