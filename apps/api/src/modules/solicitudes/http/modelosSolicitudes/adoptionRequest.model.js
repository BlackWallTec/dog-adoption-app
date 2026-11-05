// Usamos el cliente admin del equipo (salta RLS; solo vive en el servidor)
import { supabaseAdmin as supabase } from "../../../../config/supabaseAdmin.js";

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
}

export default AdoptionRequestModel;
