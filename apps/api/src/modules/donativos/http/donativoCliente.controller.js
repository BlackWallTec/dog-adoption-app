import DonativoModel from "./modelosDonativos/donativo.model.js";

// Lo que se decidió en el spike de la HU46
export const TIPOS_COMPROBANTE = ["application/pdf", "image/jpeg", "image/png"];

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const MONTO_REGEX = /^\d{1,10}(\.\d{1,2})?$/;

// POST /api/donativos/cliente/donativos/:idUsuario?monto=500&referencia=abc
// El comprobante llega crudo en el body (Content-Type = tipo del archivo)
export const postDonativo = async (req, res) => {
  try {
    const { idUsuario } = req.params;
    const monto = String(req.query.monto ?? "").trim();
    const referencia = String(req.query.referencia ?? "").trim();
    const tipo = req.get("content-type")?.split(";")[0].trim();

    if (!UUID_REGEX.test(idUsuario)) {
      return res.status(400).json({ success: false, message: "Usuario inválido." });
    }
    if (!MONTO_REGEX.test(monto) || Number(monto) <= 0) {
      return res.status(400).json({ success: false, message: "Escribe un monto válido." });
    }
    if (referencia.length > 200) {
      return res.status(400).json({ success: false, message: "La referencia es muy larga." });
    }
    // CA-02: sin comprobante no hay donativo
    if (!TIPOS_COMPROBANTE.includes(tipo) || !Buffer.isBuffer(req.body) || req.body.length === 0) {
      return res.status(400).json({
        success: false,
        message: "Adjunta tu comprobante en PDF, JPG o PNG.",
      });
    }

    const donativo = await DonativoModel.registrarMonetario({
      idUsuario,
      monto,
      referencia: referencia || null,
      comprobante: req.body,
    });

    if (!donativo) {
      return res.status(404).json({ success: false, message: "No se encontró el usuario." });
    }

    return res.status(201).json({
      success: true,
      message: "Donativo registrado.",
      data: { id_donativo: donativo.id_donativo, estado: donativo.estado },
    });
  } catch (error) {
    console.error("Error al registrar el donativo:", error);
    return res.status(500).json({
      success: false,
      message: "No fue posible registrar el donativo.",
    });
  }
};
