import { getSupabaseAdmin } from "../../../config/supabaseAdmin.js";
import { guardarDocumento, listarDocumentos } from "./modelosSolicitudes/documentos.model.js";

const TIPOS = ["INE", "DOMICILIO"];

function responderError(res, error, mensajeGenerico) {
  console.error(error);
  if (error?.estadoHttp) {
    return res.status(error.estadoHttp).json({ message: error.message });
  }
  if (error?.code === "22P02") {
    return res.status(400).json({ message: "Identificador de solicitud inválido." });
  }
  return res.status(500).json({ message: mensajeGenerico });
}

export async function subirDocumento(req, res) {
  const tipo = req.body?.tipo;

  if (!TIPOS.includes(tipo)) {
    return res.status(400).json({ message: "Tipo de documento inválido." });
  }
  if (!req.file) {
    return res.status(400).json({ message: "Falta el archivo." });
  }

  try {
    const doc = await guardarDocumento(getSupabaseAdmin(), {
      userId: req.user.id,
      solicitudId: req.params.id,
      tipo,
      archivo: req.file,
    });
    return res.status(201).json(doc);
  } catch (error) {
    return responderError(res, error, "No se pudo guardar el documento.");
  }
}

export async function obtenerDocumentos(req, res) {
  try {
    const docs = await listarDocumentos(getSupabaseAdmin(), {
      userId: req.user.id,
      solicitudId: req.params.id,
    });
    return res.json(docs);
  } catch (error) {
    return responderError(res, error, "No se pudieron obtener los documentos.");
  }
}
