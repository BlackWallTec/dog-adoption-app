import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";

// Mocks compartidos (vi.hoisted evita las advertencias de unbound-method del lint).
const mocks = vi.hoisted(() => ({
  getDetalle: vi.fn(),
  getTipo: vi.fn(),
  tuvoInscripcion: vi.fn(),
  contarInscritos: vi.fn(),
  idsInscritos: vi.fn(),
}));

// Modelos simulados: las pruebas no se conectan a Supabase.
vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/programaDetalle.model.js", () => ({
  getProgramaDetalleModel: mocks.getDetalle,
  getDescripcionTipoModel: mocks.getTipo,
  tuvoInscripcionModel: mocks.tuvoInscripcion,
}));

vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js", () => ({
  getIdsProgramasInscritosModel: mocks.idsInscritos,
  contarInscritosModel: mocks.contarInscritos,
}));

import {
  getDetallePrograma,
  esConsultaRestringida,
} from "../../src/modules/voluntarios/http/programaDetalle.controller.js";

const ID_PROGRAMA = "22222222-2222-4222-8222-222222222222";
const USUARIO = "11111111-1111-4111-8111-111111111111";

const crearPrograma = (cambios = {}) => ({
  id_programa: ID_PROGRAMA,
  nombre: "Paseos con cariño",
  tipo: "Paseo",
  descripcion: "Ropa cómoda y agua.",
  fecha_inicio: "2099-12-10T09:00:00",
  fecha_fin: "2099-12-10T11:00:00",
  ubicacion: "Refugio",
  cupo_maximo: 15,
  estado_del_programa: "Disponible          ",
  ...cambios,
});

const crearRespuesta = () => {
  const res = {};
  res.status = vi.fn().mockReturnValue(res);
  res.json = vi.fn().mockReturnValue(res);
  return res;
};

// verificarToken deja el usuario del token en req.user
const crearReq = (idPrograma = ID_PROGRAMA) => ({
  params: { idPrograma },
  user: { uuid: USUARIO },
});

beforeEach(() => {
  mocks.getDetalle.mockResolvedValue(crearPrograma());
  mocks.getTipo.mockResolvedValue("Paseos con los perros");
  mocks.contarInscritos.mockResolvedValue(8);
  mocks.idsInscritos.mockResolvedValue(new Set());
  mocks.tuvoInscripcion.mockResolvedValue(false);
  vi.spyOn(console, "error").mockImplementation(() => {});
});

afterEach(() => {
  vi.clearAllMocks();
  vi.restoreAllMocks();
});

describe("getDetallePrograma (HU36)", () => {
  it("responde 200 con el detalle, el tipo, los inscritos y el estado sin espacios", async () => {
    const res = crearRespuesta();

    await getDetallePrograma(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(200);
    const { data } = res.json.mock.calls[0][0];
    expect(data.estado_del_programa).toBe("Disponible");
    expect(data.tipo_descripcion).toBe("Paseos con los perros");
    expect(data.inscritos).toBe(8);
    expect(data.usuario_inscrito).toBe(false);
  });

  it("marca usuario_inscrito si el usuario del token ya está inscrito", async () => {
    mocks.idsInscritos.mockResolvedValue(new Set([ID_PROGRAMA]));
    const res = crearRespuesta();

    await getDetallePrograma(crearReq(), res);

    expect(mocks.idsInscritos).toHaveBeenCalledWith(USUARIO);
    expect(res.json.mock.calls[0][0].data.usuario_inscrito).toBe(true);
  });

  it("responde 404 si el id no es un UUID", async () => {
    const res = crearRespuesta();

    await getDetallePrograma(crearReq("abc"), res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(mocks.getDetalle).not.toHaveBeenCalled();
  });

  it("responde 404 si el programa no existe", async () => {
    mocks.getDetalle.mockResolvedValue(null);
    const res = crearRespuesta();

    await getDetallePrograma(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(404);
  });

  it("responde 404 si el programa está cancelado y el usuario nunca se inscribió", async () => {
    mocks.getDetalle.mockResolvedValue(crearPrograma({ estado_del_programa: "Cancelado" }));
    const res = crearRespuesta();

    await getDetallePrograma(crearReq(), res);

    expect(mocks.tuvoInscripcion).toHaveBeenCalledWith(ID_PROGRAMA, USUARIO);
    expect(res.status).toHaveBeenCalledWith(404);
  });

  it("muestra un programa cancelado a quien estuvo inscrito", async () => {
    mocks.getDetalle.mockResolvedValue(crearPrograma({ estado_del_programa: "Cancelado" }));
    mocks.tuvoInscripcion.mockResolvedValue(true);
    const res = crearRespuesta();

    await getDetallePrograma(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(200);
  });

  it("responde 404 si el programa ya terminó y el usuario nunca se inscribió", async () => {
    mocks.getDetalle.mockResolvedValue(crearPrograma({ fecha_fin: "2020-01-01T10:00:00" }));
    const res = crearRespuesta();

    await getDetallePrograma(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(404);
  });

  it("responde 500 sin exponer el error si falla la consulta", async () => {
    mocks.getDetalle.mockRejectedValue(new Error("detalle interno de la BD"));
    const res = crearRespuesta();

    await getDetallePrograma(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json.mock.calls[0][0].message).not.toContain("BD");
  });
});

describe("esConsultaRestringida", () => {
  const AHORA = "2030-06-01T12:00:00";

  it("no restringe un programa disponible que no ha terminado", () => {
    expect(esConsultaRestringida(crearPrograma(), AHORA)).toBe(false);
  });

  it("restringe un programa cancelado o finalizado", () => {
    expect(
      esConsultaRestringida(crearPrograma({ estado_del_programa: "Cancelado  " }), AHORA),
    ).toBe(true);
    expect(esConsultaRestringida(crearPrograma({ estado_del_programa: "Finalizado" }), AHORA)).toBe(
      true,
    );
  });

  it("restringe un programa cuya fecha de fin ya pasó", () => {
    expect(esConsultaRestringida(crearPrograma({ fecha_fin: "2030-06-01T11:59:00" }), AHORA)).toBe(
      true,
    );
  });
});
