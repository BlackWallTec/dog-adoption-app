// ======== Api Service de modals Admin Dog View ========
// Este archivo contiene funciones para interactuar con la API de modals en la vista de administración de perros.

const API_BASE_URL = "/api/catalogo/admin";

export async function fetchRegisterData() {
  console.log("fetchRegisterData: Llamando a la API para obtener datos de registro de perro...");
  try {
    // 1. Ejecutamos todas las peticiones en paralelo
    const [resTamanos, resCaracteristicas, resCuidados, resRazas] = await Promise.all([
      fetch(`${API_BASE_URL}/tamanos`),
      fetch(`${API_BASE_URL}/caracteristicas`),
      fetch(`${API_BASE_URL}/cuidado_especial`),
      fetch(`${API_BASE_URL}/razas`),
    ]);

    // 2. Convertimos todas las respuestas a JSON en paralelo
    const [tamanos, caracteristicas, cuidados, razas] = await Promise.all([
      resTamanos.json(),
      resCaracteristicas.json(),
      resCuidados.json(),
      resRazas.json(),
    ]);

    console.log("fetchRegisterData: Datos obtenidos de la API:", {
      tamanos,
      caracteristicas,
      cuidados,
      razas,
    });

    // 3. AHORA SÍ: Retornamos el objeto final para que el controlador lo use
    return { tamanos, caracteristicas, cuidados, razas };
  } catch (error) {
    console.error("fetchRegisterData: Error al obtener datos de la API:", error);
    throw error;
  }
}
