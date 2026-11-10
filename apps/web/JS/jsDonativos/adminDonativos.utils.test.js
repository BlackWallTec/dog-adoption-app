import { expect, test } from "vite-plus/test";
import { formatearMonto, formatearFecha } from "./adminDonativos.utils.js";

test("formatea los montos en pesos mexicanos", () => {
  const resultado = formatearMonto(1500);

  expect(resultado).toContain("$");
  expect(resultado).toContain("1,500");
});

test("formatea fechas validas", () => {
  const resultado = formatearFecha("2026-11-10T12:00:00");

  expect(resultado).toContain("2026");
  expect(resultado).toContain("nov");
});

test("maneja fechas invalidas", () => {
  expect(formatearFecha("fecha-invalida")).toBe("Sin fecha");
  expect(formatearFecha(null)).toBe("Sin fecha");
});
