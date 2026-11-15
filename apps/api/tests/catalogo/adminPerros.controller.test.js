import { beforeEach, describe, expect, test, vi } from "vite-plus/test";

const mocks = vi.hoisted(() => ({
  getDogs: vi.fn(),
}));

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: { from: vi.fn() },
  supabaseAdmin: { from: vi.fn(), storage: { from: vi.fn() } },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/dog.model.js", () => ({
  default: { getDogs: mocks.getDogs },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/multimedia.model.js", () => ({
  default: {},
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/behaviour.model.js", () => ({
  default: {},
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/special.model.js", () => ({
  default: {},
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/size.model.js", () => ({
  default: {},
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/breed.model.js", () => ({
  default: {},
}));

import { getPerrosAdmin } from "../../src/modules/catalogo/http/catalogAdmin.controller.js";

function crearRes() {
  return { status: vi.fn().mockReturnThis(), json: vi.fn().mockReturnThis() };
}

describe("HU19B: GET /api/catalogo/admin/perros", () => {
  let res;

  beforeEach(() => {
    vi.clearAllMocks();
    res = crearRes();
  });

  test("devuelve el listado de perros activos sin modificar la consulta", async () => {
    const perros = [{ id_perro: "1", nombre: "Luna", activo: true }];
    mocks.getDogs.mockResolvedValue(perros);

    await getPerrosAdmin({}, res);

    expect(mocks.getDogs).toHaveBeenCalledWith();
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({ success: true, data: perros });
  });

  test("un listado vacio sigue siendo una consulta exitosa", async () => {
    mocks.getDogs.mockResolvedValue([]);

    await getPerrosAdmin({}, res);

    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({ success: true, data: [] });
  });

  test("si la consulta falla responde 500 y no inventa perros", async () => {
    mocks.getDogs.mockRejectedValue(new Error("sin conexión"));

    await getPerrosAdmin({}, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ success: false, message: "sin conexión" });
  });
});
