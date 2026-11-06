import { describe, it, expect, vi, beforeEach } from "vite-plus/test";

// Modelo falso para no conectarse a Supabase
const mocks = vi.hoisted(() => ({
  registrarMonetario: vi.fn(),
}));

vi.mock("./modelosDonativos/donativo.model.js", () => ({
  default: mocks,
}));

import { postDonativo } from "./donativoCliente.controller.js";

const ID_USUARIO = "977abfc8-3a2c-45a2-8020-ff544e21ec7e";
const comprobante = Buffer.from("comprobante");

const crearRes = () => {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn(() => res);
  return res;
};

const llamar = async ({
  idUsuario = ID_USUARIO,
  monto = "500",
  referencia,
  tipo = "application/pdf",
  body = comprobante,
} = {}) => {
  const res = crearRes();
  await postDonativo(
    { params: { idUsuario }, query: { monto, referencia }, get: () => tipo, body },
    res,
  );
  return res;
};

beforeEach(() => {
  vi.clearAllMocks();
  vi.spyOn(console, "error").mockImplementation(() => {});
});

describe("postDonativo (HU46)", () => {
  it("CA-01: registra el donativo y regresa su estado", async () => {
    mocks.registrarMonetario.mockResolvedValue({
      id_donativo: "don-1",
      estado: "Pendiente de validación",
    });

    const res = await llamar({ monto: " 500.50 ", referencia: " ABC123 " });

    expect(res.status).toHaveBeenCalledWith(201);
    expect(mocks.registrarMonetario).toHaveBeenCalledWith({
      idUsuario: ID_USUARIO,
      monto: "500.50",
      referencia: "ABC123",
      comprobante,
    });
    expect(res.json.mock.calls[0][0].data).toEqual({
      id_donativo: "don-1",
      estado: "Pendiente de validación",
    });
  });

  it("la referencia es opcional", async () => {
    mocks.registrarMonetario.mockResolvedValue({ id_donativo: "don-1", estado: "x" });

    const res = await llamar();

    expect(res.status).toHaveBeenCalledWith(201);
    expect(mocks.registrarMonetario.mock.calls[0][0].referencia).toBeNull();
  });

  it("acepta el tipo de archivo aunque traiga más datos en el header", async () => {
    mocks.registrarMonetario.mockResolvedValue({ id_donativo: "don-1", estado: "x" });

    const res = await llamar({ tipo: "image/jpeg; charset=utf-8" });

    expect(res.status).toHaveBeenCalledWith(201);
  });

  it("rechaza un usuario que no es UUID", async () => {
    const res = await llamar({ idUsuario: "usuario1" });

    expect(res.status).toHaveBeenCalledWith(400);
    expect(mocks.registrarMonetario).not.toHaveBeenCalled();
  });

  it.each(["", "abc", "0", "-5", "10.123", "12345678901"])(
    "rechaza el monto inválido '%s'",
    async (monto) => {
      const res = await llamar({ monto });

      expect(res.status).toHaveBeenCalledWith(400);
      expect(mocks.registrarMonetario).not.toHaveBeenCalled();
    },
  );

  it("rechaza una referencia de más de 200 caracteres", async () => {
    const res = await llamar({ referencia: "a".repeat(201) });

    expect(res.status).toHaveBeenCalledWith(400);
    expect(mocks.registrarMonetario).not.toHaveBeenCalled();
  });

  it("CA-02: sin comprobante no registra nada", async () => {
    const res = await llamar({ body: {} });

    expect(res.status).toHaveBeenCalledWith(400);
    expect(mocks.registrarMonetario).not.toHaveBeenCalled();
  });

  it("CA-02: con comprobante vacío no registra nada", async () => {
    const res = await llamar({ body: Buffer.alloc(0) });

    expect(res.status).toHaveBeenCalledWith(400);
    expect(mocks.registrarMonetario).not.toHaveBeenCalled();
  });

  it("CA-02: con un tipo de archivo no permitido no registra nada", async () => {
    const res = await llamar({ tipo: "text/plain" });

    expect(res.status).toHaveBeenCalledWith(400);
    expect(mocks.registrarMonetario).not.toHaveBeenCalled();
  });

  it("regresa 404 si el usuario no existe", async () => {
    mocks.registrarMonetario.mockResolvedValue(null);

    const res = await llamar();

    expect(res.status).toHaveBeenCalledWith(404);
  });

  it("regresa 500 si falla la base de datos", async () => {
    mocks.registrarMonetario.mockRejectedValue(new Error("sin conexión"));

    const res = await llamar();

    expect(res.status).toHaveBeenCalledWith(500);
  });
});
