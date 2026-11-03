import express, { Router } from "express";
import {
  TIPOS_CONSTANCIA,
  getDatosFiscales,
  putConstancia,
  putDatosFiscales,
} from "./datosFiscales.controller.js";

export const router = Router();

router.get("/", (req, res) => {
  res.json({ module: "donativos", items: [] });
});

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
