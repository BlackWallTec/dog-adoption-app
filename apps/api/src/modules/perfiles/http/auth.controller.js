import "dotenv/config";
import bcrypt from "bcryptjs";
import Usuario from "./modelosPerfiles/user.model.js";
import ROLES from "./modelosPerfiles/roles.model.js";

export const postLogin = async (req, res) => {
  // En lugar de manejar ruta para android y ruta para web
  const aceptaHTML = req.accepts("html");

  try {
    const email = req.body.email;
    const passwd = req.body.passwd;

    const usuarioDB = await Usuario.fetchByEmail(email);

    // Sin usuarios coincidentes
    if (usuarioDB.rows.length === 0) {
      return res.status(401).json({});
    }

    // Selecciona el primer usuario siempre
    const usuario = usuarioDB.rows[0];
    // Se compara lo ingresado con el hash obtenido de la DB
    const coincide = await bcrypt.compare(passwd, usuario.contrasena);

    // Si no coinciden las credenciales
    if (!coincide) {
      return res.status(401).json({});
    }

    const vidaSeconds =
      usuario.rol === ROLES.ROOT
        ? 60 * 60
        : usuario.rol === ROLES.ADMIN
          ? 6 * 60 * 60
          : 72 * 60 * 60;

    const token = jwt.sign(
      {
        userId: usuario.usuario_id,
        email: usuario.email,
        rol: usuario.rol,
      },
      process.env.JWT_SECRET,
      {
        expiresIn: vidaSeconds,
      },
    );

    res.cookie("token", token, {
      // Impide que JS lea la cookie
      httpOnly: true,

      // HTTPS en producción
      // Asegurar que la aplicación se sirva vía HTTPS
      // o la cookie no funcionará
      secure: process.env.NODE_ENV === "production",

      // Rigor p/adjuntar cookie en solctd. iniciada
      // desde otros sitios
      sameSite: "lax",

      // Express usa milisegundos
      maxAge: vidaSeconds * 1000,

      // Rutas del sitio a las que se envía la cookie
      path: "/",
    });

    return res.status(200).json({});
  } catch (error) {
    if (aceptaHTML) {
      console.error(error);
      return res.status(500).send("Error interno del servidor");
    }

    return res.status(500).json({
      error: "Error interno del servidor",
    });
  }
};

export const getLogin = (req, res) => {
  // Renderizar vista de login
  res.render("/", { error: null });
};
