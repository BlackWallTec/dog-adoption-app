import { describe, expect, it } from "vite-plus/test";
import {
  ErrorValidacionCfdi,
  validarArchivosPdfXml,
} from "../../src/modules/donativos/http/serviciosDonativos/cfdiXml.service.js";

const uuid = "e9bf9f1a-1445-4de3-a372-47fa82fcbd0c";
const xmlValido = `<?xml version="1.0" encoding="UTF-8"?>
<cfdi:Comprobante xmlns:cfdi="http://www.sat.gob.mx/cfd/4" Version="4.0" Total="125.50">
  <cfdi:Receptor Rfc="XAXX010101000"/>
  <cfdi:Complemento>
    <tfd:TimbreFiscalDigital xmlns:tfd="http://www.sat.gob.mx/TimbreFiscalDigital" UUID="${uuid}"/>
  </cfdi:Complemento>
</cfdi:Comprobante>`;

function archivos(xml = xmlValido) {
  return {
    pdf: {
      originalname: "factura.pdf",
      mimetype: "application/pdf",
      size: 9,
      buffer: Buffer.from("%PDF-1.7"),
    },
    xml: {
      originalname: "factura.xml",
      mimetype: "application/xml",
      size: Buffer.byteLength(xml),
      buffer: Buffer.from(xml),
    },
  };
}

const donativo = { monto: 125.5 };
const datosFiscales = { rfc: "XAXX010101000" };

describe("validarArchivosPdfXml", () => {
  it("extrae y valida RFC, monto y UUID del CFDI", () => {
    expect(validarArchivosPdfXml({ ...archivos(), donativo, datosFiscales })).toEqual({
      uuid: uuid.toUpperCase(),
      rfcReceptor: "XAXX010101000",
      total: 125.5,
    });
  });

  it("rechaza si falta uno de los archivos", () => {
    expect(() =>
      validarArchivosPdfXml({ pdf: null, xml: archivos().xml, donativo, datosFiscales }),
    ).toThrow("Adjunta el archivo PDF.");
  });

  it("rechaza archivos que solo aparentan ser PDF", () => {
    const archivosPrueba = archivos();
    archivosPrueba.pdf.buffer = Buffer.from("no es un PDF");

    expect(() => validarArchivosPdfXml({ ...archivosPrueba, donativo, datosFiscales })).toThrow(
      "El archivo adjunto no es un PDF válido.",
    );
  });

  it("rechaza XML con DTD o entidades", () => {
    const xmlMalicioso = `<!DOCTYPE cfdi:Comprobante [<!ENTITY x "x">]>${xmlValido}`;

    expect(() =>
      validarArchivosPdfXml({
        ...archivos(xmlMalicioso),
        donativo,
        datosFiscales,
      }),
    ).toThrow("El XML contiene declaraciones no permitidas.");
  });

  it("rechaza un RFC o un monto que no coincide con los datos del donativo", () => {
    expect(() =>
      validarArchivosPdfXml({
        ...archivos(),
        donativo,
        datosFiscales: { rfc: "COSC8001137NA" },
      }),
    ).toThrow("El RFC receptor del XML no coincide con los datos fiscales del donante.");

    try {
      validarArchivosPdfXml({
        ...archivos(),
        donativo: { monto: 300 },
        datosFiscales,
      });
      throw new Error("Se esperaba un error de validación.");
    } catch (error) {
      expect(error).toBeInstanceOf(ErrorValidacionCfdi);
      expect(error.codigo).toBe("MONTO_NO_COINCIDE");
      expect(error.status).toBe(422);
    }
  });

  it("rechaza XML sin UUID fiscal", () => {
    const xmlSinUuid = xmlValido.replace(` UUID="${uuid}"`, "");

    expect(() =>
      validarArchivosPdfXml({
        ...archivos(xmlSinUuid),
        donativo,
        datosFiscales,
      }),
    ).toThrow("El XML no incluye un UUID de CFDI válido.");
  });
});
