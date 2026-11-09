import jwt from "jsonwebtoken";

export const cookieBase = {
  httpOnly: true,
  secure: process.env.NODE_ENV === "production",
  sameSite: "lax",
  path: "/",
};

export function respondWithSession(req, res, usuario, email, status = 200) {
  const user = { id: usuario.id_usuario, email, rol: usuario.rol };
  const maxAge = ["Root", "Administrador"].includes(user.rol) ? 86400 : 30 * 86400;
  const token = jwt.sign({ uuid: user.id, email, rol: user.rol }, process.env.JWT_SECRET, {
    algorithm: "HS256",
    expiresIn: maxAge,
  });
  res.set("Cache-Control", "no-store");
  if (req.get("x-client") === "mobile") return res.status(status).json({ user, token });
  res.cookie("token", token, { ...cookieBase, maxAge: maxAge * 1000 });
  return res.status(status).json({ user });
}
