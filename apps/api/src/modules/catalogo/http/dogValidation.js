// Validación de los campos editables de una ficha de perro (HU19C).
// Función pura: no toca la BD, así se puede probar sin Supabase.

// Columnas de la tabla `perro` que se pueden modificar. id_perro, fecha_ingreso y activo
// quedan fuera: el id no cambia, la fecha la pone el sistema y activo es de HU27.
export const CAMPOS_EDITABLES = [
  "nombre",
  "sexo",
  "edad_aprox",
  "tamano_medida",
  "descripcion",
  "cuidado_especial",
  "esterilizado",
  "vacunado",
  "estado",
  "id_raza",
  "id_tamano",
];

// Relaciones que viven en otras tablas (carácter y fotos/videos).
// - caracteres: lista COMPLETA de ids que debe tener el perro al terminar.
// - multimedia_agregar: [{ url, tipo }] de archivos ya subidos al bucket.
// - multimedia_eliminar: ids de multimedia que se quitan de la ficha.
// (Desde HU20 el cuidado especial es un texto en la columna `cuidado_especial` de `perro`,
// ya no una relación con insignias.)
export const CAMPOS_RELACION = ["caracteres", "multimedia_agregar", "multimedia_eliminar"];

export const ESTADOS_PERRO = ["Disponible", "En Tratamiento", "Adoptado"];
export const TIPOS_MULTIMEDIA = ["imagen", "video"];

const EDAD_MAX = 30;
const CUIDADO_MAX = 500; // mismo tope que el alta (HU20)
const MULTIMEDIA_NUEVA_MAX = 20;
const URL_MAX = 2048;
const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function esVacio(v) {
  return v === null || v === undefined || (typeof v === "string" && v.trim() === "");
}

// Lista de UUID sin repetidos. Devuelve { lista } o { error }.
function listaDeUuids(valor, mensaje) {
  if (!Array.isArray(valor)) return { error: mensaje };
  const lista = [];
  for (const id of valor) {
    if (typeof id !== "string" || !UUID_REGEX.test(id.trim())) return { error: mensaje };
    const limpio = id.trim();
    if (!lista.includes(limpio)) lista.push(limpio);
  }
  return { lista };
}

function urlHttp(valor) {
  if (typeof valor !== "string" || valor.length > URL_MAX) return false;
  try {
    const { protocol } = new URL(valor);
    return protocol === "https:" || protocol === "http:";
  } catch {
    return false;
  }
}

