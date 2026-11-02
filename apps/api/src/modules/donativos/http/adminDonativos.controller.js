import DonativoModel from "./modelosDonativos/donativo.model.js";

export async function obtenerDonativosPendientes(req, res) {
  try {
    const donativos = await DonativoModel.getPendientes(req.supabaseAccessToken);

    return res.status(200).json(donativos);
  } catch (error) {
    console.error("Error al consultar donativos pendientes:", error);

    return res.status(500).json({ error: "No fue posible consultar los donativos pendientes" });
  }
}
