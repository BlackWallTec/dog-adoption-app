import express, { Router } from "express";
import multer from "multer";
import {
  aprobarDonativoPendiente,
  obtenerDonativosPendientes,
  obtenerResumenDonativosPendientes,
} from "./adminDonativos.controller.js";
import {
  confirmarCargaCfdi,
  descargarCfdiCliente,
  listarCfdiCliente,
  manejarErrorSubidaCfdi,
  reintentarCorreoCfdi,
  validarCargaCfdi,
} from "./adminCfdi.controller.js";
import {
  obtenerDetalleSolicitudCfdiPendiente,
  obtenerSolicitudesCfdiPendientes,
} from "./adminSolicitudesCfdi.controller.js";
import { TIPOS_COMPROBANTE, postDonativo } from "./donativoCliente.controller.js";
import {
  TIPOS_CONSTANCIA,
  getDatosFiscales,
  putConstancia,
  putDatosFiscales,
} from "./datosFiscales.controller.js";

import { getMisDonativos, postSolicitudCfdi } from "./cfdiCliente.controller.js";

import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "../../perfiles/http/modelosPerfiles/roles.model.js";

export const router = Router();
const subirArchivosCfdi = multer({
  storage: multer.memoryStorage(),
  limits: { files: 2, fields: 1, fileSize: 10 * 1024 * 1024 },
}).fields([
  { name: "pdf", maxCount: 1 },
  { name: "xml", maxCount: 1 },
]);

// Rutas Cliente

// HU45 - datos fiscales del donante
router.get("/cliente/datos-fiscales/:idUsuario", getDatosFiscales);
router.put("/cliente/datos-fiscales/:idUsuario", putDatosFiscales);

// El archivo llega crudo, máximo 10 MB como en la app
router.put(
  "/cliente/datos-fiscales/:idUsuario/constancia",
  express.raw({ type: TIPOS_CONSTANCIA, limit: "10mb" }),
  putConstancia,
);

// HU46 - registrar donativo con comprobante
router.post(
  "/cliente/donativos/:idUsuario",
  express.raw({ type: TIPOS_COMPROBANTE, limit: "10mb" }),
  postDonativo,
);

//HU49 - mis donativos y pedir CFDI
router.get("/cliente/mis-donativos", verificarToken, getMisDonativos);
router.post("/cliente/mis-donativos/:idDonativo/cfdi", verificarToken, postSolicitudCfdi);

router.get("/", (req, res) => {
  res.json({ module: "donativos", items: [] });
});

router.get("/pendientes/resumen", obtenerResumenDonativosPendientes);

// HU44 - Listado administrativo de donativos pendientes
router.get(
  "/admin/pendientes",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  obtenerDonativosPendientes,
);
router.patch(
  "/admin/pendientes/:idDonativo/aprobar",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  aprobarDonativoPendiente,
);

router.get(
  "/admin/solicitudes-cfdi",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  obtenerSolicitudesCfdiPendientes,
);
router.get(
  "/admin/solicitudes-cfdi/:idSolicitud",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  obtenerDetalleSolicitudCfdiPendiente,
);

router.post(
  "/admin/solicitudes-cfdi/:idSolicitud/validar-cfdi",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  subirArchivosCfdi,
  manejarErrorSubidaCfdi,
  validarCargaCfdi,
);
router.post(
  "/admin/solicitudes-cfdi/:idSolicitud/cargar-cfdi",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  subirArchivosCfdi,
  manejarErrorSubidaCfdi,
  confirmarCargaCfdi,
);
router.post(
  "/admin/solicitudes-cfdi/:idSolicitud/reintentar-correo",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  reintentarCorreoCfdi,
);

router.get("/cliente/cfdi", verificarToken, requireRole(ROLES.CLIENT), listarCfdiCliente);
router.get(
  "/cliente/cfdi/:idSolicitud/:tipoArchivo",
  verificarToken,
  requireRole(ROLES.CLIENT),
  descargarCfdiCliente,
);
