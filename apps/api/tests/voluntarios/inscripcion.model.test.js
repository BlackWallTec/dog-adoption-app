/*
 * PRUEBAS DEL MODELO: las funciones que hablan con Supabase.
 *
 * Tienen poca lógica, pero importante: cómo reaccionan cuando Supabase responde con un
 * error, con null o con una lista vacía. No nos conectamos a Supabase de verdad.
 */
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";

vi.mock("../../src/config/supabaseClient.js", () => ({
<<<<<<< HEAD
  supabase: { from: vi.fn(), rpc: vi.fn() },
  supabaseAdmin: { from: vi.fn(), rpc: vi.fn() },
}));

import { supabase, supabaseAdmin } from "../../src/config/supabaseClient.js";
=======
  supabaseAdmin: { from: vi.fn() },
}));

import { supabaseAdmin } from "../../src/config/supabaseClient.js";
>>>>>>> 37c39bc (fix(api): revert inscripcion.model.test.js to the original develop code)
import {
  cancelarInscripcionModel,
  contarInscritosModel,
  contarInscritosPorProgramasModel,
  getIdsProgramasInscritosModel,
  inscribirEnProgramaModel,
} from "../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js";

/*
<<<<<<< HEAD
 * Supabase se usa "encadenando" llamadas:
 *     await supabase.from("tabla").select("x").eq("y", 1)
 * Este objeto imita esa cadena: cada método se regresa a sí mismo, y al final la consulta
 * entrega el `resultado` que le indicamos. También guardamos los métodos en `consulta`
 * para revisar con qué argumentos se llamaron.
=======
- Supabase se usa "encadenando" llamadas:
-     await supabaseAdmin.from("tabla").select("x").eq("y", 1).maybeSingle()
- Este objeto imita esa cadena: cada método se regresa a sí mismo, y al final la consulta
- entrega el `resultado` que le indicamos. También guardamos los métodos en `consulta`
- para revisar con qué argumentos se llamaron.
>>>>>>> 37c39bc (fix(api): revert inscripcion.model.test.js to the original develop code)
 */
const simularConsulta = (resultado) => {
  const consulta = {
    select: vi.fn(() => consulta),
    eq: vi.fn(() => consulta),
    // La cadena termina en `.eq()`: la hacemos "esperable" con await.
    then: (resolver) => resolver(resultado),
  };
  supabaseAdmin.from.mockReturnValue(consulta);
  return consulta;
};

const ERROR_BD = { message: "se cayó", code: "XX000" };

beforeEach(() => {
  vi.clearAllMocks();
});

describe("contarInscritosModel", () => {
  it("llama a contar_inscritos con el id del programa (cliente con RLS, no admin)", async () => {
    supabase.rpc.mockResolvedValue({ data: 3, error: null });

    await contarInscritosModel("p1");

<<<<<<< HEAD
    expect(supabase.rpc).toHaveBeenCalledWith("contar_inscritos", { p_id_programa: "p1" });
    expect(supabaseAdmin.rpc).not.toHaveBeenCalled();
=======
    expect(supabaseAdmin.from).toHaveBeenCalledWith("programa");
    expect(consulta.eq).toHaveBeenCalledWith("id_programa", "p1");
>>>>>>> 37c39bc (fix(api): revert inscripcion.model.test.js to the original develop code)
  });

  // Rangos y límites: el conteo más pequeño posible, uno normal y uno muy grande.
  it.each([0, 1, 10, Number.MAX_SAFE_INTEGER])("regresa el conteo %i", async (conteo) => {
    supabase.rpc.mockResolvedValue({ data: conteo, error: null });

    await expect(contarInscritosModel("p1")).resolves.toBe(conteo);
  });

  // Postgres puede regresar bigint como texto: Number() lo convierte.
  it("convierte un conteo en texto a número", async () => {
    supabase.rpc.mockResolvedValue({ data: "8", error: null });

    await expect(contarInscritosModel("p1")).resolves.toBe(8);
  });

  // `data ?? 0`: "usa data, pero si es null o undefined usa 0".
  it.each([[null], [undefined]])("nulos: un conteo %j se convierte en 0", async (data) => {
    supabase.rpc.mockResolvedValue({ data, error: null });

    await expect(contarInscritosModel("p1")).resolves.toBe(0);
  });

  it("estructural: un error lanza una excepción con contexto y código", async () => {
    supabase.rpc.mockResolvedValue({ data: null, error: ERROR_BD });

    await expect(contarInscritosModel("p1")).rejects.toMatchObject({
      message: "Error al contar inscritos: se cayó",
      code: "XX000",
    });
  });
});

