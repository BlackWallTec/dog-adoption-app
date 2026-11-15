import { supabaseAdmin as supabase } from "../../../../config/supabaseClient.js";

// Estados que NO cuentan como solicitud activa
const ESTADOS_INACTIVOS = ["Rechazada", "Borrador"];

class AdoptionRequestModel {
  // Busca el uuid de un estado por su nombre (Lucca lo usará: "Pendiente de Revisión")
  static async getStatusId(descripcion) {
    const { data, error } = await supabase
      .from("estado_solicitud")
      .select("id_estado")
      .eq("descripcion", descripcion)
      .single();

    if (error) {
      throw new Error(`Error en la consulta del estado: ${error.message}`);
    }

    return data.id_estado;
  }

  // ¿Tiene este usuario una solicitud activa para este perro? (null si no)
  static async getActiveRequest(idUsuario, idPerro) {
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(`
        id_solicitud,
        estado_solicitud (
          descripcion
        )
      `)
      .eq("id_usuario", idUsuario)
      .eq("id_perro", idPerro);

    if (error) {
      throw new Error(`Error en la consulta de solicitudes: ${error.message}`);
    }

    // Activa = cualquiera que no esté rechazada (ni en el Borrador viejo)
    const activa = data.find(
      (fila) => !ESTADOS_INACTIVOS.includes(fila.estado_solicitud?.descripcion),
    );

    return activa ?? null;
  }

  // Solicitudes ya enviadas de un usuario (de la más reciente a la más antigua)
  // idUsuario siempre sale del token, no de la url.
  static async getByUser(idUsuario) {
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(`
      id_solicitud,
      fecha_solicitud,
      comentario_admin,
      perro:perro!solicitud_adopcion_id_perro_fkey (
        nombre,
        multimedia ( url, tipo )
        ),
        estado:estado_solicitud!solicitud_adopcion_id_estado_fkey (
          descripcion
        ),
        documentos:documento_tiene_solicitud_adopcion (
          documento ( estado_revision)
        )
      `)
      .eq("id_usuario", idUsuario)
      .order("fecha_solicitud", { ascending: false });

    if (error) {
      throw new Error(`Error en la consulta de mis solicitudes: ${error.message}`);
    }

    return data || [];
  }
}
export default AdoptionRequestModel;
