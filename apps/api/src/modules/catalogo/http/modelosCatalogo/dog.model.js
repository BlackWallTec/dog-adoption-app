import { supabase } from "../../../../config/supabaseClient.js";

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

  // Función para crear un nuevo perro
  // 1. Inserción en tabla Perro: (Incluye Raza & Tamaño)
  // 2. Inserción en tabla Perro_Imagen: (Incluye id_perro & url_imagen)
  // 3. Inserción en tabla perro_tiene_caracter (Incluye id_perro & id_caracter)
  // 4. Inserción en tabla perro_tiene_cuidado_especial (Incluye id_perro & id_cuidado_especial)
  static async createFullDog(dogData) {
    const { data, error } = await supabase.rpc("insert_full_dog", { payload: dogData });

    if (error) {
      throw new Error(`Error al crear el perro: ${error.message}`);
    }

    return data[0]; // Retorna el primer elemento del array de datos insertados
  }

  /*




  */
}

export default DogModel;
