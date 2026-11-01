import { randomUUID } from "node:crypto";

const BUCKET = "documentos-solicitud";

//The data base uses INE and Domicilio, while android uses INE and DOMICILIO
const TIPO_A_BD = { INE: "INE", DOMICILIO: "Domicilio" };
const TIPO_DESDE_BD = { INE: "INE", Domicilio: "DOMICILIO" };
const ESTADO_INICIAL = "Pendiente";

function errorHttp(estado, mensaje) {
  const error = new Error(mensaje);
  error.estadoHttp = estado;
  return error;
}

//Takes out accents and special characters, it reduces the link to 60 characters
function limpiarNombre(nombre) {
  // multer returns the file with latin1, without accents
  const utf8 = Buffer.from(nombre, "latin1").toString("utf8");
  return utf8
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^A-Za-z0-9._-]/g, "_")
    .slice(-50);
}

//Security guideline
async function verificarDuenio(db, solicitudId, userId) {
  const { data, error } = await db
    .from("solicitud_adopcion")
    .select("id_solicitud")
    .eq("id_solicitud", solicitudId)
    .eq("id_usuario", userId)
    .maybeSingle();
  if (error) throw error;
  if (!data) throw errorHttp(404, "Solicitud no encontrada.");
}

//replaces the links of the documents befor
async function reemplazarAnteriores(db, solicitudId, tipo, idNuevo) {
  const { data, error } = await db
    .from("documento_tiene_solicitud_adopcion")
    .select("id_documento, documento(tipo)")
    .eq("id_solicitud", solicitudId);
  if (error) throw error;

  const viejos = data
    .filter((f) => f.id_documento !== idNuevo && f.documento?.tipo?.trim() === TIPO_A_BD[tipo])
    .map((f) => f.id_documento);
  if (viejos.length === 0) return;

  const { error: errorBorrado } = await db
    .from("documento_tiene_solicitud_adopcion")
    .delete()
    .eq("id_solicitud", solicitudId)
    .in("id_documento", viejos);
  if (errorBorrado) throw errorBorrado;
}

export async function guardarDocumento(db, { userId, solicitudId, tipo, archivo }) {
  await verificarDuenio(db, solicitudId, userId);
  const nombre = limpiarNombre(archivo.originalname);
  // {id_usuario}/{8 caracteres}-{nombre}: máximo 96, cabe en url_archivo (100)
  const ruta = `${userId}/${randomUUID().slice(0, 8)}-${nombre}`;
  const idDocumento = randomUUID();

  //the file goes to storage
  const { error: errorStorage } = await db.storage
    .from(BUCKET)
    .upload(ruta, archivo.buffer, { contentType: archivo.mimetype });
  if (errorStorage) throw errorStorage;

  //The queue for document
  const { error: errorDoc } = await db.from("documento").insert({
    id_documento: idDocumento,
    id_usuario: userId,
    tipo: TIPO_A_BD[tipo],
    url_archivo: ruta,
    fecha_subida: new Date().toISOString(),
    estado_revision: ESTADO_INICIAL,
  });
  if (errorDoc) {
    await db.storage.from(BUCKET).remove([ruta]);
    throw errorDoc;
  }

  //If the link fails, it disapears
  const { error: errorEnlace } = await db.from("documento_tiene_solicitud_adopcion").insert({
    id_documento: idDocumento,
    id_solicitud: solicitudId,
    fecha_agregado: new Date().toISOString(),
  });
  if (errorEnlace) {
    await db.from("documento").delete().eq("id_documento", idDocumento);
    await db.storage.from(BUCKET).remove([ruta]);
    throw errorEnlace;
  }

  //We change file only if it has been linked
  try {
    await reemplazarAnteriores(db, solicitudId, tipo, idDocumento);
  } catch (error) {
    //We always choose the most recent
    console.error("No se pudo desvincular el documento anterior:", error);
  }

  return { tipo, nombreArchivo: nombre, tamanoBytes: archivo.size };
}

export async function listarDocumentos(db, { userId, solicitudId }) {
  await verificarDuenio(db, solicitudId, userId);

  const { data, error } = await db
    .from("documento_tiene_solicitud_adopcion")
    .select("fecha_agregado, documento(tipo, url_archivo)")
    .eq("id_solicitud", solicitudId)
    .order("fecha_agregado", { ascending: false });
  if (error) throw error;
  const porTipo = new Map();
  for (const fila of data) {
    const tipo = TIPO_DESDE_BD[fila.documento?.tipo?.trim()];
    if (tipo && !porTipo.has(tipo)) {
      porTipo.set(tipo, fila.documento.url_archivo.trim());
    }
  }

  return Promise.all(
    [...porTipo.entries()].map(async ([tipo, ruta]) => {
      const corte = ruta.lastIndexOf("/");
      const carpeta = ruta.slice(0, corte);
      const archivoEnStorage = ruta.slice(corte + 1); // "a1b2c3d4-INE.pdf"
      const nombre = archivoEnStorage.slice(9); // sin los 8 caracteres y el guion

      // El tamaño no está en la tabla: se lee de los metadatos de Storage
      const { data: archivos } = await db.storage
        .from(BUCKET)
        .list(carpeta, { search: archivoEnStorage });
      const archivo = archivos?.find((a) => a.name === archivoEnStorage);

      return {
        tipo,
        nombreArchivo: nombre,
        tamanoBytes: archivo?.metadata?.size ?? 0,
      };
    }),
  );
}
