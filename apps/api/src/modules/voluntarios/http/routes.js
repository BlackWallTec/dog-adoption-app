import { Router } from "express";
import { inscribirse } from "./voluntarios.controller.js";
import * as clientController from "./voluntariadoClient.controller.js";

const router = Router();

// GET /api/voluntarios/cliente/programas
router.get("/cliente/programas", clientController.getProgramasVigentes);

router.post("/programas/:idPrograma/inscripciones", inscribirse);

export { router };
export default router;
