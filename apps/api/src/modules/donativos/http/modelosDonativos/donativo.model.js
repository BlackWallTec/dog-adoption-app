import { supabase } from "../../../../config/supabaseClient.js";

class DonativoModel {
  static async getPendientes() {
    const { data, error } = await supabase
      .from("donativo")
      .select(`
                id_donativo,
                monto,
                fecha,
                estado,
                usuario (
                    nombre,
                    apellidos
                )
            `)
      .eq("estado", "Pendiente de validación")
      .order("fecha", { ascending: false });

    if (error) {
      console.error("Error de Supabase en HU44:", error);
      throw error;
    }

    return data ?? [];
  }
}

export default DonativoModel;
