import { UMBRALES_SLA_DIAS } from "./umbrales.js";

const MS_POR_DIA = 24 * 60 * 60 * 1000;

/** @param {unknown} valor */
function instante(valor) {
  return valor instanceof Date ? valor.getTime() : Number.NaN;
}

/** @param {Date} desde @param {Date} ahora */
export function milisegundosEntre(desde, ahora) {
  const inicio = instante(desde);
  const fin = instante(ahora);
  if (Number.isNaN(inicio) || Number.isNaN(fin)) return 0;
  return Math.max(0, fin - inicio);
}

/** @param {Date} desde @param {Date} ahora */
export function diasEntre(desde, ahora) {
  return Math.floor(milisegundosEntre(desde, ahora) / MS_POR_DIA);
}

/**
 * @param {import("../entities/tareaPendiente.js").TipoTarea} tipo
 * @param {Date} creadaEn
 * @param {Date} ahora
 * @param {Readonly<Record<string, number>>} [umbrales]
 */
export function superaSla(tipo, creadaEn, ahora, umbrales = UMBRALES_SLA_DIAS) {
  const limite = umbrales[tipo];
  if (typeof limite !== "number") return false;
  return milisegundosEntre(creadaEn, ahora) > limite * MS_POR_DIA;
}
