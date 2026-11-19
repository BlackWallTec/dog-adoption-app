import { describe, expect, it, vi } from "vite-plus/test";
import { createPerfilesApi } from "./perfiles.api.js";

const respuesta = (status, body) => ({
  ok: status >= 200 && status < 300,
  status,
  json: async () => body,
});

describe("Contrato web de correo y sesión", () => {
  it("conserva el contrato de login y normaliza el correo", async () => {
    const fetchImpl = vi
      .fn()
      .mockResolvedValue(respuesta(200, { user: { uuid: "usuario-1", rol: "Administrador" } }));
    const api = createPerfilesApi({ fetchImpl });
    expect(
      (await api.login({ correo: " DEMO@GMAIL.COM ", contrasena: "Prueba1234" })).user.uuid,
    ).toBe("usuario-1");
    const [url, opciones] = fetchImpl.mock.calls[0];
    expect(url).toBe("/api/perfiles/login");
    expect(opciones.credentials).toBe("include");
    expect(JSON.parse(opciones.body).correo).toBe("demo@gmail.com");
  });

  it("permite mostrar el reenvío cuando el backend exige confirmar la cuenta", async () => {
    const api = createPerfilesApi({
      fetchImpl: vi.fn().mockResolvedValue(
        respuesta(403, {
          code: "EMAIL_NOT_CONFIRMED",
          error: "Confirma tu correo antes de iniciar sesión.",
        }),
      ),
    });
    await expect(
      api.login({ correo: "demo@gmail.com", contrasena: "Prueba1234" }),
    ).rejects.toMatchObject({
      apiCode: "EMAIL_NOT_CONFIRMED",
      message: "Confirma tu correo antes de iniciar sesión.",
    });
  });

  it("envía tokens y contraseñas por el cuerpo POST, nunca por la URL", async () => {
    const fetchImpl = vi.fn().mockResolvedValue(respuesta(200, { ok: true }));
    const api = createPerfilesApi({ fetchImpl });
    await api.confirmarCorreo("token-de-prueba");
    await api.restablecerContrasena("token-de-prueba", "Nueva1234", "Nueva1234");
    expect(fetchImpl.mock.calls[0][0]).toBe("/api/perfiles/correo/confirmar");
    const [url, opciones] = fetchImpl.mock.calls[1];
    expect(url).toBe("/api/perfiles/correo/restablecer");
    expect(opciones.method).toBe("POST");
    expect(JSON.parse(opciones.body)).toEqual({
      token: "token-de-prueba",
      contrasena: "Nueva1234",
      confirmContrasena: "Nueva1234",
    });
  });

  it("preserva el estado pendiente del registro y la respuesta genérica de recuperación", async () => {
    const fetchImpl = vi
      .fn()
      .mockResolvedValueOnce(
        respuesta(201, {
          user: { uuid: "usuario-1", rol: "Administrador" },
          requiereConfirmacion: true,
        }),
      )
      .mockResolvedValueOnce(respuesta(202, { message: "Si corresponde recibirás un enlace." }));
    const api = createPerfilesApi({ fetchImpl });
    expect((await api.registerAdmin({ correo: "demo@gmail.com" })).requiereConfirmacion).toBe(true);
    expect(fetchImpl.mock.calls[0][0]).toBe("/api/perfiles/register-admin");
    expect(await api.solicitarCorreo("demo@gmail.com", true)).toEqual({
      message: "Si corresponde recibirás un enlace.",
    });
  });

  it("muestra errores de enlace vencido y configuración sin confundirlos con el login", async () => {
    const fetchImpl = vi
      .fn()
      .mockResolvedValueOnce(
        respuesta(400, { code: "INVALID_EMAIL_TOKEN", error: "El enlace venció. Solicita otro." }),
      )
      .mockResolvedValueOnce(
        respuesta(503, {
          code: "EMAIL_UNAVAILABLE",
          error: "El correo todavía no está configurado.",
        }),
      );
    const api = createPerfilesApi({ fetchImpl });
    await expect(api.confirmarCorreo("vencido")).rejects.toMatchObject({
      apiCode: "INVALID_EMAIL_TOKEN",
      message: "El enlace venció. Solicita otro.",
    });
    await expect(api.solicitarCorreo("demo@gmail.com")).rejects.toMatchObject({
      message: "El correo todavía no está configurado.",
    });
  });

  it("una falla de red o una sesión incompleta no se presenta como acceso válido", async () => {
    const fetchImpl = vi
      .fn()
      .mockRejectedValueOnce(new TypeError("Sin conexión"))
      .mockResolvedValueOnce(respuesta(200, { user: { id: "contrato-antiguo", rol: "Cliente" } }));
    const api = createPerfilesApi({ fetchImpl });
    await expect(api.solicitarCorreo("demo@gmail.com")).rejects.toThrow("No pudimos conectar");
    await expect(api.getSession()).rejects.toThrow("No pudimos comprobar la sesión");
  });

  it("consulta la sesión por GET sin exigir correo en la respuesta", async () => {
    const fetchImpl = vi
      .fn()
      .mockResolvedValue(respuesta(200, { user: { uuid: "root", rol: "Root" } }));
    const api = createPerfilesApi({ fetchImpl });
    expect((await api.getSession()).user.uuid).toBe("root");
    expect(fetchImpl.mock.calls[0][1]).toMatchObject({ method: "GET", credentials: "include" });
    expect(fetchImpl.mock.calls[0][1].body).toBeUndefined();
  });
});
