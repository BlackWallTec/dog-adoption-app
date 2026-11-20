import { beforeEach, describe, expect, it, vi } from "vite-plus/test";

const { crearMock, getDeUsuarioMock } = vi.hoisted(() => ({
  crearMock: vi.fn(),
  getDeUsuarioMock: vi.fn(),
}));

vi.mock("../../src/modules/donativos/http/modelosDonativos/solicitudCfdi.model.js", () => ({
  default: { crear: crearMock },
}));
vi.mock("../../src/modules/donativos/http/modelosDonativos/donativo.model.js", () => ({
  default: { getDeUsuario: getDeUsuarioMock },
}));

import {
  getMisDonativos,
  postSolicitudCfdi,
} from "../../src/modules/donativos/http/cfdiCliente.controller.js";

const ID = "0ebb3c99-7827-451c-8483-7bd91c38323c";

function crearRes() {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn(() => res);
  return res;
}

describe("HU49 - cfdiCliente.controller", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("trae los donativos del usuario que viene en el token", async () => {
    getDeUsuarioMock.mockResolvedValue([{ id_donativo: ID }]);
    const res = crearRes();
    await getMisDonativos({ user: { uuid: "u1" } }, res);
    expect(getDeUsuarioMock).toHaveBeenCalledWith("u1");
    expect(res.status).toHaveBeenCalledWith(200);
  });

  it("CA-01: crea la solicitud si el donativo está validado", async () => {
    crearMock.mockResolvedValue("creada");
    const res = crearRes();
    await postSolicitudCfdi({ params: { idDonativo: ID }, user: { uuid: "u1" } }, res);
    expect(crearMock).toHaveBeenCalledWith({ idDonativo: ID, idUsuario: "u1" });
    expect(res.status).toHaveBeenCalledWith(201);
  });

  it("CA-02: no deja pedir CFDI si el donativo no está validado", async () => {
    crearMock.mockResolvedValue("no-validado");
    const res = crearRes();
    await postSolicitudCfdi({ params: { idDonativo: ID }, user: { uuid: "u1" } }, res);
    expect(res.status).toHaveBeenCalledWith(400);
  });

  it("no deja pedir el CFDI dos veces", async () => {
    crearMock.mockResolvedValue("repetida");
    const res = crearRes();
    await postSolicitudCfdi({ params: { idDonativo: ID }, user: { uuid: "u1" } }, res);
    expect(res.status).toHaveBeenCalledWith(400);
  });

  it("rechaza un id inválido sin consultar la base", async () => {
    const res = crearRes();
    await postSolicitudCfdi({ params: { idDonativo: "abc" }, user: { uuid: "u1" } }, res);
    expect(crearMock).not.toHaveBeenCalled();
    expect(res.status).toHaveBeenCalledWith(400);
  });
});
