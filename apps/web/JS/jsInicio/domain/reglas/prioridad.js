import { superaSla } from "./antiguedad.js";
import { PESO_TIPO, UMBRALES_SLA_DIAS } from "./umbrales.js";

/**
 * Hecho que lee la regla. `requiereRevision` y `correoEstado` no se quedan en la entidad.
 * @typedef {Object} HechoPrioridad
 * @property {import("../entities/tareaPendiente.js").TipoTarea} tipo
 * @property {Date} creadaEn
 * @property {boolean} [requiereRevision]
 * @property {string | null} [correoEstado]
 */

/** @param {HechoPrioridad} tarea */
function esCritica(tarea) {
  if (tarea.tipo === "correo_factura") return true;
  if (tarea.correoEstado === "Error") return true;
  return tarea.tipo === "factura" && tarea.requiereRevision === true;
}

/**
 * @param {HechoPrioridad} tarea
 * @param {Date} ahora
 * @param {Readonly<Record<string, number>>} [umbrales]
 * @returns {import("../entities/tareaPendiente.js").Urgencia}
 */
export function urgenciaDe(tarea, ahora, umbrales = UMBRALES_SLA_DIAS) {
  if (esCritica(tarea)) return "critica";
  if (superaSla(tarea.tipo, tarea.creadaEn, ahora, umbrales)) return "atrasada";
  return "normal";
}

/**
 * @template {HechoPrioridad} T
 * @param {readonly T[]} tareas
 * @param {Date} ahora
 * @param {{ umbrales?: Readonly<Record<string, number>>, pesos?: Readonly<Record<string, number>> }} [opciones]
 * @returns {T[]}
 */
export function ordenarPendientes(tareas, ahora, opciones = {}) {
  const pesos = opciones.pesos ?? PESO_TIPO;

  return tareas.slice().sort((a, b) => {
    const porPeligro = Number(esCritica(b)) - Number(esCritica(a));
    if (porPeligro !== 0) return porPeligro;

    const porEdad = a.creadaEn.getTime() - b.creadaEn.getTime();
    if (porEdad !== 0) return porEdad;

    return (pesos[a.tipo] ?? Number.MAX_SAFE_INTEGER) - (pesos[b.tipo] ?? Number.MAX_SAFE_INTEGER);
  });
}

/**
 * @template {HechoPrioridad} T
 * @param {readonly T[]} tareas
 * @param {Date} ahora
 * @param {{ umbrales?: Readonly<Record<string, number>>, pesos?: Readonly<Record<string, number>> }} [opciones]
 */
export function prepararPendientes(tareas, ahora, opciones = {}) {
  const umbrales = opciones.umbrales ?? UMBRALES_SLA_DIAS;
  const marcadas = tareas.map((tarea) => ({
    ...tarea,
    urgencia: urgenciaDe(tarea, ahora, umbrales),
  }));
  return ordenarPendientes(marcadas, ahora, opciones);
}
