import express from "express";
import verificarToken from "../../../middleware/is-auth.js";
import * as authController from "./auth.controller.js";

const router = express.Router();

// Rutas Login
router.post("/login", authController.postLogin);
router.post("/logout", authController.postLogout);
//    Notar cómo aquí sí verifica al token
router.post("/me", verificarToken, authController.getMe);

export default router;
