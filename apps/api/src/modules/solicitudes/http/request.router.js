import express from "express";
import * as clientController from "./requestClient.controller.js";

const router = express.Router();

// POST /api/solicitudes/cliente
router.post("/", clientController.createDraft);

// GET /api/solicitudes/cliente/activa?idPerro=...
router.get("/activa", clientController.getActive);

// GET /api/solicitudes/cliente/mis-solicitudes
router.get("/mis-solicitudes", clientController.getMisSolicitudes);

// GET /api/solicitudes/cliente/perfil
router.get("/perfil", clientController.getProfile);

// GET /api/solicitudes/cliente/:id/edicion
router.get("/:id/edicion", clientController.getParaEditar);

export default router;
