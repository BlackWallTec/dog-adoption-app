import dogModel from "./modelosCatalogo/dog.model.js";

export const getPerrosAdmin = async (req, res) => {
  try {
    const perros = await dogModel.DogModel.getPerrosAdminModel();
    return res.status(200).json({
      success: true,
      data: perros,
    });
  } catch (error) {
    return res.status(500).json({
      sucess: false,
      message: error.message || "Error interno al consultar los perros.",
    });
  }
};
