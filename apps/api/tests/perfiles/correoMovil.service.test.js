import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { mkdtempSync, readFileSync, existsSync, unlinkSync, rmdirSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import bcrypt from "bcryptjs";
vi.mock("../../src/config/supabaseClient.js", () => ({ supabaseAdmin: {}, supabase: {} }));
import {
  crearCorreoMovil,
  enviarCodigoResend,
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
  carpeta = mkdtempSync(join(tmpdir(), "qarino-correo-movil-"));
  archivo = join(carpeta, "estado.json");
  ahora = 1_700_000_000_000;
  usuarios = {
    fetchByEmail: vi.fn().mockResolvedValue({ id_usuario: "cliente", rol: "Cliente" }),
    updatePassword: vi.fn(),
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
        status: 400,
      });
    await expect(servicio.consumir(correo, "012345", "confirmar")).rejects.toMatchObject({
      status: 400,
    });
  });
  it("vence la confirmación a las 72 horas", async () => {
    await servicio.registrar("cliente", correo);
    ahora += 72 * 60 * 60_000;
    await expect(servicio.consumir(correo, "012345", "confirmar")).rejects.toMatchObject({
      status: 400,
    });
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
    ).rejects.toMatchObject({ status: 422 });
    await servicio.consumir(correo, "012345", "recuperar", "Nueva1234", "Nueva1234");
    expect(await bcrypt.compare("Nueva1234", usuarios.updatePassword.mock.calls[0][1])).toBe(true);
    expect(servicio.estado("cliente").version).toBe(1);
    await expect(
      servicio.consumir(correo, "012345", "recuperar", "Nueva1234", "Nueva1234"),
    ).rejects.toThrow();
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
      expect(verificarCorreoMovil({ uuid: rol, rol })).toBe(true);
    }
    expect(enviar).not.toHaveBeenCalled();
  });
  it("mantiene la cuenta pendiente si falla la entrega", async () => {
    enviar.mockRejectedValueOnce(new Error("falló proveedor"));
    expect(await servicio.registrar("cliente", correo)).toEqual({ enviado: false });
    expect(servicio.estado("cliente").confirmado).toBe(false);
  });
  it("conserva la validación anterior si está desactivado y rechaza el almacenamiento local en producción", () => {
    vi.stubEnv("CORREO_MOVIL_MODO", "desactivado");
    expect(verificarCorreoMovil({ uuid: "cliente", rol: "Cliente" })).toBe(true);
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
    expect(await enviarCodigoResend({ para: correo, codigo: "012345", tipo: "confirmar" })).toEqual(
      { id: "correo-ficticio" },
    );
    const [url, request] = fetchMock.mock.calls[0];
    expect(url).toBe("https://api.resend.com/emails");
    expect(request.headers.Authorization).toBe("Bearer clave-ficticia");
    expect(JSON.parse(request.body).text).toContain("012345");
  });
});
