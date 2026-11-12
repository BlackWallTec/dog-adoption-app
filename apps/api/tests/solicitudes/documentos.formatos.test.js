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

// Requisito: PDF, JPG o PNG, con hasta 20 MB.
// Se escribe aquí y no se importa del código, para probar el requisito
// y no lo que el código dice que hace.
const LIMITE_BYTES = 20 * 1024 * 1024;

// Así se guardan los tipos en la columna documento.tipo
const TIPOS_DE_DOCUMENTO = ["INE", "Domicilio"];

const FORMATOS_PERMITIDOS = [
  ["PDF", "documento.pdf", "application/pdf"],
  ["JPG", "foto.jpg", "image/jpeg"],
  ["PNG", "imagen.png", "image/png"],
];

const FORMATOS_NO_PERMITIDOS = [
  ["GIF", "animacion.gif", "image/gif"],
  ["WEBP", "foto.webp", "image/webp"],
  ["TXT", "nota.txt", "text/plain"],
  ["DOCX", "carta.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"],
  ["EXE", "programa.exe", "application/octet-stream"],
];

function crearApp() {
  const app = express();
  app.use(express.json());
  app.use("/api/solicitudes", solicitudesRouter);
  return app;
}

function subir({ tipo = "INE", nombre, contentType, tamano }) {
  return request(crearApp())
    .post(`/api/solicitudes/${ID}/documentos`)
    .field("tipo", tipo)
    .attach("archivo", Buffer.alloc(tamano), { filename: nombre, contentType });
}

function nadaSeGuardo() {
  expect(DocumentoModel.subirArchivoStorage).not.toHaveBeenCalled();
  expect(DocumentoModel.crearRegistroDocumento).not.toHaveBeenCalled();
  expect(DocumentoModel.relacionarDocumentoSolicitud).not.toHaveBeenCalled();
}

beforeEach(() => {
  vi.resetAllMocks();
  DocumentoModel.obtenerUsuarioPorSolicitud.mockResolvedValue("user-1");
  DocumentoModel.subirArchivoStorage.mockResolvedValue("https://storage/archivo");
  DocumentoModel.crearRegistroDocumento.mockResolvedValue("doc-1");
  DocumentoModel.relacionarDocumentoSolicitud.mockResolvedValue();
});

describe("Documentos de identificación y comprobante de domicilio", () => {
  describe.each(TIPOS_DE_DOCUMENTO)("%s", (tipo) => {
    it.each(FORMATOS_PERMITIDOS)("acepta archivos %s", async (_formato, nombre, contentType) => {
      const res = await subir({ tipo, nombre, contentType, tamano: 1024 });

      expect(res.status).toBe(200);
      expect(DocumentoModel.crearRegistroDocumento).toHaveBeenCalledWith(
        "user-1",
        tipo,
        "https://storage/archivo",
      );
    });

    it.each(FORMATOS_NO_PERMITIDOS)(
      "rechaza archivos %s con 415 y no guarda nada",
      async (_formato, nombre, contentType) => {
        const res = await subir({ tipo, nombre, contentType, tamano: 1024 });

        expect(res.status).toBe(415);
        expect(res.body.message).toMatch(/PDF, JPG o PNG/);
        nadaSeGuardo();
      },
    );
  });

  describe("tamaño máximo de 20 MB", () => {
    it("acepta un archivo de exactamente 20 MB", { timeout: 30_000 }, async () => {
      const res = await subir({
        nombre: "limite.pdf",
        contentType: "application/pdf",
        tamano: LIMITE_BYTES,
      });

      expect(res.status).toBe(200);
      expect(res.body.sizeBytes).toBe(LIMITE_BYTES);
    });

    it("rechaza 20 MB más un byte con 413 y no guarda nada", { timeout: 30_000 }, async () => {
      const res = await subir({
        nombre: "excedido.pdf",
        contentType: "application/pdf",
        tamano: LIMITE_BYTES + 1,
      });

      expect(res.status).toBe(413);
      expect(res.body.message).toMatch(/20 MB/);
      nadaSeGuardo();
    });
  });

  // Hoy solo se revisa el tipo que declara el cliente, no el contenido real
  it.todo("rechaza un .exe que declara ser application/pdf");
});
