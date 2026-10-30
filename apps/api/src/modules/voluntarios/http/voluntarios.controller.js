import {
  getProgramaInscripcionModel,
  contarInscritosModel,
  existeInscripcionModel,
  crearInscripcionModel,
} from "../../../Modelos/modelosVoluntariado/inscripcion.model.js";

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const ESTADO_DISPONIBLE = "Disponible";

// Hora actual de México en el mismo formato que guarda la BD: "2026-10-29T19:37:00"
const ahoraEnMexico = () =>
  new Date().toLocaleString("sv-SE", { timeZone: "America/Mexico_City" }).replace(" ", "T");

const responderError = (res, status, code, message) =>
  res.status(status).json({ sucess: false, code, message });

// POST /api/voluntarios/programas/:idPrograma/inscripciones
export const inscribirse = async (req, res) => {
  const { idPrograma } = req.params;
  const idUsuario = process.env.DEV_USUARIO_ID; // Cambiar aqui lo del login

  if (!idUsuario) {
    return responderError(res, 401, "NO_AUTENTICADO", "No hay un usuario configurado.");
  }

  if (!UUID_REGEX.test(idPrograma)) {
    return responderError(res, 404, "PROGRAMA_NO_ENCONTRADO", "No encontramos este programa.");
  }

  try {
    // Regla 1: el programa debe existir
    const programa = await getProgramaInscripcionModel(idPrograma);
    if (!programa) {
      return responderError(res, 404, "PROGRAMA_NO_ENCONTRADO", "No encontramos este programa.");
    }

    // Regla 2: debe estar abierto a inscripciones
    if (programa.estado_del_programa?.trim() !== ESTADO_DISPONIBLE) {
      return responderError(
        res,
        409,
        "PROGRAMA_NO_DISPONIBLE",
        "El programa no está abierto a inscripciones.",
      );
    }

    // Regla 3: no debe haber iniciado
    const ahora = ahoraEnMexico();
    if (programa.fecha_inicio <= ahora) {
      return responderError(res, 409, "PROGRAMA_INICIADO", "El programa ya comenzó.");
    }

    // Regla 4: el usuario no debe estar inscrito ya
    if (await existeInscripcionModel(idPrograma, idUsuario)) {
      return responderError(res, 409, "YA_INSCRITO", "Ya estás inscrito en este programa.");
    }

    // Regla 5: debe haber cupo (null = sin límite)
    if (programa.cupo_maximo != null) {
      const inscritos = await contarInscritosModel(idPrograma);
      if (inscritos >= programa.cupo_maximo) {
        return responderError(res, 409, "SIN_CUPO", "El programa ya no tiene lugares.");
      }
    }

    // Todo bien: crear la inscripción
    const inscripcion = await crearInscripcionModel(idPrograma, idUsuario, ahora);
    return res.status(201).json({ sucess: true, data: inscripcion });
  } catch (error) {
    // 23505: la llave primaria (id_usuario, id_programa) ya existe → doble toque simultáneo
    if (error.code === "23505") {
      return responderError(res, 409, "YA_INSCRITO", "Ya estás inscrito en este programa.");
    }
    // 23503: llave foránea inválida → el DEV_USUARIO_ID no existe en la tabla usuario
    if (error.code === "23503") {
      return responderError(res, 401, "NO_AUTENTICADO", "El usuario configurado no existe.");
    }

    console.error("[voluntarios] inscribirse:", error);
    return responderError(res, 500, "ERROR_INTERNO", "Error interno al procesar la inscripción.");
  }
};
