import jwt from "jsonwebtoken";

export default function verificarToken(req, res, next) {
  // En capa de request verifica existencia de la cookie y el token
  const token = req.cookies?.token;

  if (!token) {
    return res.status(401).json({ error: "No autenticado" });
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
