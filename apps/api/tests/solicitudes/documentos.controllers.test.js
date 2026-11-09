import { describe, it, expect, vi, beforeEach } from "vite-plus/test";

// Simulamos el modelo: no hay Supabase real
vi.mock("../../src/modules/solicitudes/http/modelosSolicitudes/documentos.model.js", () => ({
  obtenerUsuarioPorSolicitud: vi.fn(),
  subirArchivoStorage: vi.fn(),
  crearRegistroDocumento: vi.fn(),
  relacionarDocumentoSolicitud: vi.fn(),
}));

import * as DocumentoModel from "../../src/modules/solicitudes/http/modelosSolicitudes/documentos.model.js";
import { subirDocumento } from "../../src/modules/solicitudes/http/documentos.controller.js";

const ID = "ff028456-acf9-4781-a560-98dd168a5516";

function crearRes() {
  const res = {};
  res.status = vi.fn().mockReturnValue(res);
  res.json = vi.fn().mockReturnValue(res);
  return res;
}

function crearReq({ conArchivo = true, tipo } = {}) {
  return {
    params: { id: ID },
    body: tipo ? { tipo } : {},
    file: conArchivo
      ? {
          originalname: "ine.pdf",
          mimetype: "application/pdf",
          size: 1234,
          buffer: Buffer.from("contenido"),
        }
      : undefined,
  };
}

beforeEach(() => {
  vi.resetAllMocks();
});

describe("subirDocumento (controlador)", () => {
  it("responde 400 si no llega archivo", async () => {
    const res = crearRes();

    await subirDocumento(crearReq({ conArchivo: false }), res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(DocumentoModel.obtenerUsuarioPorSolicitud).not.toHaveBeenCalled();
  });

  it("sube el archivo, crea el registro y lo relaciona con la solicitud", async () => {
    DocumentoModel.obtenerUsuarioPorSolicitud.mockResolvedValue("user-1");
    DocumentoModel.subirArchivoStorage.mockResolvedValue("https://storage/ine.pdf");
    DocumentoModel.crearRegistroDocumento.mockResolvedValue("doc-1");
    DocumentoModel.relacionarDocumentoSolicitud.mockResolvedValue();
    const res = crearRes();

    await subirDocumento(crearReq({ tipo: "DOMICILIO" }), res);

    expect(DocumentoModel.crearRegistroDocumento).toHaveBeenCalledWith(
      "user-1",
      "DOMICILIO",
      "https://storage/ine.pdf",
    );
    expect(DocumentoModel.relacionarDocumentoSolicitud).toHaveBeenCalledWith("doc-1", ID);
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        id_documento: "doc-1",
        url_archivo: "https://storage/ine.pdf",
        fileName: "ine.pdf",
        sizeBytes: 1234,
      }),
    );
  });

  it("usa INE como tipo por defecto si no se envía tipo", async () => {
    DocumentoModel.obtenerUsuarioPorSolicitud.mockResolvedValue("user-1");
    DocumentoModel.subirArchivoStorage.mockResolvedValue("url");
    DocumentoModel.crearRegistroDocumento.mockResolvedValue("doc-1");
    DocumentoModel.relacionarDocumentoSolicitud.mockResolvedValue();

    await subirDocumento(crearReq(), crearRes());

    expect(DocumentoModel.crearRegistroDocumento).toHaveBeenCalledWith("user-1", "INE", "url");
  });

  it("responde 404 y no sube nada si la solicitud no existe", async () => {
    DocumentoModel.obtenerUsuarioPorSolicitud.mockRejectedValue(new Error("NOT_FOUND_SOLICITUD"));
    const res = crearRes();

    await subirDocumento(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(DocumentoModel.subirArchivoStorage).not.toHaveBeenCalled();
  });

  it("responde 500 si falla la subida a storage y no crea registro", async () => {
    DocumentoModel.obtenerUsuarioPorSolicitud.mockResolvedValue("user-1");
    DocumentoModel.subirArchivoStorage.mockRejectedValue(new Error("bucket no existe"));
    const res = crearRes();

    await subirDocumento(crearReq(), res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(DocumentoModel.crearRegistroDocumento).not.toHaveBeenCalled();
  });
});
