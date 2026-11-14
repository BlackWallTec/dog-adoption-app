import { Router } from "express";
import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "../../perfiles/http/modelosPerfiles/roles.model.js";
import { inscribirse } from "./voluntarios.controller.js";
import * as clientController from "./voluntariadoClient.controller.js";
import { getInscritos } from "./inscritosAdmin.controller.js";
import { router as programas } from "./programas.routes.js";
import * as edicionController from "./programaEdicion.controller.js";

export const router = Router();

// Rutas Cliente

// GET /api/voluntarios/cliente/programas
router.get("/cliente/programas", clientController.getProgramasVigentes);

// POST /api/voluntarios/programas/:idPrograma/inscripciones
router.post("/programas/:idPrograma/inscripciones", inscribirse);

// Rutas Admin

// GET /api/voluntarios/admin/programas/:idPrograma/inscritos
// Requiere sesión (401) y rol Administrador o Root (403).
router.get(
  "/admin/programas/:idPrograma/inscritos",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  getInscritos,
);

// HU38 editar y cancelar programas solo para administador o root
const soloAdmin = [verificarToken, requireRole(ROLES.ADMIN, ROLES.ROOT)];

// GET /api/voluntarios/admin/programas/:idPrograma
router.get("/admin/programas/:idPrograma", ...soloAdmin, edicionController.obtenerParaEditar);

// PUT /api/voluntarios/admin/programas/:idPrograma
router.put("/admin/programas/:idPrograma", ...soloAdmin, edicionController.editarPrograma);

// POST /api/voluntarios/admin/programas/:idPrograma/cancelacion
router.post(
  "/admin/programas/:idPrograma/cancelacion",
  ...soloAdmin,
  edicionController.cancelarPrograma,
);

// Rutas de creación de programas (HU31)
router.use("/programas", verificarToken, requireRole(ROLES.ADMIN, ROLES.ROOT), programas);

export default router;
