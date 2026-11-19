import { supabaseAdmin as supabase } from "../../../config/supabaseClient.js";
const first = (value) => (Array.isArray(value) ? (value[0] ?? null) : (value ?? null));

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
      .order("fecha_solicitud", { ascending: false });

    if (error) {
      throw new Error(`Error en la consulta de solicitudes: ${error.message}`);
    }

    return (data || []).filter(
      (row) => first(row.estado)?.descripcion?.trim().toLowerCase() !== "borrador",
    );
  }

  // HU05: detalle de UNA solicitud (excluyendo borradores)
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
      .maybeSingle();

    if (error) {
      throw new Error(`Error en la consulta del detalle: ${error.message}`);
    }

    if (!data || first(data.estado)?.descripcion?.trim().toLowerCase() === "borrador") {
      return null;
    }

    return data;
  }
}

export default SolicitudModel;
