import { describe, test, expect, beforeEach, vi } from "vite-plus/test";

// 1. Mock de Supabase Client para evitar que pida variables .env en CI
vi.mock("../../src/config/supabaseClient.js", () => ({
  supabase: {
    from: vi.fn(),
    rpc: vi.fn(),
    storage: {
      from: vi.fn(() => ({
        createSignedUploadUrl: vi.fn(),
        getPublicUrl: vi.fn(),
      })),
    },
  },
}));

import { postNuevoPerro } from "../../src/modules/catalogo/http/catalogAdmin.controller.js";
import dogModel from "../../src/modules/catalogo/http/modelosCatalogo/dog.model.js";

// El mock DEBE usar exactamente la misma ruta relativa que usaste para importar el modelo arriba
vi.mock("../../src/modules/catalogo/http/modelosCatalogo/dog.model.js");

describe("Branch Coverage: POST postNuevoPerro (HU19)", () => {
  let req, res;
  // UUIDs v4 válidos que pasan tu RegEx (cumplen con el '4' en el 3er grupo y '8/9/a/b' en el 4to)
  const validUUID_Raza = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";
  const validUUID_Tamano = "b1ffcd00-0d1c-4fa9-ab7e-7cc0ce491b22";
  const validUUID_Caracter = "c2aaef11-1e2d-4ab0-bc8f-8dd1df502c33";

  beforeEach(() => {
    req = { body: {} };
    res = {
      status: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnThis(),
    };
    vi.clearAllMocks();
  });

  // -------------------------------------------------------------
  // RAMA 1: Payload válido con cuidado_especial (Éxito -> Status 200)
  // -------------------------------------------------------------
  test("RAMA 1: Payload válido y normalización de cuidado_especial -> Retorna status 200", async () => {
    req.body = {
      nombre: "Rocky",
      sexo: "Macho",
      edad_aprox: 2,
      tamano_medida: 40.5,
      descripcion: "Un perro muy activo",
      // Enviamos texto sin mayúsculas iniciales ni punto final para validar el sanitizador
      cuidado_especial: "se asusta con los truenos. darle su pelota",
      esterilizado: true,
      vacunado: true,
      estado: "Disponible",
      id_raza: validUUID_Raza,
      id_tamano: validUUID_Tamano,
      multimedia: [{ url: "https://storage.com/foto.jpg", tipo: "imagen" }],
      caracteres: [validUUID_Caracter],
    };

    const mockPerroCreado = { id_perro: "perro-uuid-123", ...req.body };
    dogModel.createFullDog.mockResolvedValue(mockPerroCreado);

    await postNuevoPerro(req, res);

    // Verificamos que el modelo recibió el texto ya normalizado (capitalizado y con punto final)
    expect(dogModel.createFullDog).toHaveBeenCalledWith(
      expect.objectContaining({
        nombre: "Rocky",
        cuidado_especial: "Se asusta con los truenos. Darle su pelota.",
      }),
    );
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      success: true,
      data: mockPerroCreado,
    });
  });

  // -------------------------------------------------------------
  // RAMA 2: Falta un campo obligatorio (Error 400 Bad Request)
  // -------------------------------------------------------------
  test('RAMA 2: Falta el campo obligatorio "nombre" -> Retorna status 400', async () => {
    req.body = {
      sexo: "Macho",
      edad_aprox: 2,
      tamano_medida: 40.5,
      esterilizado: true,
      vacunado: true,
      estado: "Disponible",
      id_raza: validUUID_Raza,
      id_tamano: validUUID_Tamano,
      multimedia: [],
      caracteres: [],
    };

    await postNuevoPerro(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        success: false,
        message: "Datos inválidos en la solicitud.",
      }),
    );
  });

  // -------------------------------------------------------------
  // RAMA 3: Clave foránea / UUID en formato inválido (Error 400)
  // -------------------------------------------------------------
  test("RAMA 3: Formato de UUID inválido en id_raza -> Retorna status 400", async () => {
    req.body = {
      nombre: "Max",
      sexo: "Macho",
      edad_aprox: 3,
      tamano_medida: 30.0,
      descripcion: "Perro pequeño",
      esterilizado: true,
      vacunado: true,
      estado: "Disponible",
      id_raza: "ESTO-NO-ES-UN-UUID-VALIDO",
      id_tamano: validUUID_Tamano,
      multimedia: [],
      caracteres: [],
    };

    await postNuevoPerro(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        success: false,
        errors: expect.arrayContaining([
          expect.stringContaining("no tiene un formato UUID válido"),
        ]),
      }),
    );
  });

  // -------------------------------------------------------------
  // RAMA 4: Fallo de la Base de Datos / Supabase (Error 500)
  // -------------------------------------------------------------
  test("RAMA 4: Fallo interno en BD (Stored Procedure falla) -> Retorna status 500", async () => {
    req.body = {
      nombre: "Toby",
      sexo: "Macho",
      edad_aprox: 4,
      tamano_medida: 50.0,
      descripcion: "Perro grande",
      esterilizado: true,
      vacunado: true,
      estado: "Disponible",
      id_raza: validUUID_Raza,
      id_tamano: validUUID_Tamano,
      multimedia: [],
      caracteres: [],
    };

    dogModel.createFullDog.mockRejectedValue(
      new Error("Database connection failed in RPC insert_full_dog"),
    );

    await postNuevoPerro(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      message: "Database connection failed in RPC insert_full_dog",
    });
  });

  // -------------------------------------------------------------
  // RAMA 5: Campo opcional cuidado_especial omitido (Éxito -> Status 200)
  // -------------------------------------------------------------
  test("RAMA 5: Payload válido sin enviar cuidado_especial (campo opcional) -> Retorna status 200", async () => {
    req.body = {
      nombre: "Luna",
      sexo: "Hembra",
      edad_aprox: 1,
      tamano_medida: 25.0,
      descripcion: "Cachorra saludable",
      esterilizado: false,
      vacunado: true,
      estado: "Disponible",
      id_raza: validUUID_Raza,
      id_tamano: validUUID_Tamano,
      multimedia: [],
      caracteres: [],
    };

    const mockPerroCreado = { id_perro: "perro-uuid-456", ...req.body };
    dogModel.createFullDog.mockResolvedValue(mockPerroCreado);

    await postNuevoPerro(req, res);

    expect(dogModel.createFullDog).toHaveBeenCalledWith(
      expect.objectContaining({
        nombre: "Luna",
        cuidado_especial: "",
      }),
    );
    expect(res.status).toHaveBeenCalledWith(200);
  });

  // -------------------------------------------------------------
  // RAMA 6: cuidado_especial excede el límite de 1000 caracteres (Error 400)
  // -------------------------------------------------------------
  test("RAMA 6: cuidado_especial supera los 500 caracteres -> Retorna status 400", async () => {
    req.body = {
      nombre: "Bruno",
      sexo: "Macho",
      edad_aprox: 5,
      tamano_medida: 45.0,
      cuidado_especial: "a".repeat(501),
      esterilizado: true,
      vacunado: true,
      estado: "Disponible",
      id_raza: validUUID_Raza,
      id_tamano: validUUID_Tamano,
      multimedia: [],
      caracteres: [],
    };

    await postNuevoPerro(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        success: false,
        errors: expect.arrayContaining([
          expect.stringContaining("no puede exceder los 500 caracteres"),
        ]),
      }),
    );
  });
});
