import { supabaseAdmin } from "../../../../config/supabaseClient.js";

const lanzarError = (contexto, error) => {
  const err = new Error(`${contexto}: ${error.message}`);
  err.code = error.code;
  throw err;
};

// HU36 datos publicos del programa
export const getProgramaDetalleModel = async (idPrograma) => {
  const { data, error } = await supabaseAdmin
    .from("programa")
    .select(
      "id_programa, nombre, tipo, descripcion, fecha_inicio, fecha_fin, ubicacion, cupo_maximo, estado_del_programa",
    )
    .eq("id_programa", idPrograma)
    .maybeSingle();

  if (error) lanzarError("Error al consultar el programa", error);
  return data;
};

// HU36 descripcion del tipo de programa (null si el tipo no esta registrado)
export const getDescripcionTipoModel = async (nombreTipo) => {
  const { data, error } = await supabaseAdmin
    .from("tipo_programa")
    .select("descripcion")
    .eq("nombre", nombreTipo)
    .maybeSingle();

  if (error) lanzarError("Error al consultar el tipo de programa", error);
  return data?.descripcion ?? null;
};

// HU36 el usuario tiene o tuvo una inscripcion en el programa
// Sirve para dejarle ver un programa cancelado o finalizado desde "Mis Voluntariados"
export const tuvoInscripcionModel = async (idPrograma, idUsuario) => {
  const { count, error } = await supabaseAdmin
    .from("se_inscribe")
    .select("id_programa", { count: "exact", head: true })
    .eq("id_programa", idPrograma)
    .eq("id_usuario", idUsuario);

  if (error) lanzarError("Error al consultar la inscripción", error);
  return (count ?? 0) > 0;
};
