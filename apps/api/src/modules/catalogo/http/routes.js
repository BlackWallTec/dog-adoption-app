import { Router } from "express";

const router = Router();

// Importar los controladores
import * as adminController from "./catalogAdmin.controller.js";
import * as clientController from "./catalogClient.controller.js";

// Rutas del catálogo

//Rutas Admin

// GET /api/catalogo/admin/perros
router.get("/admin/perros", adminController.getPerrosAdmin);

// POST /api/catalogo/admin/perros
router.post("/admin/perros", adminController.postNuevoPerro);

//Rutas Cliente

// Get /api/catalogo/cliente/perros/:id (¡nueva!)
router.get("/cliente/perros/:id", clientController.getDogById);

// La forma correcta si usaste export default:
export default router;
