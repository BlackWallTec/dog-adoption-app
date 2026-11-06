import { Router } from "express";
import { getSolicitudesAdmin } from "./solicitudAdmin.controller.js";
import clientRouter from "./request.router.js";

export const router = Router();

// Rutas de la app (cliente): /cliente/activa, /cliente/perfil/:idUsuario, ...
router.use(clientRouter);

router.get("/admin/solicitudes", getSolicitudesAdmin);

router.get("/", (req, res) => {
  res.json({ module: "solicitudes", items: [] });
});
