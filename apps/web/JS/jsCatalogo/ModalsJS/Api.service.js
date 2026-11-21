// ======== Api Service de modals Admin Dog View ========
// Este archivo contiene funciones para interactuar con la API de modals en la vista de administración de perros.

const API_BASE_URL = "/api/catalogo/admin";

async function leerRespuesta(response) {
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.message || data.error || `Error en el servidor: ${response.status}`);
  }
  return data;
}

export async function fetchRegisterData() {
  try {
    const [resTamanos, resCaracteristicas, resRazas] = await Promise.all([
      fetch(`${API_BASE_URL}/tamanos`),
      fetch(`${API_BASE_URL}/caracteristicas`),
      fetch(`${API_BASE_URL}/razas`),
    ]);

    const [tamanos, caracteristicas, razas] = await Promise.all([
      leerRespuesta(resTamanos),
      leerRespuesta(resCaracteristicas),
      leerRespuesta(resRazas),
    ]);

    return { tamanos, caracteristicas, razas };
  } catch (error) {
    console.error("fetchRegisterData: Error al obtener datos de la API:", error);
    throw error;
  }
}

export async function obtenerUrlFirmadaSupabase(idPerroTemporal, nombreArchivo, tipoMime) {
  console.log("Consiguiendo URL firmada de Back previo a subir imagen a Bucket");
  try {
    const response = await fetch(`${API_BASE_URL}/solicitar-subida`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        id_perro_temporal: idPerroTemporal,
        nombreArchivo,
        tipoMime,
      }),
    });
    const data = await leerRespuesta(response);

    if (!data.uploadUrl || !data.publicUrl) {
      throw new Error("El servidor no devolvió las URLs necesarias para subir el archivo.");
    }

    return { uploadUrl: data.uploadUrl, publicUrl: data.publicUrl };
  } catch (error) {
    console.log("Error al conseguir URL firmada");
    throw error;
  }
}

export async function subirArchivoASupabase(uploadUrl, file) {
  const response = await fetch(uploadUrl, {
    method: "PUT",
    headers: { "Content-Type": file.type || "application/octet-stream" },
    body: file,
  });

  if (!response.ok) {
    throw new Error(`No se pudo subir ${file.name}: ${response.status}`);
  }
}

export async function guardarPerroBD(datosFrontend) {
  console.groupCollapsed("[Modals.service] Datos recibidos para guardar el perro");
  console.log("Datos del frontend:", datosFrontend);
  console.groupEnd();

  const multimediaFormateada = (datosFrontend.imagenes_urls || []).map((url) => ({
    url,
    tipo: url.match(/\.(mp4|mov|webm)(?:\?|$)/i) ? "video" : "imagen",
  }));

  const payloadBackend = {
    nombre: datosFrontend.nombre,
    sexo: datosFrontend.sexo,
    edad_aprox: datosFrontend.edad_aprox ?? 0,
    tamano_medida: datosFrontend.tamano_medida ?? 0,
    descripcion: datosFrontend.descripcion ?? "",
    esterilizado: Boolean(datosFrontend.esterilizado),
    vacunado: Boolean(datosFrontend.vacunado),
    estado: datosFrontend.estado || "Disponible",
    id_raza: datosFrontend.id_raza,
    id_tamano: datosFrontend.id_tamano,
    multimedia: multimediaFormateada,
    caracteres: datosFrontend.caracteristicas_ids || [],
  };

  console.groupCollapsed("[Modals.service] Payload enviado a la API");
  console.log("Payload backend:", payloadBackend);
  console.table(
    Object.entries(payloadBackend).map(([campo, valor]) => ({
      campo,
      valor: Array.isArray(valor) ? JSON.stringify(valor) : valor,
    })),
  );
  console.groupEnd();

  try {
    const response = await fetch(`${API_BASE_URL}/perros`, {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payloadBackend),
    });

    return leerRespuesta(response);
  } catch (error) {
    console.log("Error al cargar perro");
    throw error;
  }
}

export async function fetchBulkActivationToggle(idList, action) {
  console.log("Api service de front llamando a backend");
  try {
    const payloadBackend = {
      idList: idList,
      action: action,
    };

    const response = await fetch(`${API_BASE_URL}/perros/bulk/activar`, {
      method: "PATCH",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payloadBackend),
    });

    const data = await response.json().catch(() => ({}));

    // Si el backend responde 401, 403, 400 o 500, forzamos el salto al catch
    if (!response.ok) {
      const error = new Error(data.error || data.message || "Error en la petición");
      error.status = response.status; // Guardamos el 401 o 403 para leerlo en el catch
      throw error;
    }
    return data;
  } catch (error) {
    console.log("Error en bulk update: ", error);
    throw error;
  }
}
