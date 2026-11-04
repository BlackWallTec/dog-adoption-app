import { Router } from "express";
import multer from "multer";
import { subirDocumento } from "./documentos.controller.js";

const router = Router();
const upload = multer({ storage: multer.memoryStorage() });

// La ruta únicamente recibe la petición, procesa el multipart con multer y delega al controlador
router.post("/:id/documentos", upload.single("archivo"), subirDocumento);

export { router };
