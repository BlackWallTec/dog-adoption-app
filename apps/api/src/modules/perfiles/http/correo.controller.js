import { correoHabilitado, servicioCorreo, ErrorCorreo } from "../services/correo.service.js";

// Límite por IP del proceso local. La cuota por destinatario también se comprueba en el servicio.
const intentos = new Map();
export function limitarCorreo(req, res, next) {
  const ahora = Date.now();
  for (const [ip, cuota] of intentos) if (cuota.vence <= ahora) intentos.delete(ip);
  const cuota = intentos.get(req.ip) ?? { cantidad: 0, vence: ahora + 15 * 60_000 };
  if (++cuota.cantidad > 30 || intentos.size > 10_000) {
    return res
      .set("Retry-After", "900")
      .status(429)
      .json({ code: "RATE_LIMITED", error: "Espera unos minutos antes de volver a intentarlo." });
  }
  intentos.set(req.ip, cuota);
  next();
}

const atender = (operacion) => async (req, res) => {
  try {
    const resultado = await operacion(req);
    res
      .set("Cache-Control", "no-store")
      .status(resultado.status ?? 200)
      .json(resultado.body ?? resultado);
  } catch (error) {
    const conocido = error instanceof ErrorCorreo;
    if (!conocido) console.error("No se pudo completar la operación de correo.");
    res.status(conocido ? error.status : 500).json({
      code: conocido ? error.code : "INTERNAL_ERROR",
      error: conocido ? error.message : "No pudimos completar la solicitud. Inténtalo de nuevo.",
    });
  }
};

const solicitar = (tipo) =>
  atender(async (req) => {
    const correo = req.body?.correo;
    if (
      typeof correo !== "string" ||
      correo.length > 254 ||
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo.trim())
    ) {
      throw new ErrorCorreo("Ingresa un correo válido.", 422, "VALIDATION_ERROR");
    }
    return {
      status: 202,
      body: await servicioCorreo().solicitar(correo.trim().toLowerCase(), tipo),
    };
  });

export const reenviar = solicitar("confirmar");
export const recuperar = solicitar("recuperar");
export const confirmar = atender((req) => servicioCorreo().consumir(req.body?.token, "confirmar"));
export const restablecer = atender((req) =>
  servicioCorreo().consumir(
    req.body?.token,
    "recuperar",
    req.body?.contrasena,
    req.body?.confirmContrasena,
  ),
);
export const configuracion = (_req, res) =>
  res.json({
    habilitado: correoHabilitado(),
    demo:
      correoHabilitado() &&
      (!process.env.EMAIL_TRANSPORT || process.env.EMAIL_TRANSPORT === "inbox") &&
      process.env.NODE_ENV !== "production",
  });

// No se publica la bandeja en producción ni a clientes remotos, incluso con el modo mal configurado.
export const bandeja = atender((req) => {
  const local = ["127.0.0.1", "::1", "::ffff:127.0.0.1"].includes(req.socket.remoteAddress);
  const host = req.hostname;
  if (
    !local ||
    !["localhost", "127.0.0.1"].includes(host) ||
    process.env.NODE_ENV === "production" ||
    (process.env.EMAIL_TRANSPORT && process.env.EMAIL_TRANSPORT !== "inbox") ||
    !correoHabilitado()
  ) {
    throw new ErrorCorreo("No disponible.", 404, "NOT_FOUND");
  }
  return { mensajes: servicioCorreo().bandeja() };
});
