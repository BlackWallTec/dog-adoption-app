import { Router } from "express";
import multer from "multer";
import { getSolicitudesAdmin } from "./solicitudAdmin.controller.js";
import { MAX_SIZE_BYTES, manejarErrorSubida, subirDocumento } from "./documentos.controller.js";
import clientRouter from "./request.router.js";

const router = Router();
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_SIZE_BYTES, files: 1 },
});

// La ruta únicamente recibe la petición, procesa el multipart con multer y delega al controlador
router.post("/:id/documentos", upload.single("archivo"), subirDocumento, manejarErrorSubida);

export { router };
// Rutas de la app (cliente): /cliente/activa, /cliente/perfil/:idUsuario, ...
router.use(clientRouter);

router.get("/admin/solicitudes", getSolicitudesAdmin);

router.get("/", (req, res) => {
  res.json({ module: "solicitudes", items: [] });
});
