// Llamadas a la API de la lista de inscritos (HU34)

//Error con el status HTTP, para que la vista decida el mensaje que va mostrar
export class ErrorApi extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

// GET /api/voluntarios/admin/programas/:idPrograma/inscritos
// Devuelve : programa, totalInscritos, inscritos o lanza un ErrorApi
export async function obtenerInscritos(idPrograma) {
  let respuesta;
  try {
    respuesta = await fetch(
      `/api/voluntarios/admin/programas/${encodeURIComponent(idPrograma)}/inscritos`,
    );
  } catch {
    // Sin red o la API esta apagada
    throw new ErrorApi(0, "SIN_CONEXION", "No se pudo conectar con el servidor");
  }

  const contentType = respuesta.headers.get("content-type") || "";
  const cuerpo = contentType.includes("application/json") ? await respuesta.json() : null;

  if (!respuesta.ok || !cuerpo?.success) {
    throw new ErrorApi(
      respuesta.status,
      cuepor?.code ?? "ERROR_INTERNO",
      cuerpo?.message ?? "Error al consultar los inscritos",
    );
  }

  return cuerpo.data;
}
