import { afterAll, beforeAll, describe, expect, it, vi } from "vite-plus/test";
import express from "express";
import cookieParser from "cookie-parser";
import request from "supertest";
import bcrypt from "bcryptjs";

// HU63: flujo HTTP completo con CORREO_MOVIL_MODO=supabase. Las RPC se simulan en memoria con la
// semántica de la migración perfiles_codigos_correo_supabase; los contratos de Android no cambian.
vi.mock("../../src/config/supabaseClient.js", () => ({ supabaseAdmin: {}, supabase: {} }));
vi.mock("../../src/modules/perfiles/http/modelosPerfiles/user.model.js", () => ({
  default: {
    fetchByEmail: vi.fn(),
    fetchEmailByUUID: vi.fn(),
    obtenerEstadoCorreoMovil: vi.fn(),
    emitirCodigoCorreoMovil: vi.fn(),
    consumirCodigoCorreoMovil: vi.fn(),
  },
}));
vi.mock("../../src/modules/perfiles/http/modelosPerfiles/registroPublico.model.js", () => ({
  default: { crearCliente: vi.fn() },
}));
vi.mock("../../src/modules/perfiles/http/modelosPerfiles/cps.model.js", () => ({
  default: { existePorId: vi.fn().mockResolvedValue(true) },
}));
import Usuario from "../../src/modules/perfiles/http/modelosPerfiles/user.model.js";
import RegistroPublico from "../../src/modules/perfiles/http/modelosPerfiles/registroPublico.model.js";
import { router } from "../../src/modules/perfiles/index.js";

const app = express();
app.use(express.json(), cookieParser());
app.use("/api/perfiles", router);
const base = "/api/perfiles";
const correo = "nueva@gmail.com";
const HORA = 3_600_000;

// "Base de datos": compartida por todas las peticiones, como lo estaría entre instancias de Vercel.
const porEmail = new Map();
const porId = new Map();
const estados = new Map();
let fetchMock;
let ahora = Date.now();

const registro = (correoNuevo) => ({
  nombre: "Prueba",
  apellidos: "Cliente",
  correo: correoNuevo,
  telefono: "5555555555",
  fecha_nacimiento: "1998-01-01",
  calle: "Prueba",
  numero_exterior: "1",
  numero_interior: "",
  id_codigo_postal: "12345678-1234-1234-1234-123456789abc",
  contrasena: "Anterior1234",
  confirmContrasena: "Anterior1234",
  privacidad: true,
});
const codigoReciente = () =>
  JSON.parse(fetchMock.mock.calls.at(-1)[1].body).text.match(/\b(\d{6})\b/)[1];
const login = (contrasena, movil = true) => {
  let req = request(app).post(`${base}/login`);
  if (movil) req = req.set("x-client", "mobile");
  return req.send({ correo, contrasena });
};
const me = (token) =>
  request(app).get(`${base}/me`).set("x-client", "mobile").set("Authorization", `Bearer ${token}`);

beforeAll(() => {
  vi.stubEnv("CORREO_MOVIL_MODO", "supabase");
  vi.stubEnv("NODE_ENV", "production");
  vi.stubEnv("JWT_SECRET", "secreto-ficticio-de-prueba-supabase");
  vi.stubEnv("MAIL_TRANSPORT", "resend");
  vi.stubEnv("RESEND_API_KEY", "clave-ficticia");
  vi.stubEnv("MAIL_FROM", "onboarding@resend.dev");
  fetchMock = vi.fn().mockResolvedValue({ ok: true, json: async () => ({ id: "simulado" }) });
  vi.stubGlobal("fetch", fetchMock);

  RegistroPublico.crearCliente.mockImplementation(
    async ({ idUsuario, rol, usuario, contacto, requiereConfirmacion }) => {
      const cuenta = {
        id_usuario: idUsuario,
        rol,
        ...usuario,
        correo_pendiente_desde: requiereConfirmacion ? new Date(ahora).toISOString() : null,
      };
      porId.set(idUsuario, cuenta);
      porEmail.set(contacto.correo, cuenta);
    },
  );
  Usuario.fetchByEmail.mockImplementation(async (c) => {
    const u = porEmail.get(c);
    return u ? { ...u } : null;
  });
  Usuario.fetchEmailByUUID.mockImplementation(
    async (id) => [...porEmail].find(([, u]) => u.id_usuario === id)?.[0] ?? null,
  );
  Usuario.obtenerEstadoCorreoMovil.mockImplementation(async (id) => ({
    confirmado: porId.has(id) && porId.get(id).correo_pendiente_desde === null,
    version: estados.get(id)?.version ?? 0,
  }));
  Usuario.emitirCodigoCorreoMovil.mockImplementation(async (id, tipo, hash) => {
    const u = porId.get(id);
    if (u?.rol !== "Cliente") return "not_eligible";
    const pendiente = u.correo_pendiente_desde;
    if ((tipo === "confirmar") !== Boolean(pendiente)) return "not_eligible";
    const vence = tipo === "confirmar" ? Date.parse(pendiente) + 72 * HORA : ahora + 15 * 60_000;
    if (vence <= ahora) return "expired";
    const fila = estados.get(id) ?? { version: 0 };
    estados.set(id, fila);
    if (fila[tipo] && fila[tipo].enviado > ahora - 60_000) return "rate_limited";
    fila[tipo] = { hash, enviado: ahora, vence, intentos: 0 };
    return "sent";
  });
  Usuario.consumirCodigoCorreoMovil.mockImplementation(async (id, tipo, hash, contrasenaHash) => {
    const u = porId.get(id);
    const fila = estados.get(id);
    const d = fila?.[tipo];
    if (!u || !d || d.vence <= ahora || d.intentos >= 5) return "invalid";
    if (tipo === "confirmar" && !u.correo_pendiente_desde) return "invalid";
    if (tipo === "recuperar" && (u.correo_pendiente_desde || !contrasenaHash)) return "invalid";
    if (d.hash !== hash) {
      d.intentos += 1;
      return "invalid";
    }
    if (tipo === "confirmar") u.correo_pendiente_desde = null;
    else {
      u.contrasena = contrasenaHash;
      fila.version += 1;
    }
    delete fila[tipo];
    return "ok";
  });
});
afterAll(() => {
  vi.unstubAllEnvs();
  vi.unstubAllGlobals();
});

