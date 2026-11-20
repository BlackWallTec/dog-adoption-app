import "dotenv/config";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import Usuario from "./modelosPerfiles/user.model.js";
import ROLES from "../../../config/roles.js";
import { correoMovilHabilitado, servicioCorreoMovil } from "../services/correoMovil.service.js";
import {
  ERROR_CODES,
  errorBody,
  UNAUTHENTICATED,
  FORBIDDEN,
  errorResponse,
} from "@qarino/shared/errores.js";

/*
IMPORTANTE
Considerar que desde el cliente, se declara el x-client. Si esto no se
mandara desde android, se le enviaría como respuesta la cookie en
lugar del token

En el middleware también debe usarse el mismo header.
*/

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
    let correo = req.body?.correo;
    const contrasena = req.body?.contrasena;

    if (typeof correo !== "string" || typeof contrasena !== "string" || !correo || !contrasena) {
      return res
        .status(400)
        .json(errorBody(ERROR_CODES.BAD_REQUEST, "Correo y contraseña son requeridos"));
    }

    correo = correo.toLowerCase();

    const usuario = await Usuario.fetchByEmail(correo.trim());

    if (!usuario?.contrasena) {
      return res.status(401).json(errorBody(ERROR_CODES.UNAUTHENTICATED, "Credenciales inválidas"));
    }

    // Se compara lo ingresado con el hash obtenido de la DB
    const coincide = await bcrypt.compare(contrasena, usuario.contrasena);

    if (!coincide) {
      return res
        .status(401)
        .json(errorBody(ERROR_CODES.INVALID_CREDENTIALS, "Credenciales inválidas"));
    }

    // Extensión optativa: las cuentas anteriores y los roles web mantienen su acceso.
    const estadoCorreo =
      usuario.rol === ROLES.CLIENT && correoMovilHabilitado()
        ? servicioCorreoMovil().estado(usuario.id_usuario)
        : null;
    if (estadoCorreo && (!estadoCorreo.confirmado || usuario.correo_pendiente_desde))
      throw new FORBIDDEN(
        "Confirma tu correo antes de iniciar sesión.",
        ERROR_CODES.EMAIL_NOT_CONFIRMED,
      );

    const vida = vidaSeconds(usuario.rol);

    const token = jwt.sign(
      {
        uuid: usuario.id_usuario,
        rol: usuario.rol,
        ...(estadoCorreo ? { correoVersionMovil: estadoCorreo.version } : {}),
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
          uuid: usuario.id_usuario,
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
        uuid: usuario.id_usuario,
        rol: usuario.rol,
      },
    });
  } catch (error) {
    const respuesta = errorResponse(error);
    if (respuesta.status >= 500) console.error("No se completó el inicio de sesión.");
    return res.status(respuesta.status).json(respuesta.body);
  }
};

// Cerrar sesión
export const postLogout = (req, res) => {
  res.clearCookie("token", cookieBase);
  return res.status(200).json({ ok: true });
};

// Para corroborar info. de la sesión
export const getMe = async (req, res) => {
  try {
    const { uuid, rol } = req.user; // lo puso verificarToken
    // Sólo se usa para comprobar que la cuenta sigue existiendo;
    // el correo no se devuelve.
    const correo = await Usuario.fetchEmailByUUID(uuid);

    // Token válido pero la cuenta ya no existe
    if (!correo) throw new UNAUTHENTICATED();

    return res.status(200).json({
      user: { uuid, rol },
    });
  } catch (error) {
    const respuesta = errorResponse(error);
    if (respuesta.status >= 500) console.error("No se pudo consultar la sesión.");
    return res.status(respuesta.status).json(respuesta.body);
  }
};
