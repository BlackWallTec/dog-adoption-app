import CfdiModel, { ErrorFlujoCfdi } from "./modelosDonativos/cfdi.model.js";
import {
  ErrorValidacionCfdi,
  LIMITES_ARCHIVOS_CFDI,
  validarArchivosPdfXml,
} from "./serviciosDonativos/cfdiXml.service.js";
import { enviarCfdiPorCorreo } from "./serviciosDonativos/cfdiMail.service.js";

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function evitarCache(res) {
  res.set("Cache-Control", "private, no-store");
}

function obtenerArchivos(req) {
  return {
    pdf: req.files?.pdf?.[0],
    xml: req.files?.xml?.[0],
  };
}

function responderError(res, error, contexto) {
  if (error instanceof ErrorFlujoCfdi || error instanceof ErrorValidacionCfdi) {
    return res.status(error.status).json({ error: error.message, codigo: error.codigo });
  }

  console.error(contexto, error);
  return res.status(500).json({ error: "No fue posible procesar el CFDI." });
}

function validarIdSolicitud(res, idSolicitud) {
  if (UUID_REGEX.test(idSolicitud)) return true;
  res.status(400).json({ error: "Identificador de solicitud inválido." });
  return false;
}

function nombreDonante(donativo) {
  const usuario = Array.isArray(donativo?.usuario) ? donativo.usuario[0] : donativo?.usuario;
  return [usuario?.nombre, usuario?.apellidos].filter(Boolean).join(" ");
}

function normalizarErrorCorreo(error) {
  console.error("No se pudo enviar el CFDI por correo:", error);
  return "No se pudo enviar el correo. Los archivos siguen disponibles en la app.";
}

async function enviarCorreoDeSolicitud(idSolicitud, contexto, pdf, xml, uuid, res, status) {
  await CfdiModel.iniciarIntentoCorreo(idSolicitud);

  try {
    await enviarCfdiPorCorreo({
      destinatario: contexto.datosFiscales.correo_facturacion,
      nombre: nombreDonante(contexto.donativo),
      uuid,
      pdf,
      xml,
    });
  } catch (error) {
    const mensaje = normalizarErrorCorreo(error);
    await CfdiModel.fallarIntentoCorreo(idSolicitud);
    return res.status(status).json({
      almacenado: true,
      correo: { estado: "Error", mensaje },
    });
  }

  await CfdiModel.completarIntentoCorreo(idSolicitud);
  return res.status(status).json({
    almacenado: true,
    correo: { estado: "Enviado" },
  });
}

export async function validarCargaCfdi(req, res) {
  evitarCache(res);
  if (!validarIdSolicitud(res, req.params.idSolicitud)) return;

  try {
    const contexto = await CfdiModel.obtenerContextoParaCarga(req.params.idSolicitud);
    const archivos = obtenerArchivos(req);
    const datos = validarArchivosPdfXml({ ...archivos, ...contexto });
    await CfdiModel.verificarUuidDisponible(datos.uuid, req.params.idSolicitud);

    return res.status(200).json({
      valido: true,
      uuid: datos.uuid,
      rfc_receptor: datos.rfcReceptor,
      monto: datos.total,
      donativo: {
        id_donativo: contexto.donativo.id_donativo,
        monto: Number(contexto.donativo.monto),
        rfc: contexto.datosFiscales.rfc,
      },
    });
  } catch (error) {
    return responderError(res, error, "Error al validar los archivos CFDI:");
  }
}

export async function confirmarCargaCfdi(req, res) {
  evitarCache(res);
  if (!validarIdSolicitud(res, req.params.idSolicitud)) return;
  if (req.body?.confirmar_pdf !== "true") {
    return res.status(400).json({
      error: "Confirma que revisaste el PDF y que corresponde al CFDI del XML.",
      codigo: "CONFIRMACION_PDF_REQUERIDA",
    });
  }

  try {
    const contexto = await CfdiModel.obtenerContextoParaCarga(req.params.idSolicitud);
    const archivos = obtenerArchivos(req);
    const datos = validarArchivosPdfXml({ ...archivos, ...contexto });
    await CfdiModel.verificarUuidDisponible(datos.uuid, req.params.idSolicitud);

    const guardado = await CfdiModel.guardarArchivos({
      idSolicitud: req.params.idSolicitud,
      idAdministrador: req.user.uuid,
      datos,
      ...archivos,
    });

    return await enviarCorreoDeSolicitud(
      req.params.idSolicitud,
      guardado,
      archivos.pdf.buffer,
      archivos.xml.buffer,
      datos.uuid,
      res,
      201,
    );
  } catch (error) {
    return responderError(res, error, "Error al guardar el CFDI:");
  }
}

export async function reintentarCorreoCfdi(req, res) {
  evitarCache(res);
  if (!validarIdSolicitud(res, req.params.idSolicitud)) return;

  try {
    const archivos = await CfdiModel.obtenerArchivosParaReenvio(req.params.idSolicitud);

    return await enviarCorreoDeSolicitud(
      req.params.idSolicitud,
      archivos,
      archivos.pdf,
      archivos.xml,
      archivos.solicitud.cfdi_uuid,
      res,
      200,
    );
  } catch (error) {
    return responderError(res, error, "Error al reintentar el envío del CFDI:");
  }
}

export async function listarCfdiCliente(req, res) {
  evitarCache(res);

  try {
    const cfdi = await CfdiModel.listarDelCliente(req.user.uuid);
    return res.status(200).json(cfdi);
  } catch (error) {
    return responderError(res, error, "Error al consultar CFDI del donante:");
  }
}

export async function descargarCfdiCliente(req, res) {
  evitarCache(res);
  res.set("X-Content-Type-Options", "nosniff");

  if (!validarIdSolicitud(res, req.params.idSolicitud)) return;
  if (!["pdf", "xml"].includes(req.params.tipoArchivo)) {
    return res.status(400).json({ error: "Tipo de archivo inválido." });
  }

  try {
    const archivo = await CfdiModel.descargarParaCliente(
      req.params.idSolicitud,
      req.user.uuid,
      req.params.tipoArchivo,
    );
    if (!archivo) return res.status(404).json({ error: "No se encontró el CFDI solicitado." });

    const uuid = UUID_REGEX.test(archivo.uuid) ? archivo.uuid : "comprobante";
    res.type(archivo.contentType);
    res.set("Content-Disposition", `attachment; filename="CFDI-${uuid}.${req.params.tipoArchivo}"`);
    return res.status(200).send(archivo.buffer);
  } catch (error) {
    return responderError(res, error, "Error al descargar el CFDI:");
  }
}

export function manejarErrorSubidaCfdi(error, req, res, next) {
  if (error?.code === "LIMIT_FILE_SIZE") {
    return res.status(413).json({
      error: `El archivo supera el límite de ${LIMITES_ARCHIVOS_CFDI.pdf / (1024 * 1024)} MB.`,
    });
  }
  if (error?.name === "MulterError") {
    return res.status(400).json({ error: "Adjunta únicamente un PDF y un XML." });
  }
  return next(error);
}
