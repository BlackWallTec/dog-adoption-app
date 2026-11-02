import { Router } from "express";
import { obtenerDonativosPendientes } from "./adminDonativos.controller.js";
import { verificarAdminSupabase } from "./verificarAdminSupabase.js";

export const router = Router();

router.get("/", (req, res) => {
  res.json({ module: "donativos", items: [] });
});

router.get("/admin/pendientes", verificarAdminSupabase, obtenerDonativosPendientes);
