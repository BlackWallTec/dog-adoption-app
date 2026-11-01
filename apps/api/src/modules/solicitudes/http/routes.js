import { Router } from "express";
import { requireAuth } from "./auth.js";
import { uploadDocumento } from "./upload.js";
import { subirDocumento, obtenerDocumentos } from "./documentos.controller.js";

export const router = Router();

router.get("/", (req, res) => {
  res.json({ module: "solicitudes", items: [] });
});
router.post("/:id/documentos", requireAuth, uploadDocumento, subirDocumento);
router.get("/:id/documentos", requireAuth, obtenerDocumentos);
