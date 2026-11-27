import { describe, expect, test } from "vite-plus/test";
import {
  cambiosEnRelaciones,
  cambiosRespectoA,
  escaparHtml,
  idsDeRelacion,
  mismoConjunto,
  nombreTamanoPorAltura,
  tamanoPorAltura,
  validarFormulario,
} from "./perroForm.js";

const perro = {
  nombre: "Luna",
  sexo: "Hembra",
  edad_aprox: 3,
  tamano_medida: 40,
  estado: "Disponible",
  descripcion: "Cariñosa",
  esterilizado: true,
  vacunado: false,
  raza: { id_raza: "r1", nombre: "Mestizo" },
  tamano: { id_tamano: "t1", nombre: "Mediano" },
};

const formIgual = {
  nombre: "Luna",
  sexo: "Hembra",
  edad_aprox: "3",
  id_raza: "r1",
  id_tamano: "t1",
  tamano_medida: "40",
  estado: "Disponible",
  descripcion: "Cariñosa",
  esterilizado: true,
  vacunado: false,
};

describe("escaparHtml", () => {
  test("neutraliza etiquetas y comillas", () => {
    expect(escaparHtml(`<img src=x onerror="alert(1)">`)).toBe(
      "&lt;img src=x onerror=&quot;alert(1)&quot;&gt;",
    );
    expect(escaparHtml(null)).toBe("");
  });
});

describe("cambiosRespectoA", () => {
  test("sin cambios devuelve objeto vacío", () => {
    expect(cambiosRespectoA(perro, formIgual)).toEqual({});
  });

  test("solo incluye los campos modificados", () => {
    expect(cambiosRespectoA(perro, { ...formIgual, nombre: "Lunita", vacunado: true })).toEqual({
      nombre: "Lunita",
      vacunado: true,
    });
  });

  test("ignora espacios sobrantes al comparar", () => {
    expect(cambiosRespectoA(perro, { ...formIgual, nombre: " Luna " })).toEqual({});
  });

  test("detecta cambio de raza", () => {
    expect(cambiosRespectoA(perro, { ...formIgual, id_raza: "r2" })).toEqual({ id_raza: "r2" });
  });

  test("ficha con campos nulos y formulario vacío no cuenta como cambio", () => {
    const sinDatos = { ...perro, descripcion: null, tamano_medida: null };
    expect(
      cambiosRespectoA(sinDatos, { ...formIgual, descripcion: "", tamano_medida: "" }),
    ).toEqual({});
  });
});

describe("validarFormulario", () => {
  test("formulario completo es válido", () => {
    expect(validarFormulario(formIgual)).toEqual({});
  });

  test("marca los obligatorios vacíos", () => {
    const e = validarFormulario({
      ...formIgual,
      nombre: " ",
      sexo: "",
      edad_aprox: "",
      id_raza: "",
      id_tamano: "",
      estado: "",
    });
    expect(Object.keys(e).sort()).toEqual([
      "edad_aprox",
      "estado",
      "id_raza",
      "id_tamano",
      "nombre",
      "sexo",
    ]);
  });

  test("edad negativa o decimal es inválida; medida negativa también", () => {
    expect(validarFormulario({ ...formIgual, edad_aprox: "-1" }).edad_aprox).toBeDefined();
    expect(validarFormulario({ ...formIgual, edad_aprox: "2.5" }).edad_aprox).toBeDefined();
    expect(validarFormulario({ ...formIgual, tamano_medida: "-5" }).tamano_medida).toBeDefined();
  });
});

describe("tamaño según la altura", () => {
  const tamanos = [
    { id_tamano: "t1", nombre: "Chico" },
    { id_tamano: "t2", nombre: "Mediano" },
    { id_tamano: "t3", nombre: "Grande" },
  ];

  test("respeta los límites: <45 Chico, 45-75 Mediano, >75 Grande", () => {
    expect(nombreTamanoPorAltura(21.8)).toBe("Chico");
    expect(nombreTamanoPorAltura("44.9")).toBe("Chico");
    expect(nombreTamanoPorAltura(45)).toBe("Mediano");
    expect(nombreTamanoPorAltura(75)).toBe("Mediano");
    expect(nombreTamanoPorAltura(75.1)).toBe("Grande");
    expect(nombreTamanoPorAltura(85.6)).toBe("Grande");
  });

  test("sin altura válida no decide nada", () => {
    for (const v of ["", "  ", "abc", -3, null]) expect(nombreTamanoPorAltura(v)).toBeNull();
  });

  test("devuelve el id del tamaño o null si no existe en el catálogo", () => {
    expect(tamanoPorAltura(60, tamanos)).toBe("t2");
    expect(tamanoPorAltura(60, [{ id_tamano: "x", nombre: "Gigante" }])).toBeNull();
  });

  test("sexo con relleno de la BD no cuenta como cambio", () => {
    expect(cambiosRespectoA({ ...perro, sexo: "Hembra    " }, formIgual)).toEqual({});
  });

  test("altura mayor a 200 cm es inválida", () => {
    expect(validarFormulario({ ...formIgual, tamano_medida: "650" }).tamano_medida).toBeDefined();
    expect(
      validarFormulario({ ...formIgual, tamano_medida: "21.8" }).tamano_medida,
    ).toBeUndefined();
  });
});

