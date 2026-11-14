import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";

// Mocks compartidos (vi.hoisted evita las advertencias de unbound-method del lint).
const mocks = vi.hoisted(() => ({
  getPrograma: vi.fn(),
  actualizar: vi.fn(),
  cancelar: vi.fn(),
  ahora: vi.fn(),
  empalmes: vi.fn(),
  contarInscritos: vi.fn(),
}));

// Modelos simulados: las pruebas no se conectan a Supabase.
vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/programaEdicion.model.js", () => ({
  getProgramaParaEditar: mocks.getPrograma,
  actualizarPrograma: mocks.actualizar,
  cancelarPrograma: mocks.cancelar,
  ahoraMexico: mocks.ahora,
}));

vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/programaCreacion.model.js", () => ({
  obtenerEmpalmes: mocks.empalmes,
  combinarFechaHora: (fecha, hora) => `${fecha} ${hora}:00`,
}));

vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js", () => ({
  contarInscritosActivosModel: mocks.contarInscritos,
}));

import {
  obtenerParaEditar,
  editarPrograma,
  cancelarPrograma,
  esEditable,
  tomarCamposEditables,
} from "../../src/modules/voluntarios/http/programaEdicion.controller.js";

const ID_PROGRAMA = "22222222-2222-4222-8222-222222222222";
const OTRO_PROGRAMA = "33333333-3333-4333-8333-333333333333";
const AHORA = "2030-11-01 06:00:00";

function crearProgramaActual(cambios = {}) {
  return {
    idPrograma: ID_PROGRAMA,
    nombre: "Jornada prueba",
    tipo: "Limpieza",
    descripcion: "Apoyo en limpieza del refugio",
    fechaInicio: "2030-11-14",
    horaInicio: "09:00",
    fechaFin: "2030-11-14",
    horaFin: "12:00",
    ubicacion: "Qariño Animal",
    cupoMaximo: 15,
    estado: "Disponible",
    motivoCancelacion: null,
    ...cambios,
  };
}

function crearCuerpo(cambios = {}) {
  return {
    nombre: "Jornada prueba",
    tipo: "Limpieza",
    descripcion: "Apoyo en limpieza del refugio",
    ubicacion: "Qariño Animal",
    fechaInicio: "2030-11-14",
    horaInicio: "09:00",
    fechaFin: "2030-11-14",
    horaFin: "12:00",
    cupoMaximo: 15,
    ...cambios,
  };
}

function crearRespuesta() {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn(() => res);
  return res;
}

function crearReq({ idPrograma = ID_PROGRAMA, body } = {}) {
  return {
    params: { idPrograma },
    body,
    user: { uuid: "11111111-1111-4111-8111-111111111111", rol: "Administrador" },
  };
}

const respuesta404 = {
  success: false,
  code: "PROGRAMA_NO_ENCONTRADO",
  message: "No encontramos este programa.",
};

beforeEach(() => {
  vi.resetAllMocks();

  vi.useFakeTimers({ toFake: ["Date"] });
  vi.setSystemTime(new Date("2030-11-01T12:00:00Z"));

  vi.spyOn(console, "error").mockImplementation(() => {});

  mocks.ahora.mockReturnValue(AHORA);
  mocks.getPrograma.mockResolvedValue(crearProgramaActual());
  mocks.contarInscritos.mockResolvedValue(0);
  mocks.empalmes.mockResolvedValue([]);
  mocks.actualizar.mockImplementation((id, datos) =>
    Promise.resolve(crearProgramaActual({ ...datos })),
  );
  mocks.cancelar.mockImplementation((id, motivo) =>
    Promise.resolve(crearProgramaActual({ estado: "Cancelado", motivoCancelacion: motivo })),
  );
});

afterEach(() => {
  vi.useRealTimers();
  vi.restoreAllMocks();
});

