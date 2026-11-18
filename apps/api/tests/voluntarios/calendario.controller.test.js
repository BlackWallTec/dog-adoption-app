import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";

// Mocks compartidos (vi.hoisted evita las advertencias de unbound-method del lint).
const mocks = vi.hoisted(() => ({
  programasEnRango: vi.fn(),
  inscripcionesActivas: vi.fn(),
  ahora: vi.fn(),
}));

// Modelos simulados: las pruebas no se conectan a Supabase.
vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/calendario.model.js", () => ({
  getProgramasEnRango: mocks.programasEnRango,
  getInscripcionesActivas: mocks.inscripcionesActivas,
}));

vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/programaEdicion.model.js", () => ({
  ahoraMexico: mocks.ahora,
}));

import {
  esRangoValido,
  lunesDe,
  estadoDe,
  contarPorPrograma,
  semanasSinCobertura,
  armarCalendario,
  getCalendario,
} from "../../src/modules/voluntarios/http/calendarioAdmin.controller.js";

const ID_PROGRAMA = "22222222-2222-4222-8222-222222222222";
const OTRO_PROGRAMA = "33333333-3333-4333-8333-333333333333";
// Miércoles: la semana actual empieza el lunes 2026-11-16
const AHORA = "2026-11-18 10:00:00";

function crearFila(cambios = {}) {
  return {
    id_programa: ID_PROGRAMA,
    nombre: "Jornada de limpieza",
    tipo: "Limpieza",
    fecha_inicio: "2026-11-24T09:00:00",
    fecha_fin: "2026-11-24T13:00:00",
    ubicacion: "Refugio",
    cupo_maximo: 10,
    estado_del_programa: "Disponible",
    ...cambios,
  };
}

function crearRespuesta() {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn(() => res);
  return res;
}

beforeEach(() => {
  mocks.ahora.mockReturnValue(AHORA);
});

afterEach(() => {
  vi.clearAllMocks();
  vi.restoreAllMocks();
});

describe("esRangoValido", () => {
  it("acepta el rango de la vista de mes", () => {
    expect(esRangoValido("2026-10-26", "2026-12-06")).toBe(true);
  });

  it("rechaza fechas que no existen o con otro formato", () => {
    expect(esRangoValido("2026-02-30", "2026-03-01")).toBe(false);
    expect(esRangoValido("26-11-2026", "2026-12-01")).toBe(false);
    expect(esRangoValido(undefined, "2026-12-01")).toBe(false);
  });

  it("rechaza desde mayor que hasta y rangos de más de 62 días", () => {
    expect(esRangoValido("2026-12-01", "2026-11-01")).toBe(false);
    expect(esRangoValido("2026-01-01", "2026-06-01")).toBe(false);
  });
});

describe("lunesDe", () => {
  it("regresa el lunes de la semana", () => {
    expect(lunesDe("2026-11-18")).toBe("2026-11-16");
    expect(lunesDe("2026-11-16")).toBe("2026-11-16");
    // El domingo pertenece a la semana que empezó el lunes anterior
    expect(lunesDe("2026-11-22")).toBe("2026-11-16");
  });
});

describe("estadoDe", () => {
  it("Cancelado gana aunque ya haya terminado (con espacios de char())", () => {
    const fila = crearFila({
      estado_del_programa: "Cancelado ",
      fecha_fin: "2026-11-01T10:00:00",
    });
    expect(estadoDe(fila, 0, AHORA)).toBe("Cancelado");
  });

  it("Finalizado cuando la fecha fin ya pasó", () => {
    expect(estadoDe(crearFila({ fecha_fin: "2026-11-17T13:00:00" }), 0, AHORA)).toBe("Finalizado");
  });

  it("Lleno cuando inscritos >= cupo", () => {
    expect(estadoDe(crearFila({ cupo_maximo: 3 }), 3, AHORA)).toBe("Lleno");
  });

  it("Disponible si hay lugar o no tiene cupo definido", () => {
    expect(estadoDe(crearFila(), 2, AHORA)).toBe("Disponible");
    expect(estadoDe(crearFila({ cupo_maximo: null }), 50, AHORA)).toBe("Disponible");
  });
});

describe("contarPorPrograma", () => {
  it("cuenta las inscripciones de cada programa", () => {
    const conteo = contarPorPrograma([
      { id_programa: ID_PROGRAMA },
      { id_programa: ID_PROGRAMA },
      { id_programa: OTRO_PROGRAMA },
    ]);
    expect(conteo.get(ID_PROGRAMA)).toBe(2);
    expect(conteo.get(OTRO_PROGRAMA)).toBe(1);
  });
});

