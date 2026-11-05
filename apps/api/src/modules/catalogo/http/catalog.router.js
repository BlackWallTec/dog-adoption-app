import express from "express";
const router = express.Router();

// Importar los controladores
import * as adminController from "./catalogAdmin.controller.js";
import * as clientController from "./catalogClient.controller.js";

import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "../../perfiles/http/modelosPerfiles/roles.model.js";

// Rutas del catálogo

//Rutas Admin

// GET /api/catalogo/admin/perros
router.get("/admin/perros", adminController.getPerrosAdmin);

//Rutas Cliente

// GET /api/catalogo/cliente/filtros (opciones de la hoja de filtros, HU24)
router.get("/cliente/filtros", clientController.getFilterOptions);

// GET /api/catalogo/cliente/perros?sexo=&tamano=&raza=&edad=&caracter= (lista con filtros, HU16/HU24)
router.get("/cliente/perros", clientController.getDogsFiltered);

// Get /api/catalogo/cliente/perros/:id (¡nueva!)
router.get("/cliente/perros/:id", clientController.getDogById);

// HU19C: edición de fichas (solo Administrador o Root con sesión activa)
const soloAdmin = [verificarToken, requireRole(ROLES.ADMIN, ROLES.ROOT)];
router.get("/admin/perros/:id", ...soloAdmin, adminController.getPerroAdminById);
router.patch("/admin/perros/:id", ...soloAdmin, adminController.updatePerroAdmin);
router.get("/admin/razas", ...soloAdmin, adminController.getRazasAdmin);
router.get("/admin/tamanos", ...soloAdmin, adminController.getTamanosAdmin);

export default router;
