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

// HU44 - Obtener resumen público de donativos pendientes
export async function obtenerResumenDonativosPendientes(req, res) {
  try {
    const donativos = await DonativoModel.getPendientes();

    const resumen = donativos.map((donativo) => ({
      id: donativo.id_donativo,
      fecha: donativo.fecha,
      estado: donativo.estado,
      donante: "Donante anónimo",
    }));

    return res.status(200).json(resumen);
  } catch (error) {
    console.error("Error al obtener resumen de donativos:", error);

    return res.status(500).json({
      error: "No fue posible consultar los donativos pendientes.",
    });
  }
}
