import * as DocumentoModel from "./modelosSolicitudes/documentos.model.js";

// Requisito: PDF, JPG o PNG, con hasta 2 MB por archivo
export const MAX_SIZE_BYTES = 2 * 1024 * 1024;
export const TIPOS_MIMES = ["application/pdf", "image/jpeg", "image/png"];

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

const normalizarTipoDocumento = (rawTipo) => {
  const upper = String(rawTipo || "INE")
    .trim()
    .toUpperCase();
  if (upper === "INE" || upper === "IDENTIFICACION") return "INE";
  if (upper === "DOMICILIO" || upper === "COMPROBANTE_DOMICILIO") return "DOMICILIO";
  return null;
};

export const subirDocumento = async (req, res) => {
  let uploadedStoragePath = null;

  try {
    const { id: id_solicitud } = req.params;
    const idUsuarioToken = req.user.uuid;
    const file = req.file;
    const tipo = normalizarTipoDocumento(req.body?.tipo);

    if (!UUID_REGEX.test(String(id_solicitud || ""))) {
      return res.status(400).json({
        success: false,
        message: "El identificador de la solicitud no es válido.",
      });
    }

    if (!tipo) {
      return res.status(400).json({
        success: false,
        message: "Tipo de documento inválido. Usa INE o DOMICILIO.",
      });
    }

    if (!file) {
      return res.status(400).json({
        success: false,
        error: "No se proporcionó ningún archivo",
        message: "No se proporcionó ningún archivo",
      });
    }

    if (!TIPOS_MIMES.includes(file.mimetype)) {
      return res.status(415).json({
        success: false,
        message: "Formato no permitido. Usa PDF, JPG o PNG.",
      });
    }

    if (file.size > MAX_SIZE_BYTES) {
      return res.status(413).json({
        success: false,
        message: "El archivo supera los 2 MB.",
      });
    }

    // Verificar existencia, dueño (req.user.uuid) y que la solicitud sea editable
    const solicitud = await DocumentoModel.obtenerSolicitudParaDocumentos(id_solicitud);

    if (solicitud.id_usuario !== idUsuarioToken) {
      return res.status(403).json({
        success: false,
        message: "No tienes permiso para modificar esta solicitud.",
      });
    }

    if (solicitud.estado !== "Borrador" && !solicitud.puede_editar) {
      return res.status(409).json({
        success: false,
        message: "La solicitud ya fue enviada y no permite modificar documentos.",
      });
    }

    // Buscar documentos previos del mismo tipo (retry-replace)
    const documentosPrevios = await DocumentoModel.obtenerDocumentosPreviosPorTipo(
      id_solicitud,
      tipo,
    );

    // Subir archivo nuevo a Storage
    const { url_archivo, storagePath } = await DocumentoModel.subirArchivoStorage(
      id_solicitud,
      file,
    );
    uploadedStoragePath = storagePath;

    // Registrar documento nuevo y asociarlo a la solicitud
    const id_documento = await DocumentoModel.crearRegistroDocumento(
      idUsuarioToken,
      tipo,
      url_archivo,
    );
    await DocumentoModel.relacionarDocumentoSolicitud(id_documento, id_solicitud);

    // Limpiar documentos anteriores del mismo tipo una vez guardado el nuevo
    if (documentosPrevios.length > 0) {
      await DocumentoModel.eliminarDocumentosReemplazados(id_solicitud, documentosPrevios);
    }

    return res.status(200).json({
      success: true,
      message: "Documento subido y registrado exitosamente",
      id_documento,
      url_archivo,
      fileName: file.originalname,
      sizeBytes: file.size,
      data: {
        id_documento,
        tipo,
        url_archivo,
        nombre_archivo: file.originalname,
        tamano_bytes: file.size,
      },
    });
  } catch (error) {
    if (uploadedStoragePath) {
      await DocumentoModel.eliminarArchivoStorage(uploadedStoragePath);
    }

    console.error("Error en subirDocumento:", error.message);

    if (error.message === "NOT_FOUND_SOLICITUD") {
      return res.status(404).json({
        success: false,
        message: "No se encontró la solicitud",
      });
    }

    return res.status(500).json({
      success: false,
      message: error.message,
    });
  }
};

export const manejarErrorSubida = (error, req, res, next) => {
  if (error.code === "LIMIT_FILE_SIZE") {
    return res.status(413).json({
      success: false,
      message: "El archivo supera los 2 MB.",
    });
  }
  if (error.name === "MulterError") {
    return res.status(400).json({
      success: false,
      message: error.message,
    });
  }
  return next(error);
};
