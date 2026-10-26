import { Router } from "express";
import { getPerrosAdmin } from "./catalogo.controller.js";

const router = Router();

// GET /api/catalogo/admin/perros
router.get("/admin/perros", getPerrosAdmin);

export default router;
