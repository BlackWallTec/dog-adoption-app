import { Router } from "express";
import multer from "multer";
import { subirDocumento } from "./documentos.controller.js";
import { getSolicitudesAdmin } from "./solicitudAdmin.controller.js";
import clientRouter from "./request.router.js";

const router = Router();
const upload = multer({ storage: multer.memoryStorage() });

// La ruta únicamente recibe la petición, procesa el multipart con multer y delega al controlador
router.post("/:id/documentos", upload.single("archivo"), subirDocumento);

export { router };
// Rutas de la app (cliente): /cliente/activa, /cliente/perfil/:idUsuario, ...
router.use(clientRouter);

router.get("/admin/solicitudes", getSolicitudesAdmin);

router.get("/", (req, res) => {
  res.json({ module: "solicitudes", items: [] });
});
