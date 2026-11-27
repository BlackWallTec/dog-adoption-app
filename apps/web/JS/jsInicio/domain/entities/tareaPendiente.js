/** @typedef {"adopcion" | "donativo" | "factura" | "correo_factura"} TipoTarea */
/** @typedef {"normal" | "atrasada" | "critica"} Urgencia */

/**
 * @typedef {Object} TareaPendiente
 * @property {string} id
 * @property {TipoTarea} tipo
 * @property {{ nombre: string }} persona
 * @property {{ id: string, nombre: string } | null} perro
 * @property {string | null} folio
 * @property {number | null} monto
 * @property {Date} creadaEn
 * @property {Urgencia} urgencia
 */

export {};
