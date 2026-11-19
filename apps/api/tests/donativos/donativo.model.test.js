import { beforeEach, describe, expect, it, vi } from "vite-plus/test";

const { supabaseAdmin, consulta } = vi.hoisted(() => {
  const consulta = {
    update: vi.fn(),
    eq: vi.fn(),
    select: vi.fn(),
    maybeSingle: vi.fn(),
  };
  const supabaseAdmin = {
    from: vi.fn(() => consulta),
  };

  consulta.update.mockReturnValue(consulta);
  consulta.eq.mockReturnValue(consulta);
  consulta.select.mockReturnValue(consulta);

  return { supabaseAdmin, consulta };
});

vi.mock("../../src/config/supabaseClient.js", () => ({ supabaseAdmin }));

import DonativoModel from "../../src/modules/donativos/http/modelosDonativos/donativo.model.js";

describe("DonativoModel.aprobarPendiente", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("cambia a Validado solo si el donativo sigue pendiente", async () => {
    const idDonativo = "b2e87183-0b85-4840-a5e7-5eae19dce391";
    const aprobado = { id_donativo: idDonativo, estado: "Validado" };
    consulta.maybeSingle.mockResolvedValue({ data: aprobado, error: null });

    await expect(DonativoModel.aprobarPendiente(idDonativo)).resolves.toEqual(aprobado);

    expect(supabaseAdmin.from).toHaveBeenCalledWith("donativo");
    expect(consulta.update).toHaveBeenCalledWith({ estado: "Validado" });
    expect(consulta.eq).toHaveBeenNthCalledWith(1, "id_donativo", idDonativo);
    expect(consulta.eq).toHaveBeenNthCalledWith(2, "estado", "Pendiente de validación");
    expect(consulta.select).toHaveBeenCalledWith("id_donativo, estado");
  });

  it("devuelve null si el donativo ya no está pendiente", async () => {
    consulta.maybeSingle.mockResolvedValue({ data: null, error: null });

    await expect(
      DonativoModel.aprobarPendiente("b2e87183-0b85-4840-a5e7-5eae19dce391"),
    ).resolves.toBeNull();
  });

  it("propaga errores de Supabase", async () => {
    consulta.maybeSingle.mockResolvedValue({
      data: null,
      error: { message: "Fallo de conexión" },
    });

    await expect(
      DonativoModel.aprobarPendiente("b2e87183-0b85-4840-a5e7-5eae19dce391"),
    ).rejects.toThrow("Error al aprobar el donativo: Fallo de conexión");
  });
});
