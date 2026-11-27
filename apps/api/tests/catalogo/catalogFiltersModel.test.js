import { describe, test, expect, beforeEach, vi } from "vite-plus/test";

const { fromMock } = vi.hoisted(() => ({ fromMock: vi.fn() }));

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: { from: fromMock },
  supabaseAdmin: { from: vi.fn() },
}));

import dogModel from "../../src/modules/catalogo/http/modelosCatalogo/dog.model.js";

// Consulta falsa de Supabase: cada método encadenable se anota y devuelve la misma consulta.
// Al hacer await devuelve { data, error }, igual que el cliente real.
function crearConsulta(resultado) {
  const llamadas = [];
  const consulta = new Proxy(
    {},
    {
      get(_, nombre) {
        if (nombre === "then") {
          return (resolve, reject) => Promise.resolve(resultado).then(resolve, reject);
        }
        return (...args) => {
          llamadas.push([nombre, ...args]);
          return consulta;
        };
      },
    },
  );
  return { consulta, llamadas };
}

const metodos = (llamadas) => llamadas.map(([nombre]) => nombre);
const buscar = (llamadas, nombre) => llamadas.filter(([n]) => n === nombre);

describe("HU24: DogModel.getDogs con filtros", () => {
  let perros;

  beforeEach(() => {
    fromMock.mockReset();
    perros = crearConsulta({ data: [{ id_perro: "p1" }], error: null });
    fromMock.mockReturnValue(perros.consulta);
  });

  test("sin filtros: misma consulta de siempre (activo, orden, relaciones normales)", async () => {
    const data = await dogModel.getDogs();

    expect(data).toEqual([{ id_perro: "p1" }]);
    expect(fromMock).toHaveBeenCalledWith("perro");
    expect(metodos(perros.llamadas)).toEqual(["select", "eq", "eq", "order", "limit", "order"]);
    expect(buscar(perros.llamadas, "eq").map(([, ...a]) => a)).toContainEqual(["activo", true]);
    expect(buscar(perros.llamadas, "order").map(([, ...a]) => a)).toContainEqual([
      "fecha_ingreso",
      { ascending: false },
    ]);

    const select = buscar(perros.llamadas, "select")[0][1];
    expect(select).not.toContain("!inner");
    expect(select).toContain("raza (");
    expect(select).toContain("tamano (");
    expect(select).toContain("multimedia(");
  });

  test("filtros vacíos equivalen a no mandar filtros", async () => {
    await dogModel.getDogs({});
    expect(metodos(perros.llamadas)).toEqual(["select", "eq", "eq", "order", "limit", "order"]);
  });

  test("sexo -> eq sexo", async () => {
    await dogModel.getDogs({ sexo: "Hembra" });
    expect(buscar(perros.llamadas, "eq").map(([, ...a]) => a)).toContainEqual(["sexo", "Hembra"]);
  });

  test("edad con tope -> gte y lte", async () => {
    await dogModel.getDogs({ edad: { min: 2, max: 4 } });
    expect(buscar(perros.llamadas, "gte")[0].slice(1)).toEqual(["edad_aprox", 2]);
    expect(buscar(perros.llamadas, "lte")[0].slice(1)).toEqual(["edad_aprox", 4]);
  });

  test("edad sin tope (senior) -> solo gte", async () => {
    await dogModel.getDogs({ edad: { min: 5, max: null } });
    expect(buscar(perros.llamadas, "gte")[0].slice(1)).toEqual(["edad_aprox", 5]);
    expect(buscar(perros.llamadas, "lte")).toHaveLength(0);
  });

  test("tamano -> relación obligatoria y filtro por id", async () => {
    await dogModel.getDogs({ tamano: "t-1" });
    const select = buscar(perros.llamadas, "select")[0][1];
    expect(select).toContain("tamano!inner");
    expect(select).not.toContain("raza!inner");
    expect(buscar(perros.llamadas, "eq").map(([, ...a]) => a)).toContainEqual([
      "tamano.id_tamano",
      "t-1",
    ]);
  });

  test("raza -> relación obligatoria y filtro por id", async () => {
    await dogModel.getDogs({ raza: "r-1" });
    const select = buscar(perros.llamadas, "select")[0][1];
    expect(select).toContain("raza!inner");
    expect(select).not.toContain("tamano!inner");
    expect(buscar(perros.llamadas, "eq").map(([, ...a]) => a)).toContainEqual([
      "raza.id_raza",
      "r-1",
    ]);
  });

  test("varios filtros se acumulan (AND)", async () => {
    await dogModel.getDogs({ sexo: "Macho", edad: { min: 0, max: 1 }, raza: "r-1" });
    const nombres = metodos(perros.llamadas);
    expect(nombres.filter((n) => n === "eq")).toHaveLength(4); // activo + sexo + raza
    expect(nombres).toContain("gte");
    expect(nombres).toContain("lte");
  });

  test("error de Supabase -> lanza con el código para que el controlador decida", async () => {
    fromMock.mockReturnValue(
      crearConsulta({ data: null, error: { message: "boom", code: "22P02" } }).consulta,
    );
    await expect(dogModel.getDogs({ tamano: "no-es-uuid" })).rejects.toMatchObject({
      message: "Error en la consulta de perros: boom",
      code: "22P02",
    });
  });
});

describe("HU24: DogModel.getDogs con características (AND)", () => {
  let perros;
  let relacion;

  const prepararRelacion = (filas) => {
    relacion = crearConsulta({ data: filas, error: null });
    perros = crearConsulta({ data: [{ id_perro: "p1" }], error: null });
    fromMock.mockReset();
    fromMock.mockImplementation((tabla) =>
      tabla === "perro_tiene_caracter" ? relacion.consulta : perros.consulta,
    );
  };

  test("solo entran los perros que tienen TODAS las características pedidas", async () => {
    prepararRelacion([
      { id_perro: "p1", id_caracter: "c1" },
      { id_perro: "p1", id_caracter: "c2" },
      { id_perro: "p2", id_caracter: "c1" }, // solo una de las dos
      { id_perro: "p3", id_caracter: "c2" }, // solo una de las dos
    ]);

    await dogModel.getDogs({ caracteres: ["c1", "c2"] });

    expect(buscar(relacion.llamadas, "in")[0].slice(1)).toEqual(["id_caracter", ["c1", "c2"]]);
    expect(buscar(perros.llamadas, "in")[0].slice(1)).toEqual(["id_perro", ["p1"]]);
  });

  test("una sola característica -> perros que la tienen", async () => {
    prepararRelacion([
      { id_perro: "p1", id_caracter: "c1" },
      { id_perro: "p2", id_caracter: "c1" },
    ]);

    await dogModel.getDogs({ caracteres: ["c1"] });

    expect(buscar(perros.llamadas, "in")[0].slice(1)).toEqual(["id_perro", ["p1", "p2"]]);
  });

  test("ningún perro tiene todas -> [] sin consultar perros", async () => {
    prepararRelacion([
      { id_perro: "p1", id_caracter: "c1" },
      { id_perro: "p2", id_caracter: "c2" },
    ]);

    const data = await dogModel.getDogs({ caracteres: ["c1", "c2"] });

    expect(data).toEqual([]);
    expect(fromMock).not.toHaveBeenCalledWith("perro");
  });

  test("una característica que nadie tiene -> []", async () => {
    prepararRelacion([{ id_perro: "p1", id_caracter: "c1" }]);

    const data = await dogModel.getDogs({ caracteres: ["c1", "inexistente"] });

    expect(data).toEqual([]);
  });
});
