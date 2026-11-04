import * as DocumentoModel from "./modelosSolicitudes/documentos.model.js";

export const subirDocumento = async (req, res) => {
  try {
    const { id: id_solicitud } = req.params;
    const file = req.file;
    const tipo = req.body.tipo || "INE";

    if (!file) {
      return res.status(400).json({ error: "No se proporcionó ningún archivo" });
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
      return res.status(404).json({ error: "No se encontró la solicitud" });
    }

    return res.status(500).json({ error: error.message });
  }
};
