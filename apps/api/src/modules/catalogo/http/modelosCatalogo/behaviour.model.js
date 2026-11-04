import { supabase } from "../../../../config/supabaseClient.js";

class BehaviourModel {
  static async getDogBehaviourById(id) {
    const { data, error } = await supabase
      .from("perro")
      .select(`
        id_perro,
        perro_tiene_caracter (
          caracter (
            id_caracter,
            nombre,
            descripcion
          )
        )
      )
    `)
      .eq("id_perro", id)
      .single();

    if (error) {
      throw new Error(`Error en la consulta de comportamientos: ${error.message}`);
    }

    return data;
  }

  // POST

  static async addDogBehaviour(id_perro, id_caracter) {
    const { data, error } = await supabase
      .from("perro_tiene_caracter")
      .insert([{ id_perro, id_caracter }]);

    if (error) {
      throw new Error(`Error al agregar comportamiento: ${error.message}`);
    }

    return data;
  }
}

export default BehaviourModel;
