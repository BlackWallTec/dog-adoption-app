import {
  getIdsProgramasInscritosModel,
  contarInscritosModel,
} from "./modelosVoluntariado/inscripcion.model.js";
import {
  getProgramaDetalleModel,
  getDescripcionTipoModel,
  tuvoInscripcionModel,
} from "./modelosVoluntariado/programaDetalle.model.js";

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

// Hora actual de México en el formato de la BD: "2026-10-29T19:37:00"
const ahoraEnMexico = () =>
  new Date().toLocaleString("sv-SE", { timeZone: "America/Mexico_City" }).replace(" ", "T");

const responderNoEncontrado = (res) =>
  res.status(404).json({
    success: false,
    code: "PROGRAMA_NO_ENCONTRADO",
    message: "Este programa ya no está disponible.",
  });

// HU36: un programa cancelado o finalizado solo lo ve quien estuvo inscrito
export const esConsultaRestringida = (programa, ahora) => {
  const estado = programa.estado_del_programa?.trim().toLowerCase();
  const fin = programa.fecha_fin?.replace(" ", "T");
  return estado === "cancelado" || estado === "finalizado" || (fin != null && fin <= ahora);
};

// GET /api/voluntarios/cliente/programas/:idPrograma
// Requiere sesión (verificarToken), igual que el catálogo: el usuario viene en req.user
export const getDetallePrograma = async (req, res) => {
  const { idPrograma } = req.params;
  if (!UUID_REGEX.test(idPrograma)) return responderNoEncontrado(res);

  try {
    const programa = await getProgramaDetalleModel(idPrograma);
    if (!programa) return responderNoEncontrado(res);

    const idUsuario = req.user.uuid;

    if (esConsultaRestringida(programa, ahoraEnMexico())) {
      const puedeVerlo = await tuvoInscripcionModel(idPrograma, idUsuario);
      if (!puedeVerlo) return responderNoEncontrado(res);
    }

    const [tipoDescripcion, inscritos, idsInscritos] = await Promise.all([
      getDescripcionTipoModel(programa.tipo),
      contarInscritosModel(idPrograma),
      getIdsProgramasInscritosModel(idUsuario),
    ]);

    return res.status(200).json({
      success: true,
      data: {
        ...programa,
        estado_del_programa: programa.estado_del_programa?.trim() ?? null,
        tipo_descripcion: tipoDescripcion,
        inscritos,
        usuario_inscrito: idsInscritos.has(idPrograma),
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
