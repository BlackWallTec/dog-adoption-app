/*
 * PRUEBAS DEL MODELO: las funciones que hablan con Supabase.
 *
 * Tienen poca lógica, pero importante: cómo reaccionan cuando Supabase responde con un
 * error, con null o con una lista vacía. No nos conectamos a Supabase de verdad.
 */
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabaseAdmin: { from: vi.fn() },
}));

import { supabaseAdmin } from "../../src/config/supabaseClient.js";
import {
  contarInscritosModel,
  crearInscripcionModel,
  existeInscripcionModel,
  getProgramaInscripcionModel,
} from "../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js";

/*
 * Supabase se usa "encadenando" llamadas:
 *     await supabaseAdmin.from("tabla").select("x").eq("y", 1).maybeSingle()
 * Este objeto imita esa cadena: cada método se regresa a sí mismo, y al final la consulta
 * entrega el `resultado` que le indicamos. También guardamos los métodos en `consulta`
 * para revisar con qué argumentos se llamaron.
 */
const simularSupabase = (resultado) => {
  const consulta = {
    select: vi.fn(() => consulta),
    eq: vi.fn(() => consulta),
    insert: vi.fn(() => consulta),
    maybeSingle: vi.fn(async () => resultado),
    single: vi.fn(async () => resultado),
    // `contarInscritosModel` termina en `.eq()`: hacemos la cadena "esperable" con await.
    then: (resolver) => resolver(resultado),
  };
  supabaseAdmin.from.mockReturnValue(consulta);
  return consulta;
};

const ERROR_BD = { message: "se cayó", code: "XX000" };

beforeEach(() => {
  vi.clearAllMocks();
});

describe("getProgramaInscripcionModel", () => {
  it("regresa el programa y consulta el id correcto", async () => {
    const programa = { id_programa: "p1" };
    const consulta = simularSupabase({ data: programa, error: null });

    await expect(getProgramaInscripcionModel("p1")).resolves.toEqual(programa);

    expect(supabaseAdmin.from).toHaveBeenCalledWith("programa");
    expect(consulta.eq).toHaveBeenCalledWith("id_programa", "p1");
  });

  it("nulos: un programa inexistente regresa null", async () => {
    simularSupabase({ data: null, error: null });

    await expect(getProgramaInscripcionModel("p1")).resolves.toBeNull();
  });

  it("estructural: un error lanza una excepción con contexto y código", async () => {
    simularSupabase({ data: null, error: ERROR_BD });

    await expect(getProgramaInscripcionModel("p1")).rejects.toMatchObject({
      message: "Error al consultar el programa: se cayó",
      code: "XX000",
    });
  });
});

describe("contarInscritosModel", () => {
  // Rangos y límites: el conteo más pequeño posible, uno normal y uno muy grande.
  it.each([0, 1, 10, Number.MAX_SAFE_INTEGER])("regresa el conteo %i", async (count) => {
    simularSupabase({ count, error: null });

    await expect(contarInscritosModel("p1")).resolves.toBe(count);
  });

  // `count ?? 0`: "usa count, pero si es null o undefined usa 0".
  it.each([[null], [undefined]])("nulos: un conteo %j se convierte en 0", async (count) => {
    simularSupabase({ count, error: null });

    await expect(contarInscritosModel("p1")).resolves.toBe(0);
  });

  it("estructural: un error lanza una excepción", async () => {
    simularSupabase({ count: null, error: ERROR_BD });

    await expect(contarInscritosModel("p1")).rejects.toThrow("Error al contar inscritos: se cayó");
  });
});

describe("existeInscripcionModel", () => {
  it("estructural: con una fila encontrada regresa true", async () => {
    const consulta = simularSupabase({ data: { id_programa: "p1" }, error: null });

    await expect(existeInscripcionModel("p1", "u1")).resolves.toBe(true);

    // Debe filtrar por AMBOS campos: el programa y el usuario.
    expect(consulta.eq).toHaveBeenCalledWith("id_programa", "p1");
    expect(consulta.eq).toHaveBeenCalledWith("id_usuario", "u1");
  });

  it("nulos: sin fila encontrada regresa false", async () => {
    simularSupabase({ data: null, error: null });

    await expect(existeInscripcionModel("p1", "u1")).resolves.toBe(false);
  });

  it("estructural: un error lanza una excepción", async () => {
    simularSupabase({ data: null, error: ERROR_BD });

    await expect(existeInscripcionModel("p1", "u1")).rejects.toThrow(
      "Error al verificar la inscripción: se cayó",
    );
  });
});

describe("crearInscripcionModel", () => {
  it("inserta la fila con los datos correctos y la regresa", async () => {
    const fila = { id_programa: "p1", fecha_inscripcion: "2026-11-05T12:00:00", estado: "Activa" };
    const consulta = simularSupabase({ data: fila, error: null });

    await expect(crearInscripcionModel("p1", "u1", "2026-11-05T12:00:00")).resolves.toEqual(fila);

    expect(supabaseAdmin.from).toHaveBeenCalledWith("se_inscribe");
    expect(consulta.insert).toHaveBeenCalledWith({
      id_usuario: "u1",
      id_programa: "p1",
      fecha_inscripcion: "2026-11-05T12:00:00",
    });
  });

  it("estructural: el error conserva el código, que el controlador usa para decidir", async () => {
    simularSupabase({ data: null, error: { message: "duplicado", code: "23505" } });

    await expect(crearInscripcionModel("p1", "u1", "x")).rejects.toMatchObject({
      message: "Error al crear la inscripción: duplicado",
      code: "23505",
    });
  });
});
