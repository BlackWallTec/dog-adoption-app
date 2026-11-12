import { describe, it, expect, vi, beforeEach } from "vite-plus/test";

vi.mock("../../src/modules/donativos/http/modelosDonativos/donativo.model.js", () => ({
  default: {
    getPendientes: vi.fn(),
  },
}));

import DonativoModel from "../../src/modules/donativos/http/modelosDonativos/donativo.model.js";
import { obtenerDonativosPendientes } from "../../src/modules/donativos/http/adminDonativos.controller.js";

describe("HU44 - Lista de donativos pendientes", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("devuelve los donativos pendientes con sus datos", async () => {
    const donativos = [
      {
        id_donativo: 1,
        monto: 250,
        fecha: "2026-11-01T10:00:00Z",
        estado: "Pendiente de validación",
        usuario: {
          nombre: "Donante",
          apellidos: "De Prueba",
        },
      },
    ];

    DonativoModel.getPendientes.mockResolvedValue(donativos);

    const req = {};
    const res = {
      status: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnThis(),
    };

    await obtenerDonativosPendientes(req, res);

    expect(DonativoModel.getPendientes).toHaveBeenCalledOnce();
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith(donativos);
  });

  it("devuelve una lista vacía cuando no hay pendientes", async () => {
    DonativoModel.getPendientes.mockResolvedValue([]);

    const req = {};
    const res = {
      status: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnThis(),
    };

    await obtenerDonativosPendientes(req, res);

    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith([]);
  });

  it("responde con error 500 cuando falla la consulta", async () => {
    DonativoModel.getPendientes.mockRejectedValue(new Error("Error de conexión"));

    const req = {};
    const res = {
      status: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnThis(),
    };

    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    try {
      await obtenerDonativosPendientes(req, res);

      expect(res.status).toHaveBeenCalledWith(500);
      expect(res.json).toHaveBeenCalledWith({
        error: "No fue posible consultar los donativos pendientes.",
      });
    } finally {
      consoleSpy.mockRestore();
    }
  });
});
