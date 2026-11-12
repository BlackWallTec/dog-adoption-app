import { supabase } from "../../../../config/supabaseClient.js";

// Lectura pública de HU22.
// final_feliz.id_perro referencia perro.id_perro para traer nombre y estado.
class FinalFelizModel {
  static async getPublicados() {
    const { data, error } = await supabase
      .from("final_feliz")
      .select(`
        id_perro,
        foto_hogar_url,
        historia,
        fecha_adopcion,
        publicado,
        perro (
          nombre,
          estado
        )
      `)
      .eq("publicado", true);

    if (error) {
      throw new Error(`Error en la consulta de finales felices: ${error.message}`);
    }

    return data ?? [];
  }
}

export default FinalFelizModel;
