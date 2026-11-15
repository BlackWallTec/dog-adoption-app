// GET /cliente/perros (HU24).

export const SEXOS = ["Macho", "Hembra"];

// Rangos de edad_aprox
// si se cambian, se  pueden modificar desde aqui
export const RANGOS_EDAD = {
  cachorro: { min: 0, max: 1 },
  adulto: { min: 2, max: 4 },
  senior: { min: 5, max: null },
};

function esVacio(v) {
  return v === undefined || v === null || (typeof v === "string" && v.trim() === "");
}

// Devuelve { filtros, errores }. Si errores tiene claves, la petición se rechaza con 400.
// "filtros" solo contiene los parámetros que llegaron con valor, ya limpios:
// { sexo, tamano, raza, edad: { min, max }, caracteres: [ids] }
export function leerFiltrosPerros(query = {}) {
  const filtros = {};
  const errores = {};

  // Un parámetro repetido (?sexo=a&sexo=b) llega como arreglo y no es válido.
  const leerTexto = (clave) => {
    const valor = query[clave];
    if (esVacio(valor)) return undefined;
    if (typeof valor !== "string") {
      errores[clave] = "Debe enviarse un solo valor.";
      return undefined;
    }
    return valor.trim();
  };

  const sexo = leerTexto("sexo");
  if (sexo !== undefined) {
    const valido = SEXOS.find((s) => s.toLowerCase() === sexo.toLowerCase());
    if (valido) filtros.sexo = valido;
    else errores.sexo = `Valores válidos: ${SEXOS.join(", ")}.`;
  }

  const tamano = leerTexto("tamano");
  if (tamano !== undefined) filtros.tamano = tamano;

  const raza = leerTexto("raza");
  if (raza !== undefined) filtros.raza = raza;

  const edad = leerTexto("edad");
  if (edad !== undefined) {
    const clave = edad.toLowerCase();
    if (Object.hasOwn(RANGOS_EDAD, clave)) filtros.edad = { ...RANGOS_EDAD[clave] };
    else errores.edad = `Valores válidos: ${Object.keys(RANGOS_EDAD).join(", ")}.`;
  }

  // Ids separados por coma. Se quitan vacíos y repetidos.
  const caracter = leerTexto("caracter");
  if (caracter !== undefined) {
    const ids = [
      ...new Set(
        caracter
          .split(",")
          .map((id) => id.trim())
          .filter((id) => id !== ""),
      ),
    ];
    if (ids.length > 0) filtros.caracteres = ids;
  }

  return { filtros, errores };
}
