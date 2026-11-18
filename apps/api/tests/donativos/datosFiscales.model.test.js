import { beforeEach, describe, expect, it, vi } from "vite-plus/test";

const { storageFromMock, crearUrlFirmadaMock } = vi.hoisted(() => ({
  storageFromMock: vi.fn(),
  crearUrlFirmadaMock: vi.fn(),
}));

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabaseAdmin: {
    storage: { from: storageFromMock },
  },
}));

import DatosFiscalesModel from "../../src/modules/donativos/http/modelosDonativos/datosFiscales.model.js";

describe("DatosFiscalesModel.crearUrlFirmadaConstancia", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    storageFromMock.mockReturnValue({ createSignedUrl: crearUrlFirmadaMock });
  });

  it("genera una URL temporal en el bucket privado de constancias", async () => {
    crearUrlFirmadaMock.mockResolvedValue({
      data: { signedUrl: "https://storage.example/constancia" },
      error: null,
    });

    await expect(
      DatosFiscalesModel.crearUrlFirmadaConstancia(
        "00000000-0000-4000-8000-000000000001",
        "00000000-0000-4000-8000-000000000001/constancia.pdf",
      ),
    ).resolves.toBe("https://storage.example/constancia");

    expect(storageFromMock).toHaveBeenCalledWith("constancias-fiscales");
    expect(crearUrlFirmadaMock).toHaveBeenCalledWith(
      "00000000-0000-4000-8000-000000000001/constancia.pdf",
      300,
    );
  });

  it("rechaza rutas de constancia que no pertenecen al donante", async () => {
    await expect(
      DatosFiscalesModel.crearUrlFirmadaConstancia(
        "00000000-0000-4000-8000-000000000001",
        "00000000-0000-4000-8000-000000000002/constancia.pdf",
      ),
    ).rejects.toThrow("La ruta de la constancia fiscal no es válida.");

    expect(storageFromMock).not.toHaveBeenCalled();
  });
});
