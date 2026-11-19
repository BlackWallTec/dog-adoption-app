import "dotenv/config";
import bcrypt from "bcryptjs";
import { randomUUID } from "node:crypto";
import Usuario from "./modelosPerfiles/user.model.js";
import ROLES from "./modelosPerfiles/roles.model.js";
import CodigoPostal from "./modelosPerfiles/cps.model.js";
import RegistroPublico from "./modelosPerfiles/registroPublico.model.js";
import {
  ERROR_CODES,
  errorBody,
  VALIDATION,
  EMAIL_EXISTS,
  CONFLICT,
  BAD_REQUEST,
} from "@qarino/shared/errores.js";
import {
  validateRegistration,
  limpiarTelefono,
} from "@qarino/shared/perfiles/registro.validation.js";

/*
Dos maneras de crear cuenta:

1) Pública, NO pasa por verificarToken. Usa el rol
   de Postgres signup_service (que sólo tiene permiso de INSERT), en una
   sola transacción. No usa la service role key.

2) Root crea Administradores desde el panel web, ya viene protegida por
   verificarToken y requireRole(ROOT), así que conserva supabaseAdmin y su
   rollback manual.
*/

const cpInexistente = () =>
  new VALIDATION("Error de validación en los datos", {
    id_codigo_postal: "El código postal no existe en el catálogo",
  });

// Estrategia pública: transacción única con signup_service
// La existencia del CP la garantiza la FK (se traduce a 422 en el modelo).
const estrategiaPublica = {
  validarCP: async () => {},
  guardar: (datos) => RegistroPublico.crearCliente(datos),
};

// Estrategia admin: supabaseAdmin, rollback manual
const estrategiaAdmin = (tablasDeRol) => ({
  validarCP: async (idCodigoPostal) => {
    if (!(await CodigoPostal.existePorId(idCodigoPostal))) throw cpInexistente();
  },

  guardar: async ({ idUsuario, rol, usuario, contacto, direccion }) => {
    try {
      // Inserciones, en orden de dependencia de llaves foráneas
      await Usuario.registerAccount(idUsuario, { rol, ...usuario }, "usuario");

      for (const tabla of tablasDeRol) {
        await Usuario.registerAccount(idUsuario, {}, tabla);
      }

      await Usuario.registerAccount(idUsuario, contacto, "datos_confidenciales");
      await Usuario.registerAccount(idUsuario, direccion, "direcciones_fisicas");
    } catch (error) {
      // Si se alcanzó a crear algo, se revierte
      try {
        await Usuario.deleteAccount(idUsuario);
      } catch (rollbackError) {
        console.error("No se pudo revertir el registro:", rollbackError);
      }
      throw error;
    }
  },
});

const crearCuenta = (rol, estrategia) => async (req, res) => {
  try {
    const b = req.body ?? {};

    // Valida formato de todos los campos
    validateRegistration(b);

    // CP elegido debe ser un texto; su existencia la verifica la estrategia
    if (typeof b.id_codigo_postal !== "string") throw cpInexistente();
    await estrategia.validarCP(b.id_codigo_postal);

    // Valores ya validados, normalizados
    const correo = b.correo.toLowerCase();
    const telefono = limpiarTelefono(b.telefono);

    // Hashea la contraseña
    const hashContrasena = await bcrypt.hash(b.contrasena, 10);

    const idUsuario = randomUUID();

    await estrategia.guardar({
      idUsuario,
      rol,
      usuario: {
        nombre: b.nombre,
        apellidos: b.apellidos,
        fecha_nacimiento: b.fecha_nacimiento,
        aviso_privacidad_aceptado: b.privacidad,
        contrasena: hashContrasena,
      },
      contacto: { correo, telefono },
      direccion: {
        id_codigo_postal: b.id_codigo_postal,
        calle: b.calle,
        numero_exterior: b.numero_exterior,
        // No es obligatorio, esto es consistente con DB
        numero_interior: b.numero_interior?.trim() || null,
      },
    });

    return res.status(201).json({
      user: {
        uuid: idUsuario,
        rol,
      },
    });
  } catch (error) {
    console.error(error);

    if (error instanceof VALIDATION) {
      return res
        .status(422)
        .json(errorBody(ERROR_CODES.VALIDATION, error.message, error.getFields()));
    }

    // Para unique_violation de PostgreSQL
    if (error instanceof EMAIL_EXISTS || error instanceof CONFLICT) {
      const mensaje = "Ya existe una cuenta con ese correo";
      return res
        .status(409)
        .json(errorBody(ERROR_CODES.EMAIL_EXISTS, mensaje, { correo: mensaje }));
    }

    if (error instanceof BAD_REQUEST) {
      return res.status(400).json(errorBody(ERROR_CODES.BAD_REQUEST, error.message));
    }

    return res.status(500).json(errorBody(ERROR_CODES.INTERNAL, "Error interno del servidor"));
  }
};

export const postRegister = crearCuenta(ROLES.CLIENT, estrategiaPublica);

export const postRegisterAdmin = crearCuenta(ROLES.ADMIN, estrategiaAdmin(["cliente", "admin"]));