describe("funciones auxiliares (HU38)", () => {
  it("esEditable acepta un programa disponible que no ha finalizado", () => {
    expect(esEditable(crearProgramaActual(), AHORA)).toBe(true);
  });

  it("esEditable rechaza un programa cancelado", () => {
    expect(esEditable(crearProgramaActual({ estado: "Cancelado" }), AHORA)).toBe(false);
  });

  it("esEditable rechaza un programa cuya fecha de fin ya pasó", () => {
    const programa = crearProgramaActual({ fechaFin: "2030-10-01", horaFin: "12:00" });
    expect(esEditable(programa, AHORA)).toBe(false);
  });

  it("tomarCamposEditables ignora los campos que no se pueden editar", () => {
    const campos = tomarCamposEditables(
      crearCuerpo({ estado_del_programa: "Cancelado", idAdmin: "x", idPrograma: "y" }),
    );

    expect(campos).not.toHaveProperty("estado_del_programa");
    expect(campos).not.toHaveProperty("idAdmin");
    expect(campos).not.toHaveProperty("idPrograma");
    expect(campos).toHaveProperty("nombre", "Jornada prueba");
  });
});

describe("obtenerParaEditar (HU38)", () => {
  it("responde 404 sin consultar si el id no es un UUID", async () => {
    const res = crearRespuesta();

    await obtenerParaEditar(crearReq({ idPrograma: "abc" }), res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith(respuesta404);
    expect(mocks.getPrograma).not.toHaveBeenCalled();
  });

  it("responde 404 si el programa no existe", async () => {
    mocks.getPrograma.mockResolvedValue(null);
    const res = crearRespuesta();

    await obtenerParaEditar(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith(respuesta404);
  });

  it("regresa los datos, los inscritos activos y que es editable", async () => {
    mocks.contarInscritos.mockResolvedValue(4);
    const res = crearRespuesta();

    await obtenerParaEditar(crearReq(), res);

    expect(res.status).not.toHaveBeenCalled();
    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: { ...crearProgramaActual(), inscritosActivos: 4, editable: true },
    });
  });

  it("marca como no editable un programa cancelado", async () => {
    mocks.getPrograma.mockResolvedValue(crearProgramaActual({ estado: "Cancelado" }));
    const res = crearRespuesta();

    await obtenerParaEditar(crearReq(), res);

    expect(res.json.mock.calls[0][0].data.editable).toBe(false);
  });

  it("responde 500 si falla la consulta", async () => {
    mocks.getPrograma.mockRejectedValue(new Error("BD caída"));
    const res = crearRespuesta();

    await obtenerParaEditar(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ success: false, code: "ERROR_INTERNO" }),
    );
  });
});

