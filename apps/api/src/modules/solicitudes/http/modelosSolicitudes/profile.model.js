import { supabaseAdmin } from "../../../../config/supabaseClient.js";

class ProfileModel {
  static async getProfile(idUsuario) {
    const [usuario, contacto, direccion] = await Promise.all([
      supabaseAdmin
        .from("usuario")
        .select("id_usuario, nombre, apellidos, fecha_nacimiento")
        .eq("id_usuario", idUsuario)
        .maybeSingle(),

      supabaseAdmin
        .from("datos_confidenciales")
        .select("correo, telefono")
        .eq("id_usuario", idUsuario)
        .maybeSingle(),

      supabaseAdmin
        .from("direcciones_fisicas")
        .select("calle, numero_exterior, numero_interior, id_codigo_postal")
        .eq("id_usuario", idUsuario)
        .limit(1)
        .maybeSingle(),
    ]);

    for (const result of [usuario, contacto, direccion]) {
      if (result.error) {
        throw new Error(`Error en la consulta del perfil: ${result.error.message}`);
      }
    }

    if (!usuario.data) return null;

    let cp = null;
    if (direccion.data?.id_codigo_postal) {
      const consultaCp = await supabaseAdmin
        .from("catalogo_de_cps")
        .select("codigo_postal, colonia, municipio, estado")
        .eq("id_codigo_postal", direccion.data.id_codigo_postal)
        .maybeSingle();

      if (consultaCp.error) {
        throw new Error(`Error en la consulta del perfil: ${consultaCp.error.message}`);
      }

      cp = consultaCp.data;
    }

    return {
      id_usuario: usuario.data.id_usuario,
      nombre: usuario.data.nombre,
      apellidos: usuario.data.apellidos,
      fecha_nacimiento: usuario.data.fecha_nacimiento,
      cliente: [
        {
          datos_confidenciales: [
            {
              correo: contacto.data?.correo ?? "",
              telefono: contacto.data?.telefono ?? "",
            },
          ],
        },
      ],
      direcciones_fisicas: direccion.data
        ? [
            {
              calle: direccion.data.calle,
              numero_exterior: direccion.data.numero_exterior,
              numero_interior: direccion.data.numero_interior,
              catalogo_de_cps: cp ? [cp] : [],
            },
          ]
        : [],
    };
  }
}

export default ProfileModel;
