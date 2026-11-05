import { Router } from "express";
import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import { ROLES } from "../../perfiles/index.js";

const router = Router();

// Importar los controladores
import * as adminController from "./catalogAdmin.controller.js";
import * as clientController from "./catalogClient.controller.js";

// ===== Rutas Especiales =======

//POST api/catalogo/admin/solicitar-subida
//Esta ruta permite a un administrador solicitar una URL de subida para un archivo multimedia (imagen o video) que se asociará a un perro en el catálogo.
//Es necesario llamar despues a la ruta POST /api/catalogo/admin/perros para registrar el perro y asociar la URL de subida generada.
router.post("/admin/solicitar-subida", adminController.obtenerUrlSubidaMultimedia);

// ====== Fin Rutas Especiales =======

// ==== Rutas Admin ======
// GET /api/catalogo/admin/perros
router.get(
  "/admin/perros",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  adminController.getPerrosAdmin,
);

// POST /api/catalogo/admin/perros
router.post("/admin/perros", adminController.postNuevoPerro);

// GET /api/catalogo/admin/tamanos
router.get("/admin/tamanos", adminController.getTamanos);

// GET /api/catalogo/admin/caracteristicas
router.get("/admin/caracteristicas", adminController.getCaracteristicas);

// GET /api/catalogo/admin/cuidado_especial
router.get("/admin/cuidado_especial", adminController.getCuidadoEspecial);

// Get /api/catalogo/admin/admin/razas
router.get("/admin/razas", adminController.getRazas);

// ==== Fin Rutas Admin ======

// ===== Rutas Cliente ====

// Get /api/catalogo/cliente/perros/:id (¡nueva!)
router.get("/cliente/perros/:id", clientController.getDogById);

// La forma correcta si usaste export default:
export default router;

// ===== Fin Rutas Cliente =====
