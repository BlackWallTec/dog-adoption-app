import { perfilesApi } from "./perfiles.api.js";

const MUESTRA = Object.freeze({
  nombre: "Ana",
  apellidos: "Martínez",
  rol: "Administrador",
});

/** @param {unknown} valor */
function limpio(valor) {
  return typeof valor === "string" ? valor.trim() : "";
}

/** Nombre y rol de quien tiene la sesión. Si /me o /profile fallan, queda el de muestra. */
export async function usuarioActual() {
  try {
    const [{ user }, { profile }] = await Promise.all([
      perfilesApi.getSession(),
      perfilesApi.getProfile(),
    ]);
    return {
      nombre: limpio(profile?.nombre) || MUESTRA.nombre,
      apellidos: limpio(profile?.apellidos),
      rol: limpio(user?.rol) || MUESTRA.rol,
    };
  } catch {
    return { ...MUESTRA };
  }
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
