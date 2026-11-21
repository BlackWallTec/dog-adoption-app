import { afterAll, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import express from "express";
import cookieParser from "cookie-parser";
import jwt from "jsonwebtoken";
import request from "supertest";

const { getDogsMock } = vi.hoisted(() => ({
  getDogsMock: vi.fn().mockResolvedValue([{ id_perro: "1", nombre: "Luna" }]),
}));

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: {},
  supabaseAdmin: {},
}));

vi.mock("../../src/modules/catalogo/http/modelosCatalogo/dog.model.js", () => ({
  default: { getDogsAdmin: getDogsMock },
}));

import { router as catalogoRouter } from "../../src/modules/catalogo/index.js";

const JWT_SECRET_TEST = "test-secret-for-route-authorization";
const JWT_SECRET_ORIGINAL = process.env.JWT_SECRET;

function crearApp() {
  const app = express();
  app.use(cookieParser());
  app.use("/api/catalogo", catalogoRouter);
  return app;
}

function crearToken(rol) {
  return jwt.sign({ uuid: "test-user", rol }, JWT_SECRET_TEST, {
    algorithm: "HS256",
    expiresIn: "1m",
  });
}

beforeEach(() => {
  vi.clearAllMocks();
  process.env.JWT_SECRET = JWT_SECRET_TEST;
});

afterAll(() => {
  if (JWT_SECRET_ORIGINAL === undefined) {
    delete process.env.JWT_SECRET;
  } else {
    process.env.JWT_SECRET = JWT_SECRET_ORIGINAL;
  }
});

describe("GET /api/catalogo/admin/perros pasa por la sesión existente", () => {
  it("no carga el listado si la web no manda la cookie", async () => {
    const response = await request(crearApp()).get("/api/catalogo/admin/perros");

    expect(response.status).toBe(401);
    expect(getDogsMock).not.toHaveBeenCalled();
  });

  it("no carga el listado si el header móvil no trae Bearer", async () => {
    const response = await request(crearApp())
      .get("/api/catalogo/admin/perros")
      .set("x-client", "mobile")
      .set("Cookie", `token=${crearToken("Administrador")}`);

    expect(response.status).toBe(401);
    expect(getDogsMock).not.toHaveBeenCalled();
  });

  it("rechaza a un cliente con sesión", async () => {
    const response = await request(crearApp())
      .get("/api/catalogo/admin/perros")
      .set("Cookie", `token=${crearToken("Cliente")}`);

    expect(response.status).toBe(403);
    expect(getDogsMock).not.toHaveBeenCalled();
  });

  it("carga el listado con la cookie de un administrador", async () => {
    const response = await request(crearApp())
      .get("/api/catalogo/admin/perros")
      .set("Cookie", `token=${crearToken("Administrador")}`);

    expect(response.status).toBe(200);
    expect(response.body.data).toEqual([{ id_perro: "1", nombre: "Luna" }]);
  });

  it("carga el listado móvil con x-client y Authorization", async () => {
    const response = await request(crearApp())
      .get("/api/catalogo/admin/perros")
      .set("x-client", "mobile")
      .set("Authorization", `Bearer ${crearToken("Root")}`);

    expect(response.status).toBe(200);
    expect(getDogsMock).toHaveBeenCalledOnce();
  });
});
