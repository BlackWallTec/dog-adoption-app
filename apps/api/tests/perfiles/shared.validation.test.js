import { describe, expect, it } from "vite-plus/test";
import {
  BAD_REQUEST,
  FORBIDDEN,
  RATE_LIMITED,
  SERVICE_UNAVAILABLE,
  VALIDATION,
  EMAIL_EXISTS,
  CONFLICT,
  INVALID_CREDENTIALS,
  UNAUTHENTICATED,
  INTERNAL,
  ERROR_CODES,
  errorResponse,
  errorFromHTTPStatus,
} from "@qarino/shared/errores.js";
import {
  validateEmail,
  validateRegistration,
} from "@qarino/shared/perfiles/registro.validation.js";

describe("Convención compartida de errores y validación", () => {
  it.each([
    [
      new BAD_REQUEST("Código inválido", ERROR_CODES.INVALID_EMAIL_CODE),
      400,
      ERROR_CODES.INVALID_EMAIL_CODE,
    ],
    [
      new FORBIDDEN("Confirma tu correo", ERROR_CODES.EMAIL_NOT_CONFIRMED),
      403,
      ERROR_CODES.EMAIL_NOT_CONFIRMED,
    ],
    [new RATE_LIMITED(), 429, ERROR_CODES.RATE_LIMITED],
    [
      new SERVICE_UNAVAILABLE("Reintenta", ERROR_CODES.PASSWORD_UPDATE_FAILED),
      503,
      ERROR_CODES.PASSWORD_UPDATE_FAILED,
    ],
    [new SERVICE_UNAVAILABLE(), 503, ERROR_CODES.SERVICE_UNAVAILABLE],
    [new EMAIL_EXISTS(), 409, ERROR_CODES.EMAIL_EXISTS],
    [new CONFLICT(), 409, ERROR_CODES.EMAIL_EXISTS],
    [new INVALID_CREDENTIALS(), 401, ERROR_CODES.INVALID_CREDENTIALS],
    [new UNAUTHENTICATED(), 401, ERROR_CODES.UNAUTHENTICATED],
    [new INTERNAL("detalle privado"), 500, ERROR_CODES.INTERNAL],
    [new Error("detalle privado"), 500, ERROR_CODES.INTERNAL],
    [null, 500, ERROR_CODES.INTERNAL],
  ])("serializa %s con HTTP %s", (error, status, code) => {
    const r = errorResponse(error);
    expect(r.status).toBe(status);
    expect(r.body.code).toBe(code);
    expect(r.body.error).toBe(status === 500 ? "Error interno del servidor" : error.message);
  });
  it("conserva los errores de campo", () => {
    expect(errorResponse(new VALIDATION("Revisa tus datos", { correo: "Inválido" }))).toEqual({
      status: 422,
      body: {
        error: "Revisa tus datos",
        code: ERROR_CODES.VALIDATION,
        fields: { correo: "Inválido" },
      },
    });
  });
  it.each([
    [400, ERROR_CODES.INVALID_EMAIL_CODE],
    [403, ERROR_CODES.EMAIL_NOT_CONFIRMED],
    [503, ERROR_CODES.EMAIL_UNAVAILABLE],
    [503, ERROR_CODES.PASSWORD_UPDATE_FAILED],
  ])("reconstruye HTTP %s sin perder su código %s", (status, code) => {
    expect(errorResponse(errorFromHTTPStatus(status, "Reintenta", undefined, code))).toEqual({
      status,
      body: { error: "Reintenta", code },
    });
  });
  it.each(["gmail.com", "outlook.com", "yahoo.com", "hotmail.com", "icloud.com"])(
    "admite %s y normaliza",
    (dominio) => {
      expect(validateEmail(` Prueba@${dominio.toUpperCase()} `)).toBe(`prueba@${dominio}`);
    },
  );
  it.each([
    undefined,
    null,
    "",
    "sin-correo",
    "x@otro.com",
    "x@GMAIL.COM.otro.com",
    `${"x".repeat(250)}@gmail.com`,
  ])("rechaza %s con VALIDATION", (correo) => {
    expect(() => validateEmail(correo)).toThrow(VALIDATION);
  });
  it("el registro incompleto devuelve campos en lugar de fallar al normalizar", () => {
    try {
      validateRegistration({});
      throw new Error("Debe rechazar el registro");
    } catch (error) {
      expect(error).toBeInstanceOf(VALIDATION);
      expect(error.getFields()).toHaveProperty("correo");
      expect(error.getFields()).toHaveProperty("telefono");
      expect(error.getFields()).toHaveProperty("id_codigo_postal");
    }
  });
});
