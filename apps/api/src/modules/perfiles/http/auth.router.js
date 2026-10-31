import express from "express";
const router = express.Router();

// Importar los controladores
import * as authController from "./auth.controller.js";

// Rutas Login
router.get("/login", authController.getLogin);

router.post("/login", authController.postLogin);

export default router;
