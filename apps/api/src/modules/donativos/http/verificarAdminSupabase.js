import { supabase } from "../../../config/supabaseClient.js";

export async function verificarAdminSupabase(req, res, next) {
  const authorization = req.get("authorization");

  const token = authorization?.match(/^Bearer\s+(.+)$/i)?.[1];

  if (!token) {
    return res.status(401).json({
      error: "Debes iniciar sesión para consultar donativos.",
    });
  }

  try {
    const { data, error } = await supabase.auth.getUser(token);

    if (error || !data.user) {
      return res.status(401).json({
        error: "Sesión inválida o expirada.",
      });
    }

    const { data: admin, error: adminError } = await supabase
      .from("admin")
      .select("id_usuario")
      .eq("id_usuario", data.user.id)
      .maybeSingle();

    if (adminError) {
      return res.status(503).json({
        error: "No fue posible verificar los permisos.",
      });
    }

    if (!admin) {
      return res.status(403).json({
        error: "Acceso exclusivo para administradores.",
      });
    }

    req.supabaseAccessToken = token;
    next();
  } catch (error) {
    console.error("Error al verificar administrador:", error);
    return res.status(500).json({
      error: "Error al verificar la sesión.",
    });
  }
}
