import { beforeEach, describe, expect, test, vi } from "vite-plus/test";

const { fromAdmin, storageFrom } = vi.hoisted(() => ({ fromAdmin: vi.fn(), storageFrom: vi.fn() }));

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: { from: vi.fn() },
  supabaseAdmin: { from: fromAdmin, storage: { from: storageFrom } },
}));

import dogModel from "../../src/modules/catalogo/http/modelosCatalogo/dog.model.js";
import multimediaModel, {
  rutaEnBucket,
} from "../../src/modules/catalogo/http/modelosCatalogo/multimedia.model.js";

// Consulta falsa de Supabase: anota cada llamada encadenable y, al hacer await, devuelve
// { data, error } como el cliente real.
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

const buscar = (llamadas, nombre) => llamadas.filter(([n]) => n === nombre);

// Cada from() consume el siguiente resultado de la lista (lectura, luego borrado, luego alta).
function programar(...resultados) {
  const consultas = resultados.map((r) => crearConsulta(r));
  let i = 0;
  fromAdmin.mockImplementation(() => consultas[i++].consulta);
  return consultas.map((c) => c.llamadas);
}

const ID_PERRO = "99999999-9999-4999-8999-999999999999";

describe("HU19C: DogModel.setDogCaracteres / setDogCuidados", () => {
  beforeEach(() => {
    fromAdmin.mockReset();
  });

  test("solo quita lo que sobra y solo agrega lo que falta", async () => {
    const [lectura, borrado, alta] = programar(
      { data: [{ id_caracter: "a" }, { id_caracter: "b" }], error: null },
      { error: null },
      { error: null },
    );

    await dogModel.setDogCaracteres(ID_PERRO, ["b", "c"]);

    expect(fromAdmin.mock.calls.map(([t]) => t)).toEqual([
      "perro_tiene_caracter",
      "perro_tiene_caracter",
      "perro_tiene_caracter",
    ]);
    expect(buscar(lectura, "eq")[0].slice(1)).toEqual(["id_perro", ID_PERRO]);
    expect(buscar(borrado, "delete")).toHaveLength(1);
    expect(buscar(borrado, "in")[0].slice(1)).toEqual(["id_caracter", ["a"]]);
    expect(buscar(alta, "insert")[0][1]).toEqual([{ id_perro: ID_PERRO, id_caracter: "c" }]);
  });

  test("si ya está igual no borra ni inserta nada", async () => {
    programar({ data: [{ id_cuidado: "x" }, { id_cuidado: "y" }], error: null });

    await dogModel.setDogCuidados(ID_PERRO, ["y", "x"]);

    expect(fromAdmin).toHaveBeenCalledTimes(1);
  });

  test("lista vacía quita todos los que tenía", async () => {
    const [, borrado] = programar(
      { data: [{ id_cuidado: "x" }, { id_cuidado: "y" }], error: null },
      { error: null },
    );

    await dogModel.setDogCuidados(ID_PERRO, []);

    expect(fromAdmin.mock.calls.map(([t]) => t)).toEqual([
      "perro_tiene_cuidado_especial",
      "perro_tiene_cuidado_especial",
    ]);
    expect(buscar(borrado, "in")[0].slice(1)).toEqual(["id_cuidado", ["x", "y"]]);
  });

  test("un error de la base de datos conserva code y marca el campo", async () => {
    programar(
      { data: [], error: null },
      { error: { message: "viola llave foránea", code: "23503" } },
    );

    await expect(dogModel.setDogCaracteres(ID_PERRO, ["zzz"])).rejects.toMatchObject({
      code: "23503",
      campo: "caracteres",
    });
  });
});

describe("HU19C: rutaEnBucket", () => {
  test("saca la ruta del archivo dentro del bucket", () => {
    expect(
      rutaEnBucket(
        "https://x.supabase.co/storage/v1/object/public/multimedia_perros/perros/abc/123_luna.jpg",
      ),
    ).toBe("perros/abc/123_luna.jpg");
  });

  test("decodifica la ruta y descarta parámetros", () => {
    expect(
      rutaEnBucket(
        "https://x/storage/v1/object/public/multimedia_perros/perros/a/1_Le%C3%B3n.jpg?t=1",
      ),
    ).toBe("perros/a/1_León.jpg");
  });

  test("devuelve null si la url no es del bucket", () => {
    expect(rutaEnBucket("https://otro.com/foto.jpg")).toBeNull();
    expect(rutaEnBucket(null)).toBeNull();
    expect(rutaEnBucket("https://x/multimedia_perros/")).toBeNull();
  });
});

