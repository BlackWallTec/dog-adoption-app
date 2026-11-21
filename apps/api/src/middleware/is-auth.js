import jwt from "jsonwebtoken";
import { verificarCorreoMovil } from "../modules/perfiles/index.js";
import { ERROR_CODES, errorBody } from "@qarino/shared/errores.js";

/*
IMPORTANTE
Considerar que desde el cliente, android, se declara el x-client.

Se refiere a un header personalizado que el middleware interpreta como
proveniencia desde android.

Si esto no se mandara, la búsqueda de la cookie será infructuosa y
resultará en 401.

Ejemplo de una ruta:
router.post("/logout", authController.postLogout);

Ahora un ruta que verifica el token:
router.get("/me", verificarToken, authController.getMe);
*/

export default async function verificarToken(req, res, next) {
  // En capa de request verifica existencia de la cookie y el token

  const esMovil = req.get("x-client") === "mobile";

  // La IA me ayudó con el patrón, que busca Bearer al inicio,
  // seguido de uno o más espacios; i para mayúsculas/minúsculas.
  const token = esMovil
    ? req.get("authorization")?.match(/^Bearer\s+(.+)$/i)?.[1]
    : req.cookies?.token;

  if (!token) {
    return res.status(401).json({
      error: "No autenticado",
    });
  }

  // JWT verifica validez del token
  try {
    req.user = jwt.verify(token, process.env.JWT_SECRET, {
      algorithms: ["HS256"],
    });
  } catch {
    return res.status(401).json({
      error: "Token inválido o expirado",
    });
  }

  try {
    // Sin la extensión habilitada conserva la validación original. No afecta Root ni Administrador.
    if (!(await verificarCorreoMovil(req.user)))
      return res.status(401).json({ error: "Token inválido o expirado" });
  } catch {
    return res
      .status(503)
      .json(errorBody(ERROR_CODES.SERVICE_UNAVAILABLE, "Servicio temporalmente no disponible"));
  }
  next();
}
