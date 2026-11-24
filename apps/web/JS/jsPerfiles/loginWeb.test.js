import { describe, expect, it, vi } from "vite-plus/test";
import { createPerfilesApi } from "./perfiles.api.js";

/**
 * FRONTEND
 * En conjunto, se verifica que el login envía la petición
 * correctamente cuando tiene éxito y que transforma una respuesta
 * 401 en un error con los datos esperados.
 */

describe("Login Web", () => {
  it("autentica correctamente y conserva el mecanismo de sesión por cookie", async () => {
    const respuesta = {
      user: {
        uuid: "usuario-test-001",
        rol: "Root",
      },
    };

    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(respuesta),
    });

    const api = createPerfilesApi({ fetchImpl: fetchMock });

    const resultado = await api.login({
      correo: " Admin@QARINO.COM ",
      contrasena: "Password123!",
    });

    // Sólo una llamada debe tomar el autenticarses
    expect(fetchMock).toHaveBeenCalledTimes(1);

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/perfiles/login",
      expect.objectContaining({
        method: "POST",
        // incluye la cookie
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          // convierte credenciales a JSON.
          correo: "admin@qarino.com",
          contrasena: "Password123!",
        }),
      }),
    );

    // Assert
    expect(resultado).toEqual(respuesta);
  });

  // Login debe poder rechazar una promesa

  it("rechaza credenciales incorrectas con el error correspondiente", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      json: vi.fn().mockResolvedValue({
        error: "Credenciales inválidas",
        code: "INVALID_CREDENTIALS",
      }),
    });

    const api = createPerfilesApi({ fetchImpl: fetchMock });

    await expect(
      api.login({
        correo: "usuario@qarino.com",
        contrasena: "Incorrecta123!",
      }),
    ).rejects.toMatchObject({
      name: "INVALID_CREDENTIALS",
      code: "INVALID_CREDENTIALS",
      message: "Correo o contraseña incorrectos.",
    });
  });
});
