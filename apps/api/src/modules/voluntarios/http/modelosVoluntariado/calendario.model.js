import { supabaseAdmin } from "../../../../config/supabaseClient.js";

// HU30: consultas del calendario de programas. Solo lectura
const TABLA_PROGRAMA = "programa";
const TABLA_INSCRIPCION = "se_inscribe";
const ESTADO_INSCRITO = "Aceptado";

// Programas que tocan el rango [inicioRango, finRango)
// Entra si empieza antes de que acabe el rango y termina después de que empieza
export async function getProgramasEnRango(inicioRango, finRango) {
  const { data, error } = await supabaseAdmin
    .from(TABLA_PROGRAMA)
    .select(
      "id_programa, nombre, tipo, fecha_inicio, fecha_fin, ubicacion, cupo_maximo, estado_del_programa",
    )
    .lt("fecha_inicio", finRango)
    .gt("fecha_fin", inicioRango)
    .order("fecha_inicio", { ascending: true });

  if (error) throw error;
  return data ?? [];
}

// Inscripciones activas de varios programas en una sola consulta
// Regresa una fila por inscrito: [{ id_programa }]
export async function getInscripcionesActivas(idsProgramas) {
  if (idsProgramas.length === 0) return [];

  const { data, error } = await supabaseAdmin
    .from(TABLA_INSCRIPCION)
    .select("id_programa")
    .in("id_programa", idsProgramas)
    .eq("estado", ESTADO_INSCRITO);

  if (error) throw error;
  return data ?? [];
}
