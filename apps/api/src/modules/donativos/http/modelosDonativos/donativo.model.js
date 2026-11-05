import { supabaseAdmin } from "../../../../config/supabaseClient.js";

class DonativoModel {
  static async getPendientes() {
    const { data, error } = await supabaseAdmin
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
      throw new Error(`Error al consultar donativos: ${error.message}`);
    }

    return data ?? [];
  }
}

export default DonativoModel;
