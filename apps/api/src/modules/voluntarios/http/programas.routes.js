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
  next();
});

// Rutas de programas de voluntariado
router.get("/tipos", obtenerTipos);
router.post("/tipos", registrarTipo);
router.get("/empalmes", consultarEmpalmes);
router.post("/", crearPrograma);
