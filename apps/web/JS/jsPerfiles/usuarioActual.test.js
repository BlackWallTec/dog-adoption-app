import { expect, test } from "vite-plus/test";
import { iniciales, nombreCompleto } from "./usuarioActual.js";

test("las iniciales salen del nombre y el apellido, sin espacios de sobra", () => {
  expect(iniciales("Ana", "Martínez")).toBe("AM");
  expect(iniciales("  Luis  ", " Torres ")).toBe("LT");
  expect(iniciales("")).toBe("");
});

test("el nombre visible junta nombre y apellidos", () => {
  expect(nombreCompleto({ nombre: "Ana ", apellidos: " Martínez" })).toBe("Ana Martínez");
  expect(nombreCompleto({ nombre: "Ana", apellidos: "" })).toBe("Ana");
});
