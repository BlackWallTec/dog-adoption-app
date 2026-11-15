import { supabaseAdmin } from "../../../../config/supabaseClient.js";

// Valores de se_inscribe.estado
const ESTADO_INSCRITO = "Aceptado";
const ESTADO_NO_INSCRITO = "Pendiente";

const lanzarError = (contexto, error) => {
  const err = new Error(`${contexto}: ${error.message}`);
  err.code = error.code;
  throw err;
};

// Datos del programa necesarios para validar la inscripción
export const getProgramaInscripcionModel = async (idPrograma) => {
  const { data, error } = await supabaseAdmin
    .from("programa")
    .select("id_programa, fecha_inicio, cupo_maximo, estado_del_programa")
    .eq("id_programa", idPrograma)
    .maybeSingle();

  if (error) lanzarError("Error al consultar el programa", error);
  return data;
};

// personas inscritas (activas) en el programa
export const contarInscritosModel = async (idPrograma) => {
  const { count, error } = await supabaseAdmin
    .from("se_inscribe")
    .select("id_programa", { count: "exact", head: true })
    .eq("id_programa", idPrograma)
    .eq("estado", ESTADO_INSCRITO);

  if (error) lanzarError("Error al contar inscritos", error);
  return count ?? 0;
};

// usuario ya está inscrito (activo) en este programa?
export const existeInscripcionModel = async (idPrograma, idUsuario) => {
  const { data, error } = await supabaseAdmin
    .from("se_inscribe")
    .select("id_programa")
    .eq("id_programa", idPrograma)
    .eq("id_usuario", idUsuario)
    .eq("estado", ESTADO_INSCRITO)
    .maybeSingle();

  if (error) lanzarError("Error al verificar la inscripción", error);
  return data !== null;
};

// Crear la inscripción y regresar la fila.
// Si el usuario ya tenía una fila ("Pendiente", por una cancelación) se reactiva la misma fila.
export const crearInscripcionModel = async (idPrograma, idUsuario, fechaInscripcion) => {
  const { data, error } = await supabaseAdmin
    .from("se_inscribe")
    .upsert(
      {
        id_usuario: idUsuario,
        id_programa: idPrograma,
        fecha_inscripcion: fechaInscripcion,
        estado: ESTADO_INSCRITO,
      },
      { onConflict: "id_usuario,id_programa" },
    )
    .select("id_programa, fecha_inscripcion, estado")
    .single();

  if (error) lanzarError("Error al crear la inscripción", error);
  return data;
};

// HU37: cancelar = cambiar el estado de la fila, no borrarla.
// El filtro por usuario + programa + estado asegura que solo se toca la inscripción activa de ESTE programa.
// Regresa la fila actualizada, o null si el usuario no tenía una inscripción activa.
export const cancelarInscripcionModel = async (idPrograma, idUsuario) => {
  const { data, error } = await supabaseAdmin
    .from("se_inscribe")
    .update({ estado: ESTADO_NO_INSCRITO })
    .eq("id_programa", idPrograma)
    .eq("id_usuario", idUsuario)
    .eq("estado", ESTADO_INSCRITO)
    .select("id_programa, fecha_inscripcion, estado")
    .maybeSingle();

  if (error) lanzarError("Error al cancelar la inscripción", error);
  return data;
};

// HU34: inscripciones activas de un programa, en orden de llegada
// Las que no tienen fecha van al final
export const getInscritosByProgramaModel = async (idPrograma) => {
  const { data, error } = await supabaseAdmin
    .from("se_inscribe")
    .select("id_usuario, fecha_inscripcion, asistencia")
    .eq("id_programa", idPrograma)
    .eq("estado", ESTADO_INSCRITO)
    .order("fecha_inscripcion", { ascending: true, nullsFirst: false });

  if (error) lanzarError("Error al consultar los inscritos", error);
  return data;
};

// Ids de los programas en los que el usuario está inscrito (activo), para el botón del catálogo
export const getIdsProgramasInscritosModel = async (idUsuario) => {
  const { data, error } = await supabaseAdmin
    .from("se_inscribe")
    .select("id_programa")
    .eq("id_usuario", idUsuario)
    .eq("estado", ESTADO_INSCRITO);

  if (error) lanzarError("Error al consultar las inscripciones del usuario", error);
  return new Set(data.map((fila) => fila.id_programa));
};
