import { describe, it, expect, vi, beforeEach } from "vite-plus/test";

vi.mock("../../src/modules/solicitudes/http/modelosSolicitudes/documentos.model.js", () => ({
  obtenerSolicitudParaDocumentos: vi.fn(),
  obtenerDocumentosPreviosPorTipo: vi.fn(),
  subirArchivoStorage: vi.fn(),
  eliminarArchivoStorage: vi.fn(),
  crearRegistroDocumento: vi.fn(),
  relacionarDocumentoSolicitud: vi.fn(),
  eliminarDocumentosReemplazados: vi.fn(),
}));

import * as DocumentoModel from "../../src/modules/solicitudes/http/modelosSolicitudes/documentos.model.js";
import { subirDocumento } from "../../src/modules/solicitudes/http/documentos.controller.js";

const SOLICITUD_ID = "ff028456-acf9-4781-a560-98dd168a5516";

function crearReq({ tipo, conArchivo = true, userId = "user-1", id = SOLICITUD_ID } = {}) {
  return {
    params: { id },
    user: { uuid: userId },
    body: tipo !== undefined ? { tipo } : {},
    file: conArchivo
      ? {
          originalname: "documento.pdf",
          mimetype: "application/pdf",
          size: 1024,
          buffer: Buffer.from("pdf"),
        }
      : undefined,
  };
}

function crearRes() {
  const res = {};
  res.status = vi.fn().mockReturnValue(res);
  res.json = vi.fn().mockReturnValue(res);
  return res;
}

beforeEach(() => {
  vi.resetAllMocks();
  DocumentoModel.obtenerSolicitudParaDocumentos.mockResolvedValue({
    id_solicitud: SOLICITUD_ID,
    id_usuario: "user-1",
    puede_editar: true,
    estado: "Borrador",
  });
  DocumentoModel.obtenerDocumentosPreviosPorTipo.mockResolvedValue([]);
  DocumentoModel.subirArchivoStorage.mockResolvedValue({
    url_archivo: "url",
    storagePath: `${SOLICITUD_ID}/123_documento.pdf`,
  });
  DocumentoModel.crearRegistroDocumento.mockResolvedValue("doc-1");
  DocumentoModel.relacionarDocumentoSolicitud.mockResolvedValue();
  DocumentoModel.eliminarDocumentosReemplazados.mockResolvedValue();
  DocumentoModel.eliminarArchivoStorage.mockResolvedValue();
});

describe("subirDocumento (controlador)", () => {
  it("responde 400 si no llega archivo", async () => {
    const res = crearRes();

    await subirDocumento(crearReq({ conArchivo: false }), res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(DocumentoModel.obtenerSolicitudParaDocumentos).not.toHaveBeenCalled();
  });

  it("sube el archivo, crea el registro y lo relaciona con la solicitud", async () => {
    const res = crearRes();

    await subirDocumento(crearReq({ tipo: "DOMICILIO" }), res);

    expect(DocumentoModel.crearRegistroDocumento).toHaveBeenCalledWith(
      "user-1",
      "DOMICILIO",
      "url",
    );
    expect(DocumentoModel.relacionarDocumentoSolicitud).toHaveBeenCalledWith("doc-1", SOLICITUD_ID);
    expect(res.status).toHaveBeenCalledWith(200);
  });

  it("usa INE como tipo por defecto si no se envía tipo", async () => {
    await subirDocumento(crearReq(), crearRes());

    expect(DocumentoModel.crearRegistroDocumento).toHaveBeenCalledWith("user-1", "INE", "url");
  });

  it("responde 404 y no sube nada si la solicitud no existe", async () => {
    DocumentoModel.obtenerSolicitudParaDocumentos.mockRejectedValue(
      new Error("NOT_FOUND_SOLICITUD"),
    );
    const res = crearRes();

    await subirDocumento(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(DocumentoModel.subirArchivoStorage).not.toHaveBeenCalled();
  });
});
