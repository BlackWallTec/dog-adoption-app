import { supabase } from "../../../../config/supabaseClient.js";

class SizeModel {
  static async getSizes() {
    const { data, error } = await supabase
      .from("tamano")
      .select(`*`)
      .order("nombre", { ascending: true });

    if (error) {
      throw new Error(`Error en la consulta de tamaños: ${error.message}`);
    }

    return data;
  }
}
export default SizeModel;
