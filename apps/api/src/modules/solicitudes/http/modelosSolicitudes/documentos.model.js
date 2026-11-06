import { supabaseAdmin as supabase } from "../../../../config/supabaseClient.js";

//Upload the physical file to supabase storage and get the link
export const subirArchivoStorage = async (id_solicitud, file) => {
  const fileName = `${id_solicitud}/${Date.now()}_${file.originalname}`;

  const { error: storageError } = await supabase.storage
    .from("documento")
    .upload(fileName, file.buffer, {
      contentType: file.mimetype,
      upsert: true,
    });

  if (storageError) throw storageError;

  const { data: publicUrlData } = supabase.storage.from("documento").getPublicUrl(fileName);

  return publicUrlData.publicUrl;
};

//Get de id of the user and get the request
export const obtenerUsuarioPorSolicitud = async (id_solicitud) => {
  const { data: solicitudData, error: solError } = await supabase
    .from("solicitud_adopcion")
    .select("id_usuario")
    .eq("id_solicitud", id_solicitud)
    .single();

  if (solError || !solicitudData) {
    console.error("Búsqueda de solicitud:", { id_solicitud, solError, solicitudData });
    throw new Error("NOT_FOUND_SOLICITUD");
  }

  return solicitudData.id_usuario;
};

//Insert registry in table documents
export const crearRegistroDocumento = async (id_usuario, tipo, url_archivo) => {
  const { data: docData, error: docError } = await supabase
    .from("documento")
    .insert([
      {
        id_usuario: id_usuario,
        tipo: tipo,
        url_archivo: url_archivo,
        fecha_subida: new Date().toISOString(),
      },
    ])
    .select("id_documento")
    .single();

  if (docError) throw docError;
  return docData.id_documento;
};

//Make dependacies between tables
export const relacionarDocumentoSolicitud = async (id_documento, id_solicitud) => {
  const { error: relError } = await supabase.from("documento_tiene_solicitud_adopcion").insert([
    {
      id_documento: id_documento,
      id_solicitud: id_solicitud,
      fecha_agregado: new Date().toISOString(),
    },
  ]);

  if (relError) throw relError;
};
