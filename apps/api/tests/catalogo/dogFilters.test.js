import { describe, test, expect } from "vite-plus/test";

import { leerFiltrosPerros, RANGOS_EDAD } from "../../src/modules/catalogo/http/dogFilters.js";

describe("HU24: leerFiltrosPerros", () => {
  test("sin parámetros -> sin filtros y sin errores", () => {
    expect(leerFiltrosPerros({})).toEqual({ filtros: {}, errores: {} });
    expect(leerFiltrosPerros()).toEqual({ filtros: {}, errores: {} });
  });

  test("parámetros vacíos se tratan como no enviados", () => {
    const query = { sexo: "", tamano: "  ", raza: "", edad: "", caracter: " , " };
    expect(leerFiltrosPerros(query)).toEqual({ filtros: {}, errores: {} });
  });

  test("sexo válido sin importar mayúsculas -> valor canónico", () => {
    expect(leerFiltrosPerros({ sexo: "hembra" }).filtros.sexo).toBe("Hembra");
    expect(leerFiltrosPerros({ sexo: " MACHO " }).filtros.sexo).toBe("Macho");
  });

  test("sexo inválido -> error", () => {
    const { filtros, errores } = leerFiltrosPerros({ sexo: "Otro" });
    expect(filtros.sexo).toBeUndefined();
    expect(Object.keys(errores)).toEqual(["sexo"]);
  });

  test("tamano y raza se pasan recortados", () => {
    const { filtros } = leerFiltrosPerros({ tamano: " t-1 ", raza: "r-1" });
    expect(filtros).toEqual({ tamano: "t-1", raza: "r-1" });
  });

  test.each([
    ["cachorro", { min: 0, max: 1 }],
    ["adulto", { min: 2, max: 4 }],
    ["senior", { min: 5, max: null }],
    ["SENIOR", { min: 5, max: null }],
  ])("edad=%s -> rango %j", (valor, rango) => {
    expect(leerFiltrosPerros({ edad: valor }).filtros.edad).toEqual(rango);
  });

  test("los rangos de edad no se traslapan y cubren desde 0", () => {
    const { cachorro, adulto, senior } = RANGOS_EDAD;
    expect(cachorro.min).toBe(0);
    expect(adulto.min).toBe(cachorro.max + 1);
    expect(senior.min).toBe(adulto.max + 1);
    expect(senior.max).toBeNull();
  });

  test("edad inválida -> error (incluye nombres heredados de Object)", () => {
    expect(Object.keys(leerFiltrosPerros({ edad: "viejito" }).errores)).toEqual(["edad"]);
    expect(Object.keys(leerFiltrosPerros({ edad: "constructor" }).errores)).toEqual(["edad"]);
  });

  test("caracter -> lista de ids sin vacíos ni repetidos", () => {
    const { filtros } = leerFiltrosPerros({ caracter: "a, b,,a ,c" });
    expect(filtros.caracteres).toEqual(["a", "b", "c"]);
  });

  test("parámetro repetido (arreglo) -> error", () => {
    const { errores } = leerFiltrosPerros({ sexo: ["Macho", "Hembra"] });
    expect(Object.keys(errores)).toEqual(["sexo"]);
  });

  test("varios filtros a la vez se combinan", () => {
    const { filtros, errores } = leerFiltrosPerros({
      sexo: "Hembra",
      edad: "adulto",
      tamano: "t-1",
      raza: "r-1",
      caracter: "c-1,c-2",
    });
    expect(errores).toEqual({});
    expect(filtros).toEqual({
      sexo: "Hembra",
      edad: { min: 2, max: 4 },
      tamano: "t-1",
      raza: "r-1",
      caracteres: ["c-1", "c-2"],
    });
  });
});
