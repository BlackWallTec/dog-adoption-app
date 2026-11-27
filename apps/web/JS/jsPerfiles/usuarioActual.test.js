import { expect, test } from "vite-plus/test";
import { UNAUTHENTICATED } from "@qarino/shared/errores.js";
import { iniciales, nombreCompleto, usuarioActual } from "./usuarioActual.js";

test("las iniciales salen del nombre y el apellido, sin espacios de sobra", () => {
  expect(iniciales("Ana", "Martínez")).toBe("AM");
  expect(iniciales("  Luis  ", " Torres ")).toBe("LT");
  expect(iniciales("")).toBe("");
});

test("el nombre visible junta nombre y apellidos", () => {
  expect(nombreCompleto({ nombre: "Ana ", apellidos: " Martínez" })).toBe("Ana Martínez");
  expect(nombreCompleto({ nombre: "Ana", apellidos: "" })).toBe("Ana");
});

test("un 401 no se sustituye por una persona de muestra", async () => {
  const api = {
    getSession: async () => {
      throw new UNAUTHENTICATED();
    },
    getProfile: async () => ({ profile: { nombre: "Ana", apellidos: "Martínez" } }),
  };
  await expect(usuarioActual(api)).rejects.toBeInstanceOf(UNAUTHENTICATED);
});

test("con sesión usa el perfil y el rol que respondió el servidor", async () => {
  const api = {
    getSession: async () => ({ user: { rol: "Root" } }),
    getProfile: async () => ({ profile: { nombre: "Iker", apellidos: "Amaro" } }),
  };
  await expect(usuarioActual(api)).resolves.toEqual({
    nombre: "Iker",
    apellidos: "Amaro",
    rol: "Root",
  });
});

test("si el perfil falla y la sesión sigue, el nombre queda vacío", async () => {
  const api = {
    getSession: async () => ({ user: { rol: "Administrador" } }),
    getProfile: async () => {
      throw new Error("red");
    },
  };
  await expect(usuarioActual(api)).resolves.toEqual({
    nombre: "",
    apellidos: "",
    rol: "Administrador",
  });
});
