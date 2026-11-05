import { Router } from "express";
import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "../../perfiles/http/modelosPerfiles/roles.model.js";

const router = Router();

// Importar los controladores
import * as adminController from "./catalogAdmin.controller.js";
import * as clientController from "./catalogClient.controller.js";

// Rutas del catálogo

//Rutas Admin

// GET /api/catalogo/admin/perros
router.get(
  "/admin/perros",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  adminController.getPerrosAdmin,
);

//Rutas Cliente

// Get /api/catalogo/cliente/perros/:id (¡nueva!)
router.get("/cliente/perros/:id", clientController.getDogById);

// La forma correcta si usaste export default:
export default router;
