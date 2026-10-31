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
}
export default BreedModel;
