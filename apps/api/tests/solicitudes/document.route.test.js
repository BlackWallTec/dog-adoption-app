import { describe, it, expect, vi, beforeEach } from "vite-plus/test";
import express from "express";
import request from "supertest";

// Supabase falso: no se crea cliente real ni se lee .env
vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: {},
  supabaseAdmin: {},
}));

vi.mock("../../src/modules/solicitudes/http/modelosSolicitudes/documentos.model.js", () => ({
  obtenerUsuarioPorSolicitud: vi.fn(),
  subirArchivoStorage: vi.fn(),
  crearRegistroDocumento: vi.fn(),
  relacionarDocumentoSolicitud: vi.fn(),
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
});

describe("POST /api/solicitudes/:id/documentos (ruta)", () => {
  it("está montada: sin archivo responde 400, no 404", async () => {
    const res = await request(crearApp())
      .post(`/api/solicitudes/${ID}/documentos`)
      .field("tipo", "INE");

    expect(res.status).toBe(400);
  });

  it("acepta multipart con campos archivo y tipo, y responde 200", async () => {
    DocumentoModel.obtenerUsuarioPorSolicitud.mockResolvedValue("user-1");
    DocumentoModel.subirArchivoStorage.mockResolvedValue("https://storage/ine.pdf");
    DocumentoModel.crearRegistroDocumento.mockResolvedValue("doc-1");
    DocumentoModel.relacionarDocumentoSolicitud.mockResolvedValue();

    const res = await request(crearApp())
      .post(`/api/solicitudes/${ID}/documentos`)
      .field("tipo", "INE")
      .attach("archivo", Buffer.from("pdf de prueba"), {
        filename: "ine.pdf",
        contentType: "application/pdf",
      });

    expect(res.status).toBe(200);
    expect(res.body.id_documento).toBe("doc-1");
    expect(DocumentoModel.crearRegistroDocumento).toHaveBeenCalledWith(
      "user-1",
      "INE",
      "https://storage/ine.pdf",
    );
  });

  it("responde 404 si la solicitud no existe en la BD", async () => {
    DocumentoModel.obtenerUsuarioPorSolicitud.mockRejectedValue(new Error("NOT_FOUND_SOLICITUD"));

    const res = await request(crearApp())
      .post(`/api/solicitudes/${ID}/documentos`)
      .field("tipo", "INE")
      .attach("archivo", Buffer.from("pdf"), {
        filename: "ine.pdf",
        contentType: "application/pdf",
      });

    expect(res.status).toBe(404);
  });
});
