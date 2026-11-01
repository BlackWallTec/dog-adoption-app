import db from "../../../../config/supabaseClient.js";

// Se parametrizan las consultas, para que se traten como dato y no
// como instrucción.

// Inner join para filtrado de sólo las coincidencias entre tablas
export default class Usuario {
  // static permite llamar a un objeto desde la clase sin instanciar
  static async fetchByEmail(email) {
    const { data, error } = await supabase
      .from("usuarios")
      .select(
        `
      id_usuario,
      contrasena,
      rol,
      datos_confidenciales!inner ( id_usuario )
      `,
      )
      .eq("correo_electronico", email)
      .maybeSingle();

    // La IA me recomendó uso de maybeSingle() para garantizar
    // unicidad en resultados de la querie (lo que consideré
    // conveniente toda vez que sólo debe existir una cuenta
    // asociada a un email)

    if (error) {
      throw new Error("Error al consultar al usuario");
    }

    if (!data) {
      return null;
    }

    const { id_usuario, contrasena, rol } = data;
    return {
      id_usuario,
      email,
      contrasena,
      rol,
    };
  }
}
