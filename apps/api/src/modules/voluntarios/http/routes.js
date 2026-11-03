import { Router } from "express";
import { inscribirse } from "./voluntarios.controller.js";

const router = Router();

router.post("/programas/:idPrograma/inscripciones", inscribirse);

export { router };
export default router;

// GET /api/voluntarios/cliente/programas
router.get("/cliente/programas", clientController.getProgramasVigentes);
