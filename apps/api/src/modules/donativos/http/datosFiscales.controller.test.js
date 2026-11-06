import { describe, it, expect, vi, beforeEach } from "vite-plus/test";

// Modelo falso para no conectarse a Supabase
const mocks = vi.hoisted(() => ({
  getByUsuario: vi.fn(),
  update: vi.fn(),
  guardarConstancia: vi.fn(),
}));

vi.mock("./modelosDonativos/datosFiscales.model.js", () => ({
  default: mocks,
}));

import { getDatosFiscales, putConstancia, putDatosFiscales } from "./datosFiscales.controller.js";

const ID_USUARIO = "977abfc8-3a2c-45a2-8020-ff544e21ec7e";

const bodyBien = {
  rfc: "GOMS990101AB1",
  razon_social: "Sofía Gómez",
  regimen_fiscal: "626",
  codigo_postal: "76000",
  correo_facturacion: "sofia@gmail.com",
};

const crearRes = () => {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn(() => res);
  return res;
};

const crearReqArchivo = (tipo, body) => ({
  params: { idUsuario: ID_USUARIO },
  get: () => tipo,
  body,
});

beforeEach(() => {
  vi.clearAllMocks();
  vi.spyOn(console, "error").mockImplementation(() => {});
});

describe("getDatosFiscales (HU45)", () => {
  it("regresa los datos del usuario", async () => {
    mocks.getByUsuario.mockResolvedValue(bodyBien);
    const res = crearRes();

    await getDatosFiscales({ params: { idUsuario: ID_USUARIO } }, res);

    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({ success: true, data: bodyBien });
  });

  it("regresa 404 si el usuario no existe", async () => {
    mocks.getByUsuario.mockResolvedValue(null);
    const res = crearRes();

    await getDatosFiscales({ params: { idUsuario: ID_USUARIO } }, res);

    expect(res.status).toHaveBeenCalledWith(404);
  });

  it("regresa 500 si falla la base de datos", async () => {
    mocks.getByUsuario.mockRejectedValue(new Error("sin conexión"));
    const res = crearRes();

    await getDatosFiscales({ params: { idUsuario: ID_USUARIO } }, res);

    expect(res.status).toHaveBeenCalledWith(500);
  });
});

describe("putDatosFiscales (HU45)", () => {
  const llamar = async (body) => {
    const res = crearRes();
    await putDatosFiscales({ params: { idUsuario: ID_USUARIO }, body }, res);
    return res;
  };

  it("CA-01: guarda los datos limpios", async () => {
    mocks.update.mockResolvedValue(true);

    const res = await llamar({
      rfc: " goms990101ab1 ",
      razon_social: " Sofía Gómez ",
      regimen_fiscal: " 626 ",
      codigo_postal: " 76000 ",
      correo_facturacion: " sofia@gmail.com ",
    });

    expect(res.status).toHaveBeenCalledWith(200);
    expect(mocks.update).toHaveBeenCalledWith(ID_USUARIO, {
      rfc: "GOMS990101AB1",
      razonSocial: "Sofía Gómez",
      regimenFiscal: "626",
      codigoPostal: "76000",
      correoFacturacion: "sofia@gmail.com",
    });
  });

  it("CA-02: sin body marca todos los campos y no guarda", async () => {
    const res = await llamar(undefined);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(Object.keys(res.json.mock.calls[0][0].errores)).toEqual([
      "rfc",
      "razon_social",
      "regimen_fiscal",
      "codigo_postal",
      "correo_facturacion",
    ]);
    expect(mocks.update).not.toHaveBeenCalled();
  });

  it.each([
    ["rfc", "ABC123"],
    ["razon_social", "   "],
    ["regimen_fiscal", ""],
    ["codigo_postal", "7600"],
    ["correo_facturacion", "sofia.com"],
  ])("CA-02: %s mal escrito se marca solo ese campo", async (campo, valor) => {
    const res = await llamar({ ...bodyBien, [campo]: valor });

    expect(res.status).toHaveBeenCalledWith(400);
    expect(Object.keys(res.json.mock.calls[0][0].errores)).toEqual([campo]);
    expect(mocks.update).not.toHaveBeenCalled();
  });

  it("regresa 404 si no hay fila de ese usuario", async () => {
    mocks.update.mockResolvedValue(false);

    const res = await llamar(bodyBien);

    expect(res.status).toHaveBeenCalledWith(404);
  });

  it("regresa 500 si falla la base de datos", async () => {
    mocks.update.mockRejectedValue(new Error("sin conexión"));

    const res = await llamar(bodyBien);

    expect(res.status).toHaveBeenCalledWith(500);
  });
});

describe("putConstancia (HU45)", () => {
  const archivo = Buffer.from("constancia");

  it("CA-01: guarda el archivo y regresa la ruta", async () => {
    mocks.getByUsuario.mockResolvedValue({ id_usuario: ID_USUARIO });
    mocks.guardarConstancia.mockResolvedValue(`${ID_USUARIO}/constancia.pdf`);
    const res = crearRes();

    await putConstancia(crearReqArchivo("application/pdf", archivo), res);

    expect(res.status).toHaveBeenCalledWith(200);
    expect(mocks.guardarConstancia).toHaveBeenCalledWith(ID_USUARIO, "application/pdf", archivo);
    expect(res.json.mock.calls[0][0].data.constancia_url).toBe(`${ID_USUARIO}/constancia.pdf`);
  });

  it("rechaza un tipo de archivo que no es PDF, JPG o PNG", async () => {
    const res = crearRes();

    await putConstancia(crearReqArchivo("text/plain", archivo), res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(mocks.guardarConstancia).not.toHaveBeenCalled();
  });

  it("rechaza un archivo vacío", async () => {
    const res = crearRes();

    await putConstancia(crearReqArchivo("image/png", Buffer.alloc(0)), res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(mocks.guardarConstancia).not.toHaveBeenCalled();
  });

  it("regresa 404 si el usuario no existe", async () => {
    mocks.getByUsuario.mockResolvedValue(null);
    const res = crearRes();

    await putConstancia(crearReqArchivo("image/png", archivo), res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(mocks.guardarConstancia).not.toHaveBeenCalled();
  });

  it("regresa 500 si falla la subida", async () => {
    mocks.getByUsuario.mockResolvedValue({ id_usuario: ID_USUARIO });
    mocks.guardarConstancia.mockRejectedValue(new Error("sin conexión"));
    const res = crearRes();

    await putConstancia(crearReqArchivo("image/png", archivo), res);

    expect(res.status).toHaveBeenCalledWith(500);
  });
});
