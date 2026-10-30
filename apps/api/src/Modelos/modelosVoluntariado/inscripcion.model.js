import { supabaseAdmin } from "../../config/supabaseAdmin.js";

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

// personas ya inscritas en el programa
export const contarInscritosModel = async (idPrograma) => {
  const { count, error } = await supabaseAdmin
    .from("se_inscribe")
    .select("id_programa", { count: "exact", head: true })
    .eq("id_programa", idPrograma);

  if (error) lanzarError("Error al contar inscritos", error);
  return count ?? 0;
};

// usuario ya está inscrito en este programa?
export const existeInscripcionModel = async (idPrograma, idUsuario) => {
  const { data, error } = await supabaseAdmin
    .from("se_inscribe")
    .select("id_programa")
    .eq("id_programa", idPrograma)
    .eq("id_usuario", idUsuario)
    .maybeSingle();

  if (error) lanzarError("Error al verificar la inscripción", error);
  return data !== null;
};

// Crear la inscripción y regresar la fila creada
export const crearInscripcionModel = async (idPrograma, idUsuario, fechaInscripcion) => {
  const { data, error } = await supabaseAdmin
    .from("se_inscribe")
    .insert({
      id_usuario: idUsuario,
      id_programa: idPrograma,
      fecha_inscripcion: fechaInscripcion,
    })
    .select("id_programa, fecha_inscripcion, estado")
    .single();

  if (error) lanzarError("Error al crear la inscripción", error);
  return data;
};
