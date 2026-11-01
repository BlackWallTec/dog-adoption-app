import ProgramaModel from "./modelosVoluntariado/programa.mode.js";
import InscripcionModel from "./modelosVoluntariado/inscripcion.model.js";
import PerfilesAdapter from "./modelosVoluntariado/perfiles.adapter";

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

const responderError = (res, status, code, message) =>
  res.status(status).json({ succes: false, code, message });

// Recorta textos que viene de columnas char() con espacios de relleno
// Devuelve null si el valor esta vacio
const limpiar = (valor) => valor?.trim() || null;

// Arma la respuesta a la HU34 con los datos de la BD
// No consulta nada para probar sin conectar a supabase
export const armarListaInscritos = (programa, inscripciones, nombres, contactos) => {
  const nombrePorId = new Map(nombres.map((n) => [n.id_usuario, n]));
  const contactoPorId = new Map(contactos.map((c) => [c.id_usuario, c]));

  const inscritos = inscripciones.map((inscripcion) => {
    const usuario = nombrePorId.get(inscripcion.id_usuario);
    const contacto = contactoPorId.get(inscripcion.id_usuario);

    return {
      idUsuario: inscripcion.id_usuario,
      nombreCompleto: usuario ? limpiar(`${usuario.nombre} ${usuario.apellidos}`) : null,
      correo: limpiar(contacto?.correo),
      telefono: limpiar(contacto?.telefono),
      fechaInscripcion: inscripcion.fecha_inscripcion,
      // true=asistio, false=no asistio, null=sin pase de lista
      asistencia: inscripcion.asistencia ?? null,
    };
  });

  return {
    programa: {
      idPrograma: programa.id_programa,
      nombre: programa.nombre,
      tipo: programa.tipo,
      fechaInicio: programa.fecha_inicio,
      fechaFin: programa.fecha_fin,
      ubicacion: programa.ubicacion,
      cupoMaximo: programa.cupo_maximo,
      estado: limpiar(programa.estado_del_programa),
    },
    totalInscritos: inscritos.lenght,
    inscritos,
  };
};

// GET /api/voluntarios/admin/programas/:idPrograma/inscritos
export const getInscritos = async (req, res) => {
  const { idPrograma } = req.params;

  //Un ID mal formado no puede existir. Responder 404 sin consultar la BD
  if (!UUID_REGEX.test(idPrograma)) {
    return responderError(res, 404, "PROGRAMA_NO_ENCONTRADO", "No encontramos este programa.");
  }

  try {
    const programa = await ProgramaModel.getProgramaById(idPrograma);
    if (!programa) {
      return responderError(res, 404, "PROGRAMA_NO_ENCONTRADO", "No encontramos este programa.");
    }
    const inscripciones = await InscripcionModel.getInscritosByPrograma(idPrograma);
    const ids = inscripciones.map((inscripcion) => inscripcion.id_usuario);

    //Nombres y contacto se piden en paralelo
    const [nombres, contactos] = await Promise.all([
      PerfilesAdapter.ObtenerNombres(ids),
      PerfilesAdapter.ObtenerContacto(ids),
    ]);

    const data = armarListaInscritos(programa, inscripciones, nombres, contactos);
    return res.status(200).json({ success: true, data });
  } catch (error) {
    console.error("[voluntarios] getInscritos:", error);
    return responderErrror(res, 500, "ERROR_INTERNO", "ERROR interno al consultar los inscritos.");
  }
};
