import { supabase } from "../../../../config/supabaseClient.js";

const ESTADO_INSCRITO = "Aceptado";

class InscripcionModel {
  // Devuelve las inscripciones activas de un programa, en orden de llegada
  // Las inscripciones sin fecha quedan al final
  static async getInscritosByPrograma(idPrograma) {
    const { data, error } = await supabase
      .from("se_inscribe")
      .select("id_usuario, fecha_inscripcion, asistencia")
      .eq("id_programa", idPrograma)
      .eq("estado", ESTADO_INSCRITO)
      .order("fecha_inscripcion", { ascending: true, nullsFirst: false });

    if (error) {
      throw new Error(`Error en la consulta de inscritos: ${error.message}`);
    }

    return data;
  }
}
export default InscripcionModel;
