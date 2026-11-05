import express from "express";
const router = express.Router();

import * as clientController from "./requestClient.controller.js";

// ===== Rutas Cliente =====
// Antes de "Enviar solicitud" solo hay lecturas (GET).

// GET /api/solicitudes/cliente/activa?idUsuario=...&idPerro=...
router.get("/cliente/activa", clientController.getActive);

// GET /api/solicitudes/cliente/perfil/:idUsuario
router.get("/cliente/perfil/:idUsuario", clientController.getProfile);

// Andy agregará: GET /cliente/documentos/:idUsuario
// Lucca agregará: POST /cliente (envío multipart)

export default router;
