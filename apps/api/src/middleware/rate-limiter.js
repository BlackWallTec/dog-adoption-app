// ======== CODIGO GENERADO CON IA ====== //
import { Ratelimit } from "@upstash/ratelimit";
import { Redis } from "@upstash/redis";

// Se conecta automáticamente usando tus variables del .env
const redis = Redis.fromEnv();

// Limitador para usuarios autenticados (60 peticiones por minuto)
const limitadorPrivado = new Ratelimit({
  redis,
  limiter: Ratelimit.slidingWindow(60, "1 m"),
  prefix: "rl:user",
});

// Limitador más estricto para rutas públicas como Login/Registro (10 peticiones por minuto)
const limitadorPublico = new Ratelimit({
  redis,
  limiter: Ratelimit.slidingWindow(10, "1 m"),
  prefix: "rl:ip",
});

export async function rateLimiter(req, res, next) {
  // Opcional: Desactivar en desarrollo local para no gastar comandos gratuitos
  if (process.env.NODE_ENV === "development") {
    return next();
  }

  try {
    const userId = req.user?.id || req.user?.userId;

    let resultado;

    if (userId) {
      // CASO PRIVADO: Limitamos por el UUID del usuario
      resultado = await limitadorPrivado.limit(userId);
    } else {
      // CASO PÚBLICO: Limitamos por la IP real que envía Vercel
      const ip = req.headers["x-forwarded-for"]?.split(",") || "127.0.0.1";
      resultado = await limitadorPublico.limit(ip);
    }

    // Agregamos encabezados estándar para que tu Web/Android sepan su estado
    res.setHeader("X-RateLimit-Limit", resultado.limit);
    res.setHeader("X-RateLimit-Remaining", resultado.remaining);

    // 2. Si se pasó del límite, bloqueamos con error 429
    if (!resultado.success) {
      return res.status(429).json({
        error: "Demasiadas peticiones. Por favor, espera un minuto e intenta de nuevo.",
      });
    }

    // 3. Si todo está en orden, dejamos pasar la petición al controlador
    next();
  } catch (error) {
    // Si Redis llegara a fallar por red, dejamos pasar al usuario para no tumbar tu app (Fail-open)
    console.error("Error en Rate Limiter:", error);
    next();
  }
}

// ======== SECCION DE PRUEBAS ======== //
// Limitador de prueba: solo 3 peticiones cada 1 minuto
const limitadorPrueba = new Ratelimit({
  redis,
  limiter: Ratelimit.slidingWindow(3, "1 m"),
  prefix: "rl:test",
});

export async function rateLimiterPrueba(req, res, next) {
  try {
    // Usa el UUID si viene de verificarToken, o cae a la IP local
    const identificador =
      req.user?.id || req.user?.sub || req.headers["x-forwarded-for"] || "127.0.0.1";

    const { success, limit, remaining, reset } = await limitadorPrueba.limit(identificador);

    res.setHeader("X-RateLimit-Limit", limit);
    res.setHeader("X-RateLimit-Remaining", remaining);

    if (!success) {
      return res.status(429).json({
        ok: false,
        mensaje: "🛑 ¡Bloqueado por Rate Limiter! Espera 1 minuto.",
        restantes: 0,
      });
    }

    // Guardamos los restantes en req para verlos en la respuesta de prueba
    req.tokensRestantes = remaining;
    next();
  } catch (error) {
    console.error("Error conectando a Upstash:", error);
    return res.status(500).json({ error: "Fallo en Redis, revisa tus keys del .env" });
  }
}

// ======== FIN DE CODIGO GENERADO CON IA ====== //
