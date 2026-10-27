import { supabase } from "../../../../config/supabaseClient.js";

class SpecialModel {
  static async getDogSpecialConditionById(id) {
    const { data, error } = await supabase
      .from("perro")
      .select(`
      id_perro,
      perro_tiene_cuidado_especial(
      cuidado_especial(
        id_cuidado_especial,
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
}
export default SpecialModel;
