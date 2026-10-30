import { Router } from "express";
import { inscribirse } from "./voluntarios.controller.js";

const router = Router();

router.post("/programas/:idPrograma/inscripciones", inscribirse);

export { router };
export default router;
