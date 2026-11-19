const API_URL = "/api/donativos/admin/pendientes";

export async function obtenerDonativosPendientes() {
  const respuesta = await fetch(API_URL, {
    credentials: "same-origin",
  });

  if (respuesta.status === 401) {
    throw new Error("NO_AUTENTICADO");
  }

  if (respuesta.status === 403) {
    throw new Error("SIN_PERMISOS");
  }

  if (!respuesta.ok) {
    throw new Error(`Error al consultar donativos: ${respuesta.status}`);
  }

  const donativos = await respuesta.json();
  if (!Array.isArray(donativos)) {
    throw new Error("La respuesta de donativos tiene un formato inválido.");
  }

  return donativos.map((donativo) => ({
    id: donativo.id_donativo,
    donante:
      [donativo.usuario?.nombre, donativo.usuario?.apellidos].filter(Boolean).join(" ") ||
      "Sin nombre",
    monto: donativo.monto,
    fecha: donativo.fecha,
    estado: donativo.estado,
  }));
}

export async function aprobarDonativoPendiente(idDonativo) {
  const respuesta = await fetch(`${API_URL}/${encodeURIComponent(idDonativo)}/aprobar`, {
    method: "PATCH",
    credentials: "same-origin",
    headers: { Accept: "application/json" },
  });

  if (respuesta.status === 401) throw new Error("NO_AUTENTICADO");
  if (respuesta.status === 403) throw new Error("SIN_PERMISOS");

  const contentType = respuesta.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) {
    throw new Error("La respuesta de aprobación no tiene un formato válido.");
  }

  const resultado = await respuesta.json();
  if (!respuesta.ok) {
    throw new Error(resultado.error || `Error al aprobar donativo: ${respuesta.status}`);
  }

  return resultado;
}
