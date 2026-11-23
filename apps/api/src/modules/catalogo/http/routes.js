import express from "express";
const router = express.Router();

// Importar los controladores
import * as adminController from "./catalogAdmin.controller.js";
import * as clientController from "./catalogClient.controller.js";
import { rateLimiter } from "../../../middleware/rate-limiter.js";
import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "../../perfiles/http/modelosPerfiles/roles.model.js";

// HU19C: edición de fichas (solo Administrador o Root con sesión activa)
const soloAdmin = [
  verificarToken,
  rateLimiter({ max: 60, ventana: "1 m", prefijo: "admin" }),
  requireRole(ROLES.ADMIN, ROLES.ROOT),
];

// ===== Rutas Especiales =======

//POST api/catalogo/admin/solicitar-subida
//Esta ruta permite a un administrador solicitar una URL de subida para un archivo multimedia (imagen o video) que se asociará a un perro en el catálogo.
//Es necesario llamar despues a la ruta POST /api/catalogo/admin/perros para registrar el perro y asociar la URL de subida generada.
router.post("/admin/solicitar-subida", soloAdmin, adminController.obtenerUrlSubidaMultimedia);

// ====== Fin Rutas Especiales =======

// ==== Rutas Admin ======
// GET /api/catalogo/admin/perros
// Web: cookie `token` (no mandar x-client). Móvil: x-client mobile + Authorization Bearer.
router.get("/admin/perros", soloAdmin, adminController.getPerrosAdmin);

// POST /api/catalogo/admin/perros
router.post("/admin/perros", soloAdmin, adminController.postNuevoPerro);

// GET /api/catalogo/admin/perros/:id
router.get("/admin/perros/:id", ...soloAdmin, adminController.getPerroAdminById);

// PATCH /api/catalogo/admin/perros/:id
router.patch("/admin/perros/:id", ...soloAdmin, adminController.updatePerroAdmin);

// PATCH /api/catalogo/admin/perros/:id/desactivar
router.patch("/admin/perros/:id/desactivar", soloAdmin, adminController.desactivarPerro);

// GET /api/catalogo/admin/tamanos
router.get("/admin/tamanos", adminController.getTamanos);

// GET /api/catalogo/admin/caracteristicas
router.get("/admin/caracteristicas", adminController.getCaracteristicas);

// Get /api/catalogo/admin/admin/razas
router.get("/admin/razas", adminController.getRazas);

//Desactivar bulk de perros
// PATCH /api/catalogo/admin/perros/bulk/activar
router.patch("/admin/perros/bulk/activar", soloAdmin, adminController.patchActivateBulkDog);

// ==== Fin Rutas Admin ======

// ===== Rutas Cliente ====

// Get /api/catalogo/cliente/perros/:id
router.get("/cliente/perros/:id", clientController.getDogById);

//obtener todos los perros
router.get("/cliente/perros", clientController.getDogs);

// HU24: opciones de filtro (razas, tamaños y características)
// GET /api/catalogo/cliente/filtros
router.get("/cliente/filtros", clientController.getFiltros);

// ====== Seccion de pruebas de Rate Limiter ======= //
// Esto limita por IP //
router.get(
  "/test-limit",
  rateLimiter({ max: 3, ventana: "1 m", prefijo: "test", forzarEnDev: true }),
  (req, res) => {
    res.json({
      ok: true,
      mensaje: "✅ Petición exitosa",
      peticionesRestantes: req.tokensRestantes,
    });
  },
);

// ===== Fin de seccion de Rate Limiter ====== //

export default router;
