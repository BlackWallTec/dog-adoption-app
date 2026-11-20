import express from "express";
import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import { rateLimiter } from "../../../middleware/rate-limiter.js";
import ROLES from "../../../config/roles.js";
import * as authController from "./auth.controller.js";
import * as registerController from "./register.controller.js";
import * as profileController from "./profile.controller.js";
import * as cpController from "./cps.controller.js";
import correoMovilRouter from "./correoMovil.router.js";

const router = express.Router();
router.use("/correo-movil", correoMovilRouter);

// Cuotas independientes para autenticar y crear cuentas; el alta administrativa ya exige Root.
const limitarLogin = rateLimiter({ max: 10, ventana: "15 m", prefijo: "perfiles:login" });
const limitarRegistro = rateLimiter({ max: 10, ventana: "1 h", prefijo: "perfiles:registro" });
const limitarAltaAdmin = rateLimiter({
  max: 10,
  ventana: "1 h",
  prefijo: "perfiles:registro-admin",
});

// Rutas Login
router.post("/login", limitarLogin, authController.postLogin);
router.post("/logout", authController.postLogout);

router.get("/profile", verificarToken, profileController.getProfile);

// Rutas Crear cuenta
// Para cuentas de Cliente, en móviles
router.post("/register", limitarRegistro, registerController.postRegister);

// Para cuentas de Administrador, en web y para Root
router.post(
  "/register-admin",
  verificarToken,
  requireRole(ROLES.ROOT),
  limitarAltaAdmin,
  registerController.postRegisterAdmin,
);

router.get("/codigos-postales", cpController.getCodigosPostales);

// Notar cómo aquí sí verifica al token. Sólo consulta, por eso es GET
router.get("/me", verificarToken, authController.getMe);

export default router;
