import { UNAUTHENTICATED } from "@qarino/shared/errores.js";
import { perfilesApi } from "./perfiles.api.js";

const VACIO = Object.freeze({
  nombre: "",
  apellidos: "",
  rol: "",
});

/** @param {unknown} valor */
function limpio(valor) {
  return typeof valor === "string" ? valor.trim() : "";
}

/**
 * Nombre y rol de la sesión.
 * Un 401 se propaga: Inicio manda al login y el topbar no inventa una persona.
 * Otro fallo deja el nombre vacío.
 * @param {{
 *   getSession: () => Promise<{ user?: { rol?: string } }>,
 *   getProfile: () => Promise<{ profile?: { nombre?: string, apellidos?: string } }>,
 * }} [api]
 */
export async function usuarioActual(api = perfilesApi) {
  let sesion;
  try {
    sesion = await api.getSession();
  } catch (error) {
    if (error instanceof UNAUTHENTICATED) throw error;
    return { ...VACIO };
  }

  /** @type {{ nombre?: string, apellidos?: string } | null} */
  let profile = null;
  try {
    const respuesta = await api.getProfile();
    profile = respuesta?.profile ?? null;
  } catch (error) {
    if (error instanceof UNAUTHENTICATED) throw error;
  }

  return {
    nombre: limpio(profile?.nombre),
    apellidos: limpio(profile?.apellidos),
    rol: limpio(sesion?.user?.rol),
  };
}

/** @param {string} nombre @param {string} [apellidos] */
export function iniciales(nombre, apellidos = "") {
  const primera = limpio(nombre).charAt(0);
  const segunda = limpio(apellidos).charAt(0);
  return `${primera}${segunda}`.toUpperCase();
}

/** @param {{ nombre?: string, apellidos?: string }} usuario */
export function nombreCompleto(usuario) {
  return [limpio(usuario.nombre), limpio(usuario.apellidos)].filter(Boolean).join(" ");
}
