import { supabase } from "../../../../config/supabaseClient.js";

class CharacterModel {
  static async getCharacters() {
    const { data, error } = await supabase
      .from("caracter")
      .select("id_caracter, nombre")
      .order("nombre", { ascending: true });

    if (error) {
      throw new Error(`Error en la consulta de caracteres: ${error.message}`);
    }

    return data;
  }
}

export default CharacterModel;
