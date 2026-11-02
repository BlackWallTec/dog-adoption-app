import { Router } from "express";
import * as clientController from "./voluntariadoClient.controller.js";

export const router = Router();

router.get("/", (req, res) => {
  res.json({ module: "voluntarios", items: [] });
});

// GET /api/voluntarios/cliente/programas
router.get("/cliente/programas", clientController.getProgramasVigentes);
