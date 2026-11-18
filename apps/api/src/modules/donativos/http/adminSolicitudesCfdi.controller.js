import SolicitudCfdiModel from "./modelosDonativos/solicitudCfdi.model.js";

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function evitarCache(res) {
  res.set("Cache-Control", "private, no-store");
}

export async function obtenerSolicitudesCfdiPendientes(req, res) {
  evitarCache(res);

  try {
    const solicitudes = await SolicitudCfdiModel.getPendientes();
    return res.status(200).json(solicitudes);
  } catch (error) {
    console.error("Error al consultar solicitudes pendientes de CFDI:", error);
    return res.status(500).json({
      error: "No fue posible consultar las solicitudes pendientes de CFDI.",
    });
  }
}

export async function obtenerDetalleSolicitudCfdiPendiente(req, res) {
  evitarCache(res);

  if (!UUID_REGEX.test(req.params.idSolicitud)) {
    return res.status(400).json({ error: "Identificador de solicitud inválido." });
  }

  try {
    const solicitud = await SolicitudCfdiModel.getDetallePendiente(req.params.idSolicitud);

    if (!solicitud) {
      return res.status(404).json({ error: "No se encontró la solicitud pendiente de CFDI." });
    }

    return res.status(200).json(solicitud);
  } catch (error) {
    console.error("Error al consultar el detalle de una solicitud de CFDI:", error);
    return res.status(500).json({
      error: "No fue posible consultar el detalle de la solicitud de CFDI.",
    });
  }
}
