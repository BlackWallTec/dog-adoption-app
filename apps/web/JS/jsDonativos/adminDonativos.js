import { obtenerTokenDonativos } from "./adminDonativos.auth.js";

const API_URL = "http://localhost:3000/api/donativos/admin/pendientes";

export async function obtenerDonativosPendientes() {
  const token = await obtenerTokenDonativos();

  if (!token) {
    throw new Error("Debes iniciar sesión para consultar los donativos.");
  }

  const respuesta = await fetch(API_URL, {
    credentials: "include",
    headers: {
      Authorization: `Bearer ${token}`,
    },
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
