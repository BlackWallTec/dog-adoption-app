import { Router } from "express";
import { rateLimiter } from "../../../middleware/rate-limiter.js";
import * as correoController from "./correoMovil.controller.js";

const router = Router();
const limitarCorreos = rateLimiter({ max: 30, ventana: "15 m", prefijo: "perfiles:correo-movil" });

router.get("/config", correoController.getConfiguracion);
router.post("/reenviar", limitarCorreos, correoController.postReenviar);
router.post("/recuperar", limitarCorreos, correoController.postRecuperar);
router.post("/confirmar", limitarCorreos, correoController.postConfirmar);
router.post("/restablecer", limitarCorreos, correoController.postRestablecer);
export default router;