describe("editarPrograma (HU38)", () => {
  it("guarda los cambios ignorando los campos no editables", async () => {
    const res = crearRespuesta();
    const body = crearCuerpo({
      nombre: "  Jornada editada  ",
      estado_del_programa: "Cancelado",
      idAdmin: "otro",
    });

    await editarPrograma(crearReq({ body }), res);

    expect(mocks.actualizar).toHaveBeenCalledOnce();
    const [idGuardado, datosGuardados] = mocks.actualizar.mock.calls[0];
    expect(idGuardado).toBe(ID_PROGRAMA);
    expect(datosGuardados.nombre).toBe("Jornada editada");
    expect(datosGuardados).not.toHaveProperty("estado_del_programa");
    expect(datosGuardados).not.toHaveProperty("idAdmin");

    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: expect.objectContaining({ inscritosActivos: 0, avisarInscritos: false }),
    });
  });

  it("pide avisar a los inscritos si hay inscritos y cambió el horario", async () => {
    mocks.contarInscritos.mockResolvedValue(3);
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo({ horaInicio: "10:00" }) }), res);

    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: expect.objectContaining({ inscritosActivos: 3, avisarInscritos: true }),
    });
  });

  it("no pide avisar si hay inscritos pero solo cambió el nombre", async () => {
    mocks.contarInscritos.mockResolvedValue(3);
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo({ nombre: "Otro nombre" }) }), res);

    expect(res.json.mock.calls[0][0].data.avisarInscritos).toBe(false);
  });

  it("responde 404 si el id no es un UUID", async () => {
    const res = crearRespuesta();

    await editarPrograma(crearReq({ idPrograma: "abc", body: crearCuerpo() }), res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(mocks.actualizar).not.toHaveBeenCalled();
  });

  it("responde 404 si el programa no existe", async () => {
    mocks.getPrograma.mockResolvedValue(null);
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo() }), res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(mocks.actualizar).not.toHaveBeenCalled();
  });

  it("responde 409 si el programa está cancelado", async () => {
    mocks.getPrograma.mockResolvedValue(crearProgramaActual({ estado: "Cancelado" }));
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo() }), res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ code: "PROGRAMA_NO_EDITABLE" }),
    );
    expect(mocks.actualizar).not.toHaveBeenCalled();
  });

  it("responde 400 si un campo obligatorio está vacío", async () => {
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo({ nombre: "   " }) }), res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ code: "VALIDACION", campo: "nombre" }),
    );
    expect(mocks.actualizar).not.toHaveBeenCalled();
  });

  it("responde 400 si se cambia el inicio a una fecha pasada", async () => {
    const res = crearRespuesta();
    const body = crearCuerpo({ fechaInicio: "2030-10-01", fechaFin: "2030-10-01" });

    await editarPrograma(crearReq({ body }), res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ code: "VALIDACION", campo: "fechas" }),
    );
  });

  it("permite editar un programa ya iniciado si no cambia la fecha de inicio", async () => {
    vi.setSystemTime(new Date("2030-11-14T17:00:00Z"));
    mocks.ahora.mockReturnValue("2030-11-14 10:00:00");
    const res = crearRespuesta();

    await editarPrograma(
      crearReq({ body: crearCuerpo({ descripcion: "Nueva descripción" }) }),
      res,
    );

    expect(res.status).not.toHaveBeenCalled();
    expect(mocks.actualizar).toHaveBeenCalledOnce();
  });

  it("responde 400 si la nueva fecha de fin ya pasó", async () => {
    mocks.ahora.mockReturnValue("2030-11-14 13:00:00");
    mocks.getPrograma.mockResolvedValue(crearProgramaActual({ horaFin: "18:00" }));
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo({ horaFin: "12:00" }) }), res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ code: "VALIDACION", message: "La fecha de fin ya pasó." }),
    );
  });

  it("responde 409 si el cupo es menor a los inscritos activos", async () => {
    mocks.contarInscritos.mockResolvedValue(5);
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo({ cupoMaximo: 3 }) }), res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ code: "CUPO_MENOR_A_INSCRITOS", inscritosActivos: 5 }),
    );
    expect(mocks.actualizar).not.toHaveBeenCalled();
  });

  it("permite un cupo igual a los inscritos activos", async () => {
    mocks.contarInscritos.mockResolvedValue(5);
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo({ cupoMaximo: 5 }) }), res);

    expect(mocks.actualizar).toHaveBeenCalledOnce();
  });

  it("responde 409 OVERLAP sin incluir al propio programa", async () => {
    mocks.empalmes.mockResolvedValue([
      { idPrograma: ID_PROGRAMA, nombre: "Jornada prueba" },
      { idPrograma: OTRO_PROGRAMA, nombre: "Otra jornada" },
    ]);
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo() }), res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        code: "OVERLAP",
        empalmes: [{ idPrograma: OTRO_PROGRAMA, nombre: "Otra jornada" }],
      }),
    );
    expect(mocks.actualizar).not.toHaveBeenCalled();
  });

  it("no marca empalme si el único traslape es el propio programa", async () => {
    mocks.empalmes.mockResolvedValue([{ idPrograma: ID_PROGRAMA, nombre: "Jornada prueba" }]);
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo() }), res);

    expect(mocks.actualizar).toHaveBeenCalledOnce();
  });

  it("guarda con empalme si el admin lo confirma", async () => {
    mocks.empalmes.mockResolvedValue([{ idPrograma: OTRO_PROGRAMA, nombre: "Otra jornada" }]);
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo({ confirmarEmpalme: true }) }), res);

    expect(mocks.actualizar).toHaveBeenCalledOnce();
    expect(res.status).not.toHaveBeenCalled();
  });

  it("responde 409 si el programa se canceló mientras se editaba", async () => {
    mocks.actualizar.mockResolvedValue(null);
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo() }), res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ code: "PROGRAMA_NO_EDITABLE" }),
    );
  });

  it("responde 500 si falla el guardado", async () => {
    mocks.actualizar.mockRejectedValue(new Error("BD caída"));
    const res = crearRespuesta();

    await editarPrograma(crearReq({ body: crearCuerpo() }), res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ success: false, code: "ERROR_INTERNO" }),
    );
  });
});

