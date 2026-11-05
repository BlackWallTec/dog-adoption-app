import { supabase } from "../../../../config/supabaseClient.js";

class ProgramModel {
  // Solo programas cuya fecha_fin aun no ha pasado.
  // Los programas llenos se incluyen: puede liberarse un lugar.
  static async getCurrentPrograms() {
    // Las fechas de la BD no llevan zona: se comparan con la hora actual de México (zona del refugio)
    const ahora = new Date()
      .toLocaleString("sv-SE", { timeZone: "America/Mexico_City" })
      .replace(" ", "T");

    const { data, error } = await supabase
      .from("programa")
      .select(`
        id_programa,
        tipo,
        nombre,
        estado_del_programa,
        fecha_inicio,
        fecha_fin
      `)
      .gte("fecha_fin", ahora)
      .order("fecha_inicio", { ascending: true });

    if (error) {
      throw new Error(`Error en la consulta de programas: ${error.message}`);
    }

    return data;
  }
}

export default ProgramModel;
