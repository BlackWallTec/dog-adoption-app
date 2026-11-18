import programModel from "./modelosVoluntariado/program.model.js";
import {
  getIdsProgramasInscritosModel,
  contarInscritosModel,
  existeInscripcionModel,
} from "./modelosVoluntariado/inscripcion.model.js";
import {
  getProgramaDetalleModel,
  getDescripcionTipoModel,
  tuvoInscripcionModel,
} from "./modelosVoluntariado/programaDetalle.model.js";

export const getProgramasVigentes = async (req, res) => {
  try {
    const programas = await programModel.getCurrentPrograms();
    // TEMPORAL mismo usuario que inscribirse(). Cuando llegue el login, cambiar ambos juntos
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

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

// Hora actual
const ahoraEnMexico = () =>
  new Date().toLocaleString("sv-SE", { timeZone: "America/Mexico_City" }).replace(" ", "T");

const responderNoEncontrado = (res) =>
  res.status(404).json({
    success: false,
    code: "PROGRAMA_NO_ENCONTRADO",
    message: "Este programa ya no está disponible.",
  });

// HU36 un programa cancelado o finalizado solo lo ve quien estuvo inscrito
export const esConsultaRestringida = (programa, ahora) => {
  const estado = programa.estado_del_programa?.trim().toLowerCase();
  const fin = programa.fecha_fin?.replace(" ", "T");
  return estado === "cancelado" || estado === "finalizado" || (fin != null && fin <= ahora);
};

// GET /api/voluntarios/cliente/programas/:idPrograma
export const getDetallePrograma = async (req, res) => {
  const { idPrograma } = req.params;
  if (!UUID_REGEX.test(idPrograma)) return responderNoEncontrado(res);

  try {
    const programa = await getProgramaDetalleModel(idPrograma);
    if (!programa) return responderNoEncontrado(res);

    // TEMPORAL mismo usuario que el catalogo. Cuando llegue el login, cambiar junto con inscribirse()
    const idUsuario = process.env.DEV_USUARIO_ID;

    if (esConsultaRestringida(programa, ahoraEnMexico())) {
      const puedeVerlo = idUsuario ? await tuvoInscripcionModel(idPrograma, idUsuario) : false;
      if (!puedeVerlo) return responderNoEncontrado(res);
    }

    const [tipoDescripcion, inscritos, usuarioInscrito] = await Promise.all([
      getDescripcionTipoModel(programa.tipo),
      contarInscritosModel(idPrograma),
      idUsuario ? existeInscripcionModel(idPrograma, idUsuario) : false,
    ]);

    return res.status(200).json({
      success: true,
      data: {
        ...programa,
        estado_del_programa: programa.estado_del_programa?.trim() ?? null,
        tipo_descripcion: tipoDescripcion,
        inscritos,
        usuario_inscrito: usuarioInscrito,
      },
    });
  } catch (error) {
    console.error("HU36 detalle de programa:", error);
    return res.status(500).json({
      success: false,
      message: "Error interno al consultar el programa.",
    });
  }
};
