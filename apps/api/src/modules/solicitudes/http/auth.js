import { supabase } from "../../../config/supabaseClient.js";

export async function requireAuth(req, res, next) {
  const header = req.headers.authorization ?? "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : null;

  if (!token) {
    return res.status(401).json({ message: "Falta el token de autenticación." });
  }

  const { data, error } = await supabase.auth.getUser(token);
  if (error || !data?.user) {
    console.error("Error de autenticación:", error?.message ?? "sin usuario");
    return res.status(401).json({ message: "Sesión inválida o expirada." });
  }

  req.user = data.user;
  next();
}
