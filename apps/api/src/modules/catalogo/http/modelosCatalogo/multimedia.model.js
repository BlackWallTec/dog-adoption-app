import { supabase } from "../../../../config/supabaseClient.js";

class MultimediaModel {
  static async getDogMultimediaById(id) {
    const { data, error } = await supabase
      .from("multimedia")
      .select(`
        id_multimedia,
        url,
      tipo,
      fecha_subida
    `)
      .eq("id_perro", id)
      .single();

    if (error) {
      throw new Error(`Error en la consulta de multimedia: ${error.message}`);
    }

    return data;
  }
}
export default MultimediaModel;
