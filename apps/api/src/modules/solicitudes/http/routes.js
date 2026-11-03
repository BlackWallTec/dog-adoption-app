import { Router } from "express";
//we import multer for files
import multer from "multer";
import { supabase } from "../../../config/supabaseClient.js";

const router = Router();
const upload = multer({ storage: multer.memoryStorage() });

router.post("/:id/documentos", upload.single("archivo"), async (req, res) => {
  try {
    const { id: id_solicitud } = req.params;
    const file = req.file;

    if (!file) {
      return res.status(400).json({ error: "No se recibió ningún archivo" });
    }

    // 1. Subir archivo a Supabase Storage
    const fileName = `${id_solicitud}/${Date.now()}_${file.originalname}`;
    const { error: storageError } = await supabase.storage
      .from("documento")
      .upload(fileName, file.buffer, {
        contentType: file.mimetype,
        upsert: true,
      });

    if (storageError) throw storageError;

    //Obtenemos la URL de supabase storage
    const { data: publicUrlData } = supabase.storage.from("documento").getPublicUrl(fileName);

    //The URL that generates from the file
    const url_archivo = publicUrlData.publicUrl;

    //We search for the id of the user based on the request
    const { data: solicitudData, error: solError } = await supabase
      .from("solicitud_adopcion")
      .select("id_usuario")
      .eq("id_solicitud", id_solicitud)
      .single();

    if (solError || !solicitudData) {
      return res.status(404).json({ error: "No se encontró la solicitud especificada" });
    }

    const id_usuario = solicitudData.id_usuario;

    //We insert in the databse in the table document
    const { data: docData, error: docError } = await supabase
      .from("documento")
      .insert([
        {
          id_usuario: id_usuario,
          tipo: req.body.tipo || "INE",
          //We send the url of the file
          url_archivo: url_archivo,
          fecha_subida: new Date().toISOString(),
        },
      ])
      .select("id_documento")
      .single();

    if (docError) throw docError;

    //Relationship in document has adoption
    const { error: relError } = await supabase.from("documento_tiene_solicitud_adopcion").insert([
      {
        id_documento: docData.id_documento,
        id_solicitud: id_solicitud,
        fecha_agregado: new Date().toISOString(),
      },
    ]);

    if (relError) throw relError;

    //For bugging
    return res.status(200).json({
      message: "Documento subido y registrado exitosamente",
      id_documento: docData.id_documento,
      url_archivo: url_archivo,
    });
  } catch (error) {
    console.error("Error en el proceso:", error.message);
    return res.status(500).json({ error: error.message });
  }
});

export { router };
