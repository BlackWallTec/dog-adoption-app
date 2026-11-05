// JS/utils/modalLoader.js

/**
 * Script generado con Intetligencia Artificial (IA) para cargar modales de manera dinámica en el DOM.
 * Carga e instancia un HTML externo dentro de un contenedor en el DOM
 * @param {string} urlPath - Ruta del archivo HTML del modal
 * @param {string} containerSelector - Selector del contenedor destino
 */
export async function loadModal(urlPath, containerSelector = "#modal-container") {
  const container = document.querySelector(containerSelector);

  if (!container) {
    throw new Error(`El contenedor ${containerSelector} no existe en el DOM.`);
  }

  // 1. HTTP Fetch al archivo HTML
  const response = await fetch(urlPath);
  if (!response.ok) throw new Error(`Error al cargar el modal: ${response.statusText}`);

  const htmlText = await response.text();

  // 2. Inyección en el DOM (Instanciación)
  container.innerHTML = htmlText;

  // En este preciso instante, ¡todos los elementos del modal ya están vivos en el DOM!
  return container.firstElementChild;
}
