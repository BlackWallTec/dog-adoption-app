import express from "express";
import multer from "multer";
import * as clientController from "./requestClient.controller.js";
import { subirDocumento } from "./documentos.controller.js";
import verificarToken from "../../../middleware/is-auth.js";

const router = express.Router();
const upload = multer({ storage: multer.memoryStorage() });

// GET /api/solicitudes/cliente/activa?idUsuario=...&idPerro=...
router.get("/cliente/activa", verificarToken, clientController.getActive);

// GET /api/solicitudes/cliente/mis-solicitudes
router.get("/cliente/mis-solicitudes", verificarToken, clientController.getMisSolicitudes);

// GET /api/solicitudes/cliente/perfil/:idUsuario
router.get("/cliente/perfil", verificarToken, clientController.getProfile);
// POST /api/solicitudes/:id/documentos  (multipart: "archivo" y "tipo")
router.post("/:id/documentos", verificarToken, upload.single("archivo"), subirDocumento);

export default router;
