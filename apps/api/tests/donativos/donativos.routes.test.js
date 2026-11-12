import { afterAll, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import express from "express";
import cookieParser from "cookie-parser";
import jwt from "jsonwebtoken";
import request from "supertest";

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: {},
  supabaseAdmin: {},
}));

vi.mock("../../src/modules/donativos/http/modelosDonativos/donativo.model.js", () => ({
  default: {
    getPendientes: vi.fn().mockResolvedValue([]),
    countPendientes: vi.fn().mockResolvedValue(7),
    registrarMonetario: vi.fn(),
  },
}));

vi.mock("../../src/modules/donativos/http/modelosDonativos/datosFiscales.model.js", () => ({
  default: {},
}));

import { router as donativosRouter } from "../../src/modules/donativos/index.js";
import DonativoModel from "../../src/modules/donativos/http/modelosDonativos/donativo.model.js";

const JWT_SECRET_TEST = "test-secret-for-route-authorization";
const JWT_SECRET_ORIGINAL = process.env.JWT_SECRET;

function crearApp() {
  const app = express();
  app.use(cookieParser());
  app.use("/api/donativos", donativosRouter);
  return app;
}

function crearToken(rol) {
  return jwt.sign({ uuid: "test-user", rol }, JWT_SECRET_TEST, {
    algorithm: "HS256",
    expiresIn: "1m",
  });
}

beforeEach(() => {
  process.env.JWT_SECRET = JWT_SECRET_TEST;
});

afterAll(() => {
  if (JWT_SECRET_ORIGINAL === undefined) {
    delete process.env.JWT_SECRET;
  } else {
    process.env.JWT_SECRET = JWT_SECRET_ORIGINAL;
  }
});

describe("HU44 - Protección de rutas administrativas", () => {
  it("rechaza el listado administrativo si no hay sesión", async () => {
    const response = await request(crearApp()).get("/api/donativos/admin/pendientes");

    expect(response.status).toBe(401);
  });

  it("rechaza a usuarios sin rol administrativo", async () => {
    const response = await request(crearApp())
      .get("/api/donativos/admin/pendientes")
      .set("Cookie", `token=${crearToken("Cliente")}`);

    expect(response.status).toBe(403);
  });

  it("permite el listado a administradores autenticados", async () => {
    const response = await request(crearApp())
      .get("/api/donativos/admin/pendientes")
      .set("Cookie", `token=${crearToken("Administrador")}`);

    expect(response.status).toBe(200);
    expect(response.body).toEqual([]);
  });

  it("publica solo el conteo agregado, sin registros individuales", async () => {
    const response = await request(crearApp()).get("/api/donativos/pendientes/resumen");

    expect(response.status).toBe(200);
    expect(response.body).toEqual({ totalPendientes: 7 });
    expect(DonativoModel.countPendientes).toHaveBeenCalledOnce();
  });
});
