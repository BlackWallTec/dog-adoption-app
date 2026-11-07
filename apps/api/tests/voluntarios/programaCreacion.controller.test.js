import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";

// Modelos simulados: las pruebas no se conectan a Supabase.
vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/programaCreacion.model.js", () => ({
  insertarPrograma: vi.fn(),
  obtenerEmpalmes: vi.fn(),
}));

vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/TipoPrograma.model.js", () => ({
  obtenerTipos: vi.fn(),
  registrarTipo: vi.fn(),
}));

import * as ProgramaModel from "../../src/modules/voluntarios/http/modelosVoluntariado/programaCreacion.model.js";
import * as TipoProgramaModel from "../../src/modules/voluntarios/http/modelosVoluntariado/TipoPrograma.model.js";

import {
  crearPrograma,
  obtenerTipos,
  registrarTipo,
  consultarEmpalmes,
} from "../../src/modules/voluntarios/http/programaCreacion.controller.js";

const id_usuario = "11111111-1111-4111-8111-111111111111";
const ID_PROGRAMA = "22222222-2222-4222-8222-222222222222";

function crearDatosValidos(cambios = {}) {
  const datos = {
    nombre: "Jornada prueba",
    tipo: "Limpieza",
    descripcion: "Apoyo en limpieza del refugio",
    ubicacion: "Qariño Animal",
    fechaInicio: "2030-11-14",
    horaInicio: "09:00",
    fechaFin: "2030-11-14",
    horaFin: "12:00",
    cupoMaximo: 15,
    confirmarEmpalme: false,
  };

  for (const campo of Object.keys(cambios)) {
    datos[campo] = cambios[campo];
  }

  return datos;
}

function crearRespuesta() {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn(() => res);
  return res;
}

beforeEach(() => {
  vi.resetAllMocks();

  vi.useFakeTimers({ toFake: ["Date"] });
  vi.setSystemTime(new Date("2030-11-01 12:00:00Z"));

  vi.spyOn(console, "error").mockImplementation(() => {});

  ProgramaModel.obtenerEmpalmes.mockResolvedValue([]);
  ProgramaModel.insertarPrograma.mockResolvedValue({
    programa: {
      idPrograma: ID_PROGRAMA,
      nombre: "Jornada prueba",
      estado: "Disponible",
    },
  });

  TipoProgramaModel.obtenerTipos.mockResolvedValue([
    { nombre: "Limpieza" },
    { nombre: "Alimentación" },
  ]);

  TipoProgramaModel.registrarTipo.mockResolvedValue({
    nombre: "Limpieza",
    descripcion: "Apoyo en el refugio",
  });
});

afterEach(() => {
  vi.useRealTimers();
  vi.restoreAllMocks();
});

