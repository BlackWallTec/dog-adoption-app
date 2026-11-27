import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";

const { crearTransporteGmail, enviarCorreoGmail } = vi.hoisted(() => ({
  crearTransporteGmail: vi.fn(),
  enviarCorreoGmail: vi.fn(),
}));

vi.mock("nodemailer", () => ({
  default: { createTransport: crearTransporteGmail },
}));

import { enviarCfdiPorCorreo } from "../../src/modules/donativos/http/serviciosDonativos/cfdiMail.service.js";

const fetchMock = vi.fn();
const ENV_ORIGINAL = Object.fromEntries(
  ["RESEND_API_KEY", "MAIL_FROM", "MAIL_TRANSPORT", "SMTP_USER", "SMTP_PASS"].map((name) => [
    name,
    process.env[name],
  ]),
);

describe("enviarCfdiPorCorreo", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", fetchMock);
    process.env.MAIL_TRANSPORT = "resend";
    process.env.RESEND_API_KEY = "test-resend-key";
    process.env.MAIL_FROM = "cfdi@example.test";
    fetchMock.mockReset();
    crearTransporteGmail.mockReset();
    enviarCorreoGmail.mockReset().mockResolvedValue({ messageId: "test-message" });
    crearTransporteGmail.mockReturnValue({ sendMail: enviarCorreoGmail });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    for (const [name, value] of Object.entries(ENV_ORIGINAL)) {
      if (value === undefined) delete process.env[name];
      else process.env[name] = value;
    }
  });

  it("envía ambos archivos adjuntos al correo fiscal usando Resend", async () => {
    fetchMock.mockResolvedValue({ ok: true, status: 200 });

    await expect(
      enviarCfdiPorCorreo({
        destinatario: "donante@example.test",
        nombre: "Ana",
        uuid: "e9bf9f1a-1445-4de3-a372-47fa82fcbd0c",
        pdf: Buffer.from("%PDF-1.7"),
        xml: Buffer.from("<cfdi/>"),
      }),
    ).resolves.toBeUndefined();

    expect(fetchMock).toHaveBeenCalledOnce();
    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe("https://api.resend.com/emails");
    expect(options.headers.Authorization).toBe("Bearer test-resend-key");
    expect(JSON.parse(options.body).attachments).toEqual([
      {
        filename: "CFDI-e9bf9f1a-1445-4de3-a372-47fa82fcbd0c.pdf",
        content: Buffer.from("%PDF-1.7").toString("base64"),
      },
      {
        filename: "CFDI-e9bf9f1a-1445-4de3-a372-47fa82fcbd0c.xml",
        content: Buffer.from("<cfdi/>").toString("base64"),
      },
    ]);
  });

  it("envía el CFDI mediante Gmail SMTP cuando MAIL_TRANSPORT es gmail", async () => {
    process.env.MAIL_TRANSPORT = "gmail";
    process.env.SMTP_USER = "facturacion@example.test";
    process.env.SMTP_PASS = "app password";

    await enviarCfdiPorCorreo({
      destinatario: "donante@example.test",
      nombre: "Ana",
      uuid: "e9bf9f1a-1445-4de3-a372-47fa82fcbd0c",
      pdf: Buffer.from("%PDF-1.7"),
      xml: Buffer.from("<cfdi/>"),
    });

    expect(crearTransporteGmail).toHaveBeenCalledWith(
      expect.objectContaining({
        service: "gmail",
        auth: { user: "facturacion@example.test", pass: "apppassword" },
      }),
    );
    expect(enviarCorreoGmail).toHaveBeenCalledWith(
      expect.objectContaining({
        from: "Qariño Animal <facturacion@example.test>",
        to: ["donante@example.test"],
        attachments: [
          {
            filename: "CFDI-e9bf9f1a-1445-4de3-a372-47fa82fcbd0c.pdf",
            content: Buffer.from("%PDF-1.7"),
          },
          {
            filename: "CFDI-e9bf9f1a-1445-4de3-a372-47fa82fcbd0c.xml",
            content: Buffer.from("<cfdi/>"),
          },
        ],
      }),
    );
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("falla explícitamente si Gmail SMTP no tiene sus credenciales", async () => {
    process.env.MAIL_TRANSPORT = "gmail";
    delete process.env.SMTP_PASS;

    await expect(
      enviarCfdiPorCorreo({
        destinatario: "donante@example.test",
        uuid: "e9bf9f1a-1445-4de3-a372-47fa82fcbd0c",
        pdf: Buffer.from("%PDF-1.7"),
        xml: Buffer.from("<cfdi/>"),
      }),
    ).rejects.toThrow("Falta configurar SMTP_USER y SMTP_PASS para enviar el CFDI con Gmail.");
    expect(crearTransporteGmail).not.toHaveBeenCalled();
  });

  it("falla explícitamente cuando faltan variables de configuración", async () => {
    delete process.env.RESEND_API_KEY;

    await expect(
      enviarCfdiPorCorreo({
        destinatario: "donante@example.test",
        uuid: "e9bf9f1a-1445-4de3-a372-47fa82fcbd0c",
        pdf: Buffer.from("%PDF-1.7"),
        xml: Buffer.from("<cfdi/>"),
      }),
    ).rejects.toThrow("Falta configurar el servicio de correo CFDI.");
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("reporta error cuando Resend rechaza el envío", async () => {
    fetchMock.mockResolvedValue({ ok: false, status: 422 });

    await expect(
      enviarCfdiPorCorreo({
        destinatario: "donante@example.test",
        uuid: "e9bf9f1a-1445-4de3-a372-47fa82fcbd0c",
        pdf: Buffer.from("%PDF-1.7"),
        xml: Buffer.from("<cfdi/>"),
      }),
    ).rejects.toThrow("El proveedor de correo respondió HTTP 422.");
  });
});
