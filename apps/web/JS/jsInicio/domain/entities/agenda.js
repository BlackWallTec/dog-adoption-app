/** @typedef {"Disponible" | "Lleno" | "Finalizado" | "Cancelado"} EstadoJornada */

/**
 * @typedef {Object} Jornada
 * @property {string} id
 * @property {string} nombre
 * @property {string | null} tipo
 * @property {Date} inicio
 * @property {Date} fin
 * @property {string | null} ubicacion
 * @property {number | null} cupoMaximo
 * @property {number} inscritos
 * @property {EstadoJornada} estado
 */

/**
 * @typedef {Object} SemanaSinCobertura
 * @property {string} inicio
 * @property {string} fin
 */

/**
 * @typedef {Object} Agenda
 * @property {Jornada[]} jornadas
 * @property {SemanaSinCobertura[]} semanasSinCobertura
 */

export {};