describe("crearPrograma (HU31)", () => {
  it("crea una jornada utilizando el administrador autenticado", async () => {
    const datos = crearDatosValidos();
    const res = crearRespuesta();

    const req = {
      body: datos,
      user: {
        uuid: id_usuario,
        rol: "Administrador",
      },
    };

    await crearPrograma(req, res);

    expect(res.status).toHaveBeenCalledWith(201);
    expect(res.json).toHaveBeenCalledWith({
      success: true,
      programa: {
        idPrograma: ID_PROGRAMA,
        nombre: "Jornada prueba",
        estado: "Disponible",
      },
    });

    expect(ProgramaModel.insertarPrograma).toHaveBeenCalledExactlyOnceWith(
      datos,
      id_usuario,
      expect.stringMatching(
        /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i,
      ),
    );
  });

  it.each([undefined, null, [], "texto"])("rechaza un cuerpo invalido: %s", async (body) => {
    const res = crearRespuesta();

    await crearPrograma({ body }, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(ProgramaModel.obtenerEmpalmes).not.toHaveBeenCalled();
    expect(ProgramaModel.insertarPrograma).not.toHaveBeenCalled();
  });

  it.each([
    ["nombre", ""],
    ["nombre", " ".repeat(3)],
    ["nombre", "a".repeat(51)],
    ["tipo", ""],
    ["tipo", "a".repeat(21)],
    ["descripcion", ""],
    ["descripcion", "a".repeat(1001)],
    ["ubicacion", " "],
    ["ubicacion", "a".repeat(101)],
  ])("rechaza el campo %s invalido", async (campo, valor) => {
    const datos = crearDatosValidos({ [campo]: valor });
    const res = crearRespuesta();

    await crearPrograma({ body: datos }, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(ProgramaModel.insertarPrograma).not.toHaveBeenCalled();
  });

  it.each([0, -5, 2.5, "15", null])("rechaza un cupo invalido: %s", async (cupoMaximo) => {
    const res = crearRespuesta();

    await crearPrograma({ body: crearDatosValidos({ cupoMaximo }) }, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(ProgramaModel.insertarPrograma).not.toHaveBeenCalled();
  });

  it("rechaza una jornada con fecha pasada", async () => {
    const res = crearRespuesta();

    await crearPrograma(
      {
        body: crearDatosValidos({
          fechaInicio: "2020-01-01",
          fechaFin: "2020-01-01",
        }),
      },
      res,
    );

    expect(res.status).toHaveBeenCalledWith(400);
    expect(ProgramaModel.insertarPrograma).not.toHaveBeenCalled();
  });

  it.each(["08:00", "09:00"])("rechaza un fin anterior o igual al inicio: %s", async (horaFin) => {
    const res = crearRespuesta();

    await crearPrograma({ body: crearDatosValidos({ horaFin }) }, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(ProgramaModel.insertarPrograma).not.toHaveBeenCalled();
  });

  it("rechaza fechas inválidas", async () => {
    const res = crearRespuesta();

    await crearPrograma({ body: crearDatosValidos({ fechaInicio: "fecha-invalida" }) }, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(ProgramaModel.insertarPrograma).not.toHaveBeenCalled();
  });

  it("solicita confirmación cuando existen empalmes", async () => {
    const empalmes = [{ idPrograma: ID_PROGRAMA, nombre: "Otra jornada" }];
    ProgramaModel.obtenerEmpalmes.mockResolvedValue(empalmes);

    const res = crearRespuesta();

    await crearPrograma({ body: crearDatosValidos() }, res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith({
      code: "OVERLAP",
      message: "Existen jornadas que coinciden con el horario.",
      empalmes,
    });
    expect(ProgramaModel.insertarPrograma).not.toHaveBeenCalled();
  });

  it("permite publicar con empalmes si se confirma explícitamente", async () => {
    ProgramaModel.obtenerEmpalmes.mockResolvedValue([
      { idPrograma: ID_PROGRAMA, nombre: "Otra jornada" },
    ]);

    const res = crearRespuesta();

    await crearPrograma(
      {
        body: crearDatosValidos({ confirmarEmpalme: true }),
        user: { uuid: id_usuario, rol: "Administrador" },
      },
      res,
    );

    expect(res.status).toHaveBeenCalledWith(201);
    expect(ProgramaModel.insertarPrograma).toHaveBeenCalledOnce();
  });

  it("responde 500 si falla la inserción", async () => {
    ProgramaModel.insertarPrograma.mockRejectedValue(new Error("Error de base de datos"));

    const res = crearRespuesta();

    await crearPrograma(
      {
        body: crearDatosValidos(),
        user: { uuid: id_usuario, rol: "Administrador" },
      },
      res,
    );

    expect(ProgramaModel.insertarPrograma).toHaveBeenCalledOnce();

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        message: "Error al guardar la jornada.",
      }),
    );
  });
});

describe("obtenerTipos", () => {
  it("devuelve los tipos disponibles", async () => {
    const res = crearRespuesta();

    await obtenerTipos({}, res);

    expect(res.json).toHaveBeenCalledWith({
      tipos: [{ nombre: "Limpieza" }, { nombre: "Alimentación" }],
    });
    expect(TipoProgramaModel.obtenerTipos).toHaveBeenCalledOnce();
  });

  it("responde 500 si falla la consulta", async () => {
    TipoProgramaModel.obtenerTipos.mockRejectedValue(new Error("Error de base de datos"));

    const res = crearRespuesta();

    await obtenerTipos({}, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({
      message: "Error al cargar los tipos.",
    });
  });
});

describe("registrarTipo", () => {
  it("registra un tipo válido", async () => {
    const body = {
      nombre: "Limpieza",
      descripcion: "Apoyo en el refugio",
    };
    const res = crearRespuesta();

    await registrarTipo({ body }, res);

    expect(res.status).toHaveBeenCalledWith(201);
    expect(TipoProgramaModel.registrarTipo).toHaveBeenCalledWith(body);
    expect(res.json).toHaveBeenCalledWith({ tipo: body });
  });

  it("permite registrar un tipo sin descripción", async () => {
    const res = crearRespuesta();

    await registrarTipo({ body: { nombre: "Limpieza" } }, res);

    expect(res.status).toHaveBeenCalledWith(201);
    expect(TipoProgramaModel.registrarTipo).toHaveBeenCalledWith({
      nombre: "Limpieza",
      descripcion: "",
    });
  });

  it.each([
    { nombre: "" },
    { nombre: " " },
    { nombre: "a".repeat(21) },
    { nombre: "Limpieza", descripcion: "a".repeat(201) },
  ])("rechaza datos inválidos: %j", async (body) => {
    const res = crearRespuesta();

    await registrarTipo({ body }, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(TipoProgramaModel.registrarTipo).not.toHaveBeenCalled();
  });

  it("responde 409 si el tipo ya existe", async () => {
    const error = new Error("Ya existe un tipo con ese nombre.");
    error.status = 409;
    TipoProgramaModel.registrarTipo.mockRejectedValue(error);

    const res = crearRespuesta();

    await registrarTipo({ body: { nombre: "Limpieza" } }, res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith({
      message: "Ya existe un tipo con ese nombre.",
    });
  });
});

describe("consultarEmpalmes", () => {
  function crearHorario() {
    return {
      fechaInicio: "2099-11-14",
      horaInicio: "09:00",
      fechaFin: "2099-11-14",
      horaFin: "12:00",
    };
  }

  it("devuelve las jornadas con horarios coincidentes", async () => {
    const query = crearHorario();
    const empalmes = [{ idPrograma: ID_PROGRAMA }];
    ProgramaModel.obtenerEmpalmes.mockResolvedValue(empalmes);

    const res = crearRespuesta();

    await consultarEmpalmes({ query }, res);

    expect(ProgramaModel.obtenerEmpalmes).toHaveBeenCalledWith(query);
    expect(res.json).toHaveBeenCalledWith({ empalmes });
  });

  it.each(["fechaInicio", "horaInicio", "fechaFin", "horaFin"])(
    "responde 400 cuando falta %s",
    async (campo) => {
      const query = crearHorario();
      delete query[campo];

      const res = crearRespuesta();

      await consultarEmpalmes({ query }, res);

      expect(res.status).toHaveBeenCalledWith(400);
      expect(ProgramaModel.obtenerEmpalmes).not.toHaveBeenCalled();
    },
  );

  it("responde 500 si falla la consulta", async () => {
    ProgramaModel.obtenerEmpalmes.mockRejectedValue(new Error("Error de base de datos"));

    const res = crearRespuesta();

    await consultarEmpalmes({ query: crearHorario() }, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({
      message: "Error al consultar empalmes",
    });
  });
});
