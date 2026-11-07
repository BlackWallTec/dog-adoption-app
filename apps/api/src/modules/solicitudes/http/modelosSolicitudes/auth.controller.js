import { createClient } from "@supabase/supabase-js";

function crearClienteSinSesion() {
  return createClient(process.env.SUPABASE_URL, process.env.SUPABASE_ANON_KEY, {
    auth: { persistSession: false, autoRefreshToken: false, detectSessionInUrl: false },
  });
}

export async function login(req, res) {
  const { email, password } = req.body ?? {};

  if (typeof email !== "string" || typeof password !== "string" || !email.trim() || !password) {
    return res.status(400).json({ message: "Escribe tu correo y tu contraseña." });
  }

  try {
    const { data, error } = await crearClienteSinSesion().auth.signInWithPassword({
      email: email.trim(),
      password,
    });

    if (error || !data?.session) {
      console.error("Error de login:", error?.code ?? error?.message);
      if (error?.code === "email_not_confirmed") {
        return res.status(403).json({ message: "Confirma tu correo antes de iniciar sesión." });
      }
      if (error?.status === 429) {
        return res.status(429).json({ message: "Demasiados intentos. Espera un momento." });
      }
      if (error?.status === 400) {
        return res.status(401).json({ message: "Correo o contraseña incorrectos." });
      }
      return res.status(500).json({ message: "No se pudo iniciar sesión." });
    }

    return res.json({
      accessToken: data.session.access_token,
      refreshToken: data.session.refresh_token,
      expiresIn: data.session.expires_in,
    });
  } catch (error) {
    console.error("Error de login:", error?.message);
    return res.status(500).json({ message: "No se pudo iniciar sesión." });
  }
}
