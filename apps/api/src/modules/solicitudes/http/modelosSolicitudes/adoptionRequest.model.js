import { supabaseAdmin as supabase } from "../../../../config/supabaseClient.js";

class AdoptionRequestModel {
  // Busca el uuid de un estado por su nombre ("Borrador")
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

  // Borrador de ese usuario para ese perro (null si no hay)
  static async getDraft(idUsuario, idPerro) {
    const idBorrador = await this.getStatusId("Borrador");

    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(`
        id_solicitud,
        id_usuario,
        id_perro,
        id_estado,
        fecha_solicitud
      `)
      .eq("id_usuario", idUsuario)
      .eq("id_perro", idPerro)
      .eq("id_estado", idBorrador)
      .maybeSingle();

    if (error) {
      throw new Error(`Error en la consulta del borrador: ${error.message}`);
    }

    return data;
  }

  // Crea una solicitud nueva en estado Borrador
  static async createRequest(idUsuario, idPerro) {
    const idBorrador = await this.getStatusId("Borrador");

    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .insert({
        id_usuario: idUsuario,
        id_perro: idPerro,
        id_estado: idBorrador,
      })
      .select(`
        id_solicitud,
        id_usuario,
        id_perro,
        id_estado,
        fecha_solicitud
      `)
      .single();

    if (error) {
      throw new Error(`Error al crear la solicitud: ${error.message}`);
    }

    return data;
  }

  // La solicitud con lo que ya tiene guardado
  static async getRequestById(idSolicitud) {
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(`
        id_solicitud,
        id_usuario,
        ocupacion
      `)
      .eq("id_solicitud", idSolicitud)
      .single();

    if (error) {
      throw new Error(`Error en la consulta de la solicitud: ${error.message}`);
    }

    return data;
  }

  // Guarda la ocupacion en la solicitud
  static async updateOccupation(idSolicitud, ocupacion) {
    const { error } = await supabase
      .from("solicitud_adopcion")
      .update({ ocupacion: ocupacion })
      .eq("id_solicitud", idSolicitud);

    if (error) {
      throw new Error(`Error al guardar la ocupación: ${error.message}`);
    }
  }
}

export default AdoptionRequestModel;
