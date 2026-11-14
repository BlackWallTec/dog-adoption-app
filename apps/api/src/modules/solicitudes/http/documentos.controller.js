import * as DocumentoModel from "./modelosSolicitudes/documentos.model.js";

// Requisito: PDF, JPG o PNG, con hasta 20 MB por archivo
export const MAX_SIZE_BYTES = 20 * 1024 * 1024;
export const TIPOS_DOCUMENTO = ["application/pdf", "image/jpeg", "image/png"];

export const subirDocumento = async (req, res) => {
  try {
    const { id: id_solicitud } = req.params;
    const file = req.file;
    const tipo = req.body.tipo || "INE";

    if (!file) {
      return res.status(400).json({ error: "No se proporcionó ningún archivo" });
    }

    // Reglas del archivo: se validan antes de tocar Storage o la base de datos
    if (!TIPOS_DOCUMENTO.includes(file.mimetype)) {
      return res.status(415).json({ message: "Formato no permitido. Usa PDF, JPG o PNG." });
    }
    if (file.size > MAX_SIZE_BYTES) {
      return res.status(413).json({ message: "El archivo supera los 20 MB." });
    }

    //Get the id of the user based on the request
    const id_usuario = await DocumentoModel.obtenerUsuarioPorSolicitud(id_solicitud);

    //Upload the file to supabase storage
    const url_archivo = await DocumentoModel.subirArchivoStorage(id_solicitud, file);

    //Create data in the table documento
    const id_documento = await DocumentoModel.crearRegistroDocumento(id_usuario, tipo, url_archivo);

    //Safe relatioship between conected tables
    await DocumentoModel.relacionarDocumentoSolicitud(id_documento, id_solicitud);

    //Send OK if this was done correctly
    return res.status(200).json({
      message: "Documento subido y registrado exitosamente",
      id_documento: id_documento,
      url_archivo: url_archivo,
      fileName: file.originalname,
      sizeBytes: file.size,
    });
  } catch (error) {
    console.error("Error en subirDocumento:", error.message);

    if (error.message === "NOT_FOUND_SOLICITUD") {
      return res.status(404).json({ message: "No se encontró la solicitud" });
    }

    return res.status(500).json({ message: error.message });
  }
};

// Traduce los errores que multer lanza ANTES de llegar al controlador
// (por ejemplo, un archivo que pasa del tope). Va al final de la ruta.
export const manejarErrorSubida = (error, req, res, next) => {
  if (error.code === "LIMIT_FILE_SIZE") {
    return res.status(413).json({ message: "El archivo supera los 20 MB." });
  }
  if (error.name === "MulterError") {
    return res.status(400).json({ message: error.message });
  }
  return next(error);
};
