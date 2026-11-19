import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { mkdtempSync, readFileSync, unlinkSync, rmdirSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import bcrypt from "bcryptjs";

vi.mock("../../src/config/supabaseClient.js", () => ({ supabase: {}, supabaseAdmin: {} }));
import {
  crearServicioCorreo,
  enviarConBrevo,
  servicioCorreo,
} from "../../src/modules/perfiles/services/correo.service.js";

let carpeta, archivo, reloj, usuarios, servicio;
const correo = "prueba@gmail.com";
const tokenDe = (mensaje) =>
  new URLSearchParams(new URL(mensaje.enlace).hash.slice(1)).get("token");
beforeEach(() => {
  carpeta = mkdtempSync(join(tmpdir(), "qarino-correo-test-"));
  archivo = join(carpeta, "estado.json");
  reloj = Date.now();
  usuarios = {
    fetchByEmail: vi.fn().mockResolvedValue({ id_usuario: "cuenta-1" }),
    updatePassword: vi.fn().mockResolvedValue(undefined),
  };
  servicio = crearServicioCorreo({ archivo, usuarios, ahora: () => reloj });
});
afterEach(() => {
  try {
    unlinkSync(archivo);
  } catch (error) {
    if (error.code !== "ENOENT") throw error;
  }
  rmdirSync(carpeta);
  vi.unstubAllEnvs();
  vi.unstubAllGlobals();
});

describe("Correo de perfiles: tokens y recuperación", () => {
  it("persiste una cuenta pendiente y confirma con un enlace de un solo uso", async () => {
    await servicio.registrar("cuenta-1", correo);
    expect(servicio.obtenerEstado("cuenta-1").confirmado).toBe(false);
    const token = tokenDe(servicio.bandeja()[0]);
    const persistido = JSON.parse(readFileSync(archivo, "utf8"));
    expect(persistido.cuentas["cuenta-1"].confirmar.hash).not.toBe(token);
    expect(persistido.cuentas["cuenta-1"].confirmar.hash).toHaveLength(64);
    await servicio.consumir(token, "confirmar");
    expect(crearServicioCorreo({ archivo }).obtenerEstado("cuenta-1").confirmado).toBe(true);
    await expect(servicio.consumir(token, "confirmar")).rejects.toMatchObject({ status: 400 });
  });

  it("rechaza enlaces vencidos y tokens de otro propósito", async () => {
    await servicio.registrar("cuenta-1", correo);
    const token = tokenDe(servicio.bandeja()[0]);
    await expect(
      servicio.consumir(token, "recuperar", "Nueva1234", "Nueva1234"),
    ).rejects.toMatchObject({ status: 400 });
    reloj += 72 * 60 * 60_000;
    await expect(servicio.consumir(token, "confirmar")).rejects.toMatchObject({ status: 400 });
    expect(servicio.obtenerEstado("cuenta-1").confirmado).toBe(false);
  });

  it("respeta la espera del reenvío y sólo conserva válido el enlace más reciente", async () => {
    await servicio.registrar("cuenta-1", correo);
    const anterior = tokenDe(servicio.bandeja()[0]);
    await servicio.solicitar(correo, "confirmar");
    expect(servicio.bandeja()).toHaveLength(1);
    reloj += 60_000;
    await servicio.solicitar(correo, "confirmar");
    await expect(servicio.consumir(anterior, "confirmar")).rejects.toMatchObject({ status: 400 });
    await expect(servicio.consumir(tokenDe(servicio.bandeja()[0]), "confirmar")).resolves.toEqual({
      ok: true,
    });
  });

  it("no revela si el correo existe ni permite recuperar cuentas pendientes", async () => {
    usuarios.fetchByEmail.mockResolvedValueOnce(null);
    const desconocido = await servicio.solicitar("noexiste@gmail.com", "recuperar");
    await servicio.registrar("cuenta-1", correo);
    const pendiente = await servicio.solicitar(correo, "recuperar");
    expect(pendiente).toEqual(desconocido);
    expect(servicio.bandeja()).toHaveLength(1);
  });

  it("actualiza un hash bcrypt, invalida sesiones y evita un doble consumo simultáneo", async () => {
    // Las cuentas preexistentes se consideran confirmadas sin migrar su autenticación.
    expect(servicio.obtenerEstado("cuenta-1")).toEqual({ confirmado: true, version: 0 });
    await servicio.solicitar(correo, "recuperar");
    const token = tokenDe(servicio.bandeja()[0]);
    const resultados = await Promise.allSettled([
      servicio.consumir(token, "recuperar", "Nueva1234", "Nueva1234"),
      servicio.consumir(token, "recuperar", "Otra1234", "Otra1234"),
    ]);
    expect(resultados.map((r) => r.status)).toEqual(["fulfilled", "rejected"]);
    expect(usuarios.updatePassword).toHaveBeenCalledTimes(1);
    expect(await bcrypt.compare("Nueva1234", usuarios.updatePassword.mock.calls[0][1])).toBe(true);
    expect(servicio.obtenerEstado("cuenta-1").version).toBe(1);
  });

  it("no consume el enlace si falla la escritura de contraseña o la validación", async () => {
    await servicio.solicitar(correo, "recuperar");
    const token = tokenDe(servicio.bandeja()[0]);
    await expect(servicio.consumir(token, "recuperar", "corta", "corta")).rejects.toMatchObject({
      status: 422,
    });
    await expect(
      servicio.consumir(token, "recuperar", "Nueva1234", "Diferente1234"),
    ).rejects.toMatchObject({ status: 422 });
    await expect(
      servicio.consumir(token, "recuperar", "😀".repeat(20), "😀".repeat(20)),
    ).rejects.toMatchObject({ status: 422 });
    usuarios.updatePassword.mockRejectedValueOnce(new Error("Sin conexión"));
    await expect(servicio.consumir(token, "recuperar", "Nueva1234", "Nueva1234")).rejects.toThrow(
      "Sin conexión",
    );
    await expect(servicio.consumir(token, "recuperar", "Nueva1234", "Nueva1234")).resolves.toEqual({
      ok: true,
    });
  });

  it("el enlace de recuperación vence a los 30 minutos", async () => {
    await servicio.solicitar(correo, "recuperar");
    const token = tokenDe(servicio.bandeja()[0]);
    reloj += 30 * 60_000;
    await expect(
      servicio.consumir(token, "recuperar", "Nueva1234", "Nueva1234"),
    ).rejects.toMatchObject({ status: 400 });
    expect(usuarios.updatePassword).not.toHaveBeenCalled();
  });

  it("sólo construye enlaces desde un origen confiable y rechaza el modo local en producción", () => {
    expect(() => crearServicioCorreo({ archivo, baseUrl: "http://sitio-inseguro.com" })).toThrow(
      "HTTPS",
    );
    vi.stubEnv("PERFILES_CORREO_MODO", "local");
    vi.stubEnv("NODE_ENV", "production");
    expect(() => servicioCorreo()).toThrow("sólo para desarrollo");
  });

  it("el adaptador de Brevo envía texto y mantiene la clave sólo en el backend", async () => {
    vi.stubEnv("BREVO_API_KEY", "clave-ficticia-de-prueba");
    vi.stubEnv("MAIL_FROM", "cuentas@example.com");
    vi.stubEnv("MAIL_FROM_NAME", "Qariño Animal");
    const fetchMock = vi.fn().mockResolvedValue({ ok: true });
    vi.stubGlobal("fetch", fetchMock);
    await enviarConBrevo({
      para: correo,
      asunto: "Confirmación",
      enlace: "https://example.com/#token=prueba",
    });
    const [url, opciones] = fetchMock.mock.calls[0];
    expect(url).toBe("https://api.brevo.com/v3/smtp/email");
    expect(opciones.headers["api-key"]).toBe("clave-ficticia-de-prueba");
    expect(JSON.parse(opciones.body)).toMatchObject({
      sender: { email: "cuentas@example.com", name: "Qariño Animal" },
      to: [{ email: correo }],
      subject: "Confirmación",
    });
    expect(JSON.parse(opciones.body).textContent).toContain("#token=prueba");
    fetchMock.mockResolvedValueOnce({ ok: false });
    await expect(enviarConBrevo({ para: correo })).rejects.toThrow("rechazó");
  });

  it("no intenta enviar sin clave ni con un remitente inválido", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);
    vi.stubEnv("BREVO_API_KEY", "");
    await expect(enviarConBrevo({ para: correo })).rejects.toThrow("Falta configurar");
    vi.stubEnv("BREVO_API_KEY", "clave-ficticia");
    vi.stubEnv("MAIL_FROM", "Qariño <cuentas@example.com>");
    await expect(enviarConBrevo({ para: correo })).rejects.toThrow("MAIL_FROM");
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("rechaza un transporte desconocido en lugar de activar la bandeja local", () => {
    vi.stubEnv("PERFILES_CORREO_MODO", "local");
    vi.stubEnv("EMAIL_TRANSPORT", "inexistente");
    vi.stubEnv("NODE_ENV", "test");
    expect(() => servicioCorreo()).toThrow("inbox o brevo");
  });
});
