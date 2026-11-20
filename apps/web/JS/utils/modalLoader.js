// JS/utils/modalLoader.js

/**
 * Vite escanea en tiempo de compilación todos los archivos HTML dentro de /HTML/
 * y los empaqueta como módulos de texto para que funcionen en producción (Vercel).
 */
const htmlModules = import.meta.glob("/HTML/**/*.html", {
  query: "?raw",
  import: "default",
});

/**
 * Carga e instancia un HTML externo dentro de un contenedor en el DOM
 * @param {string} urlPath - Ruta del archivo HTML del modal (ej. "/HTML/htmlCatalogo/registerDogModal.html")
 * @param {string} containerSelector - Selector del contenedor destino
 * @returns {Promise<Element|null>} El primer elemento hijo instanciado en el DOM
 */
export async function loadModal(urlPath, containerSelector = "#modal-container") {
  const container = document.querySelector(containerSelector);

  if (!container) {
    throw new Error(`El contenedor ${containerSelector} no existe en el DOM.`);
  }

  // Asegura que la ruta comience con "/" para coincidir con el formato de llaves de import.meta.glob
  const normalizedPath = urlPath.startsWith("/") ? urlPath : `/${urlPath}`;
  const loadHtml = htmlModules[normalizedPath];

  if (!loadHtml) {
    throw new Error(
      `Error al cargar el modal: No se encontró "${normalizedPath}" en los archivos compilados por Vite.`,
    );
  }

  // 1. Obtener el contenido HTML empaquetado por Vite
  const htmlText = await loadHtml();

  // 2. Inyección en el DOM (Instanciación)
  container.innerHTML = htmlText;

  // En este preciso instante, todos los elementos del modal ya están vivos en el DOM
  return container.firstElementChild;
}
