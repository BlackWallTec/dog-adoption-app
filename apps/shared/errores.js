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
