import express from "express";
const router = express.Router();

// Importar los controladores
import * as adminController from "./catalogAdmin.controller.js";
import * as clientController from "./catalogClient.controller.js";

import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "../../perfiles/http/modelosPerfiles/roles.model.js";

// ===== Rutas Especiales =======

//POST api/catalogo/admin/solicitar-subida
//Esta ruta permite a un administrador solicitar una URL de subida para un archivo multimedia (imagen o video) que se asociará a un perro en el catálogo.
//Es necesario llamar despues a la ruta POST /api/catalogo/admin/perros para registrar el perro y asociar la URL de subida generada.
router.post("/admin/solicitar-subida", adminController.obtenerUrlSubidaMultimedia);

// ====== Fin Rutas Especiales =======

// ==== Rutas Admin ======
// GET /api/catalogo/admin/perros
router.get("/admin/perros", adminController.getPerrosAdmin);

// POST /api/catalogo/admin/perros
router.post("/admin/perros", adminController.postNuevoPerro);

// HU19C: edición de fichas (solo Administrador o Root con sesión activa)
const soloAdmin = [verificarToken, requireRole(ROLES.ADMIN, ROLES.ROOT)];

// GET /api/catalogo/admin/perros/:id
router.get("/admin/perros/:id", ...soloAdmin, adminController.getPerroAdminById);

// PATCH /api/catalogo/admin/perros/:id
router.patch("/admin/perros/:id", ...soloAdmin, adminController.updatePerroAdmin);

// PATCH /api/catalogo/admin/perros/:id/desactivar
router.patch("/admin/perros/:id/desactivar", adminController.desactivarPerro);

// GET /api/catalogo/admin/tamanos
router.get("/admin/tamanos", adminController.getTamanos);

// GET /api/catalogo/admin/caracteristicas
router.get("/admin/caracteristicas", adminController.getCaracteristicas);

// GET /api/catalogo/admin/cuidado_especial
router.get("/admin/cuidado_especial", adminController.getCuidadoEspecial);

// Get /api/catalogo/admin/admin/razas
router.get("/admin/razas", adminController.getRazas);

//Desactivar bulk de perros
// PATCH /api/catalogo/admin/perros/bulk/activar
router.patch("/admin/perros/bulk/activar", adminController.patchActivateBulkDog);

// ==== Fin Rutas Admin ======

// ===== Rutas Cliente ====

// Get /api/catalogo/cliente/perros/:id
router.get("/cliente/perros/:id", clientController.getDogById);

//obtener todos los perros
router.get("/cliente/perros", clientController.getDogs);

export default router;
