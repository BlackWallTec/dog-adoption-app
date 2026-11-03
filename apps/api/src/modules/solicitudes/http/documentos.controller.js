//Controller to upload documents
export const subirDocumento = async (req, res) => {
  try {
    const { id } = req.params;
    const file = req.file;

    //if a file was not sent, we send a mistake
    if (!file) {
      return res.status(400).json({ error: "No se proporcionó ningún archivo" });
    }

    //Lo que va mandar de logs cuando se subió correctamente el documento
    return res.status(200).json({
      message: "Documento subido correctamente",
      id_documento: docData.id_documento,
      url_archivo: url_archivo,
      fileName: file.originalname,
      sizeBytes: file.size,
    });
  } catch (error) {
    return res.status(500).json({ error: error.message });
  }
};
