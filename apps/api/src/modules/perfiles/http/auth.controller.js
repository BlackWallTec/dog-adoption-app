import "dotenv/config";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import Usuario from "./modelosPerfiles/user.model.js";
import ROLES from "./modelosPerfiles/roles.model.js";
import CodigoPostal from "./modelosPerfiles/cps.model.js";
import { randomUUID } from "node:crypto";

/*
IMPORTANTE
Considerar que desde el cliente, se declara el x-client. Si esto no se
mandara desde android, se le enviaría como respuesta la cookie en
lugar del token

También, no se especificó un algoritmo ni aquí ni en el
is-auth.js, por defecto está usando HS256.

En el middleware también debe usarse el mismo header.
*/

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

function entero(campo, val, obligatorio) {
  checkType(campo, "number", val, obligatorio);
  if (val === "" || val === null || val === undefined) return null;

  if (!Number.isInteger(val) || val < 0) {
    throw new ErrorValidacion(`${campo} debe ser un entero positivo`);
  }
  return val;
}

const vidaSeconds = (rol) =>
  rol === ROLES.ROOT ? 60 * 60 : rol === ROLES.ADMIN ? 6 * 60 * 60 : 72 * 60 * 60;

const cookieBase = {
  httpOnly: true, // Impide que JS lea la cookie
  // HTTPS en producción
  // Asegurar que la aplicación se sirva vía HTTPS
  // o la cookie no funcionará
  secure: process.env.NODE_ENV === "production",
  // Rigor p/adjuntar cookie en solctd. iniciada
  // desde otros sitios
  sameSite: "lax",
  // Rutas del sitio a las que se envía la cookie
  path: "/",
};

// Inicio de sesión
export const postLogin = async (req, res) => {
  try {
    const correo = req.body?.correo;
    const contrasena = req.body?.contrasena;

    if (typeof correo !== "string" || typeof contrasena !== "string" || !correo || !contrasena) {
      return res.status(400).json({
        error: "Correo y contraseña son requeridos",
      });
    }

    const usuario = await Usuario.fetchByEmail(correo.trim());

    if (!usuario?.contrasena) {
      return res.status(401).json({
        error: "Credenciales inválidas",
      });
    }

    // Se compara lo ingresado con el hash obtenido de la DB
    const coincide = await bcrypt.compare(contrasena, usuario.contrasena);
    if (!coincide) {
      return res.status(401).json({
        error: "Credenciales inválidas",
      });
    }

    const vida = vidaSeconds(usuario.rol);

    const token = jwt.sign(
      {
        uuid: usuario.id_usuario,
        correo: correo,
        rol: usuario.rol,
      },
      process.env.JWT_SECRET,
      {
        // Aquí también se especifica el algoritmo a usar
        algorithm: "HS256",
        expiresIn: vida,
      },
    );

    const esMovil = req.get("x-client") === "mobile";

    if (esMovil) {
      return res.status(200).json({
        user: {
          id: usuario.id_usuario,
          correo: correo,
          rol: usuario.rol,
        },
        token,
      });
    }

    res.cookie("token", token, {
      ...cookieBase, // ... copia las propiedades del objeto
      maxAge: vida * 1000, // Express usa milisegundos
    });

    return res.status(200).json({
      user: {
        id: usuario.id_usuario,
        correo: correo,
        rol: usuario.rol,
      },
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      error: "Error interno del servidor",
    });
  }
};

// Creación de cuenta (rol Cliente)

export const postRegister = async (req, res) => {
  let idUsuario = null;

  try {
    const b = req.body ?? {};

    // Validar y normalizar
    const regUsuario = {
      nombre: texto("nombre", b.nombre, 50),
      apellidos: texto("apellidos", b.apellidos, 50),
      fecha_nacimiento: texto("fecha_nacimiento", b.fecha_nacimiento, 30),
    };

    const fecha = new Date(regUsuario.fecha_nacimiento);
    if (Number.isNaN(fecha.getTime()) || fecha > new Date()) {
      throw new ErrorValidacion("fecha_nacimiento no es una fecha válida");
    }

    // La contraseña NO se recorta. bcrypt sólo considera los primeros 32 bytes.
    checkType("contrasena", "string", b.contrasena, true);
    if (b.contrasena.length < 8 || b.contrasena.length > 32) {
      throw new ErrorValidacion("contrasena debe tener entre 8 y 32 caracteres");
    }

    const correo = texto("correo", b.correo, 100).toLowerCase();
    if (!EMAIL_RE.test(correo)) throw new ErrorValidacion("correo no es válido");

    const telefono = texto("telefono", b.telefono, 15);
    if (!TEL_RE.test(telefono)) throw new ErrorValidacion("telefono no es válido");

    const idCodigoPostal = texto("id_codigo_postal", b.id_codigo_postal, 36);
    if (!UUID_RE.test(idCodigoPostal)) {
      throw new ErrorValidacion("id_codigo_postal no es válido");
    }

    const regDirFisicas = {
      id_codigo_postal: idCodigoPostal,
      calle: texto("calle", b.calle, 100),
      numero_exterior: entero("numero_exterior", b.numero_exterior, true),
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

// Cerrar sesión
export const postLogout = (req, res) => {
  res.clearCookie("token", cookieBase);
  return res.status(200).json({ ok: true });
};

// Para corroborar info. de la sesión
export const getMe = (req, res) => {
  // req.user lo puso verificarToken
  const { uuid, correo, rol } = req.user;
  return res.status(200).json({ user: { id: uuid, correo, rol } });
};
