import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { enviarCfdiPorCorreo } from "../../src/modules/donativos/http/serviciosDonativos/cfdiMail.service.js";

const fetchMock = vi.fn();
const API_KEY_ORIGINAL = process.env.RESEND_API_KEY;
const MAIL_FROM_ORIGINAL = process.env.MAIL_FROM;

describe("enviarCfdiPorCorreo", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", fetchMock);
    process.env.RESEND_API_KEY = "test-resend-key";
    process.env.MAIL_FROM = "cfdi@example.test";
    fetchMock.mockReset();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    if (API_KEY_ORIGINAL === undefined) delete process.env.RESEND_API_KEY;
    else process.env.RESEND_API_KEY = API_KEY_ORIGINAL;
    if (MAIL_FROM_ORIGINAL === undefined) delete process.env.MAIL_FROM;
    else process.env.MAIL_FROM = MAIL_FROM_ORIGINAL;
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
