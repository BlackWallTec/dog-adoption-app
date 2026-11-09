import express from "express";
import verificarToken from "../../../middleware/is-auth.js";
import * as authController from "./auth.controller.js";
import * as registerController from "./register.controller.js";
import * as profileController from "./profile.controller.js";
import * as cpController from "./cps.controller.js";

const router = express.Router();

// Rutas Login
router.post("/login", authController.postLogin);
router.post("/logout", authController.postLogout);

router.get("/profile", profileController.getProfile);

// Rutas Crear cuenta
router.post("/register", registerController.postRegister);

router.get("/codigos-postales", limitarCodigosPostales, cpController.getCodigosPostales);

//    Notar cómo aquí sí verifica al token
router.post("/me", verificarToken, authController.getMe);

export default router;
