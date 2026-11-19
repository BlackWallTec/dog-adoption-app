import { XMLParser, XMLValidator } from "fast-xml-parser";

const MAX_PDF_BYTES = 10 * 1024 * 1024;
const MAX_XML_BYTES = 5 * 1024 * 1024;
const TIPOS_XML = new Set(["application/xml", "text/xml"]);
const TIPOS_PDF = new Set(["application/pdf"]);

const parser = new XMLParser({
  ignoreAttributes: false,
  attributeNamePrefix: "@_",
  processEntities: false,
  parseAttributeValue: false,
  parseTagValue: false,
  trimValues: true,
});

export class ErrorValidacionCfdi extends Error {
  constructor(codigo, mensaje, status = 400) {
    super(mensaje);
    this.name = "ErrorValidacionCfdi";
    this.codigo = codigo;
    this.status = status;
  }
}

function validarArchivo(file, tipo, extensiones, maxBytes, etiqueta) {
  if (!file?.buffer?.length) {
    throw new ErrorValidacionCfdi(`FALTA_${etiqueta}`, `Adjunta el archivo ${etiqueta}.`);
  }

  const extension = file.originalname?.split(".").pop()?.toLowerCase();
  if (!extensiones.includes(extension) || !tipo.has(file.mimetype)) {
    throw new ErrorValidacionCfdi(
      `FORMATO_${etiqueta}`,
      `El archivo ${etiqueta} debe tener formato ${extensiones.join(" o ").toUpperCase()}.`,
      415,
    );
  }

  if (file.size > maxBytes || file.buffer.length > maxBytes) {
    throw new ErrorValidacionCfdi(
      `TAMANO_${etiqueta}`,
      `El archivo ${etiqueta} supera el límite permitido.`,
      413,
    );
  }
}

function buscarNodosPorNombreLocal(valor, nombreBuscado, encontrados = []) {
  if (!valor || typeof valor !== "object") return encontrados;

  for (const [nombre, contenido] of Object.entries(valor)) {
    if (nombre.split(":").pop() === nombreBuscado) {
      encontrados.push(contenido);
    }
    if (Array.isArray(contenido)) {
      for (const elemento of contenido) {
        buscarNodosPorNombreLocal(elemento, nombreBuscado, encontrados);
      }
    } else {
      buscarNodosPorNombreLocal(contenido, nombreBuscado, encontrados);
    }
  }

  return encontrados;
}

function obtenerNodoUnico(origen, nombre, etiqueta) {
  const nodos = buscarNodosPorNombreLocal(origen, nombre);
  if (nodos.length !== 1 || !nodos[0] || typeof nodos[0] !== "object") {
    throw new ErrorValidacionCfdi("XML_ESTRUCTURA", `El XML no contiene un ${etiqueta} válido.`);
  }
  return nodos[0];
}

function obtenerAtributo(nodo, nombre) {
  return nodo[`@_${nombre}`];
}

function compararDatosCfdi(datos, donativo, datosFiscales) {
  const rfcEsperado = datosFiscales?.rfc?.trim().toUpperCase();
  if (!rfcEsperado || datos.rfcReceptor !== rfcEsperado) {
    throw new ErrorValidacionCfdi(
      "RFC_NO_COINCIDE",
      "El RFC receptor del XML no coincide con los datos fiscales del donante.",
      422,
    );
  }

  const montoDonativo = Number(donativo?.monto);
  if (!Number.isFinite(montoDonativo) || Math.abs(datos.total - montoDonativo) > 0.01) {
    throw new ErrorValidacionCfdi(
      "MONTO_NO_COINCIDE",
      "El total del XML no coincide con el monto del donativo.",
      422,
    );
  }
}

export function validarArchivosPdfXml({ pdf, xml, donativo, datosFiscales }) {
  validarArchivo(pdf, TIPOS_PDF, ["pdf"], MAX_PDF_BYTES, "PDF");
  if (pdf.buffer.subarray(0, 5).toString("ascii") !== "%PDF-") {
    throw new ErrorValidacionCfdi("PDF_INVALIDO", "El archivo adjunto no es un PDF válido.");
  }

  validarArchivo(xml, TIPOS_XML, ["xml"], MAX_XML_BYTES, "XML");
  const contenidoXml = xml.buffer
    .toString("utf8")
    .replace(/^\uFEFF/, "")
    .trim();
  if (/<!DOCTYPE|<!ENTITY/i.test(contenidoXml)) {
    throw new ErrorValidacionCfdi(
      "XML_ENTIDADES_NO_PERMITIDAS",
      "El XML contiene declaraciones no permitidas.",
    );
  }

  const resultadoValidacion = XMLValidator.validate(contenidoXml);
  if (resultadoValidacion !== true) {
    throw new ErrorValidacionCfdi("XML_INVALIDO", "El XML del CFDI está mal formado.");
  }

  let documento;
  try {
    documento = parser.parse(contenidoXml);
  } catch {
    throw new ErrorValidacionCfdi("XML_INVALIDO", "No fue posible leer el XML del CFDI.");
  }

  const comprobante = obtenerNodoUnico(documento, "Comprobante", "comprobante");
  const receptor = obtenerNodoUnico(comprobante, "Receptor", "receptor");
  const timbre = obtenerNodoUnico(comprobante, "TimbreFiscalDigital", "timbre fiscal");
  const version = obtenerAtributo(comprobante, "Version");
  const rfcReceptor = obtenerAtributo(receptor, "Rfc")?.trim().toUpperCase();
  const totalTexto = obtenerAtributo(comprobante, "Total");
  const total = Number(totalTexto);
  const uuid = obtenerAtributo(timbre, "UUID")?.trim().toUpperCase();

  if (!["3.3", "4.0"].includes(version)) {
    throw new ErrorValidacionCfdi("CFDI_VERSION", "La versión del CFDI no es compatible.");
  }
  if (!rfcReceptor) {
    throw new ErrorValidacionCfdi("RFC_XML", "El XML no incluye el RFC del receptor.");
  }
  if (!Number.isFinite(total) || total < 0) {
    throw new ErrorValidacionCfdi("MONTO_XML", "El XML no incluye un total válido.");
  }
  if (!uuid || !/^[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$/.test(uuid)) {
    throw new ErrorValidacionCfdi("UUID_XML", "El XML no incluye un UUID de CFDI válido.");
  }

  const datos = { uuid, rfcReceptor, total };
  compararDatosCfdi(datos, donativo, datosFiscales);
  return datos;
}

export const LIMITES_ARCHIVOS_CFDI = {
  pdf: MAX_PDF_BYTES,
  xml: MAX_XML_BYTES,
};
