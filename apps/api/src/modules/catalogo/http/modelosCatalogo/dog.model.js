import { supabase } from "../../../../config/supabaseClient.js";

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
        ),
        multimedia(
            id_multimedia,
            url,
            tipo,
            fecha_subida
        )
    `)
      .eq("activo", true)
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
      .eq("id_perro", id)
      .eq("activo", true);

    if (error) {
      throw new Error(`Error en la consulta de perros: ${error.message}`);
    }

    return data;
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
    const { data, error } = await supabase
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

  static async createFullDog(dogData) {
    const { data, error } = await supabase.rpc("insert_full_dog", { payload: dogData });

    if (error) {
      throw new Error(`Error al crear el perro: ${error.message}`);
    }

    return data[0]; // Retorna el primer elemento del array de datos insertados
  }

  static async desactivarPerro(id) {
    const { data, error } = await supabase
      .from("perro")
      .update({ activo: false })
      .eq("id_perro", id)
      .select("id_perro, nombre, activo")
      .maybeSingle();

    if (error) {
      throw new Error(`Error al desactivar el perro: ${error.message}`);
    }

    if (data) return data;

    const { data: existente, error: errorConsulta } = await supabase
      .from("perro")
      .select("id_perro")
      .eq("id_perro", id)
      .maybeSingle();

    if (errorConsulta) {
      throw new Error(`Error al desactivar el perro: ${errorConsulta.message}`);
    }

    if (existente) {
      throw new Error(
        "Supabase no aplicó el cambio. La política de la tabla perro no permite actualizar activo con la clave anónima.",
      );
    }

    return null;
  }

  static async activarPerroBulk(idList, action) {
    const { data, error } = await supabase
      .from("perro")
      .update({ activo: action })
      .in("id_perro", idList);

    if (error) {
      console.error("Error en desactivarPerroBulk:", error.message);
      throw new Error(`No se pudieron desactivar los registros: ${error.message}`);
    }

    return data;
  }
}

export default DogModel;
