import * as EdicionModel from "./modelosVoluntariado/programaEdicion.model.js";
import {
  obtenerEmpalmes,
  combinarFechaHora,
} from "./modelosVoluntariado/programaCreacion.model.js";
import { contarInscritosActivosModel } from "./modelosVoluntariado/inscripcion.model.js";
import { validarDatosPrograma } from "./validacionPrograma.js";

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const MAX_MOTIVO = 200;
const CAMPOS_EDITABLES = [
  "nombre",
  "tipo",
  "descripcion",
  "ubicacion",
  "fechaInicio",
  "horaInicio",
  "fechaFin",
  "horaFin",
  "cupoMaximo",
];

const responderError = (res, status, code, message, extra = {}) =>
  res.status(status).json({ success: false, code, message, ...extra });

const noEncontrado = (res) =>
  responderError(res, 404, "PROGRAMA_NO_ENCONTRADO", "No encontramos este programa.");

const errorInterno = (res, error) => {
  console.error("Error en edición de programa:", error);
  return responderError(res, 500, "ERROR_INTERNO", "Ocurrió un error. Intenta de nuevo.");
};

const inicioDe = (p) => combinarFechaHora(p.fechaInicio, p.horaInicio);
const finDe = (p) => combinarFechaHora(p.fechaFin, p.horaFin);

export const esEditable = (programa, ahora) =>
  programa.estado !== "Cancelado" && finDe(programa) > ahora;

// Solo toma los campos que se pueden editar, lo demás del cuerpo se ignora
export const tomarCamposEditables = (cuerpo) =>
  Object.fromEntries(
    CAMPOS_EDITABLES.filter((campo) => cuerpo?.[campo] !== undefined).map((campo) => [
      campo,
      cuerpo[campo],
    ]),
  );

// GET /api/voluntarios/admin/programas/:idPrograma
export async function obtenerParaEditar(req, res) {
  try {
    const { idPrograma } = req.params;
    if (!UUID_REGEX.test(idPrograma)) return noEncontrado(res);

    const programa = await EdicionModel.getProgramaParaEditar(idPrograma);
    if (!programa) return noEncontrado(res);

    const inscritosActivos = await contarInscritosActivosModel(idPrograma);

    return res.json({
      success: true,
      data: {
        ...programa,
        inscritosActivos,
        editable: esEditable(programa, EdicionModel.ahoraMexico()),
      },
    });
  } catch (error) {
    return errorInterno(res, error);
  }
}

// PUT /api/voluntarios/admin/programas/:idPrograma
export async function editarPrograma(req, res) {
  try {
    const { idPrograma } = req.params;
    if (!UUID_REGEX.test(idPrograma)) return noEncontrado(res);

    const actual = await EdicionModel.getProgramaParaEditar(idPrograma);
    if (!actual) return noEncontrado(res);

    const ahora = EdicionModel.ahoraMexico();
    if (!esEditable(actual, ahora)) {
      return responderError(
        res,
        409,
        "PROGRAMA_NO_EDITABLE",
        "Este programa ya no se puede editar.",
      );
    }

    const campos = tomarCamposEditables(req.body);
    // Si no cambia el inicio, se permite conservarlo aunque ya haya pasado (programa ya iniciado)
    const cambiaInicio =
      combinarFechaHora(campos.fechaInicio, campos.horaInicio) !== inicioDe(actual);
    const validacion = validarDatosPrograma(campos, { validarInicioFuturo: cambiaInicio });

    if (!validacion.valido) {
      return responderError(res, 400, "VALIDACION", validacion.message, {
        campo: validacion.campo,
      });
    }

    const datos = validacion.datos;

    if (finDe(datos) <= ahora) {
      return responderError(res, 400, "VALIDACION", "La fecha de fin ya pasó.", {
        campo: "fechas",
      });
    }

    const inscritosActivos = await contarInscritosActivosModel(idPrograma);
    if (datos.cupoMaximo < inscritosActivos) {
      return responderError(
        res,
        409,
        "CUPO_MENOR_A_INSCRITOS",
        `Hay ${inscritosActivos} inscritos, el cupo no puede ser menor.`,
        { inscritosActivos },
      );
    }

    const empalmes = (await obtenerEmpalmes(datos)).filter(
      (jornada) => jornada.idPrograma !== idPrograma,
    );
    if (empalmes.length > 0 && req.body?.confirmarEmpalme !== true) {
      return responderError(res, 409, "OVERLAP", "Existen jornadas que coinciden con el horario.", {
        empalmes,
      });
    }

    const programa = await EdicionModel.actualizarPrograma(idPrograma, datos);
    if (!programa) {
      return responderError(
        res,
        409,
        "PROGRAMA_NO_EDITABLE",
        "Este programa ya no se puede editar.",
      );
    }

    const cambioLogistica =
      inicioDe(programa) !== inicioDe(actual) ||
      finDe(programa) !== finDe(actual) ||
      programa.ubicacion !== actual.ubicacion;

    return res.json({
      success: true,
      data: {
        programa,
        inscritosActivos,
        avisarInscritos: inscritosActivos > 0 && cambioLogistica,
      },
    });
  } catch (error) {
    return errorInterno(res, error);
  }
}

// POST /api/voluntarios/admin/programas/:idPrograma/cancelacion
export async function cancelarPrograma(req, res) {
  try {
    const { idPrograma } = req.params;
    if (!UUID_REGEX.test(idPrograma)) return noEncontrado(res);

    const motivo = req.body?.motivo;
    if (
      motivo !== undefined &&
      motivo !== null &&
      (typeof motivo !== "string" || motivo.trim().length > MAX_MOTIVO)
    ) {
      return responderError(
        res,
        400,
        "VALIDACION",
        `El motivo no puede tener más de ${MAX_MOTIVO} caracteres.`,
        { campo: "motivo" },
      );
    }

    const motivoLimpio = typeof motivo === "string" && motivo.trim() ? motivo.trim() : null;
    const programa = await EdicionModel.cancelarPrograma(idPrograma, motivoLimpio);

    if (!programa) {
      const existe = await EdicionModel.getProgramaParaEditar(idPrograma);
      if (!existe) return noEncontrado(res);
      return responderError(
        res,
        409,
        "PROGRAMA_NO_CANCELABLE",
        "Este programa ya no se puede cancelar.",
      );
    }

    const inscritosActivos = await contarInscritosActivosModel(idPrograma);

    return res.json({ success: true, data: { programa, inscritosActivos } });
  } catch (error) {
    return errorInterno(res, error);
  }
}
