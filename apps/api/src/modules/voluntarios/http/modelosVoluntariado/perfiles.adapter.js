import { supabase } from "../../../../config/supabaseClient.js";
// TEMPORAL: el modulo perfiles todavia no pone funciones en su index.js
// Este archivo se reemplazara por imports de "../../../../perfiles/index.js" y se elimina

class PerfilesAdapter {
  //Nombre y apellido de una lista de usuarios
  static async ObtenerNombres(idsUsuario) {
    if (idsUsuario.lenght === 0) return [];

    const { data, error } = await supabase
      .from("usuario")
      .select("id_usuario, nombre, apellidos")
      .in("id_usuario", idsUsuario);

    if (error) {
      throw new Error(`Error en la consulta de nombres: ${error.message}`);
    }

    return data;
  }

  // Correo y telefono de una lista de usuarios
  static async ObtenerContacto(idsUsuario) {
    if (idsUsuario.lenght === 0) return [];

    const { data, error } = await supabase
      .from("datos_confidenciales")
      .select("id_usuario, correo, telefono")
      .in("id_usuario", idsUsuario);

    if (error) {
      throw new Error(`Error en la consulta de contacto: ${error.message}`);
    }

    return data;
  }
}
export default PerfilesAdapter;
