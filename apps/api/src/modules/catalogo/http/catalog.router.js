import express from "express";
const router = express.Router();

// Importar los controladores
import * as adminController from "./catalogAdmin.controller.js";
import * as clientController from "./catalogClient.controller.js";

// Rutas del catálogo

//Rutas Admin

// GET /api/catalogo/admin/perros
router.get("/admin/perros", adminController.getPerrosAdmin);

//Rutas Cliente

//obtener todos los perros
router.get("/cliente/perros", clientController.getDogs);

// Get /api/catalogo/cliente/perros/:id (¡nueva!)
router.get("/cliente/perros/:id", clientController.getDogById);

export default router;
