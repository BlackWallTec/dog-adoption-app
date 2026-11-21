import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import bcrypt from "bcryptjs";
import nodemailer from "nodemailer";
import { ERROR_CODES } from "@qarino/shared/errores.js";

vi.mock("nodemailer", () => ({ default: { createTransport: vi.fn() } }));
vi.mock("../../src/config/supabaseClient.js", () => ({ supabaseAdmin: {}, supabase: {} }));

import {
  crearCorreoMovil,
  enviarCodigoCorreo,
  verificarCorreoMovil,
} from "../../src/modules/perfiles/services/correoMovil.service.js";
import Usuario from "../../src/modules/perfiles/http/modelosPerfiles/user.model.js";

const correo = "prueba@example.com";
let usuarios, enviar, servicio;

beforeEach(() => {
  vi.stubEnv("CORREO_MOVIL_MODO", "supabase");
  vi.stubEnv("MAIL_TRANSPORT", "resend");
  usuarios = {
    fetchByEmail: vi.fn().mockResolvedValue({
      id_usuario: "cliente",
      rol: "Cliente",
      correo_pendiente_desde: new Date().toISOString(),
    }),
    estadoCorreo: vi.fn().mockResolvedValue({ confirmado: false, version: 0 }),
    emitirCodigoCorreo: vi.fn().mockResolvedValue("creado"),
    confirmarCodigoCorreo: vi.fn().mockResolvedValue("ok"),
    restablecerContrasenaCorreo: vi.fn().mockResolvedValue("ok"),
  };
  enviar = vi.fn().mockResolvedValue({ id: "ficticio" });
  servicio = crearCorreoMovil({
    usuarios,
    enviar,
    secreto: "secreto-de-prueba",
    codigo: () => "012345",
  });
});

afterEach(() => {
  vi.unstubAllEnvs();
  vi.unstubAllGlobals();
  vi.clearAllMocks();
});

