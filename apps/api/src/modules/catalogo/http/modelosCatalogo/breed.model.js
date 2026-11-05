import { supabase } from "../../../../config/supabaseClient.js";

class BreedModel {
  static async getBreeds() {
    const { data, error } = await supabase
      .from("raza")
      .select(`*`)
      .order("nombre", { ascending: true });

    if (error) {
      throw new Error(`Error en la consulta de razas: ${error.message}`);
    }

    return data;
  }

  //get breed by id
  static async getBreedById(id) {
    const { data, error } = await supabase.from("raza").select(`*`).eq("id_raza", id).single();

    if (error) {
      throw new Error(`Error en la consulta de raza por ID: ${error.message}`);
    }

    return data;
  }
}
export default BreedModel;
