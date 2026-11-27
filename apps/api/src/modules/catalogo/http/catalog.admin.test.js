import express from "express";
import jwt from "jsonwebtoken";
import { afterAll, beforeAll, beforeEach, describe, expect, test, vi } from "vite-plus/test";

process.env.JWT_SECRET = "secreto-de-prueba";

// Se simula la capa de datos: estas pruebas no necesitan Supabase.
const dogModel = vi.hoisted(() => ({
  getDogs: vi.fn(),
  getDogAdminById: vi.fn(),
  updateDog: vi.fn(),
  setDogCaracteres: vi.fn(),
  setDogCuidados: vi.fn(),
}));
// El controlador importa el cliente de Supabase directamente: se simula para no pedir variables .env.
vi.mock("../../../config/supabaseClient.js", () => ({ supabase: {}, supabaseAdmin: {} }));
vi.mock("./modelosCatalogo/dog.model.js", () => ({ default: dogModel }));
vi.mock("./modelosCatalogo/breed.model.js", () => ({
  default: { getBreeds: vi.fn().mockResolvedValue([{ id_raza: "r1", nombre: "Mestizo" }]) },
}));
vi.mock("./modelosCatalogo/size.model.js", () => ({
  default: { getSizes: vi.fn().mockResolvedValue([{ id_tamano: "t1", nombre: "Mediano" }]) },
}));
vi.mock("./modelosCatalogo/behaviour.model.js", () => ({ default: {} }));
vi.mock("./modelosCatalogo/special.model.js", () => ({ default: {} }));
vi.mock("./modelosCatalogo/multimedia.model.js", () => ({ default: {} }));

const { default: router } = await import("./routes.js");

let server;
let base;

const cookie = (rol) =>
  `token=${jwt.sign({ uuid: "u1", email: "a@b.c", rol }, process.env.JWT_SECRET, { algorithm: "HS256" })}`;

