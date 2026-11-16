import { supabaseAdmin as supabase } from "../../../config/supabaseClient.js";

class SolicitudModel {
  // Lista para la tabla del admin
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
          cliente (
            datos_confidenciales ( correo )
          )
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

  // HU05: detalle de UNA solicitud (para el pop-up del admin)
  static async getById(idSolicitud) {
    const { data, error } = await supabase
      .from("solicitud_adopcion")
      .select(`
        *,
        usuario:usuario!solicitud_adopcion_id_usuario_fkey (
          nombre,
          apellidos,
          fecha_nacimiento,
          cliente (
            datos_confidenciales ( correo, telefono )
          ),
          direcciones_fisicas (
            calle,
            numero_exterior,
            numero_interior,
            catalogo_de_cps ( codigo_postal, colonia, municipio, estado )
          )
        ),
        perro:perro!solicitud_adopcion_id_perro_fkey (
          id_perro,
          nombre,
          sexo,
          edad_aprox,
          estado,
          raza ( nombre ),
          tamano ( nombre ),
          multimedia ( url, tipo )
        ),
        estado:estado_solicitud!solicitud_adopcion_id_estado_fkey (
          descripcion
        ),
        documentos:documento_tiene_solicitud_adopcion (
          documento ( id_documento, tipo, url_archivo, estado_revision )
        )
      `)
      .eq("id_solicitud", idSolicitud)
      .maybeSingle(); // null si no existe

    if (error) {
      throw new Error(`Error en la consulta del detalle: ${error.message}`);
    }

    return data;
  }
}

export default SolicitudModel;
