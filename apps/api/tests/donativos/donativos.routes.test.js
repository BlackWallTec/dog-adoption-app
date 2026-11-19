import { afterAll, beforeEach, describe, expect, it, vi } from "vite-plus/test";
import express from "express";
import cookieParser from "cookie-parser";
import jwt from "jsonwebtoken";
import request from "supertest";

const { getSolicitudesCfdiMock, getDetalleSolicitudCfdiMock } = vi.hoisted(() => ({
  getSolicitudesCfdiMock: vi.fn().mockResolvedValue([]),
  getDetalleSolicitudCfdiMock: vi.fn().mockResolvedValue(null),
}));

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: {},
  supabaseAdmin: {},
}));

vi.mock("../../src/modules/donativos/http/modelosDonativos/donativo.model.js", () => ({
  default: {
    getPendientes: vi.fn().mockResolvedValue([]),
    aprobarPendiente: vi.fn(),
    countPendientes: vi.fn().mockResolvedValue(7),
    registrarMonetario: vi.fn(),
  },
}));

vi.mock("../../src/modules/donativos/http/modelosDonativos/solicitudCfdi.model.js", () => ({
  default: {
    getPendientes: getSolicitudesCfdiMock,
    getDetallePendiente: getDetalleSolicitudCfdiMock,
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

  it("permite que administradores aprueben un donativo pendiente", async () => {
    const donativo = {
      id_donativo: "b2e87183-0b85-4840-a5e7-5eae19dce391",
      estado: "Validado",
    };
    DonativoModel.aprobarPendiente.mockResolvedValue(donativo);

    const response = await request(crearApp())
      .patch(`/api/donativos/admin/pendientes/${donativo.id_donativo}/aprobar`)
      .set("Cookie", `token=${crearToken("Administrador")}`);

    expect(response.status).toBe(200);
    expect(response.body).toEqual(donativo);
    expect(DonativoModel.aprobarPendiente).toHaveBeenCalledWith(donativo.id_donativo);
  });

  it("rechaza la aprobación a usuarios sin sesión o sin rol administrativo", async () => {
    const path = "/api/donativos/admin/pendientes/b2e87183-0b85-4840-a5e7-5eae19dce391/aprobar";
    const sinSesion = await request(crearApp()).patch(path);
    const cliente = await request(crearApp())
      .patch(path)
      .set("Cookie", `token=${crearToken("Cliente")}`);

    expect(sinSesion.status).toBe(401);
    expect(cliente.status).toBe(403);
    expect(DonativoModel.aprobarPendiente).not.toHaveBeenCalled();
  });

  it("publica solo el conteo agregado, sin registros individuales", async () => {
    const response = await request(crearApp()).get("/api/donativos/pendientes/resumen");

    expect(response.status).toBe(200);
    expect(response.body).toEqual({ totalPendientes: 7 });
    expect(DonativoModel.countPendientes).toHaveBeenCalledOnce();
  });
});

describe("HU53 - Solicitudes CFDI administrativas", () => {
  it("rechaza la bandeja sin sesión", async () => {
    const response = await request(crearApp()).get("/api/donativos/admin/solicitudes-cfdi");

    expect(response.status).toBe(401);
    expect(getSolicitudesCfdiMock).not.toHaveBeenCalled();
  });

  it("rechaza a usuarios que no son Administrador o Root", async () => {
    const response = await request(crearApp())
      .get("/api/donativos/admin/solicitudes-cfdi")
      .set("Cookie", `token=${crearToken("Cliente")}`);

    expect(response.status).toBe(403);
    expect(getSolicitudesCfdiMock).not.toHaveBeenCalled();
  });

  it("devuelve la bandeja al administrador sin incluir datos fiscales", async () => {
    const solicitudes = [
      {
        id_solicitud_cfdi: "b2e87183-0b85-4840-a5e7-5eae19dce391",
        id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
        fecha_solicitud: "2026-11-14T10:00:00Z",
        estado: "Pendiente de atención",
        donante: "Donante de prueba",
        requiere_revision: false,
      },
    ];
    getSolicitudesCfdiMock.mockResolvedValue(solicitudes);

    const response = await request(crearApp())
      .get("/api/donativos/admin/solicitudes-cfdi")
      .set("Cookie", `token=${crearToken("Administrador")}`);

    expect(response.status).toBe(200);
    expect(response.headers["cache-control"]).toContain("no-store");
    expect(response.body).toEqual(solicitudes);
    expect(response.body[0]).not.toHaveProperty("datos_fiscales");
  });

  it("muestra el detalle solo de la solicitud seleccionada y nunca cambia su estado", async () => {
    const idSolicitud = "b2e87183-0b85-4840-a5e7-5eae19dce391";
    const detalle = {
      id_solicitud_cfdi: idSolicitud,
      estado: "Pendiente de atención",
      listo_para_emitir: false,
      motivos_revision: ["El donativo ya no está validado."],
      datos_fiscales: { rfc: "XAXX010101000" },
    };
    getDetalleSolicitudCfdiMock.mockResolvedValue(detalle);

    const response = await request(crearApp())
      .get(`/api/donativos/admin/solicitudes-cfdi/${idSolicitud}`)
      .set("Cookie", `token=${crearToken("Root")}`);

    expect(response.status).toBe(200);
    expect(response.body).toEqual(detalle);
    expect(getDetalleSolicitudCfdiMock).toHaveBeenCalledWith(idSolicitud);
  });

  it("rechaza identificadores inválidos antes de consultar el modelo", async () => {
    const response = await request(crearApp())
      .get("/api/donativos/admin/solicitudes-cfdi/no-es-uuid")
      .set("Cookie", `token=${crearToken("Administrador")}`);

    expect(response.status).toBe(400);
    expect(getDetalleSolicitudCfdiMock).not.toHaveBeenCalled();
  });
});

describe("HU57 - Entrega protegida de CFDI", () => {
  it("rechaza carga y reintento sin una sesión administrativa", async () => {
    const app = crearApp();
    const validar = await request(app).post(
      "/api/donativos/admin/solicitudes-cfdi/b2e87183-0b85-4840-a5e7-5eae19dce391/validar-cfdi",
    );
    const reintentar = await request(app).post(
      "/api/donativos/admin/solicitudes-cfdi/b2e87183-0b85-4840-a5e7-5eae19dce391/reintentar-correo",
    );

    expect(validar.status).toBe(401);
    expect(reintentar.status).toBe(401);
  });

  it("rechaza a clientes en rutas administrativas", async () => {
    const response = await request(crearApp())
      .post(
        "/api/donativos/admin/solicitudes-cfdi/b2e87183-0b85-4840-a5e7-5eae19dce391/reintentar-correo",
      )
      .set("Cookie", `token=${crearToken("Cliente")}`);

    expect(response.status).toBe(403);
  });

  it("protege la consulta y descarga de CFDI para clientes autenticados", async () => {
    const app = crearApp();
    const sinSesion = await request(app).get("/api/donativos/cliente/cfdi");
    const admin = await request(app)
      .get("/api/donativos/cliente/cfdi")
      .set("Cookie", `token=${crearToken("Administrador")}`);

    expect(sinSesion.status).toBe(401);
    expect(admin.status).toBe(403);
  });
});
