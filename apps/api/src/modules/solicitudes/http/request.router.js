import express from "express";
import verificarToken from "../../../middleware/is-auth.js";
import * as clientController from "./requestClient.controller.js";

const router = express.Router();

// GET /api/solicitudes/cliente/activa?idPerro=...
router.get("/cliente/activa", verificarToken, clientController.getActive);

// GET /api/solicitudes/cliente/mis-solicitudes
router.get("/cliente/mis-solicitudes", verificarToken, clientController.getMisSolicitudes);

// GET /api/solicitudes/cliente/perfil
router.get("/cliente/perfil", verificarToken, clientController.getProfile);
export default router;
