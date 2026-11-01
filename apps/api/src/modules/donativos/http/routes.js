import { Router } from "express";
import { obtenerDonativosPendientes } from "./adminDonativos.controller.js";

export const router = Router();

router.get("/", (req, res) => {
  res.json({ module: "donativos", items: [] });
});

function requireAdministrador(req, res) {
  return res.status(403).json({ error: "Acceso administrativo pendiente de habilitación." });
}

router.get("/admin/pendientes", requireAdministrador, obtenerDonativosPendientes);
