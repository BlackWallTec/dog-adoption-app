import { beforeEach, describe, expect, test, vi } from "vite-plus/test";

const mocks = vi.hoisted(() => ({
  updateDog: vi.fn(),
  getDogAdminById: vi.fn(),
  setDogCaracteres: vi.fn(),
  setDogCuidados: vi.fn(),
  removeDogMultimedia: vi.fn(),
  addDogMultimediaBulk: vi.fn(),
}));

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: { from: vi.fn() },
  supabaseAdmin: { from: vi.fn(), storage: { from: vi.fn() } },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/dog.model.js", () => ({
  default: {
    updateDog: mocks.updateDog,
    getDogAdminById: mocks.getDogAdminById,
    setDogCaracteres: mocks.setDogCaracteres,
    setDogCuidados: mocks.setDogCuidados,
  },
}));
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/multimedia.model.js", () => ({
  default: {
    removeDogMultimedia: mocks.removeDogMultimedia,
    addDogMultimediaBulk: mocks.addDogMultimediaBulk,
  },
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

import { updatePerroAdmin } from "../../src/modules/catalogo/http/catalogAdmin.controller.js";

const ID = "99999999-9999-4999-8999-999999999999";
const ID_A = "11111111-1111-4111-8111-111111111111";
const ID_B = "22222222-2222-4222-9222-222222222222";

function crearRes() {
  return { status: vi.fn().mockReturnThis(), json: vi.fn().mockReturnThis() };
}

const ficha = { id_perro: ID, nombre: "Luna", estado: "Disponible" };

describe("HU19C: PATCH /api/catalogo/admin/perros/:id", () => {
  let res;

  const patch = (body) => updatePerroAdmin({ params: { id: ID }, body }, res);

  beforeEach(() => {
    vi.clearAllMocks();
    res = crearRes();
    mocks.updateDog.mockResolvedValue(ficha);
    mocks.getDogAdminById.mockResolvedValue(ficha);
  });

  test("cuerpo inválido: 400 con errores y no toca la base de datos", async () => {
    await patch({ nombre: "", estado: "Perdido", caracteres: ["x"] });

    expect(res.status).toHaveBeenCalledWith(400);
    const { errors } = res.json.mock.calls[0][0];
    expect(Object.keys(errors).sort()).toEqual(["caracteres", "estado", "nombre"]);
    expect(mocks.updateDog).not.toHaveBeenCalled();
    expect(mocks.getDogAdminById).not.toHaveBeenCalled();
  });

  test("solo columnas: actualiza la ficha (con estado) y devuelve la ficha completa", async () => {
    await patch({ nombre: "Luna", estado: "Adoptado" });

    expect(mocks.updateDog).toHaveBeenCalledWith(ID, { nombre: "Luna", estado: "Adoptado" });
    expect(mocks.setDogCaracteres).not.toHaveBeenCalled();
    expect(mocks.removeDogMultimedia).not.toHaveBeenCalled();
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({ success: true, data: ficha });
  });

  test("características y cuidados se sincronizan con la lista completa", async () => {
    await patch({ caracteres: [ID_A], cuidados: [] });

    expect(mocks.updateDog).not.toHaveBeenCalled();
    expect(mocks.getDogAdminById).toHaveBeenCalledTimes(2); // existe + ficha final
    expect(mocks.setDogCaracteres).toHaveBeenCalledWith(ID, [ID_A]);
    expect(mocks.setDogCuidados).toHaveBeenCalledWith(ID, []);
    expect(res.status).toHaveBeenCalledWith(200);
  });

  test("quita y agrega fotos en la misma edición, quitando primero", async () => {
    const orden = [];
    mocks.removeDogMultimedia.mockImplementation(async () => orden.push("quitar"));
    mocks.addDogMultimediaBulk.mockImplementation(async () => orden.push("agregar"));
    const nuevas = [{ url: "https://x/a.jpg", tipo: "imagen" }];

    await patch({ multimedia_eliminar: [ID_B], multimedia_agregar: nuevas });

    expect(mocks.removeDogMultimedia).toHaveBeenCalledWith(ID, [ID_B]);
    expect(mocks.addDogMultimediaBulk).toHaveBeenCalledWith(ID, nuevas);
    expect(orden).toEqual(["quitar", "agregar"]);
    expect(res.status).toHaveBeenCalledWith(200);
  });

  test("edición completa: columnas, relaciones y fotos juntas", async () => {
    await patch({
      descripcion: "Nueva",
      caracteres: [ID_A],
      cuidados: [ID_B],
      multimedia_eliminar: [ID_A],
      multimedia_agregar: [{ url: "https://x/v.mp4", tipo: "video" }],
    });

    expect(mocks.updateDog).toHaveBeenCalledWith(ID, { descripcion: "Nueva" });
    expect(mocks.setDogCaracteres).toHaveBeenCalledTimes(1);
    expect(mocks.setDogCuidados).toHaveBeenCalledTimes(1);
    expect(mocks.removeDogMultimedia).toHaveBeenCalledTimes(1);
    expect(mocks.addDogMultimediaBulk).toHaveBeenCalledTimes(1);
    expect(res.status).toHaveBeenCalledWith(200);
  });

  test("el perro no existe (columnas): 404 y no toca relaciones", async () => {
    mocks.updateDog.mockResolvedValue(null);

    await patch({ nombre: "Luna", caracteres: [ID_A] });

    expect(res.status).toHaveBeenCalledWith(404);
    expect(mocks.setDogCaracteres).not.toHaveBeenCalled();
  });

  test("el perro no existe (solo relaciones): 404 y no escribe nada", async () => {
    mocks.getDogAdminById.mockResolvedValue(null);

    await patch({ multimedia_eliminar: [ID_A] });

    expect(res.status).toHaveBeenCalledWith(404);
    expect(mocks.removeDogMultimedia).not.toHaveBeenCalled();
  });

  test("id con formato inválido en Postgres (22P02): 404", async () => {
    mocks.updateDog.mockRejectedValue(Object.assign(new Error("x"), { code: "22P02" }));

    await patch({ nombre: "Luna" });

    expect(res.status).toHaveBeenCalledWith(404);
  });

  test("llave foránea inexistente en una relación: 400 señalando el campo", async () => {
    mocks.setDogCaracteres.mockRejectedValue(
      Object.assign(new Error("fk"), { code: "23503", campo: "caracteres" }),
    );

    await patch({ caracteres: [ID_A] });

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json.mock.calls[0][0].errors).toHaveProperty("caracteres");
    expect(mocks.setDogCuidados).not.toHaveBeenCalled();
  });

  test("llave foránea de raza o tamaño: conserva el aviso de antes", async () => {
    mocks.updateDog.mockRejectedValue(Object.assign(new Error("fk"), { code: "23503" }));

    await patch({ id_raza: "r" });

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json.mock.calls[0][0].errors).toEqual({
      id_raza: "Revisa la raza.",
      id_tamano: "Revisa el tamaño.",
    });
  });

  test("si falla un paso intermedio: 500 y no sigue con los demás", async () => {
    mocks.setDogCuidados.mockRejectedValue(new Error("sin conexión"));

    await patch({ cuidados: [ID_A], multimedia_eliminar: [ID_B] });

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ success: false, message: "sin conexión" });
    expect(mocks.removeDogMultimedia).not.toHaveBeenCalled();
  });
});
