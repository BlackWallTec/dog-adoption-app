import { supabase, supabaseAdmin } from "../../../../config/supabaseClient.js";

const BUCKET = "multimedia_perros";

// De la url pública de Supabase Storage saca la ruta del archivo dentro del bucket
// (ej. "perros/<id>/123_foto.jpg"). Devuelve null si la url no apunta a este bucket.
export function rutaEnBucket(url) {
  if (typeof url !== "string") return null;
  const marca = `/${BUCKET}/`;
  const inicio = url.indexOf(marca);
  if (inicio === -1) return null;
  const ruta = url.slice(inicio + marca.length).split("?")[0];
  try {
    return decodeURIComponent(ruta) || null;
  } catch {
    return null;
  }
}

class MultimediaModel {
  static async getDogMultimediaById(id) {
    const { data, error } = await supabase
      .from("multimedia")
      .select(`
        id_multimedia,
        url,
      tipo,
      fecha_subida
    `)
      .eq("id_perro", id);

    if (error) {
      throw new Error(`Error en la consulta de multimedia: ${error.message}`);
    }

    return data;
  }

  //POST
  static async addDogMultimedia(id_perro, url, tipo, fecha_subida) {
    const { data, error } = await supabaseAdmin
      .from("multimedia")
      .insert([{ id_perro, url, tipo, fecha_subida }]);

    if (error) {
      throw new Error(`Error al agregar multimedia: ${error.message}`);
    }

    return data;
  }

  // HU19C: agrega varios archivos ya subidos al bucket. `items` es [{ url, tipo }].
  // Omite los que el perro ya tiene, así reenviar la misma edición tras un fallo parcial
  // no duplica fotos.
  static async addDogMultimediaBulk(id_perro, items) {
    const fallo = (error) =>
      Object.assign(new Error(`Error al agregar multimedia: ${error.message}`), {
        code: error.code,
        campo: "multimedia_agregar",
      });

    const { data: existentes, error: errorLectura } = await supabaseAdmin
      .from("multimedia")
      .select("url")
      .eq("id_perro", id_perro)
      .in(
        "url",
        items.map((item) => item.url),
      );
    if (errorLectura) throw fallo(errorLectura);

    const yaEstan = new Set((existentes ?? []).map((fila) => fila.url));
    const fecha_subida = new Date().toISOString();
    const filas = items
      .filter(({ url }) => !yaEstan.has(url))
      .map(({ url, tipo }) => ({ id_perro, url, tipo, fecha_subida }));
    if (filas.length === 0) return;

    const { error } = await supabaseAdmin.from("multimedia").insert(filas);
    if (error) throw fallo(error);
  }

  // HU19C: quita fotos/videos del perro. Filtra también por id_perro para que nadie pueda
  // borrar multimedia de otro perro mandando ids ajenos. Devuelve las filas borradas.
  // El archivo del bucket se borra después y, si falla, no tumba la edición: solo queda
  // un archivo huérfano que se registra en consola.
  static async removeDogMultimedia(id_perro, ids) {
    const { data, error } = await supabaseAdmin
      .from("multimedia")
      .delete()
      .eq("id_perro", id_perro)
      .in("id_multimedia", ids)
      .select("id_multimedia, url");

    if (error) {
      throw Object.assign(new Error(`Error al quitar multimedia: ${error.message}`), {
        code: error.code,
        campo: "multimedia_eliminar",
      });
    }

    const borradas = data ?? [];
    const rutas = borradas.map((fila) => rutaEnBucket(fila.url)).filter(Boolean);
    if (rutas.length > 0) {
      try {
        const { error: errorBucket } = await supabaseAdmin.storage.from(BUCKET).remove(rutas);
        if (errorBucket) throw errorBucket;
      } catch (errorBucket) {
        console.error("No se pudieron borrar archivos del bucket:", rutas, errorBucket?.message);
      }
    }

    return borradas;
  }
}
export default MultimediaModel;
