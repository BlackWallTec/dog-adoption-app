import { FORBIDDEN } from "@qarino/shared/errores.js";
import { rutaDe } from "../rutas.js";
import { perfilesApi } from "./perfiles.api.js";
import { usuarioActual } from "./usuarioActual.js";

const ROLES_PANEL = new Set(["Administrador", "Root"]);

export const MENSAJE_SIN_ACCESO = "Tu cuenta no tiene acceso al panel administrativo.";

/** @param {unknown} rol */
export function puedeEntrarAlPanel(rol) {
  return typeof rol === "string" && ROLES_PANEL.has(rol);
}

export function rutaAccesoDenegado() {
  return `${rutaDe("login")}?acceso=denegado`;
}

/**
 * Sesión de Administrador o Root.
 * Un Cliente (u otro rol) pierde la cookie y no entra al panel.
 * Sin rol no se rechaza: es un fallo de lectura, no una cuenta ajena.
 * @param {Parameters<typeof usuarioActual>[0] & { logout?: () => Promise<unknown> }} [api]
 */
export async function usuarioDePanel(api = perfilesApi) {
  const usuario = await usuarioActual(api);
  if (!usuario.rol || puedeEntrarAlPanel(usuario.rol)) return usuario;
  try {
    await api.logout?.();
  } catch {
    // El login también rechaza ese rol si la cookie sigue viva.
  }
  throw new FORBIDDEN(MENSAJE_SIN_ACCESO);
}
