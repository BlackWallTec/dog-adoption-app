import "dotenv/config";
import bcrypt from "bcryptjs";
import Usuario from "./modelosPerfiles/user.model.js";
import ROLES from "./modelosPerfiles/roles.model.js";
import CodigoPostal from "./modelosPerfiles/cps.model.js";
import { randomUUID } from "node:crypto";

// La IA sugirió utilizar esto para distinguir errores del cliente
// (422) de fallos internos (500)
class ErrorValidacion extends Error {}

// También me ayudó con las expresiones regulares
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const TEL_RE = /^\+?\d{7,14}$/; // cabe en varchar(15)
const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function checkType(campo, type, val, obligatorio) {
  const ausente = val === "" || val === null || val === undefined;

  if (ausente) {
    // Si es opcional y no se proporcionó, se omite.
    if (obligatorio) throw new ErrorValidacion(`${campo} es obligatorio`);
    return;
  }

  const tipoCorrecto =
    type === "number" ? typeof val === "number" && Number.isFinite(val) : typeof val === type;

  if (!tipoCorrecto) throw new ErrorValidacion(`${campo} debe ser de tipo ${type}`);
}

// string con trim y largo máximo (el de la columna). null si es opcional y viene vacío
function texto(campo, val, max, obligatorio = true) {
  checkType(campo, "string", val, obligatorio);
  const limpio = typeof val === "string" ? val.trim() : "";

  if (!limpio) {
    if (obligatorio) throw new ErrorValidacion(`${campo} es obligatorio`);
    return null;
  }
  if (limpio.length > max) {
    throw new ErrorValidacion(`${campo} no puede exceder ${max} caracteres`);
  }
  return limpio;
}

function entero(campo, val, obligatorio = true) {
  checkType(campo, "number", val, obligatorio);
  if (val === "" || val === null || val === undefined) return null;

  if (!Number.isInteger(val) || val < 0) {
    throw new ErrorValidacion(`${campo} debe ser un entero positivo`);
  }
  return val;
}

function booleano(campo, val, obligatorio = true) {
  checkType(campo, val, obligatorio);

  return val;
}

// Creación de cuenta (rol Cliente)

export const postRegister = async (req, res) => {
  let idUsuario = null;

  try {
    const b = req.body ?? {};

    const privacidad = booleano("privacidad", b.privacidad);

    if (privacidad === false) {
      throw new ErrorValidacion("Es necesario aceptar la política de privacidad");
    }

    // Validar y normalizar
    const regUsuario = {
      nombre: texto("nombre", b.nombre, 50),
      apellidos: texto("apellidos", b.apellidos, 50),
      fecha_nacimiento: texto("fecha_nacimiento", b.fecha_nacimiento, 30),
      aviso_privacidad_aceptado: booleano("privacidad", value.privacidad),
    };

    const fecha = new Date(regUsuario.fecha_nacimiento);
    if (Number.isNaN(fecha.getTime()) || fecha > new Date()) {
      throw new ErrorValidacion("fecha_nacimiento no es una fecha válida");
    }

    checkType("contrasena", "string", b.contrasena, true);
    if (b.contrasena.length < 8 || b.contrasena.length > 72) {
      throw new ErrorValidacion("contrasena debe tener entre 8 y 32 caracteres");
    }

    const correo = texto("correo", b.correo, 100).toLowerCase();
    if (!EMAIL_RE.test(correo)) throw new ErrorValidacion("correo no es válido");

    const telefono = texto("telefono", b.telefono, 12);
    if (!TEL_RE.test(telefono)) throw new ErrorValidacion("telefono no es válido");

    const idCodigoPostal = texto("id_codigo_postal", b.id_codigo_postal, 254);
    if (!UUID_RE.test(idCodigoPostal)) {
      throw new ErrorValidacion("id_codigo_postal no es válido");
    }

    const regDirFisicas = {
      id_codigo_postal: idCodigoPostal,
      calle: texto("calle", b.calle, 100),
      numero_exterior: entero("numero_exterior", b.numero_exterior),
      numero_interior: entero("numero_interior", b.numero_interior, false),
    };

    // CP elegido debe existir en el catálogo
    if (!(await CodigoPostal.existePorId(idCodigoPostal))) {
      throw new ErrorValidacion("id_codigo_postal no existe en el catálogo");
    }

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

    await Usuario.registerAccount(
      idUsuario,
      {
        correo,
        telefono,
      },
      "datos_confidenciales",
    );

    await Usuario.registerAccount(idUsuario, regDirFisicas, "direcciones_fisicas");

    return res.status(201).json({
      user: {
        id: idUsuario,
        correo: correo,
        rol: ROLES.CLIENT,
      },
    });
  } catch (error) {
    // Si ya se alcanzó a crear algo, se revierte
    if (idUsuario) await Usuario.deleteAccount(idUsuario);

    if (error instanceof ErrorValidacion) {
      return res.status(422).json({ error: error.message });
    }

    // correo duplicado, detecta una unique_violation de PostgreSQL
    // y también notifica HTTP conflict
    if (error.code === "23505" && error.tabla === "datos_confidenciales") {
      return res.status(409).json({
        error: "Ya existe una cuenta con ese correo",
      });
    }

    console.error(error);
    return res.status(500).json({
      error: "Error interno del servidor",
    });
  }
};
