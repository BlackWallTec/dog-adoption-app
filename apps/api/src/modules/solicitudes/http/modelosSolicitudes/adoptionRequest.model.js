import { supabase } from "../../../../config/supabaseClient.js";

// Columnas del PASO 3 
// Agregamos la lista de las columnas para crear arreglos.
const COLUMNAS_HOGAR = [
  "motivo",
  "tipo_vivienda",
  "otras_mascotas",
  "integrantes_hogar",
  "sobre_mascotas",
  "ha_tenido_mascotas",
  "donde_vivira",
  "todos_de_acuerdo",
  "jardin_o_patio",
  "casero_acepta",
].join(", ");

// Columnas Paso 4:

const COLUMNAS_COMPROMISO = [
  "cuidados_basicos",
  "opinion_esterilizacion",
  "permite_seguimiento",
].join(", ");

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

  // Paso 3 - Tu hogar

  // Carga lo que guardo el usuario en Tu hogar
  static async getHome(idSolicitud){
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(COLUMNAS_HOGAR)
      .eq("id_solicitud". idSolicitud)
      .single();
    
    if (error) {
      throw new Error(`Error en la consulta de tu hogar: ${error.message}`);
    }
    return data;
  }

  // Guardar las respuestas de Tu hogar
  static async updateHome(idSolicitud, datos) {
    const { error } = await supabase
      .from("solicitud_adopcion")
      .update(datos)
      .eq("id_solicitud", idSolicitud);
    
    if (error) {
      throw new Error(`Error al guardar tu hogar: ${error.message}`);
    }
  }

  // Paso 4 - Compromiso
  // Carga lo que el usuario ya guardo
  static async getCommitment(idSolicitud){
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(COLUMNAS_COMPROMISO)
      .eq("id_solicitud", idSolicitud)
      .single();
    
    if (error) {
      throw new Error(`Error en la consula del compromiso: ${error.message}`);
    }
    return data;
  }

  static async updateCommitment(idSolicitud, datos) {
    const { error } = await supabase
      .from("solicitud_adopcion")
      .update(datos)
      .eq("id_solicitud", idSolicitud);

    if (error) {
      throw new Error(`Error al guardar el compromiso: ${error.message}`);
    }
  }

}

export default AdoptionRequestModel;
