const API_URL = "/api/donativos/pendientes/resumen";

export async function obtenerDonativosPendientes() {
  const respuesta = await fetch(API_URL, {
    method: "GET",
    credentials: "include",
    headers: {
      Accept: "application/json",
    },
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

  const datos = await respuesta.json();

  if (!Array.isArray(datos)) {
    throw new Error("La API no devolvió una lista de donativos.");
  }

  return datos.map((donativo) => ({
    id: donativo.id,
    donante: donativo.donante,
    monto: null,
    fecha: donativo.fecha,
    estado: donativo.estado,
  }));
}