// Devuelve { errores, valores, relaciones }. Si errores tiene claves, no se debe guardar.
// "valores" son columnas de `perro` y solo trae los campos que llegaron, ya limpios.
// "relaciones" solo trae las relaciones que llegaron con algo que aplicar.
export function validarCambiosPerro(body) {
  const errores = {};
  const valores = {};
  const relaciones = {};

  if (body === null || typeof body !== "object" || Array.isArray(body)) {
    return {
      errores: { body: "El cuerpo de la petición debe ser un objeto." },
      valores,
      relaciones,
    };
  }

  for (const clave of Object.keys(body)) {
    if (!CAMPOS_EDITABLES.includes(clave) && !CAMPOS_RELACION.includes(clave)) {
      errores[clave] = "Este campo no se puede modificar.";
    }
  }

  if ("nombre" in body) {
    const nombre = typeof body.nombre === "string" ? body.nombre.trim() : "";
    if (!nombre) errores.nombre = "El nombre es obligatorio.";
    else if (nombre.length > 60) errores.nombre = "El nombre no puede pasar de 60 caracteres.";
    else valores.nombre = nombre;
  }

  if ("sexo" in body) {
    const sexo = typeof body.sexo === "string" ? body.sexo.trim() : "";
    if (!sexo) errores.sexo = "Selecciona el sexo.";
    else if (sexo.length > 20) errores.sexo = "Valor de sexo no válido.";
    else valores.sexo = sexo;
  }

  if ("estado" in body) {
    const estado = typeof body.estado === "string" ? body.estado.trim() : "";
    if (!estado) errores.estado = "Selecciona el estado.";
    else if (!ESTADOS_PERRO.includes(estado)) errores.estado = "Estado no válido.";
    else valores.estado = estado;
  }

  if ("edad_aprox" in body) {
    const n = esVacio(body.edad_aprox) ? NaN : Number(body.edad_aprox);
    if (!Number.isInteger(n) || n < 0 || n > EDAD_MAX) {
      errores.edad_aprox = `La edad debe ser un entero entre 0 y ${EDAD_MAX}.`;
    } else valores.edad_aprox = n;
  }

  if ("tamano_medida" in body) {
    if (esVacio(body.tamano_medida)) {
      valores.tamano_medida = null;
    } else {
      const n = Number(body.tamano_medida);
      if (!Number.isFinite(n) || n < 0 || n > 200) {
        errores.tamano_medida = "La altura debe ser un número entre 0 y 200 cm.";
      } else valores.tamano_medida = n;
    }
  }

  if ("descripcion" in body) {
    if (esVacio(body.descripcion)) valores.descripcion = null;
    else if (typeof body.descripcion !== "string") errores.descripcion = "Descripción no válida.";
    else if (body.descripcion.trim().length > 1000) {
      errores.descripcion = "La descripción no puede pasar de 1000 caracteres.";
    } else valores.descripcion = body.descripcion.trim();
  }

  // Cuidado especial (HU20): texto libre; vacío se guarda como null.
  if ("cuidado_especial" in body) {
    if (esVacio(body.cuidado_especial)) valores.cuidado_especial = null;
    else if (typeof body.cuidado_especial !== "string") {
      errores.cuidado_especial = "Cuidado especial no válido.";
    } else if (body.cuidado_especial.trim().length > CUIDADO_MAX) {
      errores.cuidado_especial = `El cuidado especial no puede pasar de ${CUIDADO_MAX} caracteres.`;
    } else valores.cuidado_especial = body.cuidado_especial.trim();
  }

  for (const campo of ["esterilizado", "vacunado"]) {
    if (campo in body) {
      if (typeof body[campo] !== "boolean") errores[campo] = "Debe ser verdadero o falso.";
      else valores[campo] = body[campo];
    }
  }

  for (const campo of ["id_raza", "id_tamano"]) {
    if (campo in body) {
      const v = body[campo];
      const ok = (typeof v === "string" && v.trim() !== "") || Number.isInteger(v);
      if (!ok)
        errores[campo] = campo === "id_raza" ? "Selecciona la raza." : "Selecciona el tamaño.";
      else valores[campo] = typeof v === "string" ? v.trim() : v;
    }
  }

  // Carácter: lista completa (puede ir vacía para quitarlos todos).
  if ("caracteres" in body) {
    const { lista, error } = listaDeUuids(
      body.caracteres,
      "Las características deben ser una lista de ids válidos.",
    );
    if (error) errores.caracteres = error;
    else relaciones.caracteres = lista;
  }

  if ("multimedia_eliminar" in body) {
    const { lista, error } = listaDeUuids(
      body.multimedia_eliminar,
      "Las fotos a quitar deben ser una lista de ids válidos.",
    );
    if (error) errores.multimedia_eliminar = error;
    else if (lista.length > 0) relaciones.multimedia_eliminar = lista;
  }

  if ("multimedia_agregar" in body) {
    const nuevos = body.multimedia_agregar;
    if (!Array.isArray(nuevos)) {
      errores.multimedia_agregar = "Las fotos nuevas deben ser una lista.";
    } else if (nuevos.length > MULTIMEDIA_NUEVA_MAX) {
      errores.multimedia_agregar = `Solo se pueden agregar ${MULTIMEDIA_NUEVA_MAX} archivos a la vez.`;
    } else {
      const limpios = [];
      let valido = true;
      for (const item of nuevos) {
        const url = typeof item?.url === "string" ? item.url.trim() : "";
        if (!urlHttp(url) || !TIPOS_MULTIMEDIA.includes(item?.tipo)) {
          valido = false;
          break;
        }
        if (!limpios.some((x) => x.url === url)) limpios.push({ url, tipo: item.tipo });
      }
      if (!valido) {
        errores.multimedia_agregar =
          "Cada archivo nuevo necesita una url válida y tipo imagen o video.";
      } else if (limpios.length > 0) relaciones.multimedia_agregar = limpios;
    }
  }

  if (
    Object.keys(errores).length === 0 &&
    Object.keys(valores).length === 0 &&
    Object.keys(relaciones).length === 0
  ) {
    errores.body = "No hay cambios que guardar.";
  }

  return { errores, valores, relaciones };
}
