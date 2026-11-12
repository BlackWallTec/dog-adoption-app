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

// Rutas Cliente

// HU45 - datos fiscales del donante
router.get("/cliente/datos-fiscales/:idUsuario", getDatosFiscales);
router.put("/cliente/datos-fiscales/:idUsuario", putDatosFiscales);
// el archivo llega crudo, máximo 10 MB como en la app
router.put(
  "/cliente/datos-fiscales/:idUsuario/constancia",
  express.raw({ type: TIPOS_CONSTANCIA, limit: "10mb" }),
  putConstancia,
);

// HU46 - registrar donativo con comprobante (mismo límite que la app)
router.post(
  "/cliente/donativos/:idUsuario",
  express.raw({ type: TIPOS_COMPROBANTE, limit: "10mb" }),
  postDonativo,
);

router.get("/", (req, res) => {
  res.json({ module: "donativos", items: [] });
});
// HU44 - Consulta general de donativos pendientes
router.get("/pendientes/resumen", async (req, res) => {
  try {
    const { default: DonativoModel } = await import("./modelosDonativos/donativo.model.js");

    const donativos = await DonativoModel.getPendientes();

    const resumen = donativos.map((donativo) => ({
      id: donativo.id_donativo,
      fecha: donativo.fecha,
      estado: donativo.estado,
      donante: "Donante anónimo",
    }));

    return res.status(200).json(resumen);
  } catch (error) {
    console.error("Error al consultar donativos:", error);

    return res.status(500).json({
      error: "No fue posible consultar los donativos.",
    });
  }
});
router.get(
  "/admin/pendientes",
  verificarToken,
  requireRole(ROLES.ADMIN, ROLES.ROOT),
  obtenerDonativosPendientes,
);
