import "dotenv/config";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import Usuario from "./modelosPerfiles/user.model.js";
import ROLES from "./modelosPerfiles/roles.model.js";

/*
IMPORTANTE
Considerar que desde el cliente, se declara el x-client. Si esto no se
mandara desde android, la búsqueda de la cookie será infructuosa y
resultará en 401.

También, no se especificó un algoritmo ni aquí ni en el
auth.controller.js, por defecto está usando HS256.

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
    const email = req.body.email;
    const passwd = req.body.passwd;

    if (!email || !passwd) {
      return res.status(400).json({
        error: "Correo y contraseña son requeridos",
      });
    }

    const usuario = await Usuario.fetchByEmail(email.trim());

    if (!usuario?.contrasena) {
      return res.status(401).json({
        error: "Credenciales inválidas",
      });
    }

    // Se compara lo ingresado con el hash obtenido de la DB
    const coincide = await bcrypt.compare(passwd, usuario.contrasena);
    if (!coincide) {
      return res.status(401).json({
        error: "Credenciales inválidas",
      });
    }

    const vida = vidaSeconds(usuario.rol);

    const token = jwt.sign(
      {
        uuid: usuario.usuario_id,
        email: usuario.email,
        rol: usuario.rol,
      },
      process.env.JWT_SECRET,
      {
        // Aquí también se especifica el algoritmo a usar
        expiresIn: vida,
      },
    );

    const esMovil = req.get("x-client") === "mobile";

    if (esMovil) {
      return res.status(200).json({
        user: {
          id: usuario.id_usuario,
          email: usuario.correo_electronico,
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
        email: usuario.correo_electronico,
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

// Cerrar sesión
export const postLogout = (req, res) => {
  res.clearCookie("token", cookieBase);
  return res.status(200).json({ ok: true });
};

// Para corroborar info. de la sesión
export const getMe = (req, res) => {
  // req.user lo puso verificarToken
  const { uuid, email, rol } = req.user;
  return res.status(200).json({ user: { id: uuid, email, rol } });
};
