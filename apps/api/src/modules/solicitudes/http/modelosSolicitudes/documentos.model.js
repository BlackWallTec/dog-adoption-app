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

// Sube el archivo físico a Supabase Storage y regresa { url_archivo, storagePath }
export const subirArchivoStorage = async (id_solicitud, file) => {
  const sanitizedName = file.originalname.replace(/[^a-zA-Z0-9._-]/g, "_");
  const fileName = `${id_solicitud}/${Date.now()}_${sanitizedName}`;

  const { error: storageError } = await supabase.storage
    .from(BUCKET_NAME)
    .upload(fileName, file.buffer, {
      contentType: file.mimetype,
      upsert: true,
    });

  if (storageError) throw storageError;

  const { data: publicUrlData } = supabase.storage.from(BUCKET_NAME).getPublicUrl(fileName);

  return {
    url_archivo: publicUrlData.publicUrl,
    storagePath: fileName,
  };
};

export const eliminarArchivoStorage = async (storagePath) => {
  if (!storagePath) return;
  const { error } = await supabase.storage.from(BUCKET_NAME).remove([storagePath]);
  if (error) {
    console.error("No se pudo limpiar el archivo en Storage:", storagePath, error.message);
  }
};

// Obtiene dueño, estado y bandera puede_editar de la solicitud
export const obtenerSolicitudParaDocumentos = async (id_solicitud) => {
  const { data: solicitudData, error: solError } = await supabase
    .from("solicitud_adopcion")
    .select(`
      id_solicitud,
      id_usuario,
      puede_editar,
      estado:estado_solicitud!solicitud_adopcion_id_estado_fkey (
        descripcion
      )
    `)
    .eq("id_solicitud", id_solicitud)
    .maybeSingle();

  if (solError) {
    throw solError;
  }

  if (!solicitudData) {
    throw new Error("NOT_FOUND_SOLICITUD");
  }

  return {
    id_solicitud: solicitudData.id_solicitud,
    id_usuario: solicitudData.id_usuario,
    puede_editar: solicitudData.puede_editar === true,
    estado: first(solicitudData.estado)?.descripcion?.trim() ?? null,
  };
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

// Crea el registro en la tabla documento
export const crearRegistroDocumento = async (id_usuario, tipo, url_archivo) => {
  const { data: docData, error: docError } = await supabase
    .from("documento")
    .insert([
      {
        id_usuario,
        tipo: tipo.trim().toUpperCase(),
        url_archivo,
        fecha_subida: new Date().toISOString(),
      },
    ])
    .select("id_documento")
    .single();

  if (docError) throw docError;
  return docData.id_documento;
};

// Crea la relación en documento_tiene_solicitud_adopcion
export const relacionarDocumentoSolicitud = async (id_documento, id_solicitud) => {
  const { error: relError } = await supabase.from("documento_tiene_solicitud_adopcion").insert([
    {
      id_documento,
      id_solicitud,
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
