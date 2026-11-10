import { supabase, supabaseAdmin } from "../../../../config/supabaseClient.js";
import { instanciarError } from "../../../../../../shared/errores.js";

export default class CodigoPostal {
  // Todas las colonias de un código postal o [] si no
  static async buscarPorCodigo(codigoPostal) {
    const { data, error } = await supabaseAdmin
      .from("catalogo_de_cps")
      .select("id_codigo_postal, codigo_postal, colonia, municipio, estado")
      .eq("codigo_postal", codigoPostal)
      .order("colonia", {
        ascending: true,
      });

    if (error) instanciarError("Error al consultar códigos postales", error.code);
    return data;
  }

  // Para el registro, true si la fila existe
  static async existePorId(idCodigoPostal) {
    const { data, error } = await supabaseAdmin
      .from("catalogo_de_cps")
      .select("id_codigo_postal")
      .eq("id_codigo_postal", idCodigoPostal)
      .maybeSingle();

    if (error) instanciarError("Error al consultar códigos postales", error.code);

    return data !== null;
  }
}
