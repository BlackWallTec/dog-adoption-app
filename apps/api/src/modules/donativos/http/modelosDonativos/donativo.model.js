import { createClient } from "@supabase/supabase-js";

class DonativoModel {
  static async getPendientes(accessToken) {
    const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_ANON_KEY, {
      global: {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      },
      auth: {
        persistSession: false,
        autoRefreshToken: false,
      },
    });

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
      throw new Error(`Error al consultar donativos: ${error.message}`);
    }

    return data ?? [];
  }
}

export default DonativoModel;
