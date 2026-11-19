import programModel from "./modelosVoluntariado/program.model.js";
import {
  getIdsProgramasInscritosModel,
  contarInscritosPorProgramasModel,
} from "./modelosVoluntariado/inscripcion.model.js";

// soloInscritos = true -> solo los programas del usuario
const responderProgramas = (soloInscritos) => async (req, res) => {
  try {
    const programas = await programModel.getCurrentPrograms();

    const idUsuario = req.user.uuid;
    // Solo los programas con límite necesitan conteo
    const idsConLimite = programas.filter((p) => p.cupo_maximo != null).map((p) => p.id_programa);

    const [inscritos, ocupadosPorPrograma] = await Promise.all([
      getIdsProgramasInscritosModel(idUsuario),
      contarInscritosPorProgramasModel(idsConLimite),
    ]);

    const data = programas.map((programa) => ({
      ...programa,
      // null = sin límite, igual que cupo_maximo
      cupo_disponible:
        programa.cupo_maximo == null
          ? null
          : Math.max(
              programa.cupo_maximo - (ocupadosPorPrograma.get(programa.id_programa) ?? 0),
              0,
            ),
      usuario_inscrito: inscritos.has(programa.id_programa),
    }));

    return res.status(200).json({
      success: true,
      data: soloInscritos ? data.filter((programa) => programa.usuario_inscrito) : data,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar los programas.",
    });
  }
};

export const getProgramasVigentes = responderProgramas(false);

export const getMisProgramas = responderProgramas(true);
