import { beforeEach, describe, expect, it, vi } from "vite-plus/test";

const { cfdiModel, validarArchivosMock, enviarCorreoMock } = vi.hoisted(() => ({
  cfdiModel: {
    obtenerContextoParaCarga: vi.fn(),
    verificarUuidDisponible: vi.fn(),
    guardarArchivos: vi.fn(),
    iniciarIntentoCorreo: vi.fn(),
    completarIntentoCorreo: vi.fn(),
    fallarIntentoCorreo: vi.fn(),
    obtenerArchivosParaReenvio: vi.fn(),
    listarDelCliente: vi.fn(),
    descargarParaCliente: vi.fn(),
  },
  validarArchivosMock: vi.fn(),
  enviarCorreoMock: vi.fn(),
}));

vi.mock("../../src/modules/donativos/http/modelosDonativos/cfdi.model.js", () => ({
  default: cfdiModel,
}));

vi.mock("../../src/modules/donativos/http/serviciosDonativos/cfdiXml.service.js", () => ({
  ErrorValidacionCfdi: class ErrorValidacionCfdi extends Error {
    constructor(codigo, mensaje, status = 400) {
      super(mensaje);
      this.codigo = codigo;
      this.status = status;
    }
  },
  LIMITES_ARCHIVOS_CFDI: { pdf: 10 * 1024 * 1024, xml: 5 * 1024 * 1024 },
  validarArchivosPdfXml: validarArchivosMock,
}));

vi.mock("../../src/modules/donativos/http/serviciosDonativos/cfdiMail.service.js", () => ({
  enviarCfdiPorCorreo: enviarCorreoMock,
}));

import {
  confirmarCargaCfdi,
  reintentarCorreoCfdi,
  validarCargaCfdi,
} from "../../src/modules/donativos/http/adminCfdi.controller.js";

const idSolicitud = "b2e87183-0b85-4840-a5e7-5eae19dce391";
const datos = {
  uuid: "E9BF9F1A-1445-4DE3-A372-47FA82FCBD0C",
  rfcReceptor: "XAXX010101000",
  total: 125.5,
};
const contexto = {
  solicitud: { id_solicitud_cfdi: idSolicitud },
  donativo: {
    id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
    monto: 125.5,
    usuario: { nombre: "Ana", apellidos: "Luna" },
  },
  datosFiscales: { rfc: "XAXX010101000", correo_facturacion: "ana@example.test" },
};
const archivos = {
  pdf: { buffer: Buffer.from("%PDF-1.7") },
  xml: { buffer: Buffer.from("<cfdi/>") },
};

function crearReq(body = {}) {
  return {
    params: { idSolicitud },
    body,
    files: { pdf: [archivos.pdf], xml: [archivos.xml] },
    user: { uuid: "admin-1" },
  };
}

function crearRes() {
  const res = {
    status: vi.fn(),
    json: vi.fn(),
    set: vi.fn(),
  };
  res.status.mockReturnValue(res);
  res.json.mockReturnValue(res);
  return res;
}

describe("HU57 - CFDI administrativo", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    cfdiModel.obtenerContextoParaCarga.mockResolvedValue(contexto);
    cfdiModel.verificarUuidDisponible.mockResolvedValue(undefined);
    cfdiModel.guardarArchivos.mockResolvedValue(contexto);
    cfdiModel.iniciarIntentoCorreo.mockResolvedValue({ cfdi_uuid: datos.uuid });
    cfdiModel.completarIntentoCorreo.mockResolvedValue(undefined);
    cfdiModel.fallarIntentoCorreo.mockResolvedValue(undefined);
    cfdiModel.obtenerArchivosParaReenvio.mockResolvedValue({
      ...contexto,
      solicitud: { ...contexto.solicitud, cfdi_uuid: datos.uuid },
      pdf: archivos.pdf.buffer,
      xml: archivos.xml.buffer,
    });
    validarArchivosMock.mockReturnValue(datos);
    enviarCorreoMock.mockResolvedValue(undefined);
  });

  it("previsualiza validaciones y no guarda archivos antes de la confirmación", async () => {
    const res = crearRes();

    await validarCargaCfdi(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      valido: true,
      uuid: datos.uuid,
      rfc_receptor: datos.rfcReceptor,
      monto: datos.total,
      donativo: {
        id_donativo: contexto.donativo.id_donativo,
        monto: 125.5,
        rfc: contexto.datosFiscales.rfc,
      },
    });
    expect(cfdiModel.guardarArchivos).not.toHaveBeenCalled();
  });

  it("requiere confirmación administrativa del PDF antes de guardar", async () => {
    const res = crearRes();

    await confirmarCargaCfdi(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(cfdiModel.guardarArchivos).not.toHaveBeenCalled();
  });

  it("guarda los dos archivos y marca atendida la solicitud cuando el correo se envía", async () => {
    const res = crearRes();

    await confirmarCargaCfdi(crearReq({ confirmar_pdf: "true" }), res);

    expect(cfdiModel.guardarArchivos).toHaveBeenCalledWith({
      idSolicitud,
      idAdministrador: "admin-1",
      datos,
      ...archivos,
    });
    expect(enviarCorreoMock).toHaveBeenCalledWith({
      destinatario: "ana@example.test",
      nombre: "Ana Luna",
      uuid: datos.uuid,
      pdf: archivos.pdf.buffer,
      xml: archivos.xml.buffer,
    });
    expect(cfdiModel.completarIntentoCorreo).toHaveBeenCalledWith(idSolicitud);
    expect(res.status).toHaveBeenCalledWith(201);
    expect(res.json).toHaveBeenCalledWith({
      almacenado: true,
      correo: { estado: "Enviado" },
    });
  });

  it("conserva los archivos e informa que el correo puede reintentarse", async () => {
    enviarCorreoMock.mockRejectedValue(new Error("Proveedor no disponible"));
    const res = crearRes();

    await confirmarCargaCfdi(crearReq({ confirmar_pdf: "true" }), res);

    expect(cfdiModel.guardarArchivos).toHaveBeenCalledOnce();
    expect(cfdiModel.fallarIntentoCorreo).toHaveBeenCalledWith(idSolicitud);
    expect(cfdiModel.completarIntentoCorreo).not.toHaveBeenCalled();
    expect(res.status).toHaveBeenCalledWith(201);
    expect(res.json).toHaveBeenCalledWith({
      almacenado: true,
      correo: {
        estado: "Error",
        mensaje: "No se pudo enviar el correo. Los archivos siguen disponibles en la app.",
      },
    });
  });

  it("reintenta el correo usando los archivos ya guardados", async () => {
    const res = crearRes();

    await reintentarCorreoCfdi({ params: { idSolicitud } }, res);

    expect(cfdiModel.guardarArchivos).not.toHaveBeenCalled();
    expect(cfdiModel.iniciarIntentoCorreo).toHaveBeenCalledWith(idSolicitud);
    expect(enviarCorreoMock).toHaveBeenCalledWith({
      destinatario: "ana@example.test",
      nombre: "Ana Luna",
      uuid: datos.uuid,
      pdf: archivos.pdf.buffer,
      xml: archivos.xml.buffer,
    });
    expect(res.status).toHaveBeenCalledWith(200);
  });
});
