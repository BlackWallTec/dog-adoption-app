import express from "express";
const router = express.Router();

// Importar los controladores (solo hay uno ahorita)
import * as clientController from "./requestClient.controller.js";

// Rutas de solicitudes

//Rutas Cliente

// GET /api/solicitudes/cliente/borrador?idUsuario=...&idPerro=...
router.get("/cliente/borrador", clientController.getDraft);

// POST /api/solicitudes/cliente
router.post("/cliente", clientController.createRequest);

// GET /api/solicitudes/cliente/perfil/:idUsuario
router.get("/cliente/perfil/:idUsuario", clientController.getProfile);

// GET /api/solicitudes/cliente/:idSolicitud/datos-generales
router.get("/cliente/:idSolicitud/datos-generales", clientController.getGeneralData);

// PUT /api/solicitudes/cliente/:idSolicitud/datos-generales
router.put("/cliente/:idSolicitud/datos-generales", clientController.updateGeneralData);

// GET /api/solicitudes/cliente/:idSolicitud/tu-hogar
router.get("/cliente/:idSolicitud/tu-hogar", clientController.getHome);

// PUT /api/solicitudes/cliente/:idSolicitud/tu-hogar
router.put("/cliente/:idSolicitud/tu-hogar", clientController.updateHome);

// GET /api/solicitudes/cliente/:idSolicitud/compromiso
router.get("/cliente/:idSolicitud/compromiso", clientController.getCommitment);

// PUT /api/solicitudes/cliente/:idSolicitud/compromiso
router.put("/cliente/:idSolicitud/compromiso", clienteController.updateCommitment)


export default router;
