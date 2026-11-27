/** @typedef {import("../entities/resumen.js").ResumenInicio} ResumenInicio */
/** @typedef {import("../entities/tareaPendiente.js").TareaPendiente} TareaPendiente */
/** @typedef {import("../entities/agenda.js").Agenda} Agenda */
/** @typedef {import("../entities/perroDestacado.js").PerroDestacado} PerroDestacado */

/**
 * Contrato de Mock, Compuesto y Api.
 * @typedef {Object} InicioRepository
 * @property {(opts?: { signal?: AbortSignal }) => Promise<ResumenInicio>} obtenerResumen
 * @property {(opts?: { limite?: number, signal?: AbortSignal }) => Promise<TareaPendiente[]>} obtenerPendientes
 * @property {(opts: { desde: string, hasta: string, signal?: AbortSignal }) => Promise<Agenda>} obtenerAgenda
 * @property {(opts?: { signal?: AbortSignal }) => Promise<PerroDestacado | null>} obtenerPerroDestacado
 */

const METODOS = ["obtenerResumen", "obtenerPendientes", "obtenerAgenda", "obtenerPerroDestacado"];

/** @param {unknown} valor */
export function esInicioRepository(valor) {
  if (!valor || typeof valor !== "object") return false;
  return METODOS.every(
    (metodo) => typeof (/** @type {Record<string, unknown>} */ (valor)[metodo]) === "function",
  );
}
