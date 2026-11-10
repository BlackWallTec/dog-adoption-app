import { Router } from "express";

import {
  obtenerTipos,
  registrarTipo,
  consultarEmpalmes,
  crearPrograma,
} from "./programaCreacion.controller.js";

export const router = Router();

// Rutas de programas de voluntariado
router.get("/tipos", obtenerTipos);
router.post("/tipos", registrarTipo);
router.get("/empalmes", consultarEmpalmes);
router.post("/", crearPrograma);
