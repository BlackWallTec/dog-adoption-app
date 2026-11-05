import { supabaseAdmin as supabase } from "../../../../config/supabaseClient.js";

class ProfileModel {
  // Datos del perfil para precargar "Datos generales"
  static async getProfile(idUsuario) {
    const { data, error } = await supabase
      .from("usuario")
      .select(`
        id_usuario,
        nombre,
        apellidos,
        fecha_nacimiento,
        cliente (
          datos_confidenciales (
            correo,
            telefono
          )
        ),
        direcciones_fisicas (
          calle,
          numero_exterior,
          numero_interior,
          catalogo_de_cps (
            codigo_postal,
            colonia,
            municipio,
            estado
          )
        )
      `)
      .eq("id_usuario", idUsuario)
      .single();

    if (error) {
      throw new Error(`Error en la consulta del perfil: ${error.message}`);
    }

    return data;
  }
}

export default ProfileModel;
