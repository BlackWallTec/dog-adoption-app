import { describe, expect, test } from "vite-plus/test";
import {
  cambiosRespectoA,
  escaparHtml,
  nombreTamanoPorAltura,
  tamanoPorAltura,
  validarFormulario,
} from "./perroForm.js";

const perro = {
  nombre: "Luna",
  sexo: "Hembra",
  edad_aprox: 3,
  tamano_medida: 40,
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
    });
    expect(Object.keys(e).sort()).toEqual(["edad_aprox", "id_raza", "id_tamano", "nombre", "sexo"]);
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
