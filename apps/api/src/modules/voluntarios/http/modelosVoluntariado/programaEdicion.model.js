import { supabaseAdmin } from "../../../../config/supabaseClient.js";
import { combinarFechaHora, normalizarPrograma } from "./programaCreacion.model.js";

const TABLA = "programa";

export function ahoraMexico() {
  return new Intl.DateTimeFormat("sv-SE", {
    timeZone: "America/Mexico_City",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).format(new Date());
}

function normalizarConMotivo(r) {
  return { ...normalizarPrograma(r), motivoCancelacion: r.motivo_cancelacion ?? null };
}

export async function getProgramaParaEditar(idPrograma) {
  const { data, error } = await supabaseAdmin
    .from(TABLA)
    .select("*")
    .eq("id_programa", idPrograma)
    .maybeSingle();

  if (error) throw error;
  return data ? normalizarConMotivo(data) : null;
}

// Solo actualiza si no esta cancelado y no ha finalizado. Regresa null si no se actualizo
export async function actualizarPrograma(idPrograma, datos) {
  const cambios = {
    tipo: datos.tipo,
    nombre: datos.nombre,
    descripcion: datos.descripcion,
    fecha_inicio: combinarFechaHora(datos.fechaInicio, datos.horaInicio),
    fecha_fin: combinarFechaHora(datos.fechaFin, datos.horaFin),
    ubicacion: datos.ubicacion,
    cupo_maximo: datos.cupoMaximo,
  };

  const { data, error } = await supabaseAdmin
    .from(TABLA)
    .update(cambios)
    .eq("id_programa", idPrograma)
    .neq("estado_del_programa", "Cancelado")
    .gt("fecha_fin", ahoraMexico())
    .select("*");

  if (error) throw error;
  return data?.[0] ? normalizarConMotivo(data[0]) : null;
}

// Solo cancela si no esta cancelado y no ha finalizado. Regresa null si no se cancelo
export async function cancelarPrograma(idPrograma, motivo) {
  const { data, error } = await supabaseAdmin
    .from(TABLA)
    .update({ estado_del_programa: "Cancelado", motivo_cancelacion: motivo })
    .eq("id_programa", idPrograma)
    .neq("estado_del_programa", "Cancelado")
    .gt("fecha_fin", ahoraMexico())
    .select("*");

  if (error) throw error;
  return data?.[0] ? normalizarConMotivo(data[0]) : null;
}
