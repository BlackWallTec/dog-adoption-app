import { describe, expect, test } from "vite-plus/test";
import { validarCambiosPerro } from "./dogValidation.js";

describe("validarCambiosPerro", () => {
  test("acepta cambios válidos y limpia espacios", () => {
    const { errores, valores } = validarCambiosPerro({
      nombre: "  Luna ",
      edad_aprox: "3",
      esterilizado: true,
      descripcion: "  Cariñosa ",
    });
    expect(errores).toEqual({});
    expect(valores).toEqual({
      nombre: "Luna",
      edad_aprox: 3,
      esterilizado: true,
      descripcion: "Cariñosa",
    });
  });

  test("rechaza nombre vacío o solo espacios", () => {
    expect(validarCambiosPerro({ nombre: "   " }).errores.nombre).toBeDefined();
    expect(validarCambiosPerro({ nombre: "" }).errores.nombre).toBeDefined();
    expect(validarCambiosPerro({ nombre: 5 }).errores.nombre).toBeDefined();
  });

  test("rechaza edad negativa, decimal, vacía o no numérica", () => {
    for (const edad of [-1, 2.5, "", "abc", null, 31]) {
      expect(validarCambiosPerro({ edad_aprox: edad }).errores.edad_aprox).toBeDefined();
    }
    expect(validarCambiosPerro({ edad_aprox: 0 }).valores.edad_aprox).toBe(0);
  });

  test("tamano_medida y descripcion vacíos se guardan como null", () => {
    const { errores, valores } = validarCambiosPerro({ tamano_medida: "", descripcion: " " });
    expect(errores).toEqual({});
    expect(valores).toEqual({ tamano_medida: null, descripcion: null });
  });

  test("rechaza tamano_medida negativa", () => {
    expect(validarCambiosPerro({ tamano_medida: -3 }).errores.tamano_medida).toBeDefined();
  });

  test("esterilizado y vacunado deben ser booleanos", () => {
    expect(validarCambiosPerro({ vacunado: "si" }).errores.vacunado).toBeDefined();
    expect(validarCambiosPerro({ esterilizado: false }).valores.esterilizado).toBe(false);
  });

  test("rechaza campos no editables (estado, id_perro, fecha_ingreso)", () => {
    const { errores } = validarCambiosPerro({
      nombre: "Luna",
      estado: "Adoptado",
      id_perro: "x",
      fecha_ingreso: "2026-01-01",
    });
    expect(Object.keys(errores).sort()).toEqual(["estado", "fecha_ingreso", "id_perro"]);
  });

  test("raza y tamaño no pueden ir vacíos", () => {
    expect(validarCambiosPerro({ id_raza: "" }).errores.id_raza).toBeDefined();
    expect(validarCambiosPerro({ id_tamano: null }).errores.id_tamano).toBeDefined();
    expect(validarCambiosPerro({ id_raza: "abc-1" }).valores.id_raza).toBe("abc-1");
  });

  test("body vacío o no objeto: no hay cambios", () => {
    expect(validarCambiosPerro({}).errores.body).toBeDefined();
    expect(validarCambiosPerro(undefined).errores.body).toBeDefined();
    expect(validarCambiosPerro([]).errores.body).toBeDefined();
  });

  test("guarda tal cual un nombre con HTML (el escape se hace al mostrarlo)", () => {
    expect(validarCambiosPerro({ nombre: "<b>Rex</b>" }).valores.nombre).toBe("<b>Rex</b>");
  });
});
