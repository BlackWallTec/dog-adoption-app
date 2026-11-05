// ===== Controlador generado con IA =====
/**
 * Inicializa la funcionalidad de Drag & Drop reteniendo los archivos en memoria
 * para permitir cambios antes de la subida definitiva.
 */
export function initDropzone({ onFilesChanged }) {
  const dropzone = document.getElementById("imageDropzone");
  const fileInput = document.getElementById("multimediaInput");
  const progressList = document.getElementById("uploadProgressList");

  if (!dropzone || !fileInput || !progressList) return null;

  fileInput.accept = ".jpg,.jpeg,.mp4,.mov,image/jpeg,video/mp4,video/quicktime";

  let archivosEnMemoria = [];
  const urlsTemporales = new Map();

  dropzone.addEventListener("click", (e) => {
    if (e.target.closest(".btn-eliminar") || e.target === fileInput) return;
    fileInput.click();
  });

  fileInput.addEventListener("change", (e) => {
    const files = Array.from(e.target.files);
    acumularArchivos(files);
    fileInput.value = "";
  });

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
    acumularArchivos(files);
  });

  function acumularArchivos(files) {
    const archivosInvalidos = [];
    const archivosValidos = [];

    files.forEach((file) => {
      const extension = obtenerExtension(file.name);
      const tipoMime = obtenerTipoMime(extension);

      if (!tipoMime || !esTipoMimeCompatible(file.type, tipoMime)) {
        archivosInvalidos.push(file.name);
        return;
      }

      const archivoNormalizado =
        file.type === tipoMime
          ? file
          : new File([file], file.name, { type: tipoMime, lastModified: file.lastModified });
      archivosValidos.push(archivoNormalizado);
    });

    if (archivosInvalidos.length > 0) {
      alert(
        `Formato no compatible: ${archivosInvalidos.join(", ")}. Solo se permiten JPG, MOV y MP4.`,
      );
    }

    archivosValidos.forEach((file) => {
      const localId = `local-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
      const urlMiniaturaTemporal = URL.createObjectURL(file);
      archivosEnMemoria.push({ id: localId, file });
      urlsTemporales.set(localId, urlMiniaturaTemporal);
      renderVistaPreviaUI(localId, file, urlMiniaturaTemporal);
    });

    notificarCambios();
  }

  function eliminarArchivo(localId) {
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
      onFilesChanged(archivosEnMemoria.map((item) => item.file));
    }
  }

  function renderVistaPreviaUI(id, file, srcTemporal) {
    const esVideo = file.type.startsWith("video/");
    const preview = document.createElement(esVideo ? "video" : "img");
    preview.src = srcTemporal;
    preview.alt = esVideo ? `Vista previa de ${file.name}` : `Vista previa de ${file.name}`;
    preview.style.cssText =
      "width:60px;height:60px;object-fit:cover;border-radius:4px;flex-shrink:0;";
    if (esVideo) {
      preview.muted = true;
      preview.playsInline = true;
      preview.preload = "metadata";
    }

    const details = document.createElement("div");
    details.style.cssText = "display:flex;align-items:center;gap:15px;min-width:0;";

    const filename = document.createElement("span");
    filename.textContent = file.name;
    filename.title = file.name;
    filename.style.cssText =
      "font-size:14px;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;";

    const extension = document.createElement("span");
    extension.textContent = `.${obtenerExtension(file.name).toUpperCase()}`;
    extension.style.cssText = "font-size:12px;font-weight:700;color:#555;";

    details.append(preview, filename, extension);

    const removeButton = document.createElement("button");
    removeButton.type = "button";
    removeButton.className = "btn-eliminar";
    removeButton.textContent = "Quitar";
    removeButton.style.cssText =
      "background:none;border:none;color:#d32f2f;cursor:pointer;font-size:14px;";
    removeButton.addEventListener("click", (event) => {
      event.stopPropagation();
      eliminarArchivo(id);
    });

    const item = document.createElement("div");
    item.id = id;
    item.className = "upload-item";
    item.style.cssText =
      "display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:10px;padding:10px;border:1px solid #eee;";
    item.append(details, removeButton);
    progressList.append(item);
  }

  return {
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

function obtenerExtension(nombreArchivo) {
  return nombreArchivo.split(".").pop()?.toLowerCase() || "";
}

function obtenerTipoMime(extension) {
  if (extension === "jpg" || extension === "jpeg") return "image/jpeg";
  if (extension === "mp4") return "video/mp4";
  if (extension === "mov") return "video/quicktime";
  return null;
}

function esTipoMimeCompatible(tipoRecibido, tipoEsperado) {
  return (
    !tipoRecibido ||
    tipoRecibido === tipoEsperado ||
    tipoRecibido === "application/octet-stream" ||
    (tipoEsperado === "video/quicktime" && tipoRecibido === "video/x-quicktime")
  );
}
