import { supabaseAdmin } from "../../../../config/supabaseClient.js";

export default class Usuario {
  // Las credenciales se consultan únicamente desde Express con la clave privada.
  static async fetchByEmail(email) {
    const { data, error } = await supabaseAdmin
      .from("usuario")
      .select(
        `
      id_usuario,
      contrasena,
      rol,
      datos_confidenciales!inner ( id_usuario )
      `,
      )
      .ilike("datos_confidenciales.correo", email.trim().replace(/[\\%_]/g, "\\$&"))
      .maybeSingle();

    if (error) {
      throw new Error(`Error (${error.code}), al consultar usuario: ${error.message}`);
    }

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

  static async register(value, passwordHash) {
    const { data, error } = await supabaseAdmin.rpc("perfiles_registrar_cliente", {
      p_nombre: value.nombre,
      p_apellidos: value.apellidos,
      p_correo: value.email,
      p_hash: passwordHash,
      p_telefono: value.telefono,
      p_fecha_nacimiento: value.fechaNacimiento,
      p_calle: value.calle,
      p_numero_exterior: Number(value.numeroExterior),
      p_codigo_postal: value.codigoPostal,
      p_privacidad: value.privacidad,
    });
    if (error) throw error;
    if (!data) throw new Error("El registro no devolvió el identificador del usuario");
    return { id_usuario: data, rol: "Cliente" };
  }

  static async fetchProfile(id) {
    const { data, error } = await supabaseAdmin
      .from("usuario")
      .select(`
      id_usuario, nombre, apellidos, fecha_nacimiento,
      datos_confidenciales (correo, telefono, codigo_postal),
      direcciones_fisicas (calle, numero_exterior)
    `)
      .eq("id_usuario", id)
      .maybeSingle();
    if (error) throw error;
    if (!data) return null;
    const confidential = data.datos_confidenciales ?? {};
    const address = data.direcciones_fisicas?.[0] ?? {};
    return {
      nombre: data.nombre,
      apellidos: data.apellidos,
      fechaNacimiento: data.fecha_nacimiento?.slice(0, 10) ?? "",
      telefono: confidential.telefono ?? "",
      codigoPostal: confidential.codigo_postal ?? "",
      calle: address.calle ?? "",
      numeroExterior: address.numero_exterior ?? "",
    };
  }
}