describe("Códigos de correo compartidos", () => {
  it("delega el estado de la cuenta a Supabase", async () => {
    await expect(servicio.estado("cliente")).resolves.toEqual({ confirmado: false, version: 0 });
    expect(usuarios.estadoCorreo).toHaveBeenCalledWith("cliente");
  });

  it("guarda sólo el hash y envía el código con ceros iniciales", async () => {
    await expect(servicio.registrar("cliente", correo)).resolves.toEqual({ enviado: true });
    const hash = usuarios.emitirCodigoCorreo.mock.calls[0][2];
    expect(hash).toMatch(/^[0-9a-f]{64}$/);
    expect(hash).not.toContain("012345");
    expect(enviar).toHaveBeenCalledWith({ para: correo, codigo: "012345", tipo: "confirmar" });
  });

  it("mantiene la cuenta pendiente cuando falla la entrega", async () => {
    enviar.mockRejectedValueOnce(new Error("proveedor no disponible"));
    await expect(servicio.registrar("cliente", correo)).resolves.toEqual({ enviado: false });
  });

  it("conserva la respuesta genérica para cuentas desconocidas y espera de reenvío", async () => {
    usuarios.fetchByEmail.mockResolvedValueOnce(null);
    const desconocido = await servicio.solicitar("nadie@example.com", "confirmar");
    usuarios.emitirCodigoCorreo.mockResolvedValueOnce("espera");
    const conocido = await servicio.solicitar(correo, "confirmar");
    expect(conocido).toEqual(desconocido);
    expect(enviar).not.toHaveBeenCalled();
  });

  it("no envía recuperación para una cuenta pendiente", async () => {
    await servicio.solicitar(correo, "recuperar");
    expect(usuarios.emitirCodigoCorreo).not.toHaveBeenCalled();
  });

  it("confirma una sola vez según el resultado atómico de Supabase", async () => {
    await expect(servicio.consumir(correo, "012345", "confirmar")).resolves.toEqual({ ok: true });
    usuarios.confirmarCodigoCorreo.mockResolvedValueOnce("invalido");
    await expect(servicio.consumir(correo, "012345", "confirmar")).rejects.toMatchObject({
      code: ERROR_CODES.INVALID_EMAIL_CODE,
    });
  });

  it("rechaza formato inválido antes de consultar la base", async () => {
    await expect(servicio.consumir(correo, "123", "confirmar")).rejects.toMatchObject({
      code: ERROR_CODES.INVALID_EMAIL_CODE,
    });
    expect(usuarios.fetchByEmail).not.toHaveBeenCalled();
  });

  it("restablece con bcrypt y valida que ambas contraseñas coincidan", async () => {
    usuarios.fetchByEmail.mockResolvedValue({
      id_usuario: "cliente",
      rol: "Cliente",
      correo_pendiente_desde: null,
    });
    await expect(
      servicio.consumir(correo, "012345", "recuperar", "Nueva1234", "Distinta123"),
    ).rejects.toMatchObject({ code: ERROR_CODES.VALIDATION });
    await servicio.consumir(correo, "012345", "recuperar", "Nueva1234", "Nueva1234");
    const hash = usuarios.restablecerContrasenaCorreo.mock.calls[0][2];
    await expect(bcrypt.compare("Nueva1234", hash)).resolves.toBe(true);
  });

  it("conserva Root y Administrador fuera de la extensión", async () => {
    for (const rol of ["Root", "Administrador"])
      await expect(verificarCorreoMovil({ uuid: rol, rol })).resolves.toBe(true);
    expect(usuarios.estadoCorreo).not.toHaveBeenCalled();
  });

  it("valida la versión compartida de la sesión del Cliente", async () => {
    vi.stubEnv("JWT_SECRET", "secreto-global-de-prueba");
    const estado = vi
      .spyOn(Usuario, "estadoCorreo")
      .mockResolvedValueOnce({ confirmado: false, version: 0 })
      .mockResolvedValueOnce({ confirmado: true, version: 0 });
    const usuario = { uuid: "cliente", rol: "Cliente", correoVersionMovil: 0 };
    await expect(verificarCorreoMovil(usuario)).resolves.toBe(false);
    await expect(verificarCorreoMovil(usuario)).resolves.toBe(true);
    expect(estado).toHaveBeenCalledTimes(2);
  });
});

describe("Proveedor de correo", () => {
  it("envía con Resend sin devolver el código al cliente", async () => {
    vi.stubEnv("RESEND_API_KEY", "clave-ficticia");
    vi.stubEnv("MAIL_FROM", "onboarding@resend.dev");
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ id: "correo-ficticio" }),
    });
    vi.stubGlobal("fetch", fetchMock);
    await enviarCodigoCorreo({ para: correo, codigo: "012345", tipo: "confirmar" });
    const [, request] = fetchMock.mock.calls[0];
    expect(request.headers.Authorization).toBe("Bearer clave-ficticia");
    expect(JSON.parse(request.body).text).toContain("012345");
  });

  it("envía con Gmail usando la contraseña de aplicación sólo en servidor", async () => {
    vi.stubEnv("MAIL_TRANSPORT", "gmail");
    vi.stubEnv("SMTP_USER", "proyecto@gmail.com");
    vi.stubEnv("SMTP_PASS", "abcd efgh ijkl mnop");
    const sendMail = vi.fn().mockResolvedValue({ messageId: "ficticio" });
    nodemailer.createTransport.mockReturnValueOnce({ sendMail });
    await enviarCodigoCorreo({ para: correo, codigo: "012345", tipo: "recuperar" });
    expect(nodemailer.createTransport).toHaveBeenCalledWith(
      expect.objectContaining({ auth: { user: "proyecto@gmail.com", pass: "abcdefghijklmnop" } }),
    );
    expect(sendMail).toHaveBeenCalledWith(expect.objectContaining({ to: [correo] }));
  });
});
