import programModel from "./modelosVoluntariado/program.model.js";
import { getIdsProgramasInscritosModel } from "./modelosVoluntariado/inscripcion.model.js";

export const getProgramasVigentes = async (req, res) => {
  try {
    const programas = await programModel.getCurrentPrograms();
    // TEMPORAL: mismo usuario que inscribirse(). Cuando llegue el login, cambiar ambos juntos.
    const idUsuario = process.env.DEV_USUARIO_ID;
    const inscritos = idUsuario ? await getIdsProgramasInscritosModel(idUsuario) : new Set();
    const data = programas.map((programa) => ({
      ...programa,
      usuario_inscrito: inscritos.has(programa.id_programa),
    }));

    return res.status(200).json({
      success: true,
      data,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar los programas.",
    });
  }
};
