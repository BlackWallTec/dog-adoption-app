import { describe, it, expect, vi, beforeEach } from "vite-plus/test";
import express from "express";
import request from "supertest";

vi.mock("../../src/middleware/is-auth.js", () => ({
  default: (req, _res, next) => {
    req.user = { uuid: "user-1" };
    next();
  },
}));

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: {},
}));

vi.mock("../../src/modules/solicitudes/http/modelosSolicitudes/adoptionRequest.model.js", () => ({
  default: {
    getStatusId: vi.fn(),
    getActiveRequest: vi.fn(),
    getByUser: vi.fn(),
    getDraftRequest: vi.fn(),
    getSubmittedRequest: vi.fn(),
    createDraft: vi.fn(),
    updateDraft: vi.fn(),
    getRequestWithDocuments: vi.fn(),
    submitDraft: vi.fn(),
  },
}));

import adoptionRequestModel from "../../src/modules/solicitudes/http/modelosSolicitudes/adoptionRequest.model.js";
import { router as solicitudesRouter } from "../../src/modules/solicitudes/index.js";

const ID_PERRO = "11111111-1111-1111-1111-111111111111";
const ID_SOLICITUD = "22222222-2222-2222-2222-222222222222";

const VALID_PAYLOAD = {
  id_perro: ID_PERRO,
  ocupacion: "Ingeniero",
  tuvo_mascotas: true,
  otras_mascotas: false,
  motivo_adopcion: "Quiero darle un hogar",
  donde_vivira: "Dentro de casa",
  personas_en_casa: 2,
  todos_de_acuerdo: true,
  tipo_vivienda: "Casa",
  jardin_patio: "Patio bardeado",
  casero_acepta: null,
  sobre_mascotas: "Tuve un perro 10 años",
  cuidados_basicos: "Alimento, agua y paseos",
  opinion_esterilizacion: "Totalmente a favor",
  tiene_veterinario: true,
  detalle_veterinario: "Clínica San Pedro",
  tiempo_solo: "4 horas",
  rutina_diaria: "Paseo mañana y noche",
  manejo_comportamiento: "Refuerzo positivo",
};

function crearApp() {
  const app = express();
  app.use(express.json());
  app.use("/api/solicitudes", solicitudesRouter);
  return app;
}

beforeEach(() => {
  vi.resetAllMocks();
});