describe("cancelarPrograma (HU38)", () => {
  it("cancela el programa y guarda el motivo sin espacios sobrantes", async () => {
    mocks.contarInscritos.mockResolvedValue(2);
    const res = crearRespuesta();

    await cancelarPrograma(crearReq({ body: { motivo: "  Lluvia  " } }), res);

    expect(mocks.cancelar).toHaveBeenCalledExactlyOnceWith(ID_PROGRAMA, "Lluvia");
    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: {
        programa: crearProgramaActual({ estado: "Cancelado", motivoCancelacion: "Lluvia" }),
        inscritosActivos: 2,
      },
    });
  });

  it("cancela sin motivo guardando null", async () => {
    const res = crearRespuesta();

    await cancelarPrograma(crearReq({ body: {} }), res);

    expect(mocks.cancelar).toHaveBeenCalledExactlyOnceWith(ID_PROGRAMA, null);
    expect(res.status).not.toHaveBeenCalled();
  });

  it("trata un motivo vacío como null", async () => {
    const res = crearRespuesta();

    await cancelarPrograma(crearReq({ body: { motivo: "   " } }), res);

    expect(mocks.cancelar).toHaveBeenCalledExactlyOnceWith(ID_PROGRAMA, null);
  });

  it("responde 400 si el motivo tiene más de 200 caracteres", async () => {
    const res = crearRespuesta();

    await cancelarPrograma(crearReq({ body: { motivo: "a".repeat(201) } }), res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ code: "VALIDACION", campo: "motivo" }),
    );
    expect(mocks.cancelar).not.toHaveBeenCalled();
  });

  it("responde 400 si el motivo no es texto", async () => {
    const res = crearRespuesta();

    await cancelarPrograma(crearReq({ body: { motivo: 123 } }), res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(mocks.cancelar).not.toHaveBeenCalled();
  });

  it("responde 404 si el id no es un UUID", async () => {
    const res = crearRespuesta();

    await cancelarPrograma(crearReq({ idPrograma: "abc", body: {} }), res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(mocks.cancelar).not.toHaveBeenCalled();
  });

  it("responde 409 si el programa ya estaba cancelado o finalizó", async () => {
    mocks.cancelar.mockResolvedValue(null);
    const res = crearRespuesta();

    await cancelarPrograma(crearReq({ body: {} }), res);

    expect(res.status).toHaveBeenCalledWith(409);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ code: "PROGRAMA_NO_CANCELABLE" }),
    );
  });

  it("responde 404 si no se canceló porque el programa no existe", async () => {
    mocks.cancelar.mockResolvedValue(null);
    mocks.getPrograma.mockResolvedValue(null);
    const res = crearRespuesta();

    await cancelarPrograma(crearReq({ body: {} }), res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith(respuesta404);
  });

  it("responde 500 si falla la cancelación", async () => {
    mocks.cancelar.mockRejectedValue(new Error("BD caída"));
    const res = crearRespuesta();

    await cancelarPrograma(crearReq({ body: {} }), res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ success: false, code: "ERROR_INTERNO" }),
    );
  });
});