describe("semanasSinCobertura", () => {
  const programa = (inicio, fin, estado = "Disponible") => ({ inicio, fin, estado });

  it("sin programas marca la semana actual y las futuras, no las pasadas", () => {
    expect(semanasSinCobertura([], "2026-11-09", "2026-11-30", AHORA)).toEqual([
      { inicio: "2026-11-16", fin: "2026-11-23" },
      { inicio: "2026-11-23", fin: "2026-11-30" },
    ]);
  });

  it("un programa cancelado no da cobertura", () => {
    const programas = [programa("2026-11-24T09:00:00", "2026-11-24T12:00:00", "Cancelado")];
    expect(semanasSinCobertura(programas, "2026-11-23", "2026-11-30", AHORA)).toEqual([
      { inicio: "2026-11-23", fin: "2026-11-30" },
    ]);
  });

  it("un programa de sábado a martes cubre las dos semanas", () => {
    const programas = [programa("2026-11-21T09:00:00", "2026-11-24T14:00:00")];
    expect(semanasSinCobertura(programas, "2026-11-16", "2026-11-30", AHORA)).toEqual([]);
  });

  it("un programa que termina el lunes a las 00:00 no cubre esa semana", () => {
    const programas = [programa("2026-11-29T18:00:00", "2026-11-30T00:00:00", "Lleno")];
    expect(semanasSinCobertura(programas, "2026-11-23", "2026-12-07", AHORA)).toEqual([
      { inicio: "2026-11-30", fin: "2026-12-07" },
    ]);
  });

  it("un programa Finalizado de la semana actual sí cuenta como cobertura", () => {
    const programas = [programa("2026-11-16T09:00:00", "2026-11-16T12:00:00", "Finalizado")];
    expect(semanasSinCobertura(programas, "2026-11-16", "2026-11-23", AHORA)).toEqual([]);
  });
});

describe("armarCalendario", () => {
  it("convierte las filas y pone 0 inscritos si no hay inscripciones", () => {
    const data = armarCalendario({
      programas: [crearFila()],
      inscripciones: [],
      inicio: "2026-11-23",
      finExclusivo: "2026-11-30",
      ahora: AHORA,
    });

    expect(data.programas).toEqual([
      {
        idPrograma: ID_PROGRAMA,
        nombre: "Jornada de limpieza",
        tipo: "Limpieza",
        inicio: "2026-11-24T09:00:00",
        fin: "2026-11-24T13:00:00",
        ubicacion: "Refugio",
        cupoMaximo: 10,
        inscritos: 0,
        estado: "Disponible",
      },
    ]);
    expect(data.semanasSinCobertura).toEqual([]);
  });
});

describe("GET /admin/programas/calendario", () => {
  it("400 si el rango no es válido y no consulta la BD", async () => {
    const res = crearRespuesta();
    await getCalendario({ query: { desde: "2026-02-30", hasta: "2026-03-01" } }, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ code: "RANGO_INVALIDO" }));
    expect(mocks.programasEnRango).not.toHaveBeenCalled();
  });

  it("200 amplía a semanas completas y calcula estado y cobertura", async () => {
    mocks.programasEnRango.mockResolvedValue([
      crearFila({ cupo_maximo: 2, estado_del_programa: "Disponible " }),
    ]);
    mocks.inscripcionesActivas.mockResolvedValue([
      { id_programa: ID_PROGRAMA },
      { id_programa: ID_PROGRAMA },
    ]);
    const res = crearRespuesta();

    // Miércoles a miércoles: se consulta del lunes 16 al lunes 30
    await getCalendario({ query: { desde: "2026-11-18", hasta: "2026-11-25" } }, res);

    expect(mocks.programasEnRango).toHaveBeenCalledWith(
      "2026-11-16 00:00:00",
      "2026-11-30 00:00:00",
    );
    expect(mocks.inscripcionesActivas).toHaveBeenCalledWith([ID_PROGRAMA]);
    expect(res.status).toHaveBeenCalledWith(200);

    const { data } = res.json.mock.calls[0][0];
    expect(data.programas[0]).toMatchObject({ inscritos: 2, estado: "Lleno" });
    expect(data.semanasSinCobertura).toEqual([{ inicio: "2026-11-16", fin: "2026-11-23" }]);
  });

  it("500 si falla la consulta", async () => {
    vi.spyOn(console, "error").mockImplementation(() => {});
    mocks.programasEnRango.mockRejectedValue(new Error("Supabase caído"));
    const res = crearRespuesta();

    await getCalendario({ query: { desde: "2026-11-16", hasta: "2026-11-22" } }, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ code: "ERROR_INTERNO" }));
  });
});