const pedir = (metodo, ruta, { rol, body } = {}) =>
  fetch(base + ruta, {
    method: metodo,
    headers: {
      "content-type": "application/json",
      ...(rol ? { cookie: cookie(rol) } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

beforeAll(async () => {
  const app = express();
  app.use(express.json());
  // Mismo parseo de cookies que usa app.js
  const { default: cookieParser } = await import("cookie-parser");
  app.use(cookieParser());
  app.use("/api/catalogo", router);
  await new Promise((ok) => {
    server = app.listen(0, ok);
  });
  base = `http://127.0.0.1:${server.address().port}/api/catalogo`;
});

afterAll(() => server.close());
beforeEach(() => vi.clearAllMocks());

describe("PATCH /admin/perros/:id", () => {
  test("sin sesión → 401 y no toca la BD", async () => {
    const r = await pedir("PATCH", "/admin/perros/1", { body: { nombre: "Luna" } });
    expect(r.status).toBe(401);
    expect(dogModel.updateDog).not.toHaveBeenCalled();
  });

  test("rol Cliente → 403", async () => {
    const r = await pedir("PATCH", "/admin/perros/1", { rol: "Cliente", body: { nombre: "Luna" } });
    expect(r.status).toBe(403);
    expect(dogModel.updateDog).not.toHaveBeenCalled();
  });

  test("admin con datos válidos → 200 y solo manda los campos recibidos", async () => {
    dogModel.updateDog.mockResolvedValue({ id_perro: "1", nombre: "Luna" });
    dogModel.getDogAdminById.mockResolvedValue({ id_perro: "1", nombre: "Luna" });
    const r = await pedir("PATCH", "/admin/perros/1", {
      rol: "Administrador",
      body: { nombre: " Luna ", vacunado: true },
    });
    expect(r.status).toBe(200);
    expect(dogModel.updateDog).toHaveBeenCalledWith("1", { nombre: "Luna", vacunado: true });
  });

  test("estado y características en un solo PATCH → 200 y se aplican ambos", async () => {
    const caracter = "11111111-1111-4111-8111-111111111111";
    dogModel.updateDog.mockResolvedValue({ id_perro: "1", estado: "Adoptado" });
    dogModel.getDogAdminById.mockResolvedValue({ id_perro: "1", estado: "Adoptado" });
    const r = await pedir("PATCH", "/admin/perros/1", {
      rol: "Administrador",
      body: { estado: "Adoptado", caracteres: [caracter] },
    });
    expect(r.status).toBe(200);
    expect(dogModel.updateDog).toHaveBeenCalledWith("1", { estado: "Adoptado" });
    expect(dogModel.setDogCaracteres).toHaveBeenCalledWith("1", [caracter]);
    expect(dogModel.setDogCuidados).not.toHaveBeenCalled();
  });

  test("datos inválidos → 400 con errores por campo", async () => {
    const r = await pedir("PATCH", "/admin/perros/1", {
      rol: "Administrador",
      body: { nombre: "", edad_aprox: -2 },
    });
    expect(r.status).toBe(400);
    const json = await r.json();
    expect(Object.keys(json.errors)).toEqual(["nombre", "edad_aprox"]);
    expect(dogModel.updateDog).not.toHaveBeenCalled();
  });

  test("estado desconocido → 400", async () => {
    const r = await pedir("PATCH", "/admin/perros/1", {
      rol: "Administrador",
      body: { estado: "Perdido" },
    });
    expect(r.status).toBe(400);
    expect((await r.json()).errors).toHaveProperty("estado");
    expect(dogModel.updateDog).not.toHaveBeenCalled();
  });

  test("intentar cambiar activo (HU27) o el id → 400", async () => {
    const r = await pedir("PATCH", "/admin/perros/1", {
      rol: "Administrador",
      body: { activo: false, id_perro: "otro" },
    });
    expect(r.status).toBe(400);
    expect(dogModel.updateDog).not.toHaveBeenCalled();
  });

  test("perro inexistente → 404", async () => {
    dogModel.updateDog.mockResolvedValue(null);
    const r = await pedir("PATCH", "/admin/perros/999", { rol: "Root", body: { nombre: "X" } });
    expect(r.status).toBe(404);
  });

  test("raza/tamaño inexistente (FK) → 400", async () => {
    dogModel.updateDog.mockRejectedValue(Object.assign(new Error("fk"), { code: "23503" }));
    const r = await pedir("PATCH", "/admin/perros/1", {
      rol: "Administrador",
      body: { id_raza: "no-existe" },
    });
    expect(r.status).toBe(400);
  });

  test("error de BD → 500", async () => {
    dogModel.updateDog.mockRejectedValue(new Error("boom"));
    const r = await pedir("PATCH", "/admin/perros/1", {
      rol: "Administrador",
      body: { nombre: "Luna" },
    });
    expect(r.status).toBe(500);
  });
});

describe("GET admin (ficha y opciones)", () => {
  test("GET /admin/perros/:id exige sesión", async () => {
    expect((await pedir("GET", "/admin/perros/1")).status).toBe(401);
  });

  test("GET /admin/perros/:id devuelve la ficha o 404", async () => {
    dogModel.getDogAdminById.mockResolvedValueOnce({ id_perro: "1", nombre: "Luna" });
    const ok = await pedir("GET", "/admin/perros/1", { rol: "Administrador" });
    expect(ok.status).toBe(200);
    expect((await ok.json()).data.nombre).toBe("Luna");

    dogModel.getDogAdminById.mockResolvedValueOnce(null);
    const no = await pedir("GET", "/admin/perros/2", { rol: "Administrador" });
    expect(no.status).toBe(404);
  });

  test("razas y tamaños para los selects", async () => {
    const razas = await (await pedir("GET", "/admin/razas", { rol: "Administrador" })).json();
    const tams = await (await pedir("GET", "/admin/tamanos", { rol: "Administrador" })).json();
    expect(razas.data[0].nombre).toBe("Mestizo");
    expect(tams.data[0].nombre).toBe("Mediano");
  });
});
