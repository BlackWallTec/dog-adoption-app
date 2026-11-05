// ===== Controlador generado con IA =====
/**
 * Inicializa la funcionalidad de Drag & Drop reteniendo los archivos en memoria
 * para permitir cambios antes de la subida definitiva.
 */
export function initDropzone({ onFilesChanged }) {
  const dropzone = document.getElementById("imageDropzone");
  const fileInput = document.getElementById("multimediaInput");
  const progressList = document.getElementById("uploadProgressList"); // Aquí mostraremos las miniaturas/lista

  if (!dropzone || !fileInput) return;

  // 📦 EL CAMBIO CLAVE: Guardamos los objetos File físicos en memoria local, NO subimos nada aún.
  let archivosEnMemoria = [];
  const urlsTemporales = new Map();

  // 1. Abrir explorador de archivos al hacer clic (Evitar disparar si hacen clic en un botón interno)
  dropzone.addEventListener("click", (e) => {
    if (e.target.closest(".btn-eliminar")) return; // No abrir buscador si se clickea en eliminar
    fileInput.click();
  });

  fileInput.addEventListener("change", (e) => {
    const files = Array.from(e.target.files);
    acumularArchivos(files);
  });

  // 2. Eventos Drag and Drop (Arrastrar y Soltar)
  ["dragenter", "dragover", "dragleave", "drop"].forEach((eventName) => {
    dropzone.addEventListener(eventName, (e) => {
      e.preventDefault();
      e.stopPropagation();
    });
  });

  ["dragenter", "dragover"].forEach((eventName) => {
    dropzone.addEventListener(eventName, () => dropzone.classList.add("drag-active"));
  });

  ["dragleave", "drop"].forEach((eventName) => {
    dropzone.addEventListener(eventName, () => dropzone.classList.remove("drag-active"));
  });

  dropzone.addEventListener("drop", (e) => {
    const files = Array.from(e.dataTransfer.files);
    // Filtrar imágenes y videos según tu necesidad de catálogo
    const multimediaValida = files.filter(
      (file) => file.type.startsWith("image/") || file.type.startsWith("video/"),
    );
    if (multimediaValida.length === 0) {
      alert("Por favor, sube solo imágenes o videos válidos.");
      return;
    }
    acumularArchivos(multimediaValida);
  });

  // ==========================================
  // 3. Procesamiento en Local (Sin llamadas de red)
  // ==========================================
  function acumularArchivos(files) {
    files.forEach((file) => {
      // Generamos un ID único local para controlar su elemento en la UI
      const localId = `local-${Date.now()}-${Math.random().toString(36).substr(2, 4)}`;

      // Guardamos el archivo binario real junto con su ID en nuestro arreglo
      archivosEnMemoria.push({ id: localId, file: file });

      // Generar una URL local temporal (Object URL) para la miniatura de la SummaryView
      const urlMiniaturaTemporal = URL.createObjectURL(file);
      urlsTemporales.set(localId, urlMiniaturaTemporal);

      renderVistaPreviaUI(localId, file.name, urlMiniaturaTemporal, file.type);
    });

    // Notificar al componente padre que la lista de archivos cambió
    notificarCambios();
  }

  function eliminarArchivo(localId) {
    // 💡 El administrador se arrepintió: Removemos el archivo de la memoria sin haber tocado Supabase
    archivosEnMemoria = archivosEnMemoria.filter((item) => item.id !== localId);

    const elementoUI = document.getElementById(localId);
    if (elementoUI) {
      elementoUI.remove();
    }
    const urlTemporal = urlsTemporales.get(localId);
    if (urlTemporal) URL.revokeObjectURL(urlTemporal);
    urlsTemporales.delete(localId);

    notificarCambios();
  }

  function notificarCambios() {
    if (typeof onFilesChanged === "function") {
      // Pasamos solo los archivos puros para que el formulario sepa qué hay acumulado
      onFilesChanged(archivosEnMemoria.map((item) => item.file));
    }
  }

  // ==========================================
  // Renderizado de Miniaturas y Botón Eliminar
  // ==========================================
  function renderVistaPreviaUI(id, filename, srcTemporal, tipo) {
    let previewElement = `<img src="${srcTemporal}" alt="Preview" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px;">`;

    if (tipo.startsWith("video/")) {
      previewElement = `<video src="${srcTemporal}" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px;"></video>`;
    }

    const html = `
      <div class="upload-item" id="${id}" style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; padding: 10px; border: 1px solid #eee;">
        <div style="display: flex; align-items: center; gap: 15px;">
          ${previewElement}
          <span style="font-size: 14px; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${filename}</span>
        </div>
        <!-- El botón mágico de arrepentimiento -->
        <button class="btn-eliminar" data-id="${id}" style="background: none; border: none; color: #d32f2f; cursor: pointer; font-size: 16px;">🗑️ Quitar</button>
      </div>
    `;

    progressList.insertAdjacentHTML("beforeend", html);

    // Escuchar el clic de eliminación para esta miniatura específica
    document
      .getElementById(id)
      .querySelector(".btn-eliminar")
      .addEventListener("click", (e) => {
        e.stopPropagation();
        eliminarArchivo(id);
      });
  }

  // ==========================================
  // Métodos Públicos que usará tu formulario final
  // ==========================================
  return {
    // Cuando el admin le dé a "GUARDAR PERRO" definitivo, este método entregará los archivos reales a subir
    obtenerArchivosAEnviar: () => archivosEnMemoria.map((item) => item.file),
    limpiar: () => {
      archivosEnMemoria = [];
      urlsTemporales.forEach((url) => URL.revokeObjectURL(url));
      urlsTemporales.clear();
      fileInput.value = "";
      progressList.innerHTML = "";
    },
  };
}

// ===== Fin Controlador generado con IA =====
