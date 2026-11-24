/*
 * CONTROLLER TESTS: PATCH /api/voluntarios/programas/:idPrograma/inscripciones  (HU37)
 *
 * The controller `cancelarInscripcion` makes five decisions, top to bottom:
 *   D1  Is the id a valid UUID?            no  -> 404 PROGRAMA_NO_ENCONTRADO
 *   D2  Does the program exist?            no  -> 404 PROGRAMA_NO_ENCONTRADO
 *   D3  Did the program already start?     yes -> 409 PROGRAMA_INICIADO
 *   D4  Was an active enrollment updated?  no  -> 409 NO_INSCRITO
 *   D5  Did anything throw?                yes -> 500 ERROR_INTERNO
 *   otherwise                                  -> 200 { sucess: true, data }
 *
 * Strategies (the words in the test names):
 *   "nulos"      void values: null, undefined, "", {}  (what systems send when something fails)
 *   "rangos"     a whole family of values instead of one example
 *   "limite"     boundary values: one second before, exactly at, one second after the edge
 *   "estructural" decision coverage: every `if` goes both ways, every exit is reached
 *
 * No database is used: the model is replaced by mocks ("pretend the DB answered X"), and the
 * clock is frozen so the tests give the same result today and in ten years.
 *
 * Run (from apps/api):  npx vp test run tests/voluntarios/cancelarInscripcion.controller.stress.test.js
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";

// Replaces the real model with fakes BEFORE the controller imports it (vitest hoists this).
vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js", () => ({
  getProgramaInscripcionModel: vi.fn(),
  cancelarInscripcionModel: vi.fn(),
  inscribirEnProgramaModel: vi.fn(),
}));

import { cancelarInscripcion } from "../../src/modules/voluntarios/http/voluntarios.controller.js";
import {
  cancelarInscripcionModel,
  getProgramaInscripcionModel,
} from "../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js";

const ID_PROGRAMA = "11111111-2222-3333-4444-555555555555";
const ID_USUARIO = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

// What the DB returns after cancelling: the row is NOT deleted, its estado becomes "Pendiente".
const FILA_CANCELADA = {
  id_programa: ID_PROGRAMA,
  fecha_inscripcion: "2026-11-05T12:00:00",
  estado: "Pendiente",
};

// The controller compares Mexico City time strings. Mexico City is UTC-6 all year.
const fijarAhoraMexico = (horaLocal) => vi.setSystemTime(new Date(`${horaLocal}-06:00`));

// Runs the controller with a fake Express `res` and returns what it answered.
// (`arguments.length` lets a test pass `undefined` on purpose; a default value would replace it.)
const ejecutar = async (...args) => {
  const idPrograma = args.length === 0 ? ID_PROGRAMA : args[0];
  const user = { uuid: ID_USUARIO };
  const res = {};
  res.status = vi.fn(() => res); // returns res so `res.status(409).json(...)` chains
  res.json = vi.fn();
  await cancelarInscripcion({ params: { idPrograma }, user }, res);
  return { status: res.status.mock.calls[0][0], cuerpo: res.json.mock.calls[0][0] };
};

const programaQueInicia = (fechaInicio) => ({
  id_programa: ID_PROGRAMA,
  fecha_inicio: fechaInicio,
  cupo_maximo: 10,
  estado_del_programa: "Disponible",
});

// Every error has the shape { sucess: false, code, message } ("sucess" is misspelled in the real API).
const esperarError = (respuesta, status, code) => {
  expect(respuesta.status).toBe(status);
  expect(respuesta.cuerpo.sucess).toBe(false);
  expect(respuesta.cuerpo.code).toBe(code);
};

beforeEach(() => {
  vi.clearAllMocks();
  vi.useFakeTimers({ toFake: ["Date"] }); // freeze only Date
  fijarAhoraMexico("2026-11-23T13:30:00");
  // Happy path by default: the program exists, starts in the future, the user is enrolled.
  getProgramaInscripcionModel.mockResolvedValue(programaQueInicia("2026-12-01T10:00:00"));
  cancelarInscripcionModel.mockResolvedValue(FILA_CANCELADA);
  vi.spyOn(console, "error").mockImplementation(() => {});
});

afterEach(() => {
  vi.useRealTimers();
  vi.restoreAllMocks();
});

describe("camino feliz", () => {
  it("responde 200 con la fila en Pendiente y usa el usuario del token", async () => {
    const respuesta = await ejecutar();

    expect(respuesta.status).toBe(200);
    expect(respuesta.cuerpo).toEqual({ sucess: true, data: FILA_CANCELADA });
    expect(cancelarInscripcionModel).toHaveBeenCalledExactlyOnceWith(ID_PROGRAMA, ID_USUARIO);
  });
});

describe("D1 - el id debe ser UUID (rangos y nulos)", () => {
  it.each([
    ["undefined", undefined],
    ["null", null],
    ["cadena vacia", ""],
    ["solo espacios", "   "],
    ["numero", "12345"],
    ["un caracter de mas", `${ID_PROGRAMA}0`],
    ["un caracter de menos", ID_PROGRAMA.slice(0, -1)],
    ["caracter no hexadecimal", ID_PROGRAMA.replace("1", "g")],
    ["con espacios alrededor", ` ${ID_PROGRAMA} `],
    ["inyeccion SQL", "1' OR '1'='1"],
  ])("rangos: id invalido (%s) -> 404 sin tocar la BD", async (_nombre, id) => {
    const respuesta = await ejecutar(id);

    esperarError(respuesta, 404, "PROGRAMA_NO_ENCONTRADO");
    expect(getProgramaInscripcionModel).not.toHaveBeenCalled();
    expect(cancelarInscripcionModel).not.toHaveBeenCalled();
  });

  it("rangos: acepta mayusculas y minusculas", async () => {
    expect((await ejecutar(ID_PROGRAMA.toUpperCase())).status).toBe(200);
    expect((await ejecutar(ID_PROGRAMA.toLowerCase())).status).toBe(200);
  });
});

describe("D2 - el programa debe existir", () => {
  it.each([
    ["null", null],
    ["undefined", undefined],
  ])("nulos: la BD regresa %s -> 404 y no intenta cancelar", async (_nombre, valor) => {
    getProgramaInscripcionModel.mockResolvedValue(valor);

    esperarError(await ejecutar(), 404, "PROGRAMA_NO_ENCONTRADO");
    expect(cancelarInscripcionModel).not.toHaveBeenCalled();
  });
});

describe("D3 - el programa no debe haber iniciado (limites)", () => {
  // Now is 2026-11-23 13:30:00. The rule is "start <= now -> blocked".
  it.each([
    ["1 segundo ANTES de iniciar", "2026-11-23T13:30:01", 200],
    ["JUSTO a la hora de inicio", "2026-11-23T13:30:00", 409],
    ["1 segundo DESPUES de iniciar", "2026-11-23T13:29:59", 409],
    ["inicio mañana a la misma hora", "2026-11-24T13:30:00", 200],
    ["inicio hace un año", "2025-11-23T13:30:00", 409],
    ["inicio dentro de 100 años", "2126-11-23T13:30:00", 200],
  ])("limite: %s -> %i", async (_nombre, fechaInicio, statusEsperado) => {
    getProgramaInscripcionModel.mockResolvedValue(programaQueInicia(fechaInicio));

    const respuesta = await ejecutar();

    expect(respuesta.status).toBe(statusEsperado);
    if (statusEsperado === 409) {
      esperarError(respuesta, 409, "PROGRAMA_INICIADO");
      expect(cancelarInscripcionModel).not.toHaveBeenCalled();
    }
  });

  it("limite: usa la hora de Mexico (UTC-6), no la UTC", async () => {
    // 23:30 in Mexico on Nov 23 = 05:30 UTC on Nov 24. A program starting at 23:45 Mexico is still future.
    fijarAhoraMexico("2026-11-23T23:30:00");
    getProgramaInscripcionModel.mockResolvedValue(programaQueInicia("2026-11-23T23:45:00"));

    expect((await ejecutar()).status).toBe(200);
  });

  it("limite: cruce de año y dia bisiesto", async () => {
    fijarAhoraMexico("2026-12-31T23:59:59");
    getProgramaInscripcionModel.mockResolvedValue(programaQueInicia("2027-01-01T00:00:00"));
    expect((await ejecutar()).status).toBe(200);

    fijarAhoraMexico("2028-02-29T12:00:00");
    getProgramaInscripcionModel.mockResolvedValue(programaQueInicia("2028-02-29T11:59:59"));
    expect((await ejecutar()).status).toBe(409);
  });
});

describe("D4 - debe existir una inscripcion activa", () => {
  it.each([
    ["null", null],
    ["undefined", undefined],
  ])("nulos: el UPDATE no toco ninguna fila (%s) -> 409 NO_INSCRITO", async (_nombre, valor) => {
    cancelarInscripcionModel.mockResolvedValue(valor);

    esperarError(await ejecutar(), 409, "NO_INSCRITO");
  });

  it("estructural: cancelar dos veces es idempotente (la segunda da NO_INSCRITO, no 500)", async () => {
    cancelarInscripcionModel.mockResolvedValueOnce(FILA_CANCELADA).mockResolvedValueOnce(null);

    expect((await ejecutar()).status).toBe(200);
    esperarError(await ejecutar(), 409, "NO_INSCRITO");
  });
});

describe("D5 - errores inesperados -> 500", () => {
  it("estructural: falla al consultar el programa", async () => {
    getProgramaInscripcionModel.mockRejectedValue(new Error("BD caida"));

    esperarError(await ejecutar(), 500, "ERROR_INTERNO");
    expect(cancelarInscripcionModel).not.toHaveBeenCalled();
  });

  it("estructural: falla al cancelar, sin filtrar el mensaje interno al cliente", async () => {
    cancelarInscripcionModel.mockRejectedValue(new Error("password=secreto"));

    const respuesta = await ejecutar();

    esperarError(respuesta, 500, "ERROR_INTERNO");
    expect(JSON.stringify(respuesta.cuerpo)).not.toContain("secreto");
  });

  it("nulos: rechazos que no son Error (string, undefined, objeto) tambien dan 500", async () => {
    for (const rechazo of ["texto", undefined, { code: "23503" }]) {
      getProgramaInscripcionModel.mockRejectedValueOnce(rechazo);

      esperarError(await ejecutar(), 500, "ERROR_INTERNO");
    }
  });
});

describe("seguridad", () => {
  it("el usuario sale SOLO del token, nunca del body ni de los params", async () => {
    const res = { status: vi.fn(() => res), json: vi.fn() };

    await cancelarInscripcion(
      {
        params: { idPrograma: ID_PROGRAMA, idUsuario: "otro" },
        body: { idUsuario: "otro" },
        user: { uuid: ID_USUARIO },
      },
      res,
    );

    expect(cancelarInscripcionModel).toHaveBeenCalledExactlyOnceWith(ID_PROGRAMA, ID_USUARIO);
  });
});
