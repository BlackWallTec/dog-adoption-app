import { supabase } from "../../../config/supabaseClient.js";

class SolicitudModel {
  static async getAll() {
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(`
        id_solicitud,
        id_usuario,
        id_perro,
        fecha_solicitud,
        usuario:usuario!solicitud_adopcion_id_usuario_fkey (
          nombre,
          apellidos,
          datos_confidenciales ( correo )
        ),
        perro:perro!solicitud_adopcion_id_perro_fkey (
          id_perro,
          nombre,
          multimedia ( url, tipo )
        ),
        estado:estado_solicitud!solicitud_adopcion_id_estado_fkey (
          descripcion
        )
      `)
      .order("fecha_solicitud", { ascending: true });

    if (error) {
      throw new Error(`Error en la consulta de solicitudes: ${error.message}`);
    }

    return data || [];
  }
}

export default SolicitudModel;
