// Reglas de publicación de HU22. Puras: no consultan la base de datos.

const MAX_FINALES = 20;
const ESTADOS_ADOPTADO = new Set(["adoptado", "adoptada"]);

function texto(valor) {
  return typeof valor === "string" ? valor.trim() : "";
}

function perroDe(fila) {
  const anidado = fila.perro;
  if (Array.isArray(anidado)) return anidado[0] ?? {};
  if (anidado && typeof anidado === "object") return anidado;
  return {};
}

function normalizar(fila) {
  if (!fila || typeof fila !== "object" || Array.isArray(fila)) return null;
  const perro = perroDe(fila);
  const fecha = texto(fila.fecha_adopcion);
  return {
    id_perro: texto(fila.id_perro),
    nombre: texto(fila.nombre || perro.nombre),
    foto_hogar_url: texto(fila.foto_hogar_url),
    historia: texto(fila.historia),
    fecha_adopcion: fecha || null,
    estado: texto(fila.estado || perro.estado),
    publicado: fila.publicado === true,
  };
}

function esPublicable(item) {
  return (
    item.publicado &&
    ESTADOS_ADOPTADO.has(item.estado.toLowerCase()) &&
    item.id_perro !== "" &&
    item.foto_hogar_url !== "" &&
    item.historia !== ""
  );
}

// Conserva la primera fila de cada perro, ordena por adopción reciente y corta en 20.
export function prepararFinalesFelices(filas) {
  if (!Array.isArray(filas)) return [];

  const vistos = new Set();
  const publicables = [];
  for (const fila of filas) {
    const item = normalizar(fila);
    if (!item || !esPublicable(item)) continue;
    if (vistos.has(item.id_perro)) continue;
    vistos.add(item.id_perro);
    publicables.push(item);
  }

  publicables.sort((a, b) => {
    const aSinFecha = a.fecha_adopcion == null;
    const bSinFecha = b.fecha_adopcion == null;
    if (aSinFecha && bSinFecha) return 0;
    if (aSinFecha !== bSinFecha) return aSinFecha ? 1 : -1;
    return b.fecha_adopcion.localeCompare(a.fecha_adopcion);
  });

  return publicables.slice(0, MAX_FINALES);
}