describe("contarInscritosPorProgramasModel", () => {
  it("regresa un Map con el conteo de cada programa", async () => {
    const conteos = { p1: 8, p2: 0, p3: 15 };
    supabase.rpc.mockImplementation(async (_fn, { p_id_programa }) => ({
      data: conteos[p_id_programa],
      error: null,
    }));

    const resultado = await contarInscritosPorProgramasModel(["p1", "p2", "p3"]);

    expect(resultado).toBeInstanceOf(Map);
    expect([...resultado]).toEqual([
      ["p1", 8],
      ["p2", 0],
      ["p3", 15],
    ]);
    // Una llamada por programa
    expect(supabase.rpc).toHaveBeenCalledTimes(3);
  });

  it("vacíos: con una lista vacía regresa un Map vacío y no llama a la BD", async () => {
    const resultado = await contarInscritosPorProgramasModel([]);

    expect(resultado).toBeInstanceOf(Map);
    expect(resultado.size).toBe(0);
    expect(supabase.rpc).not.toHaveBeenCalled();
  });

  it("estructural: si un conteo falla, la función entera falla", async () => {
    supabase.rpc
      .mockResolvedValueOnce({ data: 1, error: null })
      .mockResolvedValueOnce({ data: null, error: ERROR_BD });

    await expect(contarInscritosPorProgramasModel(["p1", "p2"])).rejects.toMatchObject({
      code: "XX000",
    });
  });
});

describe("inscribirEnProgramaModel", () => {
  it("llama a inscribir_en_programa con supabase (RLS) y los dos ids", async () => {
    supabase.rpc.mockResolvedValue({ data: { codigo: "OK" }, error: null });

    await inscribirEnProgramaModel("p1", "u1");

    expect(supabase.rpc).toHaveBeenCalledWith("inscribir_en_programa", {
      p_id_programa: "p1",
      p_id_usuario: "u1",
    });
    // Nunca debe usar service_role: pasa por RLS
    expect(supabaseAdmin.rpc).not.toHaveBeenCalled();
  });

  it.each([
    {
      codigo: "OK",
      id_programa: "p1",
      fecha_inscripcion: "2026-11-05T12:00:00",
      estado: "Aceptado",
    },
    { codigo: "SIN_CUPO" },
    { codigo: "YA_INSCRITO" },
  ])("regresa data tal cual ($codigo)", async (data) => {
    supabase.rpc.mockResolvedValue({ data, error: null });

    await expect(inscribirEnProgramaModel("p1", "u1")).resolves.toEqual(data);
  });

  it("estructural: el error conserva el código, que el controlador usa para decidir", async () => {
    supabase.rpc.mockResolvedValue({ data: null, error: { message: "fk", code: "23503" } });

    await expect(inscribirEnProgramaModel("p1", "u1")).rejects.toMatchObject({
      message: "Error al inscribir en el programa: fk",
      code: "23503",
    });
  });
});

describe("getIdsProgramasInscritosModel", () => {
  it("filtra por usuario y por estado Aceptado, y regresa un Set de ids", async () => {
    const consulta = simularConsulta({
      data: [{ id_programa: "p1" }, { id_programa: "p2" }],
      error: null,
    });

    const resultado = await getIdsProgramasInscritosModel("u1");

    expect(supabase.from).toHaveBeenCalledWith("se_inscribe");
    expect(consulta.eq).toHaveBeenCalledWith("id_usuario", "u1");
    expect(consulta.eq).toHaveBeenCalledWith("estado", "Aceptado");
    expect(resultado).toEqual(new Set(["p1", "p2"]));
  });

  it("vacíos: sin inscripciones regresa un Set vacío", async () => {
    simularConsulta({ data: [], error: null });

    await expect(getIdsProgramasInscritosModel("u1")).resolves.toEqual(new Set());
  });

  it("estructural: un error lanza una excepción con código", async () => {
    simularConsulta({ data: null, error: ERROR_BD });

    await expect(getIdsProgramasInscritosModel("u1")).rejects.toMatchObject({
      message: "Error al consultar las inscripciones del usuario: se cayó",
      code: "XX000",
    });
  });
});

// HU37: cancelar no borra la fila, la pasa a "Pendiente". inscribir_en_programa la reactiva después.
describe("cancelarInscripcionModel", () => {
  const simularUpdate = (resultado) => {
    const consulta = {
      update: vi.fn(() => consulta),
      eq: vi.fn(() => consulta),
      select: vi.fn(() => consulta),
      maybeSingle: vi.fn(async () => resultado),
    };
    supabase.from.mockReturnValue(consulta);
    return consulta;
  };

  it("cambia el estado a Pendiente solo de la inscripción activa de ese usuario y programa", async () => {
    const fila = {
      id_programa: "p1",
      fecha_inscripcion: "2026-11-05T12:00:00",
      estado: "Pendiente",
    };
    const consulta = simularUpdate({ data: fila, error: null });

    await expect(cancelarInscripcionModel("p1", "u1")).resolves.toEqual(fila);

    expect(supabase.from).toHaveBeenCalledWith("se_inscribe");
    expect(consulta.update).toHaveBeenCalledWith({ estado: "Pendiente" });
    expect(consulta.eq).toHaveBeenCalledWith("id_programa", "p1");
    expect(consulta.eq).toHaveBeenCalledWith("id_usuario", "u1");
    expect(consulta.eq).toHaveBeenCalledWith("estado", "Aceptado");
  });

  it("nulos: sin inscripción activa regresa null", async () => {
    simularUpdate({ data: null, error: null });

    await expect(cancelarInscripcionModel("p1", "u1")).resolves.toBeNull();
  });

  it("estructural: un error lanza una excepción con contexto y código", async () => {
    simularUpdate({ data: null, error: ERROR_BD });

    await expect(cancelarInscripcionModel("p1", "u1")).rejects.toMatchObject({
      message: "Error al cancelar la inscripción: se cayó",
      code: "XX000",
    });
  });
});
