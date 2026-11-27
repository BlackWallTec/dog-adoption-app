import { describe, test, expect, beforeEach, afterEach, vi } from "vite-plus/test";

const mocks = vi.hoisted(() => ({
  getDogs: vi.fn(),
  getDogMultimediaById: vi.fn(),
  getBreeds: vi.fn(),
  getSizes: vi.fn(),
  getBehaviours: vi.fn(),
}));

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: { from: vi.fn() },
  supabaseAdmin: { from: vi.fn() },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/dog.model.js", () => ({
  default: { getDogs: mocks.getDogs },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/multimedia.model.js", () => ({
  default: { getDogMultimediaById: mocks.getDogMultimediaById },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/breed.model.js", () => ({
  default: { getBreeds: mocks.getBreeds },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/size.model.js", () => ({
  default: { getSizes: mocks.getSizes },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/behaviour.model.js", () => ({
  default: { getBehaviours: mocks.getBehaviours },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/special.model.js", () => ({
  default: {},
}));

import { getDogs, getFiltros } from "../../src/modules/catalogo/http/catalogClient.controller.js";

function crearRes() {
  return { status: vi.fn().mockReturnThis(), json: vi.fn().mockReturnThis() };
}

describe("HU24: GET /cliente/filtros", () => {
  let res;

  beforeEach(() => {
    vi.clearAllMocks();
    res = crearRes();
  });

  test("devuelve razas, tamaños y características como { id, nombre } sin relleno", async () => {
    mocks.getBreeds.mockResolvedValue([{ id_raza: "r1", nombre: "Mestizo   ", descripcion: "x" }]);
    mocks.getSizes.mockResolvedValue([{ id_tamano: "t1", nombre: "Chico     " }]);
    mocks.getBehaviours.mockResolvedValue([{ id_caracter: "c1", nombre: "Cariñoso" }]);

    await getFiltros({}, res);

    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: {
        razas: [{ id: "r1", nombre: "Mestizo" }],
        tamanos: [{ id: "t1", nombre: "Chico" }],
        caracteres: [{ id: "c1", nombre: "Cariñoso" }],
      },
    });
  });

  test("tablas vacías -> listas vacías", async () => {
    mocks.getBreeds.mockResolvedValue([]);
    mocks.getSizes.mockResolvedValue([]);
    mocks.getBehaviours.mockResolvedValue([]);

    await getFiltros({}, res);

    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: { razas: [], tamanos: [], caracteres: [] },
    });
  });

  test("falla una consulta -> 500", async () => {
    mocks.getBreeds.mockRejectedValue(new Error("sin conexión"));
    mocks.getSizes.mockResolvedValue([]);
    mocks.getBehaviours.mockResolvedValue([]);

    await getFiltros({}, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ success: false, message: "sin conexión" });
  });
});

describe("HU24: GET /cliente/perros con filtros", () => {
  let res;

  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(console, "log").mockImplementation(() => {});
    res = crearRes();
    mocks.getDogMultimediaById.mockResolvedValue([{ url: "https://foto/1.jpg" }]);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  test("sin filtros: comportamiento y paginación por defecto", async () => {
    mocks.getDogs.mockResolvedValue([
      { id_perro: "p1", multimedia: [{ url: "https://foto/1.jpg" }] },
    ]);

    await getDogs({ query: {} }, res);

    expect(mocks.getDogs).toHaveBeenCalledWith({}, 0, 9);
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: [{ id_perro: "p1", multimedia: [{ url: "https://foto/1.jpg" }] }],
      meta: { per_page: 10, current_page: 1, has_more: false },
    });
  });

  test("perro sin fotos -> multimedia vacío", async () => {
    mocks.getDogs.mockResolvedValue([{ id_perro: "p1", multimedia: [] }]);

    await getDogs({ query: {} }, res);

    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: [{ id_perro: "p1", multimedia: [] }],
      meta: { per_page: 10, current_page: 1, has_more: false },
    });
  });

  test("filtros válidos se pasan al modelo ya normalizados", async () => {
    mocks.getDogs.mockResolvedValue([]);

    await getDogs({ query: { sexo: "hembra", edad: "senior", caracter: "c1,c2" } }, res);

    expect(mocks.getDogs).toHaveBeenCalledWith(
      {
        sexo: "Hembra",
        edad: { min: 5, max: null },
        caracteres: ["c1", "c2"],
      },
      0,
      9,
    );
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: [],
      meta: { per_page: 10, current_page: 1, has_more: false },
    });
  });

  test("ningún perro cumple -> 200 con lista vacía, no es error", async () => {
    mocks.getDogs.mockResolvedValue([]);

    await getDogs({ query: { raza: "no-existe" } }, res);

    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: [],
      meta: { per_page: 10, current_page: 1, has_more: false },
    });
  });

  test("parámetro inválido -> 400 y no consulta la BD", async () => {
    await getDogs({ query: { edad: "viejito" } }, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      message: "Parámetro inválido: edad",
    });
    expect(mocks.getDogs).not.toHaveBeenCalled();
  });

  test("varios parámetros inválidos se listan todos", async () => {
    await getDogs({ query: { sexo: "Otro", edad: "x" } }, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      message: "Parámetro inválido: sexo, edad",
    });
  });

  test("id con formato inválido (22P02) -> 400", async () => {
    mocks.getDogs.mockRejectedValue(Object.assign(new Error("uuid"), { code: "22P02" }));

    await getDogs({ query: { tamano: "no-es-uuid" } }, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      message: "Parámetro inválido: tamano, raza o caracter",
    });
  });

  test("error interno -> 500 como antes", async () => {
    mocks.getDogs.mockRejectedValue(new Error("falló la BD"));

    await getDogs({ query: {} }, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ success: false, message: "falló la BD" });
  });
});
