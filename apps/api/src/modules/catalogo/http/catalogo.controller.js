import { getPerrosAdminModel } from "../../../Modelos/modelosCatalogo/perro.model.js";

export const getPerrosAdmin = async (req, res) => {
  try {
    const perros = await getPerrosAdminModel();
    return res.status(200).json({
      sucess: true,
      data: perros,
    });
  } catch (error) {
    return res.status(500).json({
      sucess: false,
      message: error.message || "Error interno al consultar los perros.",
    });
  }
};
