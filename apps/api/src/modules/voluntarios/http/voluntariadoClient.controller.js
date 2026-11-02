import programModel from "./modelosVoluntariado/program.model.js";

export const getProgramasVigentes = async (req, res) => {
  try {
    const programas = await programModel.getCurrentPrograms();
    return res.status(200).json({
      success: true,
      data: programas,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar los programas.",
    });
  }
};
