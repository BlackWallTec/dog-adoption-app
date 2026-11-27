import { expect, test } from "vite-plus/test";
import { FORBIDDEN } from "@qarino/shared/errores.js";
import { puedeEntrarAlPanel, rutaAccesoDenegado, usuarioDePanel } from "./accesoPanel.js";

test("el panel admite Administrador y Root", () => {
  expect(puedeEntrarAlPanel("Administrador")).toBe(true);
  expect(puedeEntrarAlPanel("Root")).toBe(true);
  expect(puedeEntrarAlPanel("Cliente")).toBe(false);
  expect(puedeEntrarAlPanel("")).toBe(false);
});

test("el aviso de acceso denegado vuelve al login", () => {
  expect(rutaAccesoDenegado()).toBe("/admin/login?acceso=denegado");
});

test("un Cliente pierde la sesión y no entra", async () => {
  let cerro = false;
  const api = {
    getSession: async () => ({ user: { rol: "Cliente" } }),
    getProfile: async () => ({ profile: { nombre: "Ana", apellidos: "López" } }),
    logout: async () => {
      cerro = true;
      return { ok: true };
    },
  };
  await expect(usuarioDePanel(api)).rejects.toBeInstanceOf(FORBIDDEN);
  expect(cerro).toBe(true);
});

test("Root sí entra, sin cerrar la sesión", async () => {
  let cerro = false;
  const api = {
    getSession: async () => ({ user: { rol: "Root" } }),
    getProfile: async () => ({ profile: { nombre: "Luis", apellidos: "Gtz" } }),
    logout: async () => {
      cerro = true;
      return { ok: true };
    },
  };
  await expect(usuarioDePanel(api)).resolves.toEqual({
    nombre: "Luis",
    apellidos: "Gtz",
    rol: "Root",
  });
  expect(cerro).toBe(false);
});
