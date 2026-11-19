import { Router } from "express";
import {
  correoMovilHabilitado,
  servicioCorreoMovil,
  ErrorCorreoMovil,
} from "../services/correoMovil.service.js";

const router = Router();
const cuotas = new Map();
// Cuota de la instancia local; los cinco intentos por código también se comprueban en el servicio.
router.use((req, res, next) => {
  res.set("Cache-Control", "no-store");
  if (req.method === "GET") return next();
  const ahora = Date.now();
  for (const [ip, cuota] of cuotas) if (cuota.vence <= ahora) cuotas.delete(ip);
  const cuota = cuotas.get(req.ip) ?? { total: 0, vence: ahora + 15 * 60_000 };
  if (++cuota.total > 30 || cuotas.size >= 10_000)
    return res
      .set("Retry-After", "900")
      .status(429)
      .json({ code: "RATE_LIMITED", error: "Espera unos minutos antes de volver a intentarlo." });
  cuotas.set(req.ip, cuota);
  next();
});
const atender =
  (operacion, status = 200) =>
  async (req, res) => {
    try {
      const correo = req.body?.correo;
      if (
        typeof correo !== "string" ||
        correo.length > 254 ||
        !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo.trim())
      )
        throw new ErrorCorreoMovil("Ingresa un correo válido.", 422, "VALIDATION");
      const resultado = await operacion(
        servicioCorreoMovil(),
        correo.trim().toLowerCase(),
        req.body,
      );
      res.status(status).json(resultado);
    } catch (error) {
      const conocido = error instanceof ErrorCorreoMovil;
      if (!conocido) console.error("No se completó la operación de correo móvil.");
      res.status(conocido ? error.status : 500).json({
        code: conocido ? error.code : "INTERNAL",
        error: conocido ? error.message : "No pudimos completar la solicitud.",
      });
    }
  };
router.get("/config", (_req, res) => res.json({ habilitado: correoMovilHabilitado() }));
router.post(
  "/reenviar",
  atender((s, correo) => s.solicitar(correo, "confirmar"), 202),
);
router.post(
  "/recuperar",
  atender((s, correo) => s.solicitar(correo, "recuperar"), 202),
);
router.post(
  "/confirmar",
  atender((s, correo, b) => s.consumir(correo, b.codigo, "confirmar")),
);
router.post(
  "/restablecer",
  atender((s, correo, b) =>
    s.consumir(correo, b.codigo, "recuperar", b.contrasena, b.confirmContrasena),
  ),
);
export default router;