describe("Correo de Perfiles con estado en Supabase", () => {
  it("registra, bloquea el login, reenvía sin reiniciar el plazo y confirma", async () => {
    const alta = await request(app).post(`${base}/register`).send(registro(correo));
    expect(alta.status).toBe(201);
    expect(alta.body).toMatchObject({ requiereConfirmacion: true, correoEnviado: true });
    const primerCodigo = codigoReciente();
    const pendienteDesde = porEmail.get(correo).correo_pendiente_desde;

    const bloqueado = await login("Anterior1234");
    expect(bloqueado.status).toBe(403);
    expect(bloqueado.body.code).toBe("EMAIL_NOT_CONFIRMED");

    ahora += 2 * HORA;
    const reenvio = await request(app).post(`${base}/correo-movil/reenviar`).send({ correo });
    expect(reenvio.status).toBe(202);
    expect(porEmail.get(correo).correo_pendiente_desde).toBe(pendienteDesde);
    const segundoCodigo = codigoReciente();

    // El código anterior queda reemplazado; el incorrecto suma un intento.
    if (segundoCodigo !== primerCodigo)
      expect(
        (
          await request(app)
            .post(`${base}/correo-movil/confirmar`)
            .send({ correo, codigo: primerCodigo })
        ).status,
      ).toBe(400);
    const ok = await request(app)
      .post(`${base}/correo-movil/confirmar`)
      .send({ correo, codigo: segundoCodigo });
    expect(ok.status).toBe(200);
    expect(ok.body).toEqual({ ok: true });

    const reutilizado = await request(app)
      .post(`${base}/correo-movil/confirmar`)
      .send({ correo, codigo: segundoCodigo });
    expect(reutilizado.status).toBe(400);
    expect(reutilizado.body.code).toBe("INVALID_EMAIL_CODE");

    const entrar = await login("Anterior1234");
    expect(entrar.status).toBe(200);
    expect((await me(entrar.body.token)).status).toBe(200);
  });

  it("recupera la contraseña, invalida sesiones anteriores y permite entrar con la nueva", async () => {
    const anterior = (await login("Anterior1234")).body.token;
    expect((await me(anterior)).status).toBe(200);

    const solicitud = await request(app).post(`${base}/correo-movil/recuperar`).send({ correo });
    expect(solicitud.status).toBe(202);
    const codigo = codigoReciente();

    const cambio = await request(app).post(`${base}/correo-movil/restablecer`).send({
      correo,
      codigo,
      contrasena: "Nueva12345",
      confirmContrasena: "Nueva12345",
    });
    expect(cambio.status).toBe(200);
    expect(await bcrypt.compare("Nueva12345", porEmail.get(correo).contrasena)).toBe(true);

    expect((await me(anterior)).status).toBe(401);
    expect((await login("Anterior1234")).status).toBe(401);
    const nueva = await login("Nueva12345");
    expect(nueva.status).toBe(200);
    expect((await me(nueva.body.token)).status).toBe(200);
  });

  it("rechaza un código de recuperación vencido", async () => {
    ahora += 2 * 60_000;
    await request(app).post(`${base}/correo-movil/recuperar`).send({ correo });
    const codigo = codigoReciente();
    ahora += 15 * 60_000;
    const vencido = await request(app).post(`${base}/correo-movil/restablecer`).send({
      correo,
      codigo,
      contrasena: "Otra123456",
      confirmContrasena: "Otra123456",
    });
    expect(vencido.status).toBe(400);
    expect(vencido.body.code).toBe("INVALID_EMAIL_CODE");
  });

  it("responde 503 sin cerrar la sesión si Supabase no está disponible", async () => {
    const token = (await login("Nueva12345")).body.token;
    Usuario.obtenerEstadoCorreoMovil.mockRejectedValueOnce(new Error("sin conexión"));
    const respuesta = await me(token);
    expect(respuesta.status).toBe(503);
    expect((await me(token)).status).toBe(200);
  });
});
