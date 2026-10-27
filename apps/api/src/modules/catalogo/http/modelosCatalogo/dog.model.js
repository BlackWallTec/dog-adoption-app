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
}

export default DogModel;
