import { Router } from "express";
import { getSolicitudesAdmin } from "./solicitudAdmin.controller.js";
import clientRouter from "./request.router.js";

export const router = Router();

router.get("/admin/solicitudes", getSolicitudesAdmin);

router.get("/", (req, res) => {
  res.json({ module: "solicitudes", items: [] });
});
