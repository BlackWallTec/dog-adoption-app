import { supabase } from "../../../../config/supabaseClient.js";
import { instanciarError } from "../../../../../../shared/errores.js";

// Se parametrizan las consultas, para que se traten como dato y no
// como instrucción.

// Inner join para filtrado de sólo las coincidencias entre tablas
export default class Usuario {
  // static permite llamar a un objeto desde la clase sin instanciar
  static async fetchByEmail(email) {
    const { data, error } = await supabase
      .from("usuario")
      .select(
        `
      id_usuario,
      contrasena,
      rol,
      datos_confidenciales!inner ( id_usuario )
      `,
      )
      .eq("datos_confidenciales.correo", email)
      .maybeSingle();

    // La IA me recomendó uso de maybeSingle() para garantizar
    // unicidad en resultados de la querie (lo que consideré
    // conveniente toda vez que sólo debe existir una cuenta
    // asociada a un email)

    if (error) instanciarError("Error al consultar usuario", error.code);

    if (!data) {
      return null;
    }

    const { id_usuario, contrasena, rol } = data;
    return {
      id_usuario,
      contrasena,
      rol,
    };
  }

  static async registerAccount(id, Registro, tabla) {
    const { _data, error } = await supabase.from(tabla).insert({
      id_usuario: id,
      ...Registro,
    });

    if (error) instanciarError(`Error al crear cuenta (${error.code})`, error.code);
  }
}
