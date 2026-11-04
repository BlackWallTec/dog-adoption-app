import { supabase } from "../../../../config/supabaseClient.js";

class SpecialModel {
  static async getDogSpecialConditionById(id) {
    const { data, error } = await supabase
      .from("perro")
      .select(`
      id_perro,
      perro_tiene_cuidado_especial(
      cuidado_especial(
        id_cuidado,
        descripcion
      )
    )
  `)
      .eq("id_perro", id)
      .single();

    if (error) {
      throw new Error(`Error en la consulta de cuidados especiales: ${error.message}`);
    }

    return data;
  }

  // POST
  static async addDogSpecialCondition(id_perro, id_cuidado) {
    const { data, error } = await supabase
      .from("perro_tiene_cuidado_especial")
      .insert([{ id_perro, id_cuidado }]);

    if (error) {
      throw new Error(`Error al agregar cuidado especial: ${error.message}`);
    }

    return data;
  }
}
export default SpecialModel;
