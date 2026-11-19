import { afterAll, beforeAll, describe, expect, it, vi } from "vite-plus/test";
import { mkdtempSync, unlinkSync, rmdirSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import express from "express";
import cookieParser from "cookie-parser";
import request from "supertest";

vi.mock("../../src/config/supabaseClient.js", () => ({ supabase: {}, supabaseAdmin: {} }));
vi.mock("../../src/modules/perfiles/http/modelosPerfiles/user.model.js", () => ({
  default: {
    registerAccount: vi.fn(),
    deleteAccount: vi.fn(),
    fetchByEmail: vi.fn(),
    fetchEmailByUUID: vi.fn(),
    updatePassword: vi.fn(),
    fetchProfileByUUID: vi.fn(),
  },
}));
vi.mock("../../src/modules/perfiles/http/modelosPerfiles/cps.model.js", () => ({
  default: { existePorId: vi.fn().mockResolvedValue(true) },
}));
// El registro público sigue usando la estrategia transaccional de develop, sin abrir la BD.
vi.mock("../../src/modules/perfiles/http/modelosPerfiles/registroPublico.model.js", () => ({
  default: { crearCliente: vi.fn() },
}));

import { router } from "../../src/modules/perfiles/index.js";
import Usuario from "../../src/modules/perfiles/http/modelosPerfiles/user.model.js";
import RegistroPublico from "../../src/modules/perfiles/http/modelosPerfiles/registroPublico.model.js";
import jwt from "jsonwebtoken";

const carpeta = mkdtempSync(join(tmpdir(), "qarino-correo-rutas-"));
const archivo = join(carpeta, "estado.json");
const app = express();
app.use(express.json(), cookieParser());
app.use("/api/perfiles", router);
const ruta = "/api/perfiles";
const correo = "demo@gmail.com";
let cuenta;
const tokenDe = (mensaje) =>
  new URLSearchParams(new URL(mensaje.enlace).hash.slice(1)).get("token");
beforeAll(() => {
  vi.stubEnv("PERFILES_CORREO_MODO", "local");
  vi.stubEnv("PERFILES_CORREO_ARCHIVO", archivo);
  vi.stubEnv("EMAIL_TRANSPORT", "inbox");
  vi.stubEnv("JWT_SECRET", "secreto-ficticio-de-prueba-correo");
  RegistroPublico.crearCliente.mockImplementation(async ({ idUsuario, rol, usuario }) => {
    cuenta = { id_usuario: idUsuario, rol, ...usuario };
  });
  Usuario.registerAccount.mockImplementation(async (id, datos, tabla) => {
    if (tabla === "usuario") cuenta = { id_usuario: id, ...datos };
  });
  Usuario.fetchByEmail.mockImplementation(async (email) => (email === correo ? cuenta : null));
  Usuario.fetchEmailByUUID.mockResolvedValue(correo);
  Usuario.updatePassword.mockImplementation(async (_id, contrasena) => {
    cuenta.contrasena = contrasena;
  });
});
afterAll(() => {
  try {
    unlinkSync(archivo);
  } catch (error) {
    if (error.code !== "ENOENT") throw error;
  }
  rmdirSync(carpeta);
  vi.unstubAllEnvs();
});

describe("Recorrido HTTP de correo y login", () => {
  it("registra, exige confirmación, inicia sesión, recupera y revoca la sesión anterior", async () => {
    const registro = await request(app).post(`${ruta}/register`).send({
      nombre: "Prueba",
      apellidos: "Correo",
      correo,
      telefono: "5555555555",
      fecha_nacimiento: "1998-01-01",
      calle: "Prueba",
      numero_exterior: "1",
      numero_interior: "",
      id_codigo_postal: "cp-demo",
      contrasena: "Anterior1234",
      confirmContrasena: "Anterior1234",
      privacidad: true,
    });
    expect(registro.status).toBe(201);
    expect(registro.body.requiereConfirmacion).toBe(true);
    const pendiente = await request(app)
      .post(`${ruta}/login`)
      .send({ correo, contrasena: "Anterior1234" });
    expect(pendiente.status).toBe(403);
    expect(pendiente.body.code).toBe("EMAIL_NOT_CONFIRMED");
    const bandeja = await request(app).get(`${ruta}/correo/bandeja`);
    expect(bandeja.status).toBe(200);
    const token = tokenDe(bandeja.body.mensajes[0]);
    expect((await request(app).post(`${ruta}/correo/confirmar`).send({ token })).status).toBe(200);
    expect((await request(app).post(`${ruta}/correo/confirmar`).send({ token })).status).toBe(400);
    const login = await request(app)
      .post(`${ruta}/login`)
      .send({ correo, contrasena: "Anterior1234" });
    expect(login.status).toBe(200);
    expect(login.body.user).toEqual({ uuid: cuenta.id_usuario, rol: "Cliente" });
    const cookie = login.headers["set-cookie"][0].split(";")[0];
    const sesion = await request(app).get(`${ruta}/me`).set("Cookie", cookie);
    expect(sesion.status).toBe(200);
    expect(sesion.body.user).toEqual({ uuid: cuenta.id_usuario, rol: "Cliente" });
    expect((await request(app).post(`${ruta}/correo/recuperar`).send({ correo })).status).toBe(202);
    const mensajes = await request(app).get(`${ruta}/correo/bandeja`);
    const recuperacion = tokenDe(mensajes.body.mensajes[0]);
    expect(
      (
        await request(app)
          .post(`${ruta}/correo/restablecer`)
          .send({ token: recuperacion, contrasena: "Nueva1234", confirmContrasena: "Nueva1234" })
      ).status,
    ).toBe(200);
    expect((await request(app).get(`${ruta}/me`).set("Cookie", cookie)).status).toBe(401);
    expect(
      (await request(app).post(`${ruta}/login`).send({ correo, contrasena: "Anterior1234" }))
        .status,
    ).toBe(401);
    expect(
      (await request(app).post(`${ruta}/login`).send({ correo, contrasena: "Nueva1234" })).status,
    ).toBe(200);
    // Android conserva su respuesta con token y user.uuid; no se modifican sus vistas ni DTO.
    const movil = await request(app)
      .post(`${ruta}/login`)
      .set("x-client", "mobile")
      .send({ correo, contrasena: "Nueva1234" });
    expect(movil.body.user.uuid).toBe(cuenta.id_usuario);
    expect(typeof movil.body.token).toBe("string");
    expect(movil.headers["set-cookie"]).toBeUndefined();
  });

  it("oculta la bandeja a hosts externos y en producción", async () => {
    expect(
      (await request(app).get(`${ruta}/correo/bandeja`).set("Host", "publico.example.com")).status,
    ).toBe(404);
    vi.stubEnv("NODE_ENV", "production");
    expect((await request(app).get(`${ruta}/correo/bandeja`)).status).toBe(404);
    vi.stubEnv("NODE_ENV", "test");
  });

  it("mantiene el registro de Administradores reservado a Root", async () => {
    expect((await request(app).post(`${ruta}/register-admin`).send({})).status).toBe(401);
    const tokenCliente = jwt.sign({ uuid: "cliente", rol: "Cliente" }, process.env.JWT_SECRET);
    expect(
      (
        await request(app)
          .post(`${ruta}/register-admin`)
          .set("Cookie", `token=${tokenCliente}`)
          .send({})
      ).status,
    ).toBe(403);
  });

  it("crea el Administrador desde Root sin sustituir su sesión y exige confirmar al nuevo usuario", async () => {
    const tokenRoot = jwt.sign({ uuid: "root-actual", rol: "Root" }, process.env.JWT_SECRET);
    const registro = await request(app)
      .post(`${ruta}/register-admin`)
      .set("Cookie", `token=${tokenRoot}`)
      .send({
        nombre: "Nueva",
        apellidos: "Administradora",
        correo,
        telefono: "5555555555",
        fecha_nacimiento: "1998-01-01",
        calle: "Prueba",
        numero_exterior: "1",
        numero_interior: "",
        id_codigo_postal: "cp-demo",
        contrasena: "Admin1234",
        confirmContrasena: "Admin1234",
        privacidad: true,
      });
    expect(registro.status).toBe(201);
    expect(registro.body.user).toEqual({ uuid: cuenta.id_usuario, rol: "Administrador" });
    expect(registro.body.requiereConfirmacion).toBe(true);
    expect(registro.headers["set-cookie"]).toBeUndefined();
    expect(Usuario.registerAccount).toHaveBeenCalledWith(cuenta.id_usuario, {}, "admin");
    const login = await request(app)
      .post(`${ruta}/login`)
      .send({ correo, contrasena: "Admin1234" });
    expect(login.status).toBe(403);
    const mensajes = await request(app).get(`${ruta}/correo/bandeja`);
    const token = tokenDe(mensajes.body.mensajes[0]);
    expect((await request(app).post(`${ruta}/correo/confirmar`).send({ token })).status).toBe(200);
    expect(
      (await request(app).post(`${ruta}/login`).send({ correo, contrasena: "Admin1234" })).body.user
        .rol,
    ).toBe("Administrador");
  });

  it("conserva el contrato y registro transaccional cuando el correo está desactivado", async () => {
    vi.stubEnv("PERFILES_CORREO_MODO", "desactivado");
    const registro = await request(app).post(`${ruta}/register`).send({
      nombre: "Cliente",
      apellidos: "Sin correo",
      correo,
      telefono: "5555555555",
      fecha_nacimiento: "1998-01-01",
      calle: "Prueba",
      numero_exterior: "1",
      numero_interior: "",
      id_codigo_postal: "cp-demo",
      contrasena: "Prueba1234",
      confirmContrasena: "Prueba1234",
      privacidad: true,
    });
    expect(registro.status).toBe(201);
    expect(registro.body).toEqual({ user: { uuid: cuenta.id_usuario, rol: "Cliente" } });
    expect(RegistroPublico.crearCliente).toHaveBeenCalled();
    expect(
      (await request(app).post(`${ruta}/login`).send({ correo, contrasena: "Prueba1234" })).status,
    ).toBe(200);
    vi.stubEnv("PERFILES_CORREO_MODO", "local");
  });

  it("no expone una bandeja local cuando se configura Brevo", async () => {
    vi.stubEnv("EMAIL_TRANSPORT", "brevo");
    expect((await request(app).get(`${ruta}/correo/config`)).body.demo).toBe(false);
    expect((await request(app).get(`${ruta}/correo/bandeja`)).status).toBe(404);
    vi.stubEnv("EMAIL_TRANSPORT", "inbox");
  });

  it("limita las solicitudes de enlaces y valida el correo", async () => {
    expect((await request(app).post(`${ruta}/correo/recuperar`).send({ correo: [] })).status).toBe(
      422,
    );
    let respuesta;
    for (let i = 0; i < 31; i++)
      respuesta = await request(app)
        .post(`${ruta}/correo/recuperar`)
        .send({ correo: "desconocido@gmail.com" });
    expect(respuesta.status).toBe(429);
    expect(respuesta.headers["retry-after"]).toBe("900");
  });
});
