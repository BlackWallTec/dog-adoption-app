import { INTERNAL } from "@qarino/shared/errores.js";

const FECHA_SQL = /^(\d{4}-\d{2}-\d{2}) (\d{2}:\d{2}:\d{2})$/;

/** @param {unknown} valor */
export function texto(valor) {
  if (typeof valor !== "string") return null;
  const limpio = valor.trim();
  return limpio || null;
}

/** @param {unknown} valor @param {string} campo */
export function entero(valor, campo) {
  if (typeof valor !== "number" || !Number.isFinite(valor)) {
    throw new INTERNAL(`Falta ${campo}.`);
  }
  return valor;
}

/**
 * ISO 8601, o `AAAA-MM-DD HH:mm:ss` leído en America/Mexico_City.
 * @param {unknown} valor
 * @param {string} campo
 * @returns {Date | null}
 */
export function fecha(valor, campo) {
  if (valor == null || valor === "") return null;
  if (typeof valor !== "string") throw new INTERNAL(`Fecha inválida en ${campo}.`);

  const sql = FECHA_SQL.exec(valor.trim());
  const iso = sql ? `${sql[1]}T${sql[2]}-06:00` : valor.trim();
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) throw new INTERNAL(`Fecha inválida en ${campo}.`);
  return date;
}

/** @param {unknown} valor @param {string} campo */
export function fechaObligatoria(valor, campo) {
  const date = fecha(valor, campo);
  if (!date) throw new INTERNAL(`Falta ${campo}.`);
  return date;
}