describe("HU19C: MultimediaModel", () => {
  let remove;

  beforeEach(() => {
    fromAdmin.mockReset();
    storageFrom.mockReset();
    remove = vi.fn().mockResolvedValue({ error: null });
    storageFrom.mockReturnValue({ remove });
  });

  test("addDogMultimediaBulk inserta una fila por archivo con el id del perro", async () => {
    const [lectura, alta] = programar({ data: [], error: null }, { error: null });

    await multimediaModel.addDogMultimediaBulk(ID_PERRO, [
      { url: "https://x/a.jpg", tipo: "imagen" },
      { url: "https://x/b.mp4", tipo: "video" },
    ]);

    expect(buscar(lectura, "eq")[0].slice(1)).toEqual(["id_perro", ID_PERRO]);
    const filas = buscar(alta, "insert")[0][1];
    expect(filas).toHaveLength(2);
    expect(filas[0]).toMatchObject({ id_perro: ID_PERRO, url: "https://x/a.jpg", tipo: "imagen" });
    expect(filas[1]).toMatchObject({ id_perro: ID_PERRO, url: "https://x/b.mp4", tipo: "video" });
    expect(filas[0].fecha_subida).toEqual(expect.any(String));
  });

  test("addDogMultimediaBulk omite lo que el perro ya tiene (reintento sin duplicar)", async () => {
    const [, alta] = programar(
      { data: [{ url: "https://x/a.jpg" }], error: null },
      { error: null },
    );

    await multimediaModel.addDogMultimediaBulk(ID_PERRO, [
      { url: "https://x/a.jpg", tipo: "imagen" },
      { url: "https://x/b.mp4", tipo: "video" },
    ]);

    expect(buscar(alta, "insert")[0][1].map((f) => f.url)).toEqual(["https://x/b.mp4"]);
  });

  test("addDogMultimediaBulk no inserta nada si ya estaba todo", async () => {
    programar({ data: [{ url: "https://x/a.jpg" }], error: null });

    await multimediaModel.addDogMultimediaBulk(ID_PERRO, [
      { url: "https://x/a.jpg", tipo: "imagen" },
    ]);

    expect(fromAdmin).toHaveBeenCalledTimes(1);
  });

  test("addDogMultimediaBulk lanza el error con el campo si falla el alta", async () => {
    programar({ data: [], error: null }, { error: { message: "rls", code: "42501" } });

    await expect(
      multimediaModel.addDogMultimediaBulk(ID_PERRO, [{ url: "https://x/a.jpg", tipo: "imagen" }]),
    ).rejects.toMatchObject({ code: "42501", campo: "multimedia_agregar" });
  });

  test("removeDogMultimedia borra solo multimedia de ese perro y limpia el bucket", async () => {
    const url = "https://x/storage/v1/object/public/multimedia_perros/perros/p/1_a.jpg";
    const [borrado] = programar({ data: [{ id_multimedia: "m1", url }], error: null });

    const borradas = await multimediaModel.removeDogMultimedia(ID_PERRO, ["m1", "m-ajena"]);

    expect(buscar(borrado, "eq")[0].slice(1)).toEqual(["id_perro", ID_PERRO]);
    expect(buscar(borrado, "in")[0].slice(1)).toEqual(["id_multimedia", ["m1", "m-ajena"]]);
    expect(borradas).toHaveLength(1);
    expect(storageFrom).toHaveBeenCalledWith("multimedia_perros");
    expect(remove).toHaveBeenCalledWith(["perros/p/1_a.jpg"]);
  });

  test("si falla el borrado del bucket la edición no falla", async () => {
    const url = "https://x/storage/v1/object/public/multimedia_perros/perros/p/1_a.jpg";
    programar({ data: [{ id_multimedia: "m1", url }], error: null });
    remove.mockResolvedValue({ error: { message: "sin permiso" } });
    const consola = vi.spyOn(console, "error").mockImplementation(() => {});

    await expect(multimediaModel.removeDogMultimedia(ID_PERRO, ["m1"])).resolves.toHaveLength(1);

    expect(consola).toHaveBeenCalled();
    consola.mockRestore();
  });

  test("una url que no es del bucket se quita de la ficha sin tocar el bucket", async () => {
    programar({ data: [{ id_multimedia: "m1", url: "https://otro.com/a.jpg" }], error: null });

    await multimediaModel.removeDogMultimedia(ID_PERRO, ["m1"]);

    expect(remove).not.toHaveBeenCalled();
  });

  test("si falla el borrado en la tabla lanza el error con el campo", async () => {
    programar({ data: null, error: { message: "rls", code: "42501" } });

    await expect(multimediaModel.removeDogMultimedia(ID_PERRO, ["m1"])).rejects.toMatchObject({
      code: "42501",
      campo: "multimedia_eliminar",
    });
    expect(remove).not.toHaveBeenCalled();
  });
});
