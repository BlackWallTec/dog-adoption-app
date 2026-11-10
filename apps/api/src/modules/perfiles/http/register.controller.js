import "dotenv/config";
import bcrypt from "bcryptjs";
import { randomUUID } from "node:crypto";
import Usuario from "./modelosPerfiles/user.model.js";
import ROLES from "./modelosPerfiles/roles.model.js";
import CodigoPostal from "./modelosPerfiles/cps.model.js";
import {
  ERROR_CODES,
  errorBody,
  VALIDATION,
  EMAIL_EXISTS,
  CONFLICT,
  BAD_REQUEST,
} from "../../../../../shared/errores.js";
import {
  validateRegistration,
  limpiarTelefono,
} from "../../../../../shared/perfiles/registro.validation.js";

// Creación de cuenta (rol Cliente)

export const postRegister = async (req, res) => {
  // Fuera del try para que el catch pueda revertir lo ya creado
  let idUsuario = null;

  try {
    const b = req.body ?? {};

    // Valida formato de todos los campos
    validateRegistration(b);

    // CP elegido debe existir en el catálogo
    if (
      typeof b.id_codigo_postal !== "string" ||
      !(await CodigoPostal.existePorId(b.id_codigo_postal))
    ) {
      throw new VALIDATION("Error de validación en los datos", {
        id_codigo_postal: "El código postal no existe en el catálogo",
      });
    }

    // Valores ya validados, normalizados
    const correo = b.correo.toLowerCase();
    const telefono = limpiarTelefono(b.telefono);

    const regUsuario = {
      nombre: b.nombre,
      apellidos: b.apellidos,
      fecha_nacimiento: b.fecha_nacimiento,
      aviso_privacidad_aceptado: b.privacidad,
    };

    const regDirFisicas = {
      id_codigo_postal: b.id_codigo_postal,
      calle: b.calle,
      numero_exterior: b.numero_exterior,
      // No es obligatorio, esto es consistente con DB
      numero_interior: b.numero_interior?.trim() || null,
    };

    // Hashea la contraseña
    const hashContrasena = await bcrypt.hash(b.contrasena, 10);

    // Inserciones, en orden de dependencia de llaves foráneas
    idUsuario = randomUUID();

    await Usuario.registerAccount(
      idUsuario,
      {
        rol: ROLES.CLIENT,
        ...regUsuario,
        contrasena: hashContrasena,
      },
      "usuario",
    );

    await Usuario.registerAccount(idUsuario, {}, "cliente");

    await Usuario.registerAccount(idUsuario, { correo, telefono }, "datos_confidenciales");

    await Usuario.registerAccount(idUsuario, regDirFisicas, "direcciones_fisicas");

    return res.status(201).json({
      user: {
        id: idUsuario,
        correo,
        rol: ROLES.CLIENT,
      },
    });
  } catch (error) {
    // Si se alcanzó a crear algo, se revierte
    if (idUsuario) {
      try {
        await Usuario.deleteAccount(idUsuario);
      } catch (rollbackError) {
        console.error("No se pudo revertir el registro:", rollbackError);
      }
    }

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
