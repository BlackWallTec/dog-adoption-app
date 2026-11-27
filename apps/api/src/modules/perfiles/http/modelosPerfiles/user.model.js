import { supabase, supabaseAdmin } from "../../../../config/supabaseClient.js";
import { instanciarError } from "@qarino/shared/errores.js";
import { correoMovilHabilitado } from "../../services/correoMovil.config.js";

// Se parametrizan las consultas, para que se traten como dato y no
// como instrucción.

// Inner join para filtrado de sólo las coincidencias entre tablas
export default class Usuario {
  // static permite llamar a un objeto desde la clase sin instanciar
  static async fetchByEmail(email) {
    const { data, error } = await supabaseAdmin
      .from("usuario")
      .select(
        `
      id_usuario,
      contrasena,
      rol,
      ${correoMovilHabilitado() ? "correo_pendiente_desde," : ""}
      datos_confidenciales!inner ( id_usuario )
      `,
      )
      .eq("datos_confidenciales.correo", email)
      .maybeSingle();

    // La IA me recomendó uso de maybeSingle() para garantizar
    // unicidad en resultados de la querie (lo que consideré
    // conveniente toda vez que sólo debe existir una cuenta
    // asociada a un email)

    if (error) instanciarError("Error al consultar usuario por email", error.code);

    if (!data) {
      return null;
    }

    const { id_usuario, contrasena, rol } = data;
    return {
      id_usuario,
      contrasena,
      rol,
      correo_pendiente_desde: data.correo_pendiente_desde ?? null,
    };
  }

  static async fetchEmailByUUID(id) {
    const { data, error } = await supabaseAdmin
      .from("datos_confidenciales")
      .select("correo")
      .eq("id_usuario", id)
      .maybeSingle();

    if (error) instanciarError("Error al consultar email por id", error.code);
    return data?.correo ?? null;
  }

  static async registerAccount(id, Registro, tabla) {
    const { _data, error } = await supabaseAdmin.from(tabla).insert({
      id_usuario: id,
      ...Registro,
    });

    if (error) instanciarError(`Error al crear cuenta (${error.code})`, error.code);
  }

  // Revierte un registro incompleto. Las FK con ON DELETE CASCADE
  // (cliente, admin, direcciones_fisicas y, vía cliente,
  // datos_confidenciales) se borran junto con el usuario.
  static async deleteAccount(id) {
    const { error } = await supabaseAdmin.from("usuario").delete().eq("id_usuario", id);

    if (error) instanciarError("Error al revertir la cuenta", error.code);
  }

  // La base decide si aún está dentro de las 72 horas y excluye la cuenta de la limpieza.
  static async confirmarCorreo(id) {
    const { data, error } = await supabaseAdmin.rpc("confirmar_correo_cliente", { p_id: id });
    if (error) instanciarError("Error al confirmar el correo", error.code);
    return data === true;
  }

  // HU63 modo supabase: las RPC sólo se ejecutan con service_role (ver la migración
  // perfiles_codigos_correo_supabase). Devuelven un estado de texto, nunca el código.
  static async obtenerEstadoCorreoMovil(id) {
    const { data, error } = await supabaseAdmin.rpc("estado_correo_movil", { p_id_usuario: id });
    if (error) instanciarError("Error al consultar el estado del correo", error.code);
    return data;
  }

  static async emitirCodigoCorreoMovil(id, tipo, codigoHash) {
    const { data, error } = await supabaseAdmin.rpc("emitir_codigo_correo_movil", {
      p_id_usuario: id,
      p_tipo: tipo,
      p_codigo_hash: codigoHash,
    });
    if (error) instanciarError("Error al emitir el código de correo", error.code);
    return data;
  }

  static async consumirCodigoCorreoMovil(id, tipo, codigoHash, contrasenaHash) {
    const { data, error } = await supabaseAdmin.rpc("consumir_codigo_correo_movil", {
      p_id_usuario: id,
      p_tipo: tipo,
      p_codigo_hash: codigoHash,
      p_contrasena_hash: contrasenaHash,
    });
    if (error) instanciarError("Error al consumir el código de correo", error.code);
    return data;
  }

  // La recuperación sólo sustituye el hash del usuario validado por el código de correo.
  static async updatePassword(id, contrasena) {
    const { error } = await supabaseAdmin
      .from("usuario")
      .update({ contrasena })
      .eq("id_usuario", id);
    if (error) instanciarError("Error al actualizar contraseña", error.code);
  }

  static async fetchProfileByUUID(id) {
    // Tres tablas distintas, se consultan en paralelo
    const [usuario, contacto, direccion] = await Promise.all([
      supabaseAdmin
        .from("usuario")
        .select("nombre, apellidos, fecha_nacimiento")
        .eq("id_usuario", id)
        .maybeSingle(),
      supabaseAdmin
        .from("datos_confidenciales")
        .select("telefono")
        .eq("id_usuario", id)
        .maybeSingle(),
      supabaseAdmin
        .from("direcciones_fisicas")
        .select("calle, numero_exterior, id_codigo_postal")
        .eq("id_usuario", id)
        .limit(1)
        .maybeSingle(),
    ]);

    for (const { error } of [usuario, contacto, direccion]) {
      if (error) instanciarError("Error al consultar perfil", error.code);
    }

    // Token válido pero la cuenta ya no existe
    if (!usuario.data) return null;

    // El perfil muestra el código postal, la dirección sólo guarda su id
    let codigo_postal = null;
    if (direccion.data?.id_codigo_postal) {
      const { data, error } = await supabaseAdmin
        .from("catalogo_de_cps")
        .select("codigo_postal")
        .eq("id_codigo_postal", direccion.data.id_codigo_postal)
        .maybeSingle();

      if (error) instanciarError("Error al consultar perfil", error.code);
      codigo_postal = data?.codigo_postal ?? null;
    }

    return {
      nombre: usuario.data.nombre,
      apellidos: usuario.data.apellidos,
      fecha_nacimiento: usuario.data.fecha_nacimiento,
      telefono: contacto.data?.telefono ?? null,
      calle: direccion.data?.calle ?? null,
      numero_exterior: direccion.data?.numero_exterior ?? null,
      codigo_postal,
    };
  }
}
