import express from "express";
const router = express.Router();

import * as clientController from "./requestClient.controller.js";
import verificarToken from "../../../middleware/is-auth.js";

// El ID conserva el contrato de Android, pero solo puede referirse a la sesión.
router.use("/cliente", verificarToken);
const verificarPropietario = (req, res, next) => {
  const idUsuario = req.params.idUsuario ?? req.query.idUsuario;
  if (idUsuario && idUsuario !== req.user.uuid) {
    return res
      .status(403)
      .json({ success: false, message: "Solo puedes consultar tus propios datos." });
  }
  next();
};

// ===== Rutas Cliente =====
// Antes de "Enviar solicitud" solo hay lecturas (GET).

// GET /api/solicitudes/cliente/activa?idUsuario=...&idPerro=...
router.get("/cliente/activa", verificarPropietario, clientController.getActive);

// GET /api/solicitudes/cliente/perfil/:idUsuario
router.get("/cliente/perfil/:idUsuario", verificarPropietario, clientController.getProfile);

// Andy agregará: GET /cliente/documentos/:idUsuario
// Lucca agregará: POST /cliente (envío multipart)

export default router;
