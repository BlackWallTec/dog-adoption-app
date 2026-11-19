import { describe, it, expect, vi, beforeEach } from "vite-plus/test";
import express from "express";
import request from "supertest";

vi.mock("../../src/middleware/is-auth.js", () => ({
  default: (req, _res, next) => {
    req.user = { uuid: "user-1" };
    next();
  },
}));

// Supabase falso: no se crea cliente real ni se lee .env
vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: {},
  supabaseAdmin: {},
}));

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
import { router as solicitudesRouter } from "../../src/modules/solicitudes/index.js";

const ID = "ff028456-acf9-4781-a560-98dd168a5516";

function crearApp() {
  const app = express();
  app.use(express.json());
  app.use("/api/solicitudes", solicitudesRouter);
  return app;
}

beforeEach(() => {
  vi.resetAllMocks();
  DocumentoModel.obtenerSolicitudParaDocumentos.mockResolvedValue({
    id_solicitud: ID,
    id_usuario: "user-1",
    puede_editar: true,
    estado: "Borrador",
  });
  DocumentoModel.obtenerDocumentosPreviosPorTipo.mockResolvedValue([]);
  DocumentoModel.subirArchivoStorage.mockResolvedValue({
    url_archivo: "https://storage/ine.pdf",
    storagePath: `${ID}/123_ine.pdf`,
  });
  DocumentoModel.crearRegistroDocumento.mockResolvedValue("doc-1");
  DocumentoModel.relacionarDocumentoSolicitud.mockResolvedValue();
  DocumentoModel.eliminarDocumentosReemplazados.mockResolvedValue();
  DocumentoModel.eliminarArchivoStorage.mockResolvedValue();
});

describe("POST /api/solicitudes/:id/documentos (ruta)", () => {
  it("está montada: sin archivo responde 400, no 404", async () => {
    const res = await request(crearApp())
      .post(`/api/solicitudes/${ID}/documentos`)
      .field("tipo", "INE");

    expect(res.status).toBe(400);
  });

  it("acepta multipart con campos archivo y tipo, y responde 200", async () => {
    const res = await request(crearApp())
      .post(`/api/solicitudes/${ID}/documentos`)
      .field("tipo", "INE")
      .attach("archivo", Buffer.from("pdf de prueba"), {
        filename: "ine.pdf",
        contentType: "application/pdf",
      });

    expect(res.status).toBe(200);
    expect(res.body.id_documento).toBe("doc-1");
    expect(res.body.data).toEqual({
      id_documento: "doc-1",
      tipo: "INE",
      url_archivo: "https://storage/ine.pdf",
      nombre_archivo: "ine.pdf",
      tamano_bytes: Buffer.from("pdf de prueba").length,
    });
    expect(DocumentoModel.crearRegistroDocumento).toHaveBeenCalledWith(
      "user-1",
      "INE",
      "https://storage/ine.pdf",
    );
  });

  it("responde 404 si la solicitud no existe en la BD", async () => {
    DocumentoModel.obtenerSolicitudParaDocumentos.mockRejectedValue(
      new Error("NOT_FOUND_SOLICITUD"),
    );

    const res = await request(crearApp())
      .post(`/api/solicitudes/${ID}/documentos`)
      .field("tipo", "INE")
      .attach("archivo", Buffer.from("pdf"), {
        filename: "ine.pdf",
        contentType: "application/pdf",
      });

    expect(res.status).toBe(404);
  });

  it("responde 403 si la solicitud pertenece a otro usuario", async () => {
    DocumentoModel.obtenerSolicitudParaDocumentos.mockResolvedValue({
      id_solicitud: ID,
      id_usuario: "otro-usuario",
      puede_editar: true,
      estado: "Borrador",
    });

    const res = await request(crearApp())
      .post(`/api/solicitudes/${ID}/documentos`)
      .field("tipo", "INE")
      .attach("archivo", Buffer.from("pdf"), {
        filename: "ine.pdf",
        contentType: "application/pdf",
      });

    expect(res.status).toBe(403);
    expect(DocumentoModel.subirArchivoStorage).not.toHaveBeenCalled();
  });

  it("reemplaza el documento anterior si se vuelve a subir el mismo tipo (retry-replace)", async () => {
    const prevDocs = [
      { id_documento: "doc-viejo", tipo: "INE", url_archivo: "https://storage/viejo.pdf" },
    ];
    DocumentoModel.obtenerDocumentosPreviosPorTipo.mockResolvedValue(prevDocs);

    const res = await request(crearApp())
      .post(`/api/solicitudes/${ID}/documentos`)
      .field("tipo", "INE")
      .attach("archivo", Buffer.from("pdf nuevo"), {
        filename: "ine_nuevo.pdf",
        contentType: "application/pdf",
      });

    expect(res.status).toBe(200);
    expect(DocumentoModel.eliminarDocumentosReemplazados).toHaveBeenCalledWith(ID, prevDocs);
  });
});
