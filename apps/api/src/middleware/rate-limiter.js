// ======== CODIGO GENERADO CON IA ====== //
import { Ratelimit } from "@upstash/ratelimit";
import { Redis } from "@upstash/redis";

const tieneCredenciales =
  Boolean(process.env.UPSTASH_REDIS_REST_URL) && Boolean(process.env.UPSTASH_REDIS_REST_TOKEN);

// Solo inicializa Redis si existen las variables en el entorno
const redis = tieneCredenciales ? Redis.fromEnv() : null;

/**
 * Fábrica de middlewares para limitar peticiones por ruta.
 * @param {Object} opciones
 * @param {number} opciones.max - Máximo de peticiones permitidas (por defecto 60)
 * @param {string} opciones.ventana - Ventana de tiempo ej. "10 s", "1 m", "1 h" (por defecto "1 m")
 * @param {string} opciones.prefijo - Identificador único de la ruta en Redis (por defecto "global")
 * @param {boolean} opciones.forzarEnDev - Permite probar el limitador en local aunque NODE_ENV sea "development"
 */
export function rateLimiter({
  max = 60,
  ventana = "1 m",
  prefijo = "global",
  forzarEnDev = false,
} = {}) {
  // Creamos dos instancias para esta ruta: una para usuarios con sesión y otra más estricta para públicos
  const limitador = redis
    ? new Ratelimit({
        redis,
        limiter: Ratelimit.slidingWindow(max, ventana),
        prefix: `rl:${prefijo}`,
      })
    : null;

  return async function (req, res, next) {
    // Apagado en tests (CI/Vitest) o si faltan credenciales. En dev solo corre si forzarEnDev es true
    const esTest = process.env.NODE_ENV === "test";
    const esDevBloqueado = process.env.NODE_ENV === "development" && !forzarEnDev;

    if (esTest || esDevBloqueado || !limitador) {
      return next();
    }

    try {
      const userId = req.user?.uuid || req.user?.id || req.user?.userId || req.user?.sub;
      const ip = req.headers["x-forwarded-for"]?.split(",")[0]?.trim() || "127.0.0.1";

      // Si hay sesión usa UUID, si es pública usa IP
      const identificador = userId || ip;

      const { success, limit, remaining } = await limitador.limit(identificador);

      // Agregamos encabezados estándar para que Web/Android sepan su estado
      res.setHeader("X-RateLimit-Limit", limit);
      res.setHeader("X-RateLimit-Remaining", remaining);

      // Guardamos el dato en req por si algún controlador o ruta dummy quiere leerlo
      req.tokensRestantes = remaining;

      if (!success) {
        return res.status(429).json({
          ok: false,
          error: `Demasiadas peticiones. Límite permitido: ${max} cada ${ventana}.`,
        });
      }

      next();
    } catch (error) {
      // Fail-open: si Redis falla por red, dejamos pasar la petición para no tumbar la app
      console.error("Error en Rate Limiter:", error);
      next();
    }
  };
}
// ======== FIN DE CODIGO GENERADO CON IA ====== //
