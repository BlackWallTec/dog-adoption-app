import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { mkdtempSync, readFileSync, existsSync, unlinkSync, rmdirSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import bcrypt from "bcryptjs";
import { ERROR_CODES } from "@qarino/shared/errores.js";
import nodemailer from "nodemailer";
vi.mock("nodemailer", () => ({ default: { createTransport: vi.fn() } }));
vi.mock("../../src/config/supabaseClient.js", () => ({ supabaseAdmin: {}, supabase: {} }));
import {
  crearCorreoMovil,
  crearCorreoMovilSupabase,
  enviarCodigoCorreo,
  verificarCorreoMovil,
  servicioCorreoMovil,
} from "../../src/modules/perfiles/services/correoMovil.service.js";

let carpeta, archivo, ahora, usuarios, enviar, codigo, servicio;
const correo = "prueba@example.com";
const construir = () =>
  crearCorreoMovil({
    archivo,
    usuarios,
    enviar,
    codigo,
    ahora: () => ahora,
    secreto: "secreto-de-prueba",
  });
beforeEach(() => {
  vi.stubEnv("MAIL_TRANSPORT", "resend");
  carpeta = mkdtempSync(join(tmpdir(), "qarino-correo-movil-"));
  archivo = join(carpeta, "estado.json");
  ahora = 1_700_000_000_000;
  const usuario = { id_usuario: "cliente", rol: "Cliente" };
  usuarios = {
    fetchByEmail: vi.fn().mockResolvedValue(usuario),
    confirmarCorreo: vi.fn().mockResolvedValue(true),
    updatePassword: vi.fn(async (_id, hash) => {
      usuario.contrasena = hash;
    }),
  };
  enviar = vi.fn().mockResolvedValue({ id: "ficticio" });
  codigo = vi.fn().mockReturnValue("012345");
  servicio = construir();
});
afterEach(() => {
  for (const ruta of [archivo, `${archivo}.tmp`]) if (existsSync(ruta)) unlinkSync(ruta);
  rmdirSync(carpeta);
  vi.unstubAllEnvs();
  vi.unstubAllGlobals();
});

describe("Códigos de correo para Clientes", () => {
  it("conserva el acceso de cuentas anteriores", () => {
    expect(servicio.estado("anterior")).toEqual({ confirmado: true, version: 0 });
  });
  it("conserva ceros iniciales, persiste hashes y confirma una sola vez", async () => {
    await servicio.registrar("cliente", correo);
    expect(enviar.mock.calls[0][0].codigo).toBe("012345");
    const datos = JSON.parse(readFileSync(archivo, "utf8"));
    expect(datos.cuentas.cliente.confirmar.hash).toHaveLength(64);
    expect(datos.cuentas.cliente.confirmar.codigo).toBeUndefined();
    expect(construir().estado("cliente").confirmado).toBe(false);
    const resultados = await Promise.allSettled([
      servicio.consumir(correo, "012345", "confirmar"),
      servicio.consumir(correo, "012345", "confirmar"),
    ]);
    expect(resultados.map((r) => r.status)).toEqual(["fulfilled", "rejected"]);
    expect(servicio.estado("cliente").confirmado).toBe(true);
  });
  it("bloquea el código después de cinco intentos incorrectos", async () => {
    await servicio.registrar("cliente", correo);
    for (let i = 0; i < 5; i++)
      await expect(servicio.consumir(correo, "999999", "confirmar")).rejects.toMatchObject({
        code: ERROR_CODES.INVALID_EMAIL_CODE,
      });
    await expect(servicio.consumir(correo, "012345", "confirmar")).rejects.toMatchObject({
      code: ERROR_CODES.INVALID_EMAIL_CODE,
    });
  });
  it("vence la confirmación a las 72 horas", async () => {
    await servicio.registrar("cliente", correo);
    ahora += 72 * 60 * 60_000;
    await expect(servicio.consumir(correo, "012345", "confirmar")).rejects.toMatchObject({
      code: ERROR_CODES.INVALID_EMAIL_CODE,
    });
  });
  it("reenviar no extiende el plazo original de la cuenta", async () => {
    await servicio.registrar("cliente", correo);
    ahora += 71 * 60 * 60_000;
    await servicio.solicitar(correo, "confirmar");
    ahora += 60 * 60_000;
    await expect(servicio.consumir(correo, "012345", "confirmar")).rejects.toThrow();
    await servicio.solicitar(correo, "confirmar");
    expect(enviar).toHaveBeenCalledTimes(2);
    expect(usuarios.confirmarCorreo).not.toHaveBeenCalled();
  });
  it("conserva el código si falla Supabase y no confirma una cuenta eliminada o vencida", async () => {
    await servicio.registrar("cliente", correo);
    usuarios.confirmarCorreo.mockRejectedValueOnce(new Error("sin conexión"));
    await expect(servicio.consumir(correo, "012345", "confirmar")).rejects.toThrow("sin conexión");
    usuarios.confirmarCorreo.mockResolvedValueOnce(false);
    await expect(servicio.consumir(correo, "012345", "confirmar")).rejects.toThrow("no es válido");
    expect(servicio.estado("cliente").confirmado).toBe(false);
    await expect(servicio.consumir(correo, "012345", "confirmar")).resolves.toEqual({ ok: true });
  });
  it("recupera la condición pendiente desde Supabase si no existe el archivo local", async () => {
    usuarios.fetchByEmail.mockResolvedValue({
      id_usuario: "cliente",
      rol: "Cliente",
      correo_pendiente_desde: new Date(ahora).toISOString(),
    });
    ahora += 71 * 60 * 60_000;
    await servicio.solicitar(correo, "confirmar");
    expect(servicio.estado("cliente").confirmado).toBe(false);
    expect(enviar).toHaveBeenCalledTimes(1);
    ahora += 60 * 60_000;
    await expect(servicio.consumir(correo, "012345", "confirmar")).rejects.toThrow();
  });
  it("limita el reenvío, mantiene una respuesta genérica e invalida el código anterior", async () => {
    await servicio.registrar("cliente", correo);
    const conocido = await servicio.solicitar(correo, "confirmar");
    expect(enviar).toHaveBeenCalledTimes(1);
    usuarios.fetchByEmail.mockResolvedValueOnce(null);
    expect(await servicio.solicitar("desconocido@example.com", "confirmar")).toEqual(conocido);
    ahora += 60_000;
    codigo.mockReturnValue("654321");
    await servicio.solicitar(correo, "confirmar");
    await expect(servicio.consumir(correo, "012345", "confirmar")).rejects.toThrow("no es válido");
    expect(await servicio.consumir(correo, "654321", "confirmar")).toEqual({ ok: true });
  });
  it("recupera mediante bcrypt y aumenta la versión de sesión", async () => {
    await servicio.solicitar(correo, "recuperar");
    await expect(
      servicio.consumir(correo, "012345", "recuperar", "Nueva1234", "Distinta123"),
    ).rejects.toMatchObject({ code: ERROR_CODES.VALIDATION });
    await servicio.consumir(correo, "012345", "recuperar", "Nueva1234", "Nueva1234");
    expect(await bcrypt.compare("Nueva1234", usuarios.updatePassword.mock.calls[0][1])).toBe(true);
    expect(servicio.estado("cliente").version).toBe(1);
    await expect(
      servicio.consumir(correo, "012345", "recuperar", "Nueva1234", "Nueva1234"),
    ).rejects.toThrow();
  });
  it("no confirma la recuperación si la base no guarda el cambio y permite reintentar", async () => {
    await servicio.solicitar(correo, "recuperar");
    usuarios.updatePassword.mockImplementationOnce(async () => {});
    await expect(
      servicio.consumir(correo, "012345", "recuperar", "Nueva1234", "Nueva1234"),
    ).rejects.toMatchObject({ code: ERROR_CODES.PASSWORD_UPDATE_FAILED });
    expect(servicio.estado("cliente").version).toBe(0);
    await expect(
      servicio.consumir(correo, "012345", "recuperar", "Nueva1234", "Nueva1234"),
    ).resolves.toEqual({ ok: true });
  });
  it("vence la recuperación a los quince minutos", async () => {
    await servicio.solicitar(correo, "recuperar");
    ahora += 15 * 60_000;
    await expect(
      servicio.consumir(correo, "012345", "recuperar", "Nueva1234", "Nueva1234"),
    ).rejects.toThrow();
    expect(usuarios.updatePassword).not.toHaveBeenCalled();
  });
  it("no actúa sobre Root ni Administradores", async () => {
    for (const rol of ["Root", "Administrador"]) {
      usuarios.fetchByEmail.mockResolvedValue({ id_usuario: rol, rol });
      await servicio.solicitar(correo, "recuperar");
      expect(await verificarCorreoMovil({ uuid: rol, rol })).toBe(true);
    }
    expect(enviar).not.toHaveBeenCalled();
  });
  it("mantiene la cuenta pendiente si falla la entrega", async () => {
    enviar.mockRejectedValueOnce(new Error("falló proveedor"));
    expect(await servicio.registrar("cliente", correo)).toEqual({ enviado: false });
    expect(servicio.estado("cliente").confirmado).toBe(false);
  });
  it("conserva la validación anterior si está desactivado y rechaza el almacenamiento local en producción", async () => {
    vi.stubEnv("CORREO_MOVIL_MODO", "desactivado");
    expect(await verificarCorreoMovil({ uuid: "cliente", rol: "Cliente" })).toBe(true);
    vi.stubEnv("CORREO_MOVIL_MODO", "local");
    vi.stubEnv("NODE_ENV", "production");
    expect(() => servicioCorreoMovil()).toThrow("producción");
  });
  it("usa Resend sin devolver el código ni exponer la clave al cliente", async () => {
    vi.stubEnv("RESEND_API_KEY", "clave-ficticia");
    vi.stubEnv("MAIL_FROM", "onboarding@resend.dev");
    const fetchMock = vi
      .fn()
      .mockResolvedValue({ ok: true, json: async () => ({ id: "correo-ficticio" }) });
    vi.stubGlobal("fetch", fetchMock);
    expect(await enviarCodigoCorreo({ para: correo, codigo: "012345", tipo: "confirmar" })).toEqual(
      { id: "correo-ficticio" },
    );
    const [url, request] = fetchMock.mock.calls[0];
    expect(url).toBe("https://api.resend.com/emails");
    expect(request.headers.Authorization).toBe("Bearer clave-ficticia");
    expect(JSON.parse(request.body).text).toContain("012345");
  });
  it("envía con Gmail al destinatario solicitado usando la contraseña de aplicación", async () => {
    vi.stubEnv("MAIL_TRANSPORT", "gmail");
    vi.stubEnv("SMTP_USER", "proyecto@gmail.com");
    vi.stubEnv("SMTP_PASS", "abcd efgh ijkl mnop");
    const sendMail = vi.fn().mockResolvedValue({ messageId: "correo-ficticio" });
    nodemailer.createTransport.mockReturnValueOnce({ sendMail });
    await expect(
      enviarCodigoCorreo({ para: correo, codigo: "012345", tipo: "recuperar" }),
    ).resolves.toEqual({ messageId: "correo-ficticio" });
    expect(nodemailer.createTransport).toHaveBeenLastCalledWith(
      expect.objectContaining({
        service: "gmail",
        auth: { user: "proyecto@gmail.com", pass: "abcdefghijklmnop" },
      }),
    );
    expect(sendMail).toHaveBeenCalledWith(
      expect.objectContaining({
        from: "Qariño Animal <proyecto@gmail.com>",
        to: [correo],
        subject: "Recupera tu contraseña",
        text: expect.stringContaining("012345"),
      }),
    );
  });
  it("rechaza Gmail sin contraseña de aplicación antes de conectarse", async () => {
    vi.stubEnv("MAIL_TRANSPORT", "gmail");
    vi.stubEnv("SMTP_USER", "proyecto@gmail.com");
    vi.stubEnv("SMTP_PASS", "");
    nodemailer.createTransport.mockClear();
    await expect(
      enviarCodigoCorreo({ para: correo, codigo: "012345", tipo: "confirmar" }),
    ).rejects.toThrow("Falta SMTP_PASS");
    expect(nodemailer.createTransport).not.toHaveBeenCalled();
  });
});

// Doble en memoria con la misma semántica que las RPC de la migración
// perfiles_codigos_correo_supabase: 72 h desde el registro, 15 min, 1 min y 5 intentos.
function baseSupabase(t) {
  const usuario = {
    id_usuario: "cliente",
    rol: "Cliente",
    contrasena: "hash-anterior",
    correo_pendiente_desde: new Date(t.ahora).toISOString(),
  };
  const fila = { version: 0 };
  return {
    usuario,
    fila,
    fetchByEmail: vi.fn(async () => ({ ...usuario })),
    obtenerEstadoCorreoMovil: vi.fn(async () => ({
      confirmado: usuario.correo_pendiente_desde === null,
      version: fila.version,
    })),
    emitirCodigoCorreoMovil: vi.fn(async (_id, tipo, hash) => {
      if (usuario.rol !== "Cliente") return "not_eligible";
      const pendiente = usuario.correo_pendiente_desde;
      if ((tipo === "confirmar") !== Boolean(pendiente)) return "not_eligible";
      const vence =
        tipo === "confirmar" ? Date.parse(pendiente) + 72 * 3_600_000 : t.ahora + 15 * 60_000;
      if (vence <= t.ahora) return "expired";
      if (fila[tipo] && fila[tipo].enviado > t.ahora - 60_000) return "rate_limited";
      fila[tipo] = { hash, enviado: t.ahora, vence, intentos: 0 };
      return "sent";
    }),
    consumirCodigoCorreoMovil: vi.fn(async (_id, tipo, hash, contrasenaHash) => {
      const pendiente = usuario.correo_pendiente_desde;
      if (tipo === "confirmar" && (!pendiente || Date.parse(pendiente) + 72 * 3_600_000 <= t.ahora))
        return "invalid";
      if (tipo === "recuperar" && (pendiente || !contrasenaHash)) return "invalid";
      const d = fila[tipo];
      if (!d || d.vence <= t.ahora || d.intentos >= 5) return "invalid";
      if (d.hash !== hash) {
        d.intentos += 1;
        return "invalid";
      }
      if (tipo === "confirmar") usuario.correo_pendiente_desde = null;
      else {
        usuario.contrasena = contrasenaHash;
        fila.version += 1;
      }
      delete fila[tipo];
      return "ok";
    }),
  };
}

describe("Códigos de correo en modo supabase", () => {
  let t, db, sb, enviarSb, codigoSb;
  const invalido = { code: ERROR_CODES.INVALID_EMAIL_CODE };
  beforeEach(() => {
    t = { ahora: 1_700_000_000_000 };
    db = baseSupabase(t);
    enviarSb = vi.fn().mockResolvedValue({});
    codigoSb = vi.fn().mockReturnValue("012345");
    sb = crearCorreoMovilSupabase({
      usuarios: db,
      enviar: enviarSb,
      codigo: codigoSb,
      secreto: "secreto-de-prueba",
    });
  });

  it("envía el código real, guarda sólo el hash y confirma una sola vez", async () => {
    expect(await sb.registrar("cliente", correo)).toEqual({ enviado: true });
    expect(enviarSb).toHaveBeenCalledWith({ para: correo, codigo: "012345", tipo: "confirmar" });
    const [, , hash] = db.emitirCodigoCorreoMovil.mock.calls[0];
    expect(hash).toMatch(/^[0-9a-f]{64}$/);
    expect(hash).not.toContain("012345");
    expect(await sb.estado("cliente")).toEqual({ confirmado: false, version: 0 });
    await expect(sb.consumir(correo, "012345", "confirmar")).resolves.toEqual({ ok: true });
    expect(await sb.estado("cliente")).toEqual({ confirmado: true, version: 0 });
    await expect(sb.consumir(correo, "012345", "confirmar")).rejects.toMatchObject(invalido);
  });

  it("rechaza códigos incorrectos, mal formados y bloquea tras cinco intentos", async () => {
    await sb.registrar("cliente", correo);
    await expect(sb.consumir(correo, "12345", "confirmar")).rejects.toMatchObject(invalido);
    expect(db.consumirCodigoCorreoMovil).not.toHaveBeenCalled();
    for (let i = 0; i < 5; i++)
      await expect(sb.consumir(correo, "999999", "confirmar")).rejects.toMatchObject(invalido);
    await expect(sb.consumir(correo, "012345", "confirmar")).rejects.toMatchObject(invalido);
  });

  it("reenviar respeta el minuto de espera y no prolonga las 72 horas", async () => {
    await sb.registrar("cliente", correo);
    await sb.solicitar(correo, "confirmar");
    expect(enviarSb).toHaveBeenCalledTimes(1);
    t.ahora += 71 * 3_600_000;
    codigoSb.mockReturnValue("654321");
    await sb.solicitar(correo, "confirmar");
    expect(enviarSb).toHaveBeenCalledTimes(2);
    await expect(sb.consumir(correo, "012345", "confirmar")).rejects.toMatchObject(invalido);
    t.ahora += 3_600_000;
    await expect(sb.consumir(correo, "654321", "confirmar")).rejects.toMatchObject(invalido);
    expect(await sb.solicitar(correo, "confirmar")).toEqual({
      message: "Si la cuenta corresponde, recibirás un código en tu correo.",
    });
    expect(enviarSb).toHaveBeenCalledTimes(2);
  });

  it("recupera la contraseña con bcrypt, sube la versión y vence a los 15 minutos", async () => {
    db.usuario.correo_pendiente_desde = null;
    await sb.solicitar(correo, "recuperar");
    await expect(
      sb.consumir(correo, "012345", "recuperar", "Nueva1234", "Otra12345"),
    ).rejects.toMatchObject({ code: ERROR_CODES.VALIDATION });
    expect(db.consumirCodigoCorreoMovil).not.toHaveBeenCalled();
    await sb.consumir(correo, "012345", "recuperar", "Nueva1234", "Nueva1234");
    expect(await bcrypt.compare("Nueva1234", db.usuario.contrasena)).toBe(true);
    expect((await sb.estado("cliente")).version).toBe(1);

    t.ahora += 60_000;
    codigoSb.mockReturnValue("111111");
    await sb.solicitar(correo, "recuperar");
    t.ahora += 15 * 60_000;
    await expect(
      sb.consumir(correo, "111111", "recuperar", "Otra12345", "Otra12345"),
    ).rejects.toMatchObject(invalido);
  });

  it("no envía recuperación a cuentas pendientes ni códigos a Root o Administradores", async () => {
    await sb.solicitar(correo, "recuperar");
    for (const rol of ["Root", "Administrador"]) {
      db.usuario.rol = rol;
      await sb.solicitar(correo, "recuperar");
      await expect(sb.consumir(correo, "012345", "confirmar")).rejects.toMatchObject(invalido);
    }
    expect(db.emitirCodigoCorreoMovil).not.toHaveBeenCalled();
    expect(enviarSb).not.toHaveBeenCalled();
  });

  it("responde igual para correos desconocidos y reporta la entrega fallida al registrar", async () => {
    db.fetchByEmail.mockResolvedValueOnce(null);
    const desconocido = await sb.solicitar("nadie@example.com", "confirmar");
    expect(await sb.solicitar(correo, "confirmar")).toEqual(desconocido);
    enviarSb.mockRejectedValueOnce(new Error("proveedor caído"));
    t.ahora += 60_000;
    expect(await sb.registrar("cliente", correo)).toEqual({ enviado: false });
  });

  it("propaga fallas de Supabase al consumir para que el código no se pierda", async () => {
    await sb.registrar("cliente", correo);
    db.consumirCodigoCorreoMovil.mockRejectedValueOnce(new Error("sin conexión"));
    await expect(sb.consumir(correo, "012345", "confirmar")).rejects.toThrow("sin conexión");
    await expect(sb.consumir(correo, "012345", "confirmar")).resolves.toEqual({ ok: true });
  });

  it("servicioCorreoMovil usa Supabase en producción y verifica la versión de sesión", async () => {
    vi.stubEnv("CORREO_MOVIL_MODO", "supabase");
    vi.stubEnv("NODE_ENV", "production");
    vi.stubEnv("JWT_SECRET", "secreto");
    expect(() => servicioCorreoMovil()).not.toThrow();
    expect(await verificarCorreoMovil({ uuid: "admin", rol: "Administrador" })).toBe(true);
  });
});
