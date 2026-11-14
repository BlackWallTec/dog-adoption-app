// Llamadas a la API de administracion de programas HU38
export class ErrorApi extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

async function pedir(url, opciones = {}) {
  let respuesta;
  try {
    respuesta = await fetch(url, {
      credentials: "include",
      ...opciones,
      headers: { "Content-Type": "application/json", ...opciones.headers },
    });
  } catch {
    throw new ErrorApi(0, "RED", "No hay conexión con el servidor.");
  }

  const esJson = (respuesta.headers.get("content-type") ?? "").includes("application/json");
  const cuerpo = esJson ? await respuesta.json() : null;

  if (!respuesta.ok) {
    // Los middlewares de sesión responden { error }, el resto { code, message }
    const error = new ErrorApi(
      respuesta.status,
      cuerpo?.code ?? null,
      cuerpo?.message ?? cuerpo?.error ?? "Ocurrió un error. Intenta de nuevo.",
    );
    // En un 409 OVERLAP la API manda la lista de jornadas que se empalman
    error.empalmes = cuerpo?.empalmes;
    throw error;
  }

  return cuerpo;
}

const rutaPrograma = (idPrograma) =>
  `/api/voluntarios/admin/programas/${encodeURIComponent(idPrograma)}`;

export async function obtenerProgramaAdmin(idPrograma) {
  const cuerpo = await pedir(rutaPrograma(idPrograma));
  return cuerpo.data;
}

export async function editarProgramaAdmin(idPrograma, datos) {
  const cuerpo = await pedir(rutaPrograma(idPrograma), {
    method: "PUT",
    body: JSON.stringify(datos),
  });
  return cuerpo.data;
}

export async function cancelarProgramaAdmin(idPrograma, motivo) {
  const cuerpo = await pedir(`${rutaPrograma(idPrograma)}/cancelacion`, {
    method: "POST",
    body: JSON.stringify({ motivo }),
  });
  return cuerpo.data;
}
