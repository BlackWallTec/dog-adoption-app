// Convención de errores de la API
//
//   { "error": "mensaje legible", "code": "CODIGO_ESTABLE",
//     "fields": { campo: "mensaje" } }
//
//   - error:  texto para mostrar
//   - code:   lo que el cliente usa para decidir qué hacer
//   - fields: opcional
//
//   422 VALIDATION_ERROR: body inválido
//   409 EMAIL_EXISTS: ya hay una cuenta con ese correo
//   400 BAD_REQUEST: solicitud inválida
//   401 INVALID_CREDENTIALS: login fallido
//   401 UNAUTHENTICATED: token nulo, inválido o vencido
//   403 FORBIDDEN: autenticado pero sin permiso
//   429 RATE_LIMITED: demasiados intentos
//   500 INTERNAL_ERROR: todo lo demás

export const ERROR_CODES = Object.freeze({
  VALIDATION: "VALIDATION_ERROR",
  EMAIL_EXISTS: "EMAIL_EXISTS",
  CONFLICT: "CONFLICT",
  BAD_REQUEST: "BAD_REQUEST",
  INVALID_CREDENTIALS: "INVALID_CREDENTIALS",
  UNAUTHENTICATED: "UNAUTHENTICATED",
  FORBIDDEN: "FORBIDDEN",
  RATE_LIMITED: "RATE_LIMITED",
  INTERNAL: "INTERNAL_ERROR",
});

export function errorBody(code, error, fields) {
  return fields ? { error, code, fields } : { error, code };
}

export class VALIDATION extends Error {
  constructor(message = "Error de validación", fields) {
    super(message);

    this.name = "VALIDATION_ERROR";
    this.code = ERROR_CODES.VALIDATION;

    if (fields) {
      this.fields = fields;
    }
  }

  mostrarFields() {
    if (this.fields) {
      return Object.entries(this.fields)
        .map(([campo, valor]) => `${campo}: ${valor}`)
        .join("\n\n");
    }
  }

  getFields() {
    if (this.fields) return this.fields;
  }
}

export class EMAIL_EXISTS extends Error {
  constructor(message = "Ya existe una cuenta con ese correo") {
    super(message);
    this.name = "EMAIL_EXISTS";
    this.code = ERROR_CODES.EMAIL_EXISTS;
  }
}

export class CONFLICT extends Error {
  constructor(message = "Ya existe una cuenta con ese correo") {
    super(message);
    this.name = "EMAIL_EXISTS";
    this.code = ERROR_CODES.EMAIL_EXISTS;
  }
}

export class BAD_REQUEST extends Error {
  constructor(message = "Solicitud inválida") {
    super(message);
    this.name = "BAD_REQUEST";
    this.code = ERROR_CODES.BAD_REQUEST;
  }
}

export class INVALID_CREDENTIALS extends Error {
  constructor(message = "Credenciales inválidas") {
    super(message);
    this.name = "INVALID_CREDENTIALS";
    this.code = ERROR_CODES.INVALID_CREDENTIALS;
  }
}

export class UNAUTHENTICATED extends Error {
  constructor(message = "No autenticado") {
    super(message);
    this.name = "UNAUTHENTICATED";
    this.code = ERROR_CODES.UNAUTHENTICATED;
  }
}

export class FORBIDDEN extends Error {
  constructor(message = "No tienes permiso para realizar esta acción") {
    super(message);
    this.name = "FORBIDDEN";
    this.code = ERROR_CODES.FORBIDDEN;
  }
}

export class RATE_LIMITED extends Error {
  constructor(message = "Demasiados intentos; inténtalo más tarde") {
    super(message);
    this.name = "RATE_LIMITED";
    this.code = ERROR_CODES.RATE_LIMITED;
  }
}

export class INTERNAL extends Error {
  constructor(message = "Error interno del servidor") {
    super(message);
    this.name = "INTERNAL";
    this.code = ERROR_CODES.INTERNAL;
  }
}

// Para obtener un código HTTP de errores de supabase (PostgreSQL)
export const getHTTPErrorByCode = (code) => {
  switch (code) {
    case "23502":
      return 422;
    case "23514":
      return 422;
    case "22P02":
      return 422;
    case "42804":
      return 422;
    case "4260":
      return 422;
    case "23505":
      return 409;
    case "P0001":
      return 400;
    case "42703":
      return 400;
    case "42P01":
      return 400;
    case "42704":
      return 400;
    case "42883":
      return 400;
    case "42830":
      return 400;
    case "42P02":
      return 400;
    case "invalid_credentials":
      return 401;
    default:
      return 500;
  }
};

export function errorFromHTTPStatus(status, message, fields, code) {
  switch (status) {
    case 422:
      return new VALIDATION(message, fields);

    case 409:
      return code === ERROR_CODES.CONFLICT ? new CONFLICT(message) : new EMAIL_EXISTS(message);

    case 400:
      return new BAD_REQUEST(message);

    case 401:
      return code === ERROR_CODES.INVALID_CREDENTIALS
        ? new INVALID_CREDENTIALS(message)
        : new UNAUTHENTICATED(message);

    case 403:
      return new FORBIDDEN(message);

    case 429:
      return new RATE_LIMITED(message);

    case 500:
    default:
      return new INTERNAL(message);
  }
}

export function instanciarError(message, errorCode) {
  const status = getHTTPErrorByCode(errorCode);

  throw errorFromHTTPStatus(status, message);
}
