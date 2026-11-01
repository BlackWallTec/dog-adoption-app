import jwt from "jsonwebtoken";

/*
IMPORTANTE
Considerar que desde el cliente, se declara el x-client. Si esto no se
mandara desde android, la búsqueda de la cookie será infructuosa y
resultará en 401.

También, no se especificó un algoritmo ni aquí ni en el
auth.controller.js, por defecto está usando HS256.

En el postLogin del auth.controller.js también debe usarse el mismo
header.
*/

export default function verificarToken(req, res, next) {
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
    req.user = jwt.verify(token, process.env.JWT_SECRET);
    next();
  } catch {
    return res.status(401).json({
      error: "Token inválido o expirado",
    });
  }
}
