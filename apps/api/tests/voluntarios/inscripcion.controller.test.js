/*
 * PRUEBAS DEL CONTROLADOR: POST /api/voluntarios/programas/:idPrograma/inscripciones
 *
 * Las reglas (programa existe, está disponible, no ha iniciado, no inscrito, hay cupo) las
 * valida la función SQL `inscribir_en_programa` en una sola transacción. El controlador solo:
 *   1. Revisa que haya usuario y que el id sea UUID (sin tocar la BD).
 *   2. Llama a `inscribirEnProgramaModel`.
 *   3. Traduce el `codigo` que regresa la BD a un status HTTP.
 *
 * Para correrlas, desde apps/api:
 *   npx vp test run tests/voluntarios
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";

vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js", () => ({
  inscribirEnProgramaModel: vi.fn(),
}));

import { inscribirse } from "../../src/modules/voluntarios/http/voluntarios.controller.js";
import { inscribirEnProgramaModel } from "../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js";

const ID_PROGRAMA = "11111111-2222-3333-4444-555555555555";
const ID_USUARIO = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

const RESULTADO_OK = {
  codigo: "OK",
  id_programa: ID_PROGRAMA,
  fecha_inscripcion: "2026-11-05T12:00:00",
  estado: "Aceptado",
};

const ejecutar = async (idPrograma = ID_PROGRAMA) => {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn();

  await inscribirse({ params: { idPrograma } }, res);

  return { status: res.status.mock.calls[0][0], cuerpo: res.json.mock.calls[0][0] };
};

const expectError = (respuesta, status, code) => {
  expect(respuesta.status).toBe(status);
  expect(respuesta.cuerpo.sucess).toBe(false);
  expect(respuesta.cuerpo.code).toBe(code);
};

const conCodigo = (codigo) => Object.assign(new Error("x"), { code: codigo });

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubEnv("DEV_USUARIO_ID", ID_USUARIO);
  inscribirEnProgramaModel.mockResolvedValue(RESULTADO_OK);
});

afterEach(() => {
  vi.unstubAllEnvs();
  vi.restoreAllMocks();
});

describe("validaciones previas (sin tocar la BD)", () => {
  it.each([[undefined], [""]])("DEV_USUARIO_ID=%j da 401 NO_AUTENTICADO", async (valor) => {
    vi.stubEnv("DEV_USUARIO_ID", valor);

    const respuesta = await ejecutar();

    expectError(respuesta, 401, "NO_AUTENTICADO");
    expect(respuesta.cuerpo.message).toBe("No hay un usuario configurado.");
    expect(inscribirEnProgramaModel).not.toHaveBeenCalled();
  });

  it("sin usuario gana sobre un id inválido", async () => {
    vi.stubEnv("DEV_USUARIO_ID", "");

    expectError(await ejecutar("id-invalido"), 401, "NO_AUTENTICADO");
  });

  it.each([
    "no-es-un-uuid",
    "123",
    "' OR 1=1 --",
    "",
    ID_PROGRAMA.slice(0, -1), // 35 caracteres
    `${ID_PROGRAMA}5`, // 37 caracteres
    `g${ID_PROGRAMA.slice(1)}`, // carácter no hexadecimal
    `${ID_PROGRAMA}\n`,
    ` ${ID_PROGRAMA}`,
  ])("el id %j da 404 sin llamar al modelo", async (id) => {
    const respuesta = await ejecutar(id);

    expectError(respuesta, 404, "PROGRAMA_NO_ENCONTRADO");
    expect(respuesta.cuerpo.message).toBe("No encontramos este programa.");
    expect(inscribirEnProgramaModel).not.toHaveBeenCalled();
  });

  it.each([[undefined], [null]])("idPrograma=%j da 404 sin llamar al modelo", async (id) => {
    const res = { status: vi.fn(() => res), json: vi.fn() };

    await inscribirse({ params: { idPrograma: id } }, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(inscribirEnProgramaModel).not.toHaveBeenCalled();
  });

  it.each([
    ID_PROGRAMA,
    "ABCDEFAB-CDEF-ABCD-EFAB-CDEFABCDEFAB",
    "00000000-0000-0000-0000-000000000000",
    "ffffffff-ffff-ffff-ffff-ffffffffffff",
  ])("el UUID %s es válido y llega al modelo", async (id) => {
    expect((await ejecutar(id)).status).toBe(201);
    expect(inscribirEnProgramaModel).toHaveBeenCalledWith(id, ID_USUARIO);
  });
});

describe("respuesta de inscribir_en_programa", () => {
  it("OK da 201 con solo id_programa, fecha_inscripcion y estado (sin codigo)", async () => {
    inscribirEnProgramaModel.mockResolvedValue({ ...RESULTADO_OK, extra: "no debe salir" });

    const respuesta = await ejecutar();

    expect(respuesta).toEqual({
      status: 201,
      cuerpo: {
        sucess: true,
        data: {
          id_programa: ID_PROGRAMA,
          fecha_inscripcion: "2026-11-05T12:00:00",
          estado: "Aceptado",
        },
      },
    });
    expect(respuesta.cuerpo.data).not.toHaveProperty("codigo");
    expect(inscribirEnProgramaModel).toHaveBeenCalledWith(ID_PROGRAMA, ID_USUARIO);
  });

  it.each([
    { codigo: "PROGRAMA_NO_ENCONTRADO", status: 404, message: "No encontramos este programa." },
    {
      codigo: "PROGRAMA_NO_DISPONIBLE",
      status: 409,
      message: "El programa no está abierto a inscripciones.",
    },
    { codigo: "PROGRAMA_INICIADO", status: 409, message: "El programa ya comenzó." },
    { codigo: "YA_INSCRITO", status: 409, message: "Ya estás inscrito en este programa." },
    { codigo: "SIN_CUPO", status: 409, message: "El programa ya no tiene lugares." },
  ])("$codigo da $status con su mensaje", async ({ codigo, status, message }) => {
    inscribirEnProgramaModel.mockResolvedValue({ codigo });

    const respuesta = await ejecutar();

    expect(respuesta).toEqual({ status, cuerpo: { sucess: false, code: codigo, message } });
  });

  it.each([
    { descripcion: "código desconocido", resultado: { codigo: "NUEVO_CODIGO" } },
    { descripcion: "código en minúsculas", resultado: { codigo: "ok" } },
    { descripcion: "sin código", resultado: {} },
    { descripcion: "resultado null", resultado: null },
    { descripcion: "resultado undefined", resultado: undefined },
  ])("$descripcion da 500 ERROR_INTERNO", async ({ resultado }) => {
    const consola = vi.spyOn(console, "error").mockImplementation(() => {});
    inscribirEnProgramaModel.mockResolvedValue(resultado);

    expectError(await ejecutar(), 500, "ERROR_INTERNO");
    expect(consola).toHaveBeenCalledOnce();
  });
});

describe("errores de la BD", () => {
  it("23503 (el usuario configurado no existe) da 401 NO_AUTENTICADO", async () => {
    inscribirEnProgramaModel.mockRejectedValue(conCodigo("23503"));

    const respuesta = await ejecutar();

    expectError(respuesta, 401, "NO_AUTENTICADO");
    expect(respuesta.cuerpo.message).toBe("El usuario configurado no existe.");
  });

  // 23505 ya no tiene trato especial: la función SQL regresa YA_INSCRITO en su lugar.
  it.each([
    { descripcion: "23505 (ya no es un caso especial)", codigo: "23505" },
    { descripcion: "otro código", codigo: "42P01" },
    { descripcion: "código parecido", codigo: "23504" },
    { descripcion: "sin código", codigo: undefined },
  ])("un error con $descripcion da 500 ERROR_INTERNO", async ({ codigo }) => {
    const consola = vi.spyOn(console, "error").mockImplementation(() => {});
    inscribirEnProgramaModel.mockRejectedValue(conCodigo(codigo));

    const respuesta = await ejecutar();

    expectError(respuesta, 500, "ERROR_INTERNO");
    expect(respuesta.cuerpo.message).toBe("Error interno al procesar la inscripción.");
    expect(consola).toHaveBeenCalledOnce();
  });
});
