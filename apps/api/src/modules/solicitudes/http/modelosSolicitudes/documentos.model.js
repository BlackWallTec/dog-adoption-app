import { supabaseAdmin as supabase } from "../../../../config/supabaseClient.js";

const BUCKET_NAME = "documento";
const first = (value) => (Array.isArray(value) ? (value[0] ?? null) : (value ?? null));

const extractStoragePath = (urlArchivo) => {
  if (!urlArchivo) return null;
  const marker = `/object/public/${BUCKET_NAME}/`;
  const idx = urlArchivo.indexOf(marker);
  if (idx !== -1) {
    return decodeURIComponent(urlArchivo.slice(idx + marker.length));
  }
  const signedMarker = `/object/sign/${BUCKET_NAME}/`;
  const signedIdx = urlArchivo.indexOf(signedMarker);
  if (signedIdx !== -1) {
    return decodeURIComponent(urlArchivo.slice(signedIdx + signedMarker.length).split("?")[0]);
  }
  return null;
};

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

export const eliminarArchivoStorage = async (storagePath) => {
  if (!storagePath) return;
  const { error } = await supabase.storage.from(BUCKET_NAME).remove([storagePath]);
  if (error) {
    console.error("No se pudo limpiar el archivo en Storage:", storagePath, error.message);
  }
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

// Lista documentos previos del mismo tipo asociados a esta solicitud
export const obtenerDocumentosPreviosPorTipo = async (id_solicitud, tipo) => {
  const { data, error } = await supabase
    .from("documento_tiene_solicitud_adopcion")
    .select(`
      id_documento,
      documento (
        id_documento,
        tipo,
        url_archivo
      )
    `)
    .eq("id_solicitud", id_solicitud);

  if (error) throw error;

  const targetTipo = tipo.trim().toUpperCase();
  return (data || [])
    .map((row) => first(row.documento))
    .filter((doc) => doc && doc.tipo?.trim().toUpperCase() === targetTipo);
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

// Elimina registros y archivos en Storage de documentos reemplazados en retry
export const eliminarDocumentosReemplazados = async (id_solicitud, docsPrevios) => {
  for (const doc of docsPrevios) {
    const { error: relErr } = await supabase
      .from("documento_tiene_solicitud_adopcion")
      .delete()
      .eq("id_solicitud", id_solicitud)
      .eq("id_documento", doc.id_documento);

    if (relErr) {
      console.error("Error al desvincular documento previo:", doc.id_documento, relErr.message);
    }

    const { error: docErr } = await supabase
      .from("documento")
      .delete()
      .eq("id_documento", doc.id_documento);

    if (docErr) {
      console.error("Error al eliminar documento previo:", doc.id_documento, docErr.message);
    }

    const oldPath = extractStoragePath(doc.url_archivo);
    await eliminarArchivoStorage(oldPath);
  }
};
