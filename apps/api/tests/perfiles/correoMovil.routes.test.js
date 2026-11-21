import { afterAll, beforeAll, describe, expect, it, vi } from "vite-plus/test";
import { mkdtempSync, existsSync, unlinkSync, rmdirSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import express from "express";
import cookieParser from "cookie-parser";
import request from "supertest";
import jwt from "jsonwebtoken";
import bcrypt from "bcryptjs";
vi.mock("../../src/config/supabaseClient.js", () => ({ supabaseAdmin: {}, supabase: {} }));
vi.mock("../../src/modules/perfiles/http/modelosPerfiles/user.model.js", () => ({
  default: {
    fetchByEmail: vi.fn(),
    fetchEmailByUUID: vi.fn(),
    estadoCorreo: vi.fn(),
    emitirCodigoCorreo: vi.fn(),
    confirmarCodigoCorreo: vi.fn(),
    restablecerContrasenaCorreo: vi.fn(),
    correoMovilDisponible: vi.fn(),
    registerAccount: vi.fn(),
    deleteAccount: vi.fn(),
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
const carpeta = mkdtempSync(join(tmpdir(), "qarino-correo-http-"));
const archivo = join(carpeta, "estado.json");
const porEmail = new Map(),
  porId = new Map();
const retos = new Map();
let fetchMock, nuevoToken, rootCookie;
const registro = (correo) => ({
  nombre: "Prueba",
  apellidos: "Cliente",
  correo,
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
beforeAll(async () => {
  vi.stubEnv("CORREO_MOVIL_MODO", "desactivado");
  vi.stubEnv("CORREO_MOVIL_ARCHIVO", archivo);
  vi.stubEnv("JWT_SECRET", "secreto-ficticio-de-prueba-movil");
  vi.stubEnv("RESEND_API_KEY", "clave-ficticia");
  vi.stubEnv("MAIL_TRANSPORT", "resend");
  vi.stubEnv("MAIL_FROM", "onboarding@resend.dev");
  fetchMock = vi.fn().mockResolvedValue({ ok: true, json: async () => ({ id: "simulado" }) });
  vi.stubGlobal("fetch", fetchMock);
  for (const [correo, rol] of [
    ["anterior@gmail.com", "Cliente"],
    ["root@gmail.com", "Root"],
    ["admin@gmail.com", "Administrador"],
  ]) {
    const usuario = {
      id_usuario: correo,
      rol,
      contrasena: await bcrypt.hash("Anterior1234", 4),
      correo_pendiente_desde: null,
      correo_version_movil: 0,
    };
    porEmail.set(correo, usuario);
    porId.set(usuario.id_usuario, usuario);
  }
  Usuario.fetchByEmail.mockImplementation(async (correo) => porEmail.get(correo) ?? null);
  Usuario.fetchEmailByUUID.mockImplementation(
    async (id) => [...porEmail].find(([, u]) => u.id_usuario === id)?.[0] ?? null,
  );
  Usuario.estadoCorreo.mockImplementation(async (id) => {
    const usuario = porId.get(id);
    return {
      confirmado: Boolean(usuario) && !usuario.correo_pendiente_desde,
      version: usuario?.correo_version_movil ?? 0,
    };
  });
  Usuario.correoMovilDisponible.mockResolvedValue(true);
  Usuario.emitirCodigoCorreo.mockImplementation(async (id, tipo, hash) => {
    retos.set(`${id}:${tipo}`, hash);
    return "creado";
  });
  Usuario.confirmarCodigoCorreo.mockImplementation(async (id, hash) => {
    const clave = `${id}:confirmar`;
    if (retos.get(clave) !== hash) return "invalido";
    porId.get(id).correo_pendiente_desde = null;
    retos.delete(clave);
    return "ok";
  });
  Usuario.restablecerContrasenaCorreo.mockImplementation(async (id, hash, contrasena) => {
    const clave = `${id}:recuperar`;
    if (retos.get(clave) !== hash) return "invalido";
    const usuario = porId.get(id);
    usuario.contrasena = contrasena;
    usuario.correo_version_movil += 1;
    retos.delete(clave);
    return "ok";
  });
  RegistroPublico.crearCliente.mockImplementation(
    async ({ idUsuario, rol, usuario, contacto, requiereConfirmacion }) => {
      const cuenta = {
        id_usuario: idUsuario,
        rol,
        ...usuario,
        correo_pendiente_desde: requiereConfirmacion ? new Date().toISOString() : null,
        correo_version_movil: 0,
      };
      porId.set(idUsuario, cuenta);
      porEmail.set(contacto.correo, cuenta);
    },
  );
  Usuario.registerAccount.mockImplementation(async (id, datos, tabla) => {
    if (tabla === "usuario") porId.set(id, { id_usuario: id, ...datos });
    if (tabla === "datos_confidenciales") porEmail.set(datos.correo, porId.get(id));
  });
});
afterAll(() => {
  for (const ruta of [archivo, `${archivo}.tmp`]) if (existsSync(ruta)) unlinkSync(ruta);
  rmdirSync(carpeta);
  vi.unstubAllEnvs();
  vi.unstubAllGlobals();
});

describe("Extensión móvil sin sustituir el login actual", () => {
  it("desactivada conserva exactamente el contrato móvil y la cookie web", async () => {
    const movil = await request(app)
      .post(`${base}/login`)
      .set("x-client", "mobile")
      .send({ correo: "anterior@gmail.com", contrasena: "Anterior1234" });
    expect(movil.status).toBe(200);
    expect(movil.body.user).toEqual({ uuid: "anterior@gmail.com", rol: "Cliente" });
    expect(movil.headers["set-cookie"]).toBeUndefined();
    expect(jwt.verify(movil.body.token, process.env.JWT_SECRET).correoVersionMovil).toBeUndefined();
    const web = await request(app)
      .post(`${base}/login`)
      .send({ correo: "anterior@gmail.com", contrasena: "Anterior1234" });
    expect(web.body).toEqual({ user: { uuid: "anterior@gmail.com", rol: "Cliente" } });
    expect(
      (await request(app).get(`${base}/me`).set("Cookie", web.headers["set-cookie"])).status,
    ).toBe(200);
  });
  it("habilitada conserva las cuentas anteriores y los roles web", async () => {
    vi.stubEnv("CORREO_MOVIL_MODO", "local");
    expect(
      (
        await request(app)
          .post(`${base}/login`)
          .set("x-client", "mobile")
          .send({ correo: "anterior@gmail.com", contrasena: "Anterior1234" })
      ).status,
    ).toBe(200);
    for (const correo of ["root@gmail.com", "admin@gmail.com"]) {
      const login = await request(app)
        .post(`${base}/login`)
        .send({ correo, contrasena: "Anterior1234" });
      expect(login.status).toBe(200);
      const cookie = login.headers["set-cookie"][0].split(";")[0];
      expect(
        jwt.verify(cookie.slice(6), process.env.JWT_SECRET).correoVersionMovil,
      ).toBeUndefined();
      if (correo.startsWith("root")) rootCookie = cookie;
    }
  });
  it.each([undefined, null, "", "   ", "06700", "colonia-invalida"])(
    "rechaza la colonia inválida %s antes de escribir en Postgres",
    async (idCodigoPostal) => {
      const altasPrevias = RegistroPublico.crearCliente.mock.calls.length;
      const respuesta = await request(app)
        .post(`${base}/register`)
        .send({ ...registro("nuevo@gmail.com"), id_codigo_postal: idCodigoPostal });
      expect(respuesta.status).toBe(422);
      expect(respuesta.body.fields.id_codigo_postal).toContain("Selecciona una colonia");
      expect(RegistroPublico.crearCliente).toHaveBeenCalledTimes(altasPrevias);
    },
  );
  it("añade confirmación sólo al Cliente nuevo, sin permitir saltarla usando cookies", async () => {
    const respuesta = await request(app).post(`${base}/register`).send(registro("nuevo@gmail.com"));
    expect(respuesta.status).toBe(201);
    expect(respuesta.body.requiereConfirmacion).toBe(true);
    expect(respuesta.body.correoEnviado).toBe(true);
    expect(respuesta.body.codigo).toBeUndefined();
    expect(RegistroPublico.crearCliente).toHaveBeenCalled();
    expect(RegistroPublico.crearCliente).toHaveBeenLastCalledWith(
      expect.objectContaining({ requiereConfirmacion: true }),
    );
    const codigo = codigoReciente();
    for (const movil of [false, true]) {
      let req = request(app).post(`${base}/login`);
      if (movil) req = req.set("x-client", "mobile");
      const login = await req.send({ correo: "nuevo@gmail.com", contrasena: "Anterior1234" });
      expect(login.status).toBe(403);
      expect(login.body.code).toBe("EMAIL_NOT_CONFIRMED");
      expect(login.body.token).toBeUndefined();
    }
    const previo = jwt.sign(
      { uuid: respuesta.body.user.uuid, rol: "Cliente" },
      process.env.JWT_SECRET,
    );
    expect(
      (
        await request(app)
          .get(`${base}/me`)
          .set("x-client", "mobile")
          .set("Authorization", `Bearer ${previo}`)
      ).status,
    ).toBe(401);
    expect(
      (
        await request(app)
          .post(`${base}/correo-movil/confirmar`)
          .send({ correo: "nuevo@gmail.com", codigo })
      ).status,
    ).toBe(200);
    expect(
      (
        await request(app)
          .post(`${base}/correo-movil/confirmar`)
          .send({ correo: "nuevo@gmail.com", codigo })
      ).status,
    ).toBe(400);
    const login = await request(app)
      .post(`${base}/login`)
      .set("x-client", "mobile")
      .send({ correo: "nuevo@gmail.com", contrasena: "Anterior1234" });
    expect(login.status).toBe(200);
    nuevoToken = login.body.token;
  });
  it("la marca en Supabase impide entrar aunque falte el estado local", async () => {
    porEmail.set("pendiente@gmail.com", {
      id_usuario: "sin-archivo",
      rol: "Cliente",
      contrasena: await bcrypt.hash("Anterior1234", 4),
      correo_pendiente_desde: new Date().toISOString(),
    });
    const login = await request(app)
      .post(`${base}/login`)
      .send({ correo: "pendiente@gmail.com", contrasena: "Anterior1234" });
    expect(login.status).toBe(403);
    expect(login.body.code).toBe("EMAIL_NOT_CONFIRMED");
  });
  it("cambia la contraseña sólo con código válido e invalida el JWT anterior", async () => {
    expect(
      (
        await request(app)
          .post(`${base}/correo-movil/recuperar`)
          .send({ correo: "nuevo@gmail.com" })
      ).status,
    ).toBe(202);
    const codigo = codigoReciente();
    const cambio = await request(app).post(`${base}/correo-movil/restablecer`).send({
      correo: "nuevo@gmail.com",
      codigo,
      contrasena: "Nueva1234",
      confirmContrasena: "Nueva1234",
    });
    expect(cambio.status).toBe(200);
    expect(
      (
        await request(app)
          .get(`${base}/me`)
          .set("x-client", "mobile")
          .set("Authorization", `Bearer ${nuevoToken}`)
      ).status,
    ).toBe(401);
    expect(
      (
        await request(app)
          .post(`${base}/login`)
          .send({ correo: "nuevo@gmail.com", contrasena: "Anterior1234" })
      ).status,
    ).toBe(401);
    expect(
      (
        await request(app)
          .post(`${base}/login`)
          .send({ correo: "nuevo@gmail.com", contrasena: "Nueva1234" })
      ).status,
    ).toBe(200);
  });
  it("el alta de Administradores conserva su respuesta y su protección de Root", async () => {
    expect((await request(app).post(`${base}/register-admin`).send({})).status).toBe(401);
    const respuesta = await request(app)
      .post(`${base}/register-admin`)
      .set("Cookie", rootCookie)
      .send(registro("otraadmin@gmail.com"));
    expect(respuesta.status).toBe(201);
    expect(respuesta.body.requiereConfirmacion).toBeUndefined();
    expect(respuesta.body.user.rol).toBe("Administrador");
  });
  it("oculta la existencia de cuentas", async () => {
    const r = await request(app)
      .post(`${base}/correo-movil/recuperar`)
      .send({ correo: "nadie@gmail.com" });
    expect(r.status).toBe(202);
    expect(r.body.codigo).toBeUndefined();
  });
  it("no expone detalles internos cuando falla el login", async () => {
    Usuario.fetchByEmail.mockRejectedValueOnce(new Error("detalle privado de la base"));
    const respuesta = await request(app)
      .post(`${base}/login`)
      .send({ correo: "anterior@gmail.com", contrasena: "Anterior1234" });
    expect(respuesta.status).toBe(500);
    expect(respuesta.body).toEqual({ code: "INTERNAL_ERROR", error: "Error interno del servidor" });
  });
  it("distingue una falla de Supabase de un token inválido", async () => {
    Usuario.estadoCorreo.mockRejectedValueOnce(new Error("Supabase no disponible"));
    const respuesta = await request(app)
      .get(`${base}/me`)
      .set("x-client", "mobile")
      .set("Authorization", `Bearer ${nuevoToken}`);
    expect(respuesta.status).toBe(503);
    expect(respuesta.body).toEqual({
      code: "SERVICE_UNAVAILABLE",
      error: "Servicio temporalmente no disponible",
    });
  });
  it("no expone detalles internos cuando falla el registro", async () => {
    RegistroPublico.crearCliente.mockRejectedValueOnce(new Error("detalle privado de la base"));
    const respuesta = await request(app).post(`${base}/register`).send(registro("fallo@gmail.com"));
    expect(respuesta.status).toBe(500);
    expect(respuesta.body).toEqual({ code: "INTERNAL_ERROR", error: "Error interno del servidor" });
  });
});
