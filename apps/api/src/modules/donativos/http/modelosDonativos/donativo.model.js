import { supabaseAdmin } from "../../../../config/supabaseClient.js";

class DonativoModel {
  static async countPendientes() {
    const { count, error } = await supabaseAdmin
      .from("donativo")
      .select("id_donativo", { count: "exact", head: true })
      .eq("estado", "Pendiente de validación");

    if (error) {
      throw new Error(`Error al consultar el total de donativos pendientes: ${error.message}`);
    }

    return count ?? 0;
  }

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
      .order("fecha", { ascending: true });

    if (error) {
      throw new Error(`Error al consultar donativos: ${error.message}`);
    }

    return data ?? [];
  }

  // HU46: el procedimiento vive en Supabase (donativos_registrar_monetario)
  static async registrarMonetario({ idUsuario, monto, referencia, comprobante }) {
    const { data, error } = await supabaseAdmin.rpc("donativos_registrar_monetario", {
      p_id_usuario: idUsuario,
      p_monto: monto,
      p_referencia: referencia,
      p_comprobante: `\\x${comprobante.toString("hex")}`,
    });

    if (error) {
      if (error.code === "23503") return null; // el usuario no existe
      throw new Error(`Error al registrar el donativo: ${error.message}`);
    }

    return data?.[0] ?? null;
  }

  // HU49: los donativos del usuario y si ya pidio su CFDI
  static async getDeUsuario(idUsuario) {
    const { data, error } = await supabaseAdmin
      .from("donativo")
      .select("id_donativo, monto, fecha, estado, solicitud_cfdi ( id_solicitud_cfdi )")
      .eq("id_usuario", idUsuario)
      .order("fecha", { ascending: false });

    if (error) {
      throw new Error(`Error al consultar donativos del usuario: ${error.message}`);
    }

    return (data ?? []).map(({ solicitud_cfdi, ...donativo }) => ({
      ...donativo,
      cfdi_solicitado: Array.isArray(solicitud_cfdi)
        ? solicitud_cfdi.length > 0
        : Boolean(solicitud_cfdi),
    }));
  }
}

export default DonativoModel;
