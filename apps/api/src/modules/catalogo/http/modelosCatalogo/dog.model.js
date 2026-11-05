import { supabase, supabaseAdmin } from "../../../../config/supabaseClient.js";

const FICHA_ADMIN = `
  id_perro, nombre, sexo, edad_aprox, tamano_medida, descripcion,
  esterilizado, vacunado, estado, fecha_ingreso,
  raza ( id_raza, nombre ),
  tamano ( id_tamano, nombre )
`;

class DogModel {
  static async getDogs() {
    const { data, error } = await supabase
      .from("perro")
      .select(`
        id_perro,
        nombre,
        sexo,
        edad_aprox,
        tamano_medida,
        descripcion,
        esterilizado,
        vacunado,
        estado,
        fecha_ingreso,
        raza (
            id_raza,
            nombre
        ),
        tamano (
            id_tamano,
            nombre
        )
    `)
      .order("fecha_ingreso", { ascending: false });

    if (error) {
      throw new Error(`Error en la consulta de perros: ${error.message}`);
    }

    return data;
  }

  static async getDogById(id) {
    const { data, error } = await supabase
      .from("perro")
      .select(`
        id_perro,
        nombre,
        sexo,
        edad_aprox,
        tamano_medida,
        descripcion,
        esterilizado,
        vacunado,
        estado,
        fecha_ingreso,
        raza (
            id_raza,
            nombre,
            descripcion
        ),
        tamano (
            id_tamano,
            nombre,
            descripcion
        )
    `)
      .eq("id_perro", id);

    if (error) {
      throw new Error(`Error en la consulta de perros: ${error.message}`);
    }

    return data;
  }
  // Lista pública del catálogo con filtros opcionales (HU24).
  // Solo devuelve perros Disponibles o En proceso de adopción.
  static async getDogsFiltered({ sexo, tamano, raza, edad, caracteres = [] } = {}) {
    let query = supabase
      .from("perro")
      .select(`
        id_perro,
        nombre,
        sexo,
        edad_aprox,
        estado,
        fecha_ingreso,
        raza ( id_raza, nombre ),
        tamano ( id_tamano, nombre ),
        multimedia ( url, tipo, fecha_subida ),
        perro_tiene_caracter ( id_caracter )
      `)
      .in("estado", ["Disponible", "En proceso de adopción"])
      .order("fecha_ingreso", { ascending: false });

    // sexo viene con espacios al final en la BD ("Hembra    "), por eso ilike con comodín
    if (sexo) query = query.ilike("sexo", `${sexo}%`);
    if (tamano) query = query.eq("id_tamano", tamano);
    if (raza) query = query.eq("id_raza", raza);
    if (edad === "cachorro") query = query.lte("edad_aprox", 1);
    if (edad === "adulto") query = query.gte("edad_aprox", 2).lte("edad_aprox", 7);
    if (edad === "senior") query = query.gte("edad_aprox", 8);

    const { data, error } = await query;

    if (error) {
      throw new Error(`Error en la consulta de perros filtrados: ${error.message}`);
    }

    // Carácter: el perro debe tener TODOS los seleccionados
    const filtrados =
      caracteres.length > 0
        ? data.filter((p) =>
            caracteres.every((id) => p.perro_tiene_caracter.some((c) => c.id_caracter === id)),
          )
        : data;

    return filtrados.map((p) => ({
      id_perro: p.id_perro,
      nombre: p.nombre,
      sexo: (p.sexo ?? "").trim(),
      edad_aprox: p.edad_aprox,
      estado: p.estado,
      raza: p.raza,
      tamano: p.tamano,
      foto_url: p.multimedia?.[0]?.url ?? "",
    }));
  }

  // HU19C: ficha completa para el formulario de edición (admin). Devuelve null si no existe.
  static async getDogAdminById(id) {
    const { data, error } = await supabase
      .from("perro")
      .select(FICHA_ADMIN)
      .eq("id_perro", id)
      .maybeSingle();

    if (error) {
      throw Object.assign(new Error(`Error en la consulta del perro: ${error.message}`), {
        code: error.code,
      });
    }

    return data;
  }

  // HU19C: actualiza solo los campos recibidos. Devuelve la ficha actualizada o null si no existe.
  static async updateDog(id, valores) {
    const { data, error } = await supabaseAdmin
      .from("perro")
      .update(valores)
      .eq("id_perro", id)
      .select(FICHA_ADMIN)
      .maybeSingle();

    if (error) {
      throw Object.assign(new Error(`Error al actualizar el perro: ${error.message}`), {
        code: error.code,
      });
    }

    return data;
  }
}

export default DogModel;
