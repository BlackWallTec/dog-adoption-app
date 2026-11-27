import { expect, test } from "vite-plus/test";
import { diasEntre, milisegundosEntre, superaSla } from "./antiguedad.js";

const ahora = new Date("2026-11-26T12:00:00-06:00");
const MS_POR_DIA = 24 * 60 * 60 * 1000;

/** @param {number} dias @param {number} [msExtra] */
function hace(dias, msExtra = 0) {
  return new Date(ahora.getTime() - dias * MS_POR_DIA - msExtra);
}

test("una fecha futura o inválida no tiene antigüedad", () => {
  expect(milisegundosEntre(new Date(ahora.getTime() + 1000), ahora)).toBe(0);
  expect(diasEntre(new Date(ahora.getTime() + MS_POR_DIA), ahora)).toBe(0);
  expect(milisegundosEntre(/** @type {Date} */ (/** @type {unknown} */ (null)), ahora)).toBe(0);
});

test("el día se cuenta completo y el SLA solo se supera al pasarlo", () => {
  expect(diasEntre(hace(3, MS_POR_DIA / 2), ahora)).toBe(3);
  expect(superaSla("adopcion", hace(3), ahora)).toBe(false);
  expect(superaSla("adopcion", hace(3, 1), ahora)).toBe(true);
  expect(superaSla("donativo", hace(2), ahora)).toBe(false);
  expect(superaSla("donativo", hace(2, 1), ahora)).toBe(true);
  expect(superaSla("factura", hace(3, 1), ahora)).toBe(true);
});

test("un tipo sin umbral no se marca atrasado", () => {
  expect(superaSla("adopcion", hace(10), ahora, {})).toBe(false);
});
