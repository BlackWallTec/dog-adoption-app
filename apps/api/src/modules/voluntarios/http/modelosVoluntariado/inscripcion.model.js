import { supabase, supabaseAdmin } from "../../../../config/supabaseClient.js";

// Valores de se_inscribe.estado
const ESTADO_INSCRITO = "Aceptado";
const ESTADO_NO_INSCRITO = "Pendiente";

const lanzarError = (contexto, error) => {
  const err = new Error(`${contexto}: ${error.message}`);
  err.code = error.code;
  throw err;
};

// Datos del programa necesarios para validar la cancelación (HU37)
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
// Usa contar_inscritos con RLS, un count directo sobre se_inscribe con el cliente anon solo vería las filas propias y daría 0 o 1.
export const contarInscritosModel = async (idPrograma) => {
  const { data, error } = await supabase.rpc("contar_inscritos", {
    p_id_programa: idPrograma,
  });

  if (error) lanzarError("Error al contar inscritos", error);
  return Number(data ?? 0);
};

// HU37: cancelar = cambiar el estado de la fila, no borrarla.
// El filtro por usuario + programa + estado asegura que solo se toca la inscripción activa de ESTE programa.
// Regresa la fila actualizada, o null si el usuario no tenía una inscripción activa.
// Al volver a inscribirse, inscribir_en_programa reactiva esta misma fila.
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

// Cuenta las inscripciones activas de un programa HU38. (estado Aceptado)
export const contarInscritosActivosModel = async (idPrograma) => {
  const { count, error } = await supabaseAdmin
    .from("se_inscribe")
    .select("id_usuario", { count: "exact", head: true })
    .eq("id_programa", idPrograma)
    .eq("estado", ESTADO_INSCRITO);

  if (error) lanzarError("Error al contar los inscritos", error);
  return count ?? 0;
};

// Lugares ocupados por programa (tarjetas del catálogo): Map<id_programa, número>
export const contarInscritosPorProgramasModel = async (idsProgramas) => {
  const conteos = await Promise.all(
    idsProgramas.map(async (idPrograma) => [idPrograma, await contarInscritosModel(idPrograma)]),
  );
  return new Map(conteos);
};

// Inscribe al usuario de forma atómica: la función SQL bloquea el programa, valida las reglas,
// cuenta el cupo e inscribe en una sola transacción (así dos personas no ocupan el último lugar).
// Si el usuario había cancelado (fila "Pendiente"), la función reactiva esa misma fila.
// Usa supabaseAdmin porque inscribir_en_programa solo tiene permiso para service_role.
export const inscribirEnProgramaModel = async (idPrograma, idUsuario) => {
  const { data, error } = await supabaseAdmin.rpc("inscribir_en_programa", {
    p_id_programa: idPrograma,
    p_id_usuario: idUsuario,
  });

  if (error) lanzarError("Error al inscribir en el programa", error);
  return data;
};
