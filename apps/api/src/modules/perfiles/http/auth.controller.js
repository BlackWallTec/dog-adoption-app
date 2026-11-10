import "dotenv/config";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import Usuario from "./modelosPerfiles/user.model.js";
import ROLES from "./modelosPerfiles/roles.model.js";
import { ERROR_CODES, errorBody } from "../../../../../shared/errores.js";

/*
IMPORTANTE
Considerar que desde el cliente, se declara el x-client. Si esto no se
mandara desde android, se le enviaría como respuesta la cookie en
lugar del token

También, no se especificó un algoritmo ni aquí ni en el
is-auth.js, por defecto está usando HS256.

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

    return res
      .status(500)
      .json(errorBody(ERROR_CODES.INTERNAL, `Error interno del servidor: ${error.message}`));
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
