import { supabaseAdmin } from "../../../../config/supabaseClient.js";
import { ERROR_CODES, SERVICE_UNAVAILABLE, instanciarError } from "@qarino/shared/errores.js";

const errorCorreo = () =>
  new SERVICE_UNAVAILABLE(
    "El servicio de correo no está disponible temporalmente.",
    ERROR_CODES.EMAIL_UNAVAILABLE,
  );

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
      correo_pendiente_desde,
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

  static async estadoCorreo(id) {
    const { data, error } = await supabaseAdmin
      .from("usuario")
      .select("correo_pendiente_desde, correo_version_movil")
      .eq("id_usuario", id)
      .maybeSingle();
    if (error) throw errorCorreo();
    return {
      confirmado: data ? data.correo_pendiente_desde === null : false,
      version: data?.correo_version_movil ?? 0,
    };
  }

  static async emitirCodigoCorreo(id, tipo, hash) {
    const { data, error } = await supabaseAdmin.rpc("emitir_codigo_correo", {
      p_id: id,
      p_tipo: tipo,
      p_hash: hash,
    });
    if (error) throw errorCorreo();
    return data;
  }

  static async confirmarCodigoCorreo(id, hash) {
    const { data, error } = await supabaseAdmin.rpc("confirmar_codigo_correo", {
      p_id: id,
      p_hash: hash,
    });
    if (error) throw errorCorreo();
    return data;
  }

  static async restablecerContrasenaCorreo(id, hash, contrasena) {
    const { data, error } = await supabaseAdmin.rpc("restablecer_contrasena_correo", {
      p_id: id,
      p_hash: hash,
      p_contrasena: contrasena,
    });
    if (error) throw errorCorreo();
    return data;
  }

  // Smoke test sin datos personales: comprueba que la migración compartida
  // esté disponible antes de crear una cuenta que requiera confirmación.
  static async correoMovilDisponible() {
    const [usuario, desafio, funcion] = await Promise.all([
      supabaseAdmin.from("usuario").select("correo_version_movil").limit(1),
      supabaseAdmin.from("correo_movil_desafio").select("id_usuario").limit(1),
      supabaseAdmin.rpc("emitir_codigo_correo", {
        p_id: "00000000-0000-0000-0000-000000000000",
        p_tipo: "confirmar",
        p_hash: "0".repeat(64),
      }),
    ]);
    return !usuario.error && !desafio.error && !funcion.error && funcion.data === "invalido";
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
