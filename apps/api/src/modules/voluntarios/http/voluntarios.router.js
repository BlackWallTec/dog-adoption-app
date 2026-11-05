import { Router } from "express";
import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "../../perfiles/http/modelosPerfiles/roles.model.js";
import { getInscritos } from "./inscritosAdmin.controller.js";

export const router = Router();

router.get("/", (req, res) => {
  res.json({ module: "voluntarios", items: [] });
});

// Rutas Admin

// GET /api/voluntarios/admin/programas/:idPrograma/inscritos
// Pendiente de proteger con sesion y rol de administrador con el login de perfiles

router.get(
  "/admin/programas/:idPrograma/inscritos",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  getInscritos,
);

export default router;
