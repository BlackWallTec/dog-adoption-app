import express from "express";
import verificarToken from "../../../middleware/is-auth.js";
import * as authController from "./auth.controller.js";
import { createLoginThrottle } from "./auth.throttle.js";

const router = express.Router();
router.use((_req, res, next) => {
  res.set("Cache-Control", "no-store");
  next();
});

// Rutas Login
router.post("/login", createLoginThrottle(), authController.postLogin);
router.post("/register", authController.postRegister);
router.post("/logout", authController.postLogout);
//    Notar cómo aquí sí verifica al token
router.post("/me", verificarToken, authController.getMe);
router.get("/profile", verificarToken, authController.getProfile);

export default router;
