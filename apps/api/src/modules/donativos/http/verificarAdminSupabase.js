import { supabase } from "../../../config/supabaseClient.js";

export async function verificarAdminSupabase(req, res, next) {
  const token = req.get("authorization")?.match(/^Bearer\s+(.+)$/i)?.[1];

  if (!token) {
    return res.status(401).json({ error: "No autenticado" });
  }

  try {
    const { data, error } = await supabase.auth.getUser(token);

    if (error || !data.user) {
      return res.status(401).json({ error: "Sesión inválida" });
    }

    const usuarioPruebaAutorizado = process.env.HU44_TEST_ADMIN_UID;

    if (!usuarioPruebaAutorizado || data.user.id !== usuarioPruebaAutorizado) {
      return res.status(403).json({
        error: "Acceso exclusivo para administradores",
      });
    }

    req.supabaseAccessToken = token;
    next();
  } catch (error) {
    console.error("Error de autenticación HU44:", error);
    return res.status(500).json({
      error: "No fue posible verificar la sesión",
    });
  }
}
