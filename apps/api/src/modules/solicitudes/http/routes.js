import { Router } from "express";
import multer from "multer";
import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "../../perfiles/http/modelosPerfiles/roles.model.js";
import {
  getSolicitudAdminDetalle,
  getSolicitudesAdmin,
  adoptionRequest,
} from "./solicitudAdmin.controller.js";
import { MAX_SIZE_BYTES, manejarErrorSubida, subirDocumento } from "./documentos.controller.js";
import { createDraft, submitSolicitud } from "./requestClient.controller.js";
import clientRouter from "./request.router.js";

const router = Router();
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_SIZE_BYTES, files: 1 },
});

// Solo Administrador o Root con sesion activa
const soloAdmin = [verificarToken, requireRole(ROLES.ADMIN, ROLES.ROOT)];

// Crear o reutilizar borrador (POST /api/solicitudes)
router.post("/", verificarToken, createDraft);

// Subir o reemplazar documento (POST /api/solicitudes/:id/documentos)
router.post(
  "/:id/documentos",
  verificarToken,
  upload.single("archivo"),
  subirDocumento,
  manejarErrorSubida,
);

// Finalizar y enviar solicitud (POST /api/solicitudes/:id/enviar)
router.post("/:id/enviar", verificarToken, submitSolicitud);

// Rutas de la app (cliente): /cliente/activa, /cliente/mis-solicitudes, /cliente/perfil
router.use("/cliente", verificarToken, clientRouter);
// ===== Rutas Admin =====
router.get("/admin/solicitudes", soloAdmin, getSolicitudesAdmin);
router.post("/admin/solicitudes/:id/resolucion", soloAdmin, adoptionRequest);
router.get("/admin/solicitudes/:id", soloAdmin, getSolicitudAdminDetalle);

router.get("/", (req, res) => {
  res.json({ module: "solicitudes", items: [] });
});

export { router };
