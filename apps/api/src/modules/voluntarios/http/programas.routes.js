import { Router } from "express";

import {
  obtenerTipos,
  registrarTipo,
  consultarEmpalmes,
  crearPrograma,
} from "./programaCreacion.controller.js";

export const router = Router();

// Restringe el acceso al entorno de desarrollo local
router.use((req, res, next) => {
  const conexionLocal = ["127.0.0.1", "::1", "::ffff:127.0.0.1"].includes(req.ip);

  const origen = req.get("Origin");

  const origenLocal = !origen || /^http:\/\/(localhost|127\.0\.0\.1):\d+$/.test(origen);

  if (process.env.NODE_ENV !== "development" || !conexionLocal || !origenLocal) {
    return res.status(403).json({
      message: "Solo disponible en desarrollo local.",
    });
  }

  next();
});

// Rutas de programas de voluntariado
router.get("/tipos", obtenerTipos);
router.post("/tipos", registrarTipo);
router.get("/empalmes", consultarEmpalmes);
router.post("/", crearPrograma);