// ---- HU19C: estado, características y cuidados ----

const perroCompleto = {
  ...perro,
  sexo: "Hembra     ", // CHAR con relleno
  estado: "Disponible ",
  perro_tiene_caracter: [{ id_caracter: "c1" }, { id_caracter: "c2" }],
  perro_tiene_cuidado_especial: [{ id_cuidado: "u1" }],
};

// Lo que devuelve leerFormulario si el administrador no toca nada.
const formSinTocar = {
  ...formIgual,
  caracteres: ["c1", "c2"],
  cuidados: ["u1"],
};

describe("idsDeRelacion / mismoConjunto", () => {
  test("saca los ids de la relación y tolera datos ausentes", () => {
    expect(idsDeRelacion(perroCompleto, "perro_tiene_caracter", "id_caracter")).toEqual([
      "c1",
      "c2",
    ]);
    expect(idsDeRelacion({}, "perro_tiene_caracter", "id_caracter")).toEqual([]);
    expect(idsDeRelacion(null, "perro_tiene_caracter", "id_caracter")).toEqual([]);
    expect(
      idsDeRelacion({ perro_tiene_caracter: [null, {}] }, "perro_tiene_caracter", "id_caracter"),
    ).toEqual([]);
  });

  test("compara conjuntos sin importar el orden", () => {
    expect(mismoConjunto(["a", "b"], ["b", "a"])).toBe(true);
    expect(mismoConjunto([], [])).toBe(true);
    expect(mismoConjunto(["a"], ["a", "b"])).toBe(false);
    expect(mismoConjunto(["a", "b"], ["a", "c"])).toBe(false);
  });
});

describe("cambiosRespectoA con estado", () => {
  test("sin tocar nada no hay cambios (el relleno de la BD no cuenta)", () => {
    expect(cambiosRespectoA(perroCompleto, formSinTocar)).toEqual({});
  });

  test("cambiar el estado se manda como cambio", () => {
    expect(cambiosRespectoA(perroCompleto, { ...formSinTocar, estado: "Adoptado" })).toEqual({
      estado: "Adoptado",
    });
  });

  test("un perro sin estado en la BD manda el estado elegido", () => {
    expect(cambiosRespectoA({ ...perroCompleto, estado: null }, formSinTocar)).toEqual({
      estado: "Disponible",
    });
  });
});

describe("cambiosEnRelaciones", () => {
  test("sin cambios no manda nada", () => {
    expect(cambiosEnRelaciones(perroCompleto, formSinTocar)).toEqual({});
    expect(
      cambiosEnRelaciones(perroCompleto, { ...formSinTocar, caracteres: ["c2", "c1"] }),
    ).toEqual({});
  });

  test("manda la lista completa cuando cambia el conjunto", () => {
    expect(
      cambiosEnRelaciones(perroCompleto, { ...formSinTocar, caracteres: ["c1", "c3"] }),
    ).toEqual({ caracteres: ["c1", "c3"] });
    expect(cambiosEnRelaciones(perroCompleto, { ...formSinTocar, cuidados: ["u1", "u2"] })).toEqual(
      { cuidados: ["u1", "u2"] },
    );
  });

  test("quitar todo es un cambio: se manda la lista vacía", () => {
    expect(
      cambiosEnRelaciones(perroCompleto, { ...formSinTocar, caracteres: [], cuidados: [] }),
    ).toEqual({ caracteres: [], cuidados: [] });
  });

  test("agregar a un perro que no tenía nada", () => {
    expect(cambiosEnRelaciones({}, { ...formSinTocar, caracteres: ["c1"], cuidados: [] })).toEqual({
      caracteres: ["c1"],
    });
  });

  test("si el formulario no trae la lista, no toca esa relación", () => {
    expect(cambiosEnRelaciones(perroCompleto, { nombre: "x" })).toEqual({});
  });
});

describe("validarFormulario con estado", () => {
  test("el estado es obligatorio", () => {
    expect(validarFormulario({ ...formIgual, estado: "" }).estado).toBe("Selecciona el estado.");
  });
});
