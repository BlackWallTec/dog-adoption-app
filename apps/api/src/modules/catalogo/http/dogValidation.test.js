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

  test("rechaza campos no editables (activo, id_perro, fecha_ingreso)", () => {
    const { errores } = validarCambiosPerro({
      nombre: "Luna",
      activo: false,
      id_perro: "x",
      fecha_ingreso: "2026-01-01",
    });
    expect(Object.keys(errores).sort()).toEqual(["activo", "fecha_ingreso", "id_perro"]);
  });

  test("el estado es editable pero solo con valores conocidos", () => {
    expect(validarCambiosPerro({ estado: " En Tratamiento " }).valores).toEqual({
      estado: "En Tratamiento",
    });
    expect(validarCambiosPerro({ estado: "Perdido" }).errores.estado).toBe("Estado no válido.");
    expect(validarCambiosPerro({ estado: "  " }).errores.estado).toBe("Selecciona el estado.");
    expect(validarCambiosPerro({ estado: 3 }).errores.estado).toBe("Selecciona el estado.");
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

const ID_A = "11111111-1111-4111-8111-111111111111";
const ID_B = "22222222-2222-4222-9222-222222222222";
const URL_A = "https://x.supabase.co/storage/v1/object/public/multimedia_perros/a.jpg";
const URL_B = "https://x.supabase.co/storage/v1/object/public/multimedia_perros/b.mp4";

describe("validarCambiosPerro: características y cuidados", () => {
  test("acepta la lista completa, sin repetidos y limpia espacios", () => {
    const { errores, relaciones, valores } = validarCambiosPerro({
      caracteres: [ID_A, ` ${ID_B} `, ID_A],
      cuidados: [ID_B],
    });

    expect(errores).toEqual({});
    expect(valores).toEqual({});
    expect(relaciones).toEqual({ caracteres: [ID_A, ID_B], cuidados: [ID_B] });
  });

  test("una lista vacía es un cambio válido: quitar todo", () => {
    const { errores, relaciones } = validarCambiosPerro({ caracteres: [], cuidados: [] });

    expect(errores).toEqual({});
    expect(relaciones).toEqual({ caracteres: [], cuidados: [] });
  });

  test("rechaza ids que no son uuid o listas que no son arreglo", () => {
    expect(validarCambiosPerro({ caracteres: ["1"] }).errores.caracteres).toMatch(/ids válidos/);
    expect(validarCambiosPerro({ cuidados: [42] }).errores.cuidados).toMatch(/ids válidos/);
    expect(validarCambiosPerro({ caracteres: ID_A }).errores.caracteres).toMatch(/ids válidos/);
    expect(validarCambiosPerro({ caracteres: null }).errores.caracteres).toMatch(/ids válidos/);
  });
});

describe("validarCambiosPerro: fotos y videos", () => {
  test("acepta archivos nuevos con url http(s) y tipo imagen o video", () => {
    const { errores, relaciones } = validarCambiosPerro({
      multimedia_agregar: [
        { url: ` ${URL_A} `, tipo: "imagen" },
        { url: URL_B, tipo: "video" },
      ],
    });

    expect(errores).toEqual({});
    expect(relaciones.multimedia_agregar).toEqual([
      { url: URL_A, tipo: "imagen" },
      { url: URL_B, tipo: "video" },
    ]);
  });

  test("rechaza urls que no son http(s), tipos raros y elementos mal formados", () => {
    for (const malo of [
      { url: "javascript:alert(1)", tipo: "imagen" },
      { url: "ftp://x/a.jpg", tipo: "imagen" },
      { url: "no es url", tipo: "imagen" },
      { url: URL_A, tipo: "audio" },
      { url: URL_A },
      { tipo: "imagen" },
      URL_A,
      null,
    ]) {
      expect(
        validarCambiosPerro({ multimedia_agregar: [malo] }).errores.multimedia_agregar,
      ).toMatch(/url válida y tipo/);
    }
    expect(validarCambiosPerro({ multimedia_agregar: "x" }).errores.multimedia_agregar).toMatch(
      /lista/,
    );
  });

  test("no deja agregar más de 20 archivos en una sola edición", () => {
    const muchos = Array.from({ length: 21 }, (_, i) => ({
      url: `https://x/${i}.jpg`,
      tipo: "imagen",
    }));

    expect(validarCambiosPerro({ multimedia_agregar: muchos }).errores.multimedia_agregar).toMatch(
      /20/,
    );
  });

  test("ids de fotos a quitar: valida uuid y quita repetidos", () => {
    expect(validarCambiosPerro({ multimedia_eliminar: [ID_A, ID_A] }).relaciones).toEqual({
      multimedia_eliminar: [ID_A],
    });
    expect(validarCambiosPerro({ multimedia_eliminar: ["x"] }).errores.multimedia_eliminar).toMatch(
      /ids válidos/,
    );
  });

  test("listas de multimedia vacías no cuentan como cambio", () => {
    const { errores, relaciones } = validarCambiosPerro({
      multimedia_agregar: [],
      multimedia_eliminar: [],
    });

    expect(relaciones).toEqual({});
    expect(errores.body).toBe("No hay cambios que guardar.");
  });

  test("solo cambiar fotos ya es una edición válida", () => {
    expect(validarCambiosPerro({ multimedia_eliminar: [ID_A] }).errores).toEqual({});
  });
});
