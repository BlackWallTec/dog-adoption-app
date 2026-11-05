import { Router } from "express";
import { getInscritos } from "./inscritosAdmin.controller.js";

export const router = Router();

router.get("/", (req, res) => {
  res.json({ module: "voluntarios", items: [] });
});

// Rutas Admin

// GET /api/voluntarios/admin/programas/:idPrograma/inscritos
// Pendiente de proteger con sesion y rol de administrador con el login de perfiles

router.get("/admin/programas/:idPrograma/inscritos", getInscritos);

export default router;
