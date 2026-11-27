import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import nodemailer from "nodemailer";
import { ERROR_CODES } from "@qarino/shared/errores.js";
import { enviarCfdiPorCorreo } from "../../src/modules/donativos/http/serviciosDonativos/cfdiMail.service.js";

vi.mock("nodemailer", () => ({ default: { createTransport: vi.fn() } }));

const fetchMock = vi.fn();
const UUID = "e9bf9f1a-1445-4de3-a372-47fa82fcbd0c";
const cfdi = (extra = {}) => ({
  destinatario: "donante@example.test",
  uuid: UUID,
  pdf: Buffer.from("%PDF-1.7"),
  xml: Buffer.from("<cfdi/>"),
  ...extra,
});

describe("enviarCfdiPorCorreo", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", fetchMock);
    vi.stubEnv("MAIL_TRANSPORT", "resend");
    vi.stubEnv("RESEND_API_KEY", "test-resend-key");
    vi.stubEnv("MAIL_FROM", "cfdi@example.test");
    fetchMock.mockReset();
    nodemailer.createTransport.mockReset();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.unstubAllEnvs();
  });

  it("envía ambos archivos adjuntos al correo fiscal usando Resend", async () => {
    fetchMock.mockResolvedValue({ ok: true, status: 200 });

    await expect(enviarCfdiPorCorreo(cfdi({ nombre: "Ana" }))).resolves.toBeUndefined();

    expect(fetchMock).toHaveBeenCalledOnce();
    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe("https://api.resend.com/emails");
    expect(options.headers.Authorization).toBe("Bearer test-resend-key");
    const cuerpo = JSON.parse(options.body);
    expect(cuerpo.to).toEqual(["donante@example.test"]);
    expect(cuerpo.text).toContain("Ana");
    expect(cuerpo.attachments).toEqual([
      { filename: `CFDI-${UUID}.pdf`, content: Buffer.from("%PDF-1.7").toString("base64") },
      { filename: `CFDI-${UUID}.xml`, content: Buffer.from("<cfdi/>").toString("base64") },
    ]);
  });

  it("conserva un MAIL_FROM que ya incluye nombre", async () => {
    vi.stubEnv("MAIL_FROM", "Qariño CFDI <cfdi@example.test>");
    fetchMock.mockResolvedValue({ ok: true, status: 200 });

    await enviarCfdiPorCorreo(cfdi());

    expect(JSON.parse(fetchMock.mock.calls[0][1].body).from).toBe(
      "Qariño CFDI <cfdi@example.test>",
    );
  });

  it("envía PDF y XML con Gmail usando el transporte compartido", async () => {
    vi.stubEnv("MAIL_TRANSPORT", "gmail");
    vi.stubEnv("SMTP_USER", "proyecto@gmail.com");
    vi.stubEnv("SMTP_PASS", "abcd efgh ijkl mnop");
    const sendMail = vi.fn().mockResolvedValue({ messageId: "cfdi" });
    nodemailer.createTransport.mockReturnValueOnce({ sendMail });

    await expect(enviarCfdiPorCorreo(cfdi())).resolves.toBeUndefined();

    expect(fetchMock).not.toHaveBeenCalled();
    expect(nodemailer.createTransport).toHaveBeenCalledWith(
      expect.objectContaining({
        service: "gmail",
        auth: { user: "proyecto@gmail.com", pass: "abcdefghijklmnop" },
      }),
    );
    expect(sendMail).toHaveBeenCalledWith(
      expect.objectContaining({
        from: "Qariño Animal <proyecto@gmail.com>",
        to: ["donante@example.test"],
        attachments: [
          { filename: `CFDI-${UUID}.pdf`, content: Buffer.from("%PDF-1.7") },
          { filename: `CFDI-${UUID}.xml`, content: Buffer.from("<cfdi/>") },
        ],
      }),
    );
  });

  it("falla explícitamente cuando faltan variables de configuración", async () => {
    vi.stubEnv("RESEND_API_KEY", "");

    await expect(enviarCfdiPorCorreo(cfdi())).rejects.toMatchObject({
      message: "Falta configurar la clave de Resend.",
      code: ERROR_CODES.EMAIL_UNAVAILABLE,
    });
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("rechaza un proveedor desconocido sin intentar enviar", async () => {
    vi.stubEnv("MAIL_TRANSPORT", "otro");

    await expect(enviarCfdiPorCorreo(cfdi())).rejects.toThrow("Proveedor de correo inválido.");
    expect(fetchMock).not.toHaveBeenCalled();
    expect(nodemailer.createTransport).not.toHaveBeenCalled();
  });

  it("reporta error cuando Resend rechaza el envío", async () => {
    fetchMock.mockResolvedValue({ ok: false, status: 422 });

    await expect(enviarCfdiPorCorreo(cfdi())).rejects.toMatchObject({
      code: ERROR_CODES.EMAIL_UNAVAILABLE,
      proveedorStatus: 422,
    });
  });
});
