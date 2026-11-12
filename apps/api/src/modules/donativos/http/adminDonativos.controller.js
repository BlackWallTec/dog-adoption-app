import DonativoModel from "./modelosDonativos/donativo.model.js";

// HU44 - Obtener donativos pendientes para administradores
export async function obtenerDonativosPendientes(req, res) {
  try {
    const donativos = await DonativoModel.getPendientes();

    return res.status(200).json(donativos);
  } catch (error) {
    console.error("Error al obtener donativos pendientes:", error);

    return res.status(500).json({
      error: "No fue posible consultar los donativos pendientes.",
    });
  }
}
