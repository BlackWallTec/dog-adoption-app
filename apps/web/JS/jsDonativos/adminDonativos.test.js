import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { aprobarDonativoPendiente } from "./adminDonativos.js";

const fetchMock = vi.fn();

describe("aprobarDonativoPendiente", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", fetchMock);
    fetchMock.mockReset();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("solicita la aprobación con la sesión web", async () => {
    const donativo = {
      id_donativo: "donativo-1",
      estado: "Validado",
    };
    fetchMock.mockResolvedValue({
      status: 200,
      ok: true,
      headers: { get: () => "application/json" },
      json: async () => donativo,
    });

    await expect(aprobarDonativoPendiente("donativo-1")).resolves.toEqual(donativo);

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/donativos/admin/pendientes/donativo-1/aprobar",
      expect.objectContaining({
        method: "PATCH",
        credentials: "same-origin",
      }),
    );
  });

  it("muestra el error de conflicto cuando ya no está pendiente", async () => {
    fetchMock.mockResolvedValue({
      status: 409,
      ok: false,
      headers: { get: () => "application/json" },
      json: async () => ({ error: "El donativo ya no está pendiente." }),
    });

    await expect(aprobarDonativoPendiente("donativo-1")).rejects.toThrow(
      "El donativo ya no está pendiente.",
    );
  });
});
