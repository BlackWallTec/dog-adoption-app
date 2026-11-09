import { supabase } from "../../../../config/supabaseClient.js";

export default class CodigoPostal {
  // Todas las colonias de un código postal o [] si no
  static async buscarPorCodigo(codigoPostal) {
    const { data, error } = await supabase
      .from("catalogo_de_cps")
      .select("id_codigo_postal, codigo_postal, colonia, municipio, estado")
      .eq("codigo_postal", codigoPostal)
      .order("colonia", {
        ascending: true,
      });

    if (error) {
      throw new Error(`Error (${error.code}), al consultar códigos postales: ${error.message}`);
    }

    return data;
  }

  // Para el registro, true si la fila existe
  static async existePorId(idCodigoPostal) {
    const { data, error } = await supabase
      .from("catalogo_de_cps")
      .select("id_codigo_postal")
      .eq("id_codigo_postal", idCodigoPostal)
      .maybeSingle();

    if (error) {
      throw new Error(`Error (${error.code}), al consultar el código postal: ${error.message}`);
    }

    return data !== null;
  }
}
