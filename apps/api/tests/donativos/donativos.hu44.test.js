import { describe, it, expect, vi, beforeEach } from "vite-plus/test";

vi.mock("../../src/modules/donativos/http/modelosDonativos/donativo.model.js", () => ({
  default: {
    getPendientes: vi.fn(),
    aprobarPendiente: vi.fn(),
  },
}));

import DonativoModel from "../../src/modules/donativos/http/modelosDonativos/donativo.model.js";
import {
  aprobarDonativoPendiente,
  obtenerDonativosPendientes,
} from "../../src/modules/donativos/http/adminDonativos.controller.js";

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

  it("aprueba un donativo pendiente y devuelve su nuevo estado", async () => {
    const donativo = {
      id_donativo: "b2e87183-0b85-4840-a5e7-5eae19dce391",
      estado: "Validado",
    };
    DonativoModel.aprobarPendiente.mockResolvedValue(donativo);

    const req = { params: { idDonativo: donativo.id_donativo } };
    const res = {
      status: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnThis(),
    };

    await aprobarDonativoPendiente(req, res);

    expect(DonativoModel.aprobarPendiente).toHaveBeenCalledWith(donativo.id_donativo);
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith(donativo);
  });

  it("rechaza IDs inválidos sin llamar al modelo", async () => {
    const req = { params: { idDonativo: "no-es-uuid" } };
    const res = {
      status: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnThis(),
    };

    await aprobarDonativoPendiente(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(DonativoModel.aprobarPendiente).not.toHaveBeenCalled();
  });

  it("informa si el donativo ya no está pendiente", async () => {
    DonativoModel.aprobarPendiente.mockResolvedValue(null);
    const req = { params: { idDonativo: "b2e87183-0b85-4840-a5e7-5eae19dce391" } };
    const res = {
      status: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnThis(),
    };

    await aprobarDonativoPendiente(req, res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith({
      error: "El donativo ya no está pendiente o no existe.",
    });
  });
});
