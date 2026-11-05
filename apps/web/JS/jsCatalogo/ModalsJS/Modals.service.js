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

export async function guardarPerroBD(datosFrontend) {
  // 1. Transformar el arreglo de URLs de multimedia al formato de objetos que espera el backend
  const multimediaFormateada = (datosFrontend.imagenes_urls || []).map((url) => ({
    url: url,
    tipo: url.match(/\.(mp4|mov|webm)$/i) ? "video" : "imagen", // Detección simple de tipo
  }));

  // 2. Construir el JSON exacto exigido por el Backend
  const payloadBackend = {
    nombre: datosFrontend.nombre,
    sexo: datosFrontend.sexo,
    edad_aprox: datosFrontend.edad_aprox,
    tamano_medida: datosFrontend.tamano_medida,
    descripcion: datosFrontend.descripcion,
    esterilizado: Boolean(datosFrontend.esterilizado),
    vacunado: Boolean(datosFrontend.vacunado),
    estado: datosFrontend.estado || "Disponible",
    id_raza: datosFrontend.id_raza,
    id_tamano: datosFrontend.id_tamano,

    // Arrays mapeados con los nombres exactos de tu backend
    multimedia: multimediaFormateada,
    caracteres: datosFrontend.caracteristicas_ids || [],
    cuidados: datosFrontend.cuidados_ids || [],
  };

  console.log("🚀 Enviando Payload final al Backend:", payloadBackend);

  // 3. Petición POST estándar
  const response = await fetch("/api/perros/alta", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payloadBackend),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `Error en el servidor: ${response.status}`);
  }

  return await response.json();
}
