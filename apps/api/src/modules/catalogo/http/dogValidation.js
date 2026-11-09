// Validación de los campos editables de una ficha de perro (HU19C).
// Función pura: no toca la BD, así se puede probar sin Supabase.

// Solo estos campos se pueden modificar. Estado, fotos, carácter, cuidados,
// id_perro y fecha_ingreso quedan fuera (otras HU o no editables).
export const CAMPOS_EDITABLES = [
  "nombre",
  "sexo",
  "edad_aprox",
  "tamano_medida",
  "descripcion",
  "esterilizado",
  "vacunado",
  "id_raza",
  "id_tamano",
];

const EDAD_MAX = 30;

function esVacio(v) {
  return v === null || v === undefined || (typeof v === "string" && v.trim() === "");
}

// Devuelve { errores, valores }. Si errores tiene claves, no se debe guardar.
// "valores" solo contiene los campos que llegaron en el body, ya limpios.
export function validarCambiosPerro(body) {
  const errores = {};
  const valores = {};

  if (body === null || typeof body !== "object" || Array.isArray(body)) {
    return { errores: { body: "El cuerpo de la petición debe ser un objeto." }, valores };
  }

  for (const clave of Object.keys(body)) {
    if (!CAMPOS_EDITABLES.includes(clave)) {
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

  if (Object.keys(errores).length === 0 && Object.keys(valores).length === 0) {
    errores.body = "No hay cambios que guardar.";
  }

  return { errores, valores };
}