describe("POST /api/solicitudes y POST /api/solicitudes/:id/enviar", () => {
  it("crea un borrador nuevo (201) ignorando idUsuario en el body", async () => {
    adoptionRequestModel.getSubmittedRequest.mockResolvedValue(null);
    adoptionRequestModel.getDraftRequest.mockResolvedValue(null);
    adoptionRequestModel.getStatusId.mockResolvedValue("estado-borrador");
    adoptionRequestModel.createDraft.mockResolvedValue({ id_solicitud: ID_SOLICITUD });

    const res = await request(crearApp())
      .post("/api/solicitudes")
      .send({ ...VALID_PAYLOAD, idUsuario: "hacker-id", extraField: "ignorar" });

    expect(res.status).toBe(201);
    expect(res.body).toEqual({
      success: true,
      data: { id_solicitud: ID_SOLICITUD, estado: "Borrador" },
    });
    expect(adoptionRequestModel.createDraft).toHaveBeenCalledWith(
      expect.objectContaining({
        id_usuario: "user-1",
        id_perro: ID_PERRO,
        id_estado: "estado-borrador",
        puede_editar: true,
      }),
    );
  });

  it("reutiliza y actualiza un borrador existente (200)", async () => {
    adoptionRequestModel.getSubmittedRequest.mockResolvedValue(null);
    adoptionRequestModel.getDraftRequest.mockResolvedValue({ id_solicitud: ID_SOLICITUD });
    adoptionRequestModel.updateDraft.mockResolvedValue({ id_solicitud: ID_SOLICITUD });

    const res = await request(crearApp()).post("/api/solicitudes").send(VALID_PAYLOAD);

    expect(res.status).toBe(200);
    expect(res.body.data.id_solicitud).toBe(ID_SOLICITUD);
    expect(adoptionRequestModel.updateDraft).toHaveBeenCalledWith(
      ID_SOLICITUD,
      "user-1",
      expect.any(Object),
    );
  });

  it("responde 409 si ya existe una solicitud enviada/activa para ese perro", async () => {
    adoptionRequestModel.getSubmittedRequest.mockResolvedValue({ id_solicitud: "otra-sol" });

    const res = await request(crearApp()).post("/api/solicitudes").send(VALID_PAYLOAD);

    expect(res.status).toBe(409);
  });

  it("responde 400 VALIDATION_ERROR si faltan campos o exceden longitud", async () => {
    const res = await request(crearApp())
      .post("/api/solicitudes")
      .send({ ...VALID_PAYLOAD, ocupacion: "a".repeat(101) });

    expect(res.status).toBe(400);
    expect(res.body.code).toBe("VALIDATION_ERROR");
    expect(res.body.fields.ocupacion).toBeDefined();
  });

  it("rechaza finalizar (400) si faltan documentos INE o DOMICILIO", async () => {
    adoptionRequestModel.getRequestWithDocuments.mockResolvedValue({
      id_solicitud: ID_SOLICITUD,
      id_usuario: "user-1",
      id_perro: ID_PERRO,
      puede_editar: true,
      estado: { descripcion: "Borrador" },
      ocupacion: "Ingeniero",
      ha_tenido_mascotas: true,
      otras_mascotas: false,
      motivo: "Hogar",
      donde_vivira: "Casa",
      integrantes_hogar: 2,
      todos_de_acuerdo: true,
      tipo_vivienda: "Casa",
      jardin_o_patio: "Patio",
      casero_acepta: null,
      sobre_mascotas: "Tuve perro",
      cuidados_basicos: "Alimento",
      opinion_esterilizacion: "A favor",
      tiene_veterinario: true,
      detalle_veterinario: "Vet",
      tiempo_solo: "4h",
      rutina_diaria: "Paseos",
      manejo_comportamiento: "Positivo",
      documentos: [{ documento: { id_documento: "d1", tipo: "INE" } }],
    });

    const res = await request(crearApp()).post(`/api/solicitudes/${ID_SOLICITUD}/enviar`);

    expect(res.status).toBe(400);
    expect(res.body.fields.documentos).toBeDefined();
  });

  it("finaliza el borrador (200) cuando respuestas y ambos documentos están completos", async () => {
    adoptionRequestModel.getRequestWithDocuments.mockResolvedValue({
      id_solicitud: ID_SOLICITUD,
      id_usuario: "user-1",
      id_perro: ID_PERRO,
      puede_editar: true,
      estado: { descripcion: "Borrador" },
      ocupacion: "Ingeniero",
      ha_tenido_mascotas: true,
      otras_mascotas: false,
      motivo: "Hogar",
      donde_vivira: "Casa",
      integrantes_hogar: 2,
      todos_de_acuerdo: true,
      tipo_vivienda: "Casa",
      jardin_o_patio: "Patio",
      casero_acepta: null,
      sobre_mascotas: "Tuve perro",
      cuidados_basicos: "Alimento",
      opinion_esterilizacion: "A favor",
      tiene_veterinario: true,
      detalle_veterinario: "Vet",
      tiempo_solo: "4h",
      rutina_diaria: "Paseos",
      manejo_comportamiento: "Positivo",
      documentos: [
        { documento: { id_documento: "d1", tipo: "INE" } },
        { documento: { id_documento: "d2", tipo: "DOMICILIO" } },
      ],
    });
    adoptionRequestModel.getStatusId.mockResolvedValue("estado-pendiente");
    adoptionRequestModel.submitDraft.mockResolvedValue({ id_solicitud: ID_SOLICITUD });

    const res = await request(crearApp()).post(`/api/solicitudes/${ID_SOLICITUD}/enviar`);

    expect(res.status).toBe(200);
    expect(res.body.data.estado).toBe("Pendiente de Revisión");
    expect(adoptionRequestModel.submitDraft).toHaveBeenCalledWith(
      ID_SOLICITUD,
      "user-1",
      "estado-pendiente",
    );
  });
});
