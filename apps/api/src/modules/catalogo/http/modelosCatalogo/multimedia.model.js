import { supabaseAdmin as supabase } from "../../../../config/supabaseClient.js";

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
      .eq("id_perro", id);

    if (error) {
      throw new Error(`Error en la consulta de multimedia: ${error.message}`);
    }

    return data;
  }

  //POST
  static async addDogMultimedia(id_perro, url, tipo, fecha_subida) {
    const { data, error } = await supabase
      .from("multimedia")
      .insert([{ id_perro, url, tipo, fecha_subida }]);

    if (error) {
      throw new Error(`Error al agregar multimedia: ${error.message}`);
    }

    return data;
  }
}
export default MultimediaModel;
