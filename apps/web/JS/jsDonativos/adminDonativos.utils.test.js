import { expect, test } from "vite-plus/test";
import {
  formatearMonto,
  formatearFecha,
  prepararFilaDonativo,
  obtenerMensajeListaVacia,
} from "./adminDonativos.utils.js";

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
test("prepara correctamente las cinco columnas de un donativo", () => {
  const donativo = {
    id: 44,
    donante: "Donante de prueba",
    monto: 1500,
    fecha: "2026-11-10T12:00:00",
    estado: "Pendiente de validación",
  };

  const columnas = prepararFilaDonativo(donativo);

  expect(columnas).toHaveLength(5);
  expect(columnas[0]).toBe(44);
  expect(columnas[1]).toBe("Donante de prueba");
  expect(columnas[2]).toContain("1,500");
  expect(columnas[3]).toContain("2026");
  expect(columnas[4]).toBe("Pendiente de validación");
});

test("CA-02: muestra un mensaje cuando no hay donativos pendientes", () => {
  const mensaje = obtenerMensajeListaVacia([]);

  expect(mensaje).toBe("No hay donativos pendientes.");
});

test("CA-02: no muestra el mensaje de lista vacia cuando hay pendientes", () => {
  const donativos = [{ id: 44, estado: "Pendiente de validación" }];

  expect(obtenerMensajeListaVacia(donativos)).toBeNull();
});
