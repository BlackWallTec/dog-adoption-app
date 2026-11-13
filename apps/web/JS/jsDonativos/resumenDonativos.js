const API_URL = "/api/donativos/pendientes/resumen";

export async function obtenerResumenPublicoDonativos() {
  const respuesta = await fetch(API_URL, {
    headers: { Accept: "application/json" },
  });

  if (!respuesta.ok) {
    throw new Error(`Error al consultar el resumen de donativos: ${respuesta.status}`);
  }

  const resumen = await respuesta.json();
  if (!Number.isSafeInteger(resumen?.totalPendientes) || resumen.totalPendientes < 0) {
    throw new Error("La respuesta del resumen de donativos tiene un formato inválido.");
  }

  return resumen.totalPendientes;
}
