import { expect, test } from "vite-plus/test";
import {
  buildQueryString,
  estadoBadgeClass,
  formatSolicitudDate,
  estadoLabel,
} from "./adminSolicitudes.utils.js";

test("builds a query with only non-default filters and the page", () => {
  expect(buildQueryString({ perro: "12", estado: "en_revision", fecha: "todas", page: 2 })).toBe(
    "perro=12&estado=en_revision&page=2",
  );
});

test("formats date-only values without shifting to the previous Mexico City day", () => {
  expect(formatSolicitudDate("2026-10-12")).toBe("12 oct. 2026");
});

test("maps statuses to labels and badge families", () => {
  expect(estadoLabel("en_revision")).toBe("En revisión");
  expect(estadoBadgeClass("aprobada")).toBe("badge-aprobada");
  expect(estadoBadgeClass("rechazada")).toBe("badge-rechazada");
});
