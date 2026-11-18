// Llamadas a la API del calendario de programas (HU30)

// Error con el status HTTP, para que la vista decida qué mensaje mostrar
export class ErrorApi extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

// GET /api/voluntarios/admin/programas/calendario?desde=AAAA-MM-DD&hasta=AAAA-MM-DD
// Devuelve { programas, semanasSinCobertura } o lanza un ErrorApi
export async function obtenerCalendario(desde, hasta, { signal } = {}) {
  const parametros = new URLSearchParams({ desde, hasta });

  let respuesta;
  try {
    respuesta = await fetch(`/api/voluntarios/admin/programas/calendario?${parametros}`, {
      credentials: "include",
      signal,
    });
  } catch (error) {
    // Si la vista canceló la petición, se deja pasar tal cual para ignorarla
    if (error?.name === "AbortError") throw error;
    // Sin red o la API está apagada
    throw new ErrorApi(0, "SIN_CONEXION", "No se pudo conectar con el servidor.");
  }

  const contentType = respuesta.headers.get("content-type") || "";
  const cuerpo = contentType.includes("application/json") ? await respuesta.json() : null;

  if (!respuesta.ok || !cuerpo?.success) {
    // Los middlewares de sesión responden { error }, el controller { code, message }
    throw new ErrorApi(
      respuesta.status,
      cuerpo?.code ?? "ERROR_INTERNO",
      cuerpo?.message ?? cuerpo?.error ?? "Error al consultar el calendario.",
    );
  }

  return cuerpo.data;
}
