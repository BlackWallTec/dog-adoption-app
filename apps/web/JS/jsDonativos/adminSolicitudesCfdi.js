const API_URL = "/api/donativos/admin/solicitudes-cfdi";

async function leerJson(respuesta) {
  const contentType = respuesta.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) {
    throw new Error("La respuesta de solicitudes CFDI no tiene un formato válido.");
  }
  return respuesta.json();
}

function validarUrlConstancia(url) {
  if (url === null) return;

  let urlValida;
  try {
    urlValida = new URL(url);
  } catch {
    throw new Error("La URL de la constancia fiscal no es válida.");
  }

  if (urlValida.protocol !== "https:") {
    throw new Error("La URL de la constancia fiscal no es segura.");
  }
}

export async function obtenerSolicitudesCfdiPendientes() {
  const respuesta = await fetch(API_URL, {
    credentials: "same-origin",
    cache: "no-store",
    headers: { Accept: "application/json" },
  });

  if (respuesta.status === 401) throw new Error("NO_AUTENTICADO");
  if (respuesta.status === 403) throw new Error("SIN_PERMISOS");
  if (!respuesta.ok) {
    throw new Error(`Error al consultar solicitudes CFDI: ${respuesta.status}`);
  }

  const solicitudes = await leerJson(respuesta);
  if (!Array.isArray(solicitudes)) {
    throw new Error("La respuesta de solicitudes CFDI debe ser una lista.");
  }

  return solicitudes;
}

export async function obtenerDetalleSolicitudCfdi(idSolicitud) {
  const respuesta = await fetch(`${API_URL}/${encodeURIComponent(idSolicitud)}`, {
    credentials: "same-origin",
    cache: "no-store",
    headers: { Accept: "application/json" },
  });

  if (respuesta.status === 401) throw new Error("NO_AUTENTICADO");
  if (respuesta.status === 403) throw new Error("SIN_PERMISOS");
  if (!respuesta.ok) {
    throw new Error(`Error al consultar el detalle CFDI: ${respuesta.status}`);
  }

  const detalle = await leerJson(respuesta);
  if (!detalle || typeof detalle !== "object" || Array.isArray(detalle)) {
    throw new Error("La respuesta del detalle CFDI tiene un formato inválido.");
  }

  validarUrlConstancia(detalle.datos_fiscales?.constancia_url ?? null);
  return detalle;
}
