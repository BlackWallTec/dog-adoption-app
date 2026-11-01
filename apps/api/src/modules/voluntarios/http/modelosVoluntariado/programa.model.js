import { supabase } from "../../../../config/supabaseClient.js";

class ProgramaModel {
  // Devuelve el programa o null si no existe
  static async getProgramaById(idPrograma) {
    const { data, error } = await supabase
      .from("programa")
      .select(`
                id_programa,
                nombre,
                tipo,
                fecha_inicio,
                fecha_fin,
                ubicacion,
                cupo_maximo,
                estado_del_programa
                `)
      .eq("id_programa", idPrograma)
      .maybeSingle();

    if (error) {
      throw new Error(`Error en la consulta del programa: ${error.message}`);
    }

    return data;
  }
}
export default ProgramaModel;
