import { Router } from "express";
import { getSolicitudesAdmin } from "./solicitudAdmin.controller.js";

export const router = Router();

router.get("/admin/solicitudes", getSolicitudesAdmin);

router.get("/", (req, res) => {
  res.json({ module: "solicitudes", items: [] });
});
