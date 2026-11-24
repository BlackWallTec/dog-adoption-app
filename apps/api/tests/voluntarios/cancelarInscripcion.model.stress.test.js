/*
 * MODEL TESTS (HU37) - only what `inscripcion.model.test.js` does NOT already cover.
 *
 * The existing file already tests the happy path, the "no row -> null" case and the
 * error case of `cancelarInscripcionModel`. Here we add:
 *   1. The safety of the UPDATE: it never deletes and it uses exactly 3 filters.
 *   2. `getProgramaInscripcionModel`, which the controller uses first and has no tests yet.
 *
 * Run (from apps/api):  npx vp test run tests/voluntarios/cancelarInscripcion.model.stress.test.js
 */
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: { from: vi.fn(), rpc: vi.fn() },
  supabaseAdmin: { from: vi.fn(), rpc: vi.fn() },
}));

import { supabaseAdmin } from "../../src/config/supabaseClient.js";
import {
  cancelarInscripcionModel,
  getProgramaInscripcionModel,
} from "../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js";

// Fake Supabase query chain. `resultado` is what `maybeSingle()` finally returns.
// `delete` exists on purpose so we can prove it is never called.
const simularCadena = (resultado) => {
  const cadena = {
    select: vi.fn(() => cadena),
    update: vi.fn(() => cadena),
    delete: vi.fn(() => cadena),
    eq: vi.fn(() => cadena),
    maybeSingle: vi.fn(async () => resultado),
  };
  supabaseAdmin.from.mockReturnValue(cadena);
  return cadena;
};

const ERROR_BD = { message: "se cayó", code: "XX000" };

beforeEach(() => {
  vi.clearAllMocks();
});

describe("cancelarInscripcionModel - seguridad de la consulta", () => {
  it("estructural: nunca borra la fila, solo cambia el estado", async () => {
    const cadena = simularCadena({ data: { estado: "Pendiente" }, error: null });

    await cancelarInscripcionModel("p1", "u1");

    expect(cadena.delete).not.toHaveBeenCalled();
    expect(cadena.update).toHaveBeenCalledExactlyOnceWith({ estado: "Pendiente" });
  });

  it("estructural: usa exactamente 3 filtros (programa, usuario y estado Aceptado)", async () => {
    // Fewer filters would update other people's rows: this test fails if one is removed.
    const cadena = simularCadena({ data: null, error: null });

    await cancelarInscripcionModel("p1", "u1");

    expect(cadena.eq.mock.calls).toEqual([
      ["id_programa", "p1"],
      ["id_usuario", "u1"],
      ["estado", "Aceptado"],
    ]);
  });

  it("nulos: si hay error y tambien datos, gana el error", async () => {
    simularCadena({ data: { estado: "Pendiente" }, error: ERROR_BD });

    await expect(cancelarInscripcionModel("p1", "u1")).rejects.toMatchObject({ code: "XX000" });
  });
});

describe("getProgramaInscripcionModel", () => {
  it("regresa el programa y lo busca por su id", async () => {
    const programa = { id_programa: "p1", fecha_inicio: "2026-12-01T10:00:00" };
    const cadena = simularCadena({ data: programa, error: null });

    await expect(getProgramaInscripcionModel("p1")).resolves.toEqual(programa);

    expect(supabaseAdmin.from).toHaveBeenCalledWith("programa");
    expect(cadena.eq).toHaveBeenCalledExactlyOnceWith("id_programa", "p1");
  });

  it("nulos: un programa que no existe regresa null (el controlador responde 404)", async () => {
    simularCadena({ data: null, error: null });

    await expect(getProgramaInscripcionModel("p1")).resolves.toBeNull();
  });

  it("estructural: un error lanza una excepcion con contexto y codigo", async () => {
    simularCadena({ data: null, error: ERROR_BD });

    await expect(getProgramaInscripcionModel("p1")).rejects.toMatchObject({
      message: "Error al consultar el programa: se cayó",
      code: "XX000",
    });
  });
});
