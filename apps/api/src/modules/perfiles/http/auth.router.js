import express from "express";
import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "./modelosPerfiles/roles.model.js";
import * as authController from "./auth.controller.js";
import * as registerController from "./register.controller.js";
import * as profileController from "./profile.controller.js";
import * as cpController from "./cps.controller.js";
import correoMovilRouter from "./correoMovil.router.js";

const router = express.Router();
router.use("/correo-movil", correoMovilRouter);

// Rutas Login
router.post("/login", authController.postLogin);
router.post("/logout", authController.postLogout);

router.get("/profile", verificarToken, profileController.getProfile);

// Rutas Crear cuenta
// Para cuentas de Cliente, en móviles
router.post("/register", registerController.postRegister);

// Para cuentas de Administrador, en web y para Root
router.post(
  "/register-admin",
  verificarToken,
  requireRole(ROLES.ROOT),
  registerController.postRegisterAdmin,
);

router.get("/codigos-postales", cpController.getCodigosPostales);

// Notar cómo aquí sí verifica al token. Sólo consulta, por eso es GET
router.get("/me", verificarToken, authController.getMe);

export default router;
