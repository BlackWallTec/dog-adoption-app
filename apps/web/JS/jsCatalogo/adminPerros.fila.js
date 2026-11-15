export const FOTO_RESPALDO = "/public/favicon.svg";
export const NO_ESPECIFICADO = "No especificado";

export function urlFoto(perro) {
  const multimedia = Array.isArray(perro?.multimedia) ? perro.multimedia : [];
  const primera = multimedia.find((item) => typeof item?.url === "string" && item.url.trim());
  const candidata = primera?.url || perro?.foto_url || perro?.foto;
  if (typeof candidata === "string" && candidata.trim()) return candidata.trim();
  return FOTO_RESPALDO;
}

export function textoDato(valor) {
  if (valor && typeof valor === "object") return textoDato(valor.nombre);
  if (valor === null || valor === undefined) return NO_ESPECIFICADO;
  const texto = String(valor).trim();
  return texto || NO_ESPECIFICADO;
}

export function textoEdad(perro) {
  const edad = perro?.edad ?? perro?.edad_aprox;
  if (edad === null || edad === undefined || String(edad).trim() === "") return NO_ESPECIFICADO;
  if (typeof edad === "number" || /^\d+(\.\d+)?$/.test(String(edad).trim())) {
    return `${edad} años`;
  }
  return String(edad).trim();
}

export function textoContador(total) {
  if (total === 0) return "No hay perros registrados.";
  return `Mostrando ${total} perro${total === 1 ? "" : "s"} bajo cuidado`;
}

export function textoPaginacion(total) {
  return total === 0 ? "Mostrando 0 de 0 perros" : `Mostrando 1-${total} de ${total} perros`;
}

export function marcadoErrorListado() {
  return `<div class="estado-error"><span class="material-symbols-rounded">error</span><p>No se pudo cargar el listado de perros.</p><button type="button" id="btn-reintentar-perros" class="btn-primary">Reintentar</button></div>`;
}

export function listadoDeRespuesta(data) {
  if (data && typeof data === "object" && !Array.isArray(data) && data.success === false) {
    return null;
  }
  if (Array.isArray(data)) return data;
  if (data && typeof data === "object" && Array.isArray(data.data)) return data.data;
  if (data && typeof data === "object" && Array.isArray(data.perros)) return data.perros;
  return null;
}
