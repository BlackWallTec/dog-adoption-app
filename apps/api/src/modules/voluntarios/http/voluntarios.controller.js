import {
  getProgramaInscripcionModel,
  cancelarInscripcionModel,
  inscribirEnProgramaModel,
} from "./modelosVoluntariado/inscripcion.model.js";

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

// Hora actual de México en el mismo formato que guarda la BD: "2026-10-29T19:37:00"
// La inscripción ya valida la fecha en SQL; la cancelación (HU37) la sigue validando aquí.
const ahoraEnMexico = () =>
  new Date().toLocaleString("sv-SE", { timeZone: "America/Mexico_City" }).replace(" ", "T");

// Respuesta HTTP para cada código que regresa inscribir_en_programa.
// Los mensajes son los mismos que ya mostraba la app.
const RESPUESTAS_POR_CODIGO = {
  PROGRAMA_NO_ENCONTRADO: { status: 404, message: "No encontramos este programa." },
  PROGRAMA_NO_DISPONIBLE: { status: 409, message: "El programa no está abierto a inscripciones." },
  PROGRAMA_INICIADO: { status: 409, message: "El programa ya comenzó." },
  YA_INSCRITO: { status: 409, message: "Ya estás inscrito en este programa." },
  SIN_CUPO: { status: 409, message: "El programa ya no tiene lugares." },
};

const responderError = (res, status, code, message) =>
  res.status(status).json({ sucess: false, code, message });

// POST /api/voluntarios/programas/:idPrograma/inscripciones
export const inscribirse = async (req, res) => {
  const { idPrograma } = req.params;
  const idUsuario = req.user.uuid;

  // Un id que no es UUID haría fallar a Postgres; se responde 404 antes de llamar a la BD
  if (!UUID_REGEX.test(idPrograma)) {
    return responderError(res, 404, "PROGRAMA_NO_ENCONTRADO", "No encontramos este programa.");
  }

  try {
    // Valida las reglas 1-5 e inscribe en una sola transacción (ver inscribir_en_programa)
    const resultado = await inscribirEnProgramaModel(idPrograma, idUsuario);

    if (resultado?.codigo === "OK") {
      const { id_programa, fecha_inscripcion, estado } = resultado;
      return res
        .status(201)
        .json({ sucess: true, data: { id_programa, fecha_inscripcion, estado } });
    }

    const respuesta = RESPUESTAS_POR_CODIGO[resultado?.codigo];
    if (respuesta) {
      return responderError(res, respuesta.status, resultado.codigo, respuesta.message);
    }

    // Código desconocido: la función SQL cambió y este mapa no
    throw new Error(`Código inesperado de inscribir_en_programa: ${resultado?.codigo}`);
  } catch (error) {
    // 23503: llave foránea inválida
    if (error.code === "23503") {
      return responderError(res, 401, "NO_AUTENTICADO", "Tu cuenta no tiene un perfil registrado.");
    }

    console.error("[voluntarios] inscribirse:", error);
    return responderError(res, 500, "ERROR_INTERNO", "Error interno al procesar la inscripción.");
  }
};

// PATCH /api/voluntarios/programas/:idPrograma/inscripciones  (HU37)
// No borra la fila: cambia su estado de "Aceptado" a "Pendiente".
export const cancelarInscripcion = async (req, res) => {
  const { idPrograma } = req.params;
  const idUsuario = req.user.uuid;

  if (!UUID_REGEX.test(idPrograma)) {
    return responderError(res, 404, "PROGRAMA_NO_ENCONTRADO", "No encontramos este programa.");
  }

  try {
    // Regla 1: el programa debe existir
    const programa = await getProgramaInscripcionModel(idPrograma);
    if (!programa) {
      return responderError(res, 404, "PROGRAMA_NO_ENCONTRADO", "No encontramos este programa.");
    }

    // Regla 2: no se cancela si el programa ya comenzó (el admin necesita la lista exacta de inscritos)
    if (programa.fecha_inicio <= ahoraEnMexico()) {
      return responderError(
        res,
        409,
        "PROGRAMA_INICIADO",
        "El programa ya comenzó, ya no puedes cancelar.",
      );
    }

    // Regla 3: debe haber una inscripción activa de este usuario en ESTE programa
    const cancelada = await cancelarInscripcionModel(idPrograma, idUsuario);
    if (!cancelada) {
      return responderError(res, 409, "NO_INSCRITO", "No estás inscrito en este programa.");
    }

    return res.status(200).json({ sucess: true, data: cancelada });
  } catch (error) {
    console.error("[voluntarios] cancelarInscripcion:", error);
    return responderError(res, 500, "ERROR_INTERNO", "Error interno al cancelar la inscripción.");
  }
};
