import { getSqlSignup } from "../../../../config/postgresSignup.js";
import { instanciarError, VALIDATION } from "@qarino/shared/errores.js";

/*
Registro público de Clientes con el rol signup_service.

Todo ocurre en UNA transacción: si algo falla, Postgres revierte solo.

Los valores van parametrizados por postgres.js (nunca concatenados).
*/

const FK_CODIGO_POSTAL = "direcciones_fisicas_id_codigo_postal_fkey";

export default class RegistroPublico {
  static async crearCliente({
    idUsuario,
    rol,
    usuario,
    contacto,
    direccion,
    requiereConfirmacion = false,
  }) {
    const sql = getSqlSignup();

    try {
      await sql.begin(async (tx) => {
        // Orden según llaves foráneas
        await tx`
          insert into public.usuario ${tx(
            {
              id_usuario: idUsuario,
              rol,
              nombre: usuario.nombre,
              apellidos: usuario.apellidos,
              contrasena: usuario.contrasena,
              fecha_nacimiento: usuario.fecha_nacimiento ?? null,
              aviso_privacidad_aceptado: usuario.aviso_privacidad_aceptado,
              // La marca nace en la misma transacción; reenviar códigos nunca cambia esta fecha.
              ...(requiereConfirmacion
                ? { correo_pendiente_desde: tx`statement_timestamp()` }
                : {}),
            },
            "id_usuario",
            "rol",
            "nombre",
            "apellidos",
            "contrasena",
            "fecha_nacimiento",
            "aviso_privacidad_aceptado",
            ...(requiereConfirmacion ? ["correo_pendiente_desde"] : []),
          )}
        `;

        await tx`
          insert into public.cliente ${tx({ id_usuario: idUsuario }, "id_usuario")}
        `;

        await tx`
          insert into public.datos_confidenciales ${tx(
            {
              id_usuario: idUsuario,
              correo: contacto.correo,
              telefono: contacto.telefono ?? null,
            },
            "id_usuario",
            "correo",
            "telefono",
          )}
        `;

        await tx`
          insert into public.direcciones_fisicas ${tx(
            {
              id_usuario: idUsuario,
              id_codigo_postal: direccion.id_codigo_postal,
              calle: direccion.calle ?? null,
              numero_exterior: direccion.numero_exterior ?? null,
              numero_interior: direccion.numero_interior ?? null,
            },
            "id_usuario",
            "id_codigo_postal",
            "calle",
            "numero_exterior",
            "numero_interior",
          )}
        `;
      });
    } catch (error) {
      // CP que no existe en el catálogo
      if (error.code === "23503" && error.constraint_name === FK_CODIGO_POSTAL) {
        throw new VALIDATION("Error de validación en los datos", {
          id_codigo_postal: "El código postal no existe en el catálogo",
        });
      }

      // postgres.js entrega el SQLSTATE en error.code, igual que supabase-js,
      // 42501 (privilegios/RLS) cae en 500 y queda en logs.
      instanciarError(`Error al crear cuenta (${error.code})`, error.code);
    }
  }
}
