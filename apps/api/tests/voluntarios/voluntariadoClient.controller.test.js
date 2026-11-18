/*
 * PRUEBAS DEL CONTROLADOR: GET /cliente/programas (getProgramasVigentes)
 *
 * Cada tarjeta del catálogo muestra los lugares disponibles en tiempo real. El controlador
 * agrega a cada programa:
 *   - cupo_disponible: null si no hay límite; si no, max(cupo_maximo - ocupados, 0).
 *   - usuario_inscrito: si el usuario del token (req.user) ya está inscrito (estado "Aceptado").
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";

vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/program.model.js", () => ({
  default: { getCurrentPrograms: vi.fn() },
}));

vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js", () => ({
  getIdsProgramasInscritosModel: vi.fn(),
  contarInscritosPorProgramasModel: vi.fn(),
}));

import { getProgramasVigentes } from "../../src/modules/voluntarios/http/voluntariadoClient.controller.js";
import programModel from "../../src/modules/voluntarios/http/modelosVoluntariado/program.model.js";
import {
  contarInscritosPorProgramasModel,
  getIdsProgramasInscritosModel,
} from "../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js";

const ID_USUARIO = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

const programa = (id, cupo_maximo) => ({
  id_programa: id,
  tipo: "Paseo",
  nombre: `Programa ${id}`,
  estado_del_programa: "Disponible",
  fecha_inicio: "2026-12-01T09:00:00",
  fecha_fin: "2026-12-01T12:00:00",
  cupo_maximo,
});

const ejecutar = async () => {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn();

  await getProgramasVigentes({ user: { uuid: ID_USUARIO } }, res);

  return { status: res.status.mock.calls[0][0], cuerpo: res.json.mock.calls[0][0] };
};

// Prepara el escenario: programas, ocupados por id e ids donde el usuario ya está inscrito.
const preparar = (programas, ocupados = {}, inscritos = []) => {
  programModel.getCurrentPrograms.mockResolvedValue(programas);
  contarInscritosPorProgramasModel.mockResolvedValue(new Map(Object.entries(ocupados)));
  getIdsProgramasInscritosModel.mockResolvedValue(new Set(inscritos));
};

const cupoDe = (cuerpo, id) => cuerpo.data.find((p) => p.id_programa === id).cupo_disponible;

beforeEach(() => {
  vi.clearAllMocks();
});

afterEach(() => {
  vi.restoreAllMocks();
});

describe("cupo_disponible", () => {
  it("programa sin límite da cupo_disponible null", async () => {
    preparar([programa("p1", null)]);

    const { status, cuerpo } = await ejecutar();

    expect(status).toBe(200);
    expect(cupoDe(cuerpo, "p1")).toBeNull();
  });

  it("15 lugares con 8 ocupados da 7", async () => {
    preparar([programa("p1", 15)], { p1: 8 });

    expect(cupoDe((await ejecutar()).cuerpo, "p1")).toBe(7);
  });

  it("lleno (10/10) da 0", async () => {
    preparar([programa("p1", 10)], { p1: 10 });

    expect(cupoDe((await ejecutar()).cuerpo, "p1")).toBe(0);
  });

  it("más ocupados que cupo da 0, nunca negativo", async () => {
    preparar([programa("p1", 10)], { p1: 13 });

    expect(cupoDe((await ejecutar()).cuerpo, "p1")).toBe(0);
  });

  it("límites: uno antes de llenarse (9/10) da 1", async () => {
    preparar([programa("p1", 10)], { p1: 9 });

    expect(cupoDe((await ejecutar()).cuerpo, "p1")).toBe(1);
  });

  it("un programa con límite sin conteo en el Map cuenta 0 ocupados", async () => {
    preparar([programa("p1", 15)], {});

    expect(cupoDe((await ejecutar()).cuerpo, "p1")).toBe(15);
  });

  it("cupo_maximo 0 da cupo_disponible 0 (no se trata como sin límite)", async () => {
    preparar([programa("p1", 0)]);

    const { cuerpo } = await ejecutar();

    expect(cupoDe(cuerpo, "p1")).toBe(0);
    expect(contarInscritosPorProgramasModel).toHaveBeenCalledWith(["p1"]);
  });

  it("solo cuenta los programas que tienen cupo_maximo", async () => {
    preparar([
      programa("p1", 15),
      programa("p2", null),
      programa("p3", 10),
      programa("p4", undefined),
    ]);

    await ejecutar();

    expect(contarInscritosPorProgramasModel).toHaveBeenCalledWith(["p1", "p3"]);
  });

  it("conserva los campos del programa y responde { success: true, data }", async () => {
    const p = programa("p1", 15);
    preparar([p], { p1: 8 });

    const { status, cuerpo } = await ejecutar();

    expect(status).toBe(200);
    expect(cuerpo).toEqual({
      success: true,
      data: [{ ...p, cupo_disponible: 7, usuario_inscrito: false }],
    });
  });

  it("vacíos: sin programas responde una lista vacía", async () => {
    preparar([]);

    const { status, cuerpo } = await ejecutar();

    expect(status).toBe(200);
    expect(cuerpo).toEqual({ success: true, data: [] });
    expect(contarInscritosPorProgramasModel).toHaveBeenCalledWith([]);
  });
});

describe("usuario_inscrito", () => {
  it("se marca según el Set de inscripciones del usuario", async () => {
    preparar([programa("p1", 15), programa("p2", null), programa("p3", 10)], {}, ["p1", "p2"]);

    const { cuerpo } = await ejecutar();

    expect(getIdsProgramasInscritosModel).toHaveBeenCalledWith(ID_USUARIO);
    expect(cuerpo.data.map((p) => [p.id_programa, p.usuario_inscrito])).toEqual([
      ["p1", true],
      ["p2", true],
      ["p3", false],
    ]);
  });
});

describe("errores", () => {
  it.each([
    ["getCurrentPrograms", () => programModel.getCurrentPrograms],
    ["getIdsProgramasInscritosModel", () => getIdsProgramasInscritosModel],
    ["contarInscritosPorProgramasModel", () => contarInscritosPorProgramasModel],
  ])("si %s falla responde 500 con el mensaje", async (_nombre, modelo) => {
    preparar([programa("p1", 15)]);
    modelo().mockRejectedValue(new Error("base de datos caída"));

    const { status, cuerpo } = await ejecutar();

    expect(status).toBe(500);
    expect(cuerpo).toEqual({ success: false, message: "base de datos caída" });
  });

  it("un error sin mensaje usa el mensaje por defecto", async () => {
    programModel.getCurrentPrograms.mockRejectedValue(new Error(""));

    const { status, cuerpo } = await ejecutar();

    expect(status).toBe(500);
    expect(cuerpo.message).toBe("Error interno al consultar los programas.");
  });
});
