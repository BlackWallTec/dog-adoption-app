import express, { Router } from "express";
import { obtenerDonativosPendientes } from "./adminDonativos.controller.js";
import { TIPOS_COMPROBANTE, postDonativo } from "./donativoCliente.controller.js";
import {
  TIPOS_CONSTANCIA,
  getDatosFiscales,
  putConstancia,
  putDatosFiscales,
} from "./datosFiscales.controller.js";

import verificarToken from "../../../middleware/is-auth.js";
import requireRole from "../../../middleware/require-role.js";
import ROLES from "../../perfiles/http/modelosPerfiles/roles.model.js";

export const router = Router();

router.get("/", (req, res) => {
  res.json({ module: "donativos", items: [] });
});

router.get(
  "/admin/pendientes",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  obtenerDonativosPendientes,
);
