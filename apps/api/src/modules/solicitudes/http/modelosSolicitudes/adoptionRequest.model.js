import { supabase } from "../../../../config/supabaseClient.js";

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
      (fila) => !ESTADOS_INACTIVOS.includes(fila.estado_solicitud?.descripcion?.trim()),
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
      puede_editar,
      perro:perro!solicitud_adopcion_id_perro_fkey (
        nombre,
        multimedia ( url, tipo )
        ),
        estado:estado_solicitud!solicitud_adopcion_id_estado_fkey (
          descripcion
        )
      `)
      .eq("id_usuario", idUsuario)
      .order("fecha_solicitud", { ascending: false });

    if (error) {
      throw new Error(`Error en la consulta de mis solicitudes: ${error.message}`);
    }

    return data || [];
  }

  static async getDraftRequest(idUsuario, idPerro) {
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(`
        *,
        estado_solicitud (
          descripcion
        )
      `)
      .eq("id_usuario", idUsuario)
      .eq("id_perro", idPerro);

    if (error) {
      throw new Error(`Error en la consulta del borrador: ${error.message}`);
    }

    return data.find((fila) => fila.estado_solicitud?.descripcion?.trim() === "Borrador") ?? null;
  }

  static async getSubmittedRequest(idUsuario, idPerro) {
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(`
        *,
        estado_solicitud (
          descripcion
        )
      `)
      .eq("id_usuario", idUsuario)
      .eq("id_perro", idPerro);

    if (error) {
      throw new Error(`Error en la consulta de solicitudes: ${error.message}`);
    }

    return (
      data.find(
        (fila) => !ESTADOS_INACTIVOS.includes(fila.estado_solicitud?.descripcion?.trim()),
      ) ?? null
    );
  }

  static async createDraft(payload) {
    if (!payload?.id_usuario) {
      throw new Error("id_usuario es requerido para crear un borrador.");
    }

    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .insert(payload)
      .select("id_solicitud")
      .single();

    if (error) {
      throw new Error(`Error al crear el borrador: ${error.message}`);
    }

    return data;
  }

  static async updateDraft(idSolicitud, idUsuarioOrPayload, maybePayload) {
    const idUsuario =
      typeof idUsuarioOrPayload === "string" ? idUsuarioOrPayload : idUsuarioOrPayload?.id_usuario;

    const rawPayload = typeof idUsuarioOrPayload === "string" ? maybePayload : idUsuarioOrPayload;

    if (!idUsuario) {
      throw new Error("id_usuario es requerido para actualizar un borrador.");
    }

    const { id_usuario: _ignored, id_solicitud: _ignoredId, ...updateFields } = rawPayload ?? {};

    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .update(updateFields)
      .eq("id_solicitud", idSolicitud)
      .eq("id_usuario", idUsuario)
      .select("id_solicitud")
      .single();

    if (error) {
      throw new Error(`Error al actualizar el borrador: ${error.message}`);
    }

    return data;
  }

  static async getRequestWithDocuments(idSolicitud, idUsuario) {
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(`
        *,
        estado:estado_solicitud!solicitud_adopcion_id_estado_fkey (
          descripcion
        ),
        documentos:documento_tiene_solicitud_adopcion (
          documento (
            id_documento,
            tipo,
            url_archivo
          )
        )
      `)
      .eq("id_solicitud", idSolicitud)
      .eq("id_usuario", idUsuario)
      .maybeSingle();

    if (error) {
      throw new Error(`Error al consultar la solicitud: ${error.message}`);
    }

    return data;
  }

  static async submitDraft(idSolicitud, idUsuario, idEstadoPendiente) {
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .update({
        id_estado: idEstadoPendiente,
        puede_editar: false,
        fecha_solicitud: new Date().toISOString(),
      })
      .eq("id_solicitud", idSolicitud)
      .eq("id_usuario", idUsuario)
      .select("id_solicitud")
      .single();

    if (error) {
      throw new Error(`Error al enviar la solicitud: ${error.message}`);
    }

    return data;
  }
}
export default AdoptionRequestModel;
