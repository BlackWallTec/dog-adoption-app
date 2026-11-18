import { Router } from "express";
import multer from "multer";
import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "../../perfiles/http/modelosPerfiles/roles.model.js";
import { getSolicitudAdminDetalle, getSolicitudesAdmin } from "./solicitudAdmin.controller.js";
import { MAX_SIZE_BYTES, manejarErrorSubida, subirDocumento } from "./documentos.controller.js";
import clientRouter from "./request.router.js";

const router = Router();
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_SIZE_BYTES, files: 1 },
});

// Solo Administrador o Root con sesion activa
const soloAdmin = [verificarToken, requireRole(ROLES.ADMIN, ROLES.ROOT)];

router.post("/:id/documentos", upload.single("archivo"), subirDocumento, manejarErrorSubida);

// Rutas de la app (cliente): /cliente/activa, /cliente/perfil/:idUsuario, ...
router.use(clientRouter);

// ===== Rutas Admin =====
// GET /api/solicitudes/admin/solicitudes
router.get("/admin/solicitudes", soloAdmin, getSolicitudesAdmin);

// GET /api/solicitudes/admin/solicitudes/:id
router.get("/admin/solicitudes/:id", soloAdmin, getSolicitudAdminDetalle);

router.get("/", (req, res) => {
  res.json({ module: "solicitudes", items: [] });
});

export { router };
