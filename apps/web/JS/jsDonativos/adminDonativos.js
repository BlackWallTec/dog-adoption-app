const API_URL = "http://localhost:3000/api/donativos/admin/pendientes";

export async function obtenerDonativosPendientes() {
  const respuesta = await fetch(API_URL, {
    credentials: "include",
  });

  if (!respuesta.ok) {
    throw new Error(`No se pudieron cargar los donativos: ${respuesta.status}`);
  }

  const datos = await respuesta.json();

  return datos.map((donativo) => ({
    id: donativo.id_donativo,
    donante:
      [donativo.usuario?.nombre, donativo.usuario?.apellidos].filter(Boolean).join(" ") ||
      "Sin nombre",
    monto: donativo.monto,
    fecha: donativo.fecha,
    estado: donativo.estado,
  }));
}
