// summaryModalJS/summaryModal.controller.js
import * as apiService from "./Api.service.js";

export function initDogSummaryModal({ onBack, onSuccess, onError }) {
  // 1. Centralización de referencias DOM (Estilo @export)
  const ui = {
    modalOverlay: document.querySelector("#modalResumenPerro"),
    btnClose: document.querySelector("#btnCerrarResumen"),
    btnBack: document.querySelector("#back-2-register"),
    btnConfirm: document.querySelector("#btnConfirmarBD"),

    // Nodos de datos textuales
    name: document.querySelector("#resNombre"),
    breed: document.querySelector("#resRaza"),
    sex: document.querySelector("#resSexo"),
    age: document.querySelector("#resEdad"),
    size: document.querySelector("#resTamano"),
    status: document.querySelector("#resEstado"),
    description: document.querySelector("#resDescripcion"),

    // Nodos de Salud y Badges
    sterilized: document.querySelector("#resEsterilizado"),
    vaccinated: document.querySelector("#resVacunado"),
    characteristicsContainer: document.querySelector("#resCaracteristicas"),
    careContainer: document.querySelector("#resCuidados"),

    // Galería
    mainImage: document.querySelector("#resImagenPrincipal"),
    mainVideo: null,
    videoBadge: document.querySelector("#resVideoBadge"),
    thumbsContainer: document.querySelector("#resMiniaturas"),
  };

  const galleryMain = ui.mainImage?.closest(".gallery-main");
  if (galleryMain) {
    ui.mainVideo = document.createElement("video");
    ui.mainVideo.controls = true;
    ui.mainVideo.playsInline = true;
    ui.mainVideo.preload = "metadata";
    ui.mainVideo.style.cssText = "display:none;width:100%;height:100%;object-fit:contain;";
    galleryMain.append(ui.mainVideo);
  }

  let currentData = null;

  // 2. Control de visibilidad
  const open = () => ui.modalOverlay?.classList.add("active");
  const close = () => ui.modalOverlay?.classList.remove("active");

  // 3. Renderizador de datos (Poblado de UI)
  const populateSummary = (datos) => {
    console.groupCollapsed("[Resumen de perro] Datos recibidos para construir el resumen");
    console.log("Datos entrantes:", datos);
    console.table(
      Object.entries(datos ?? {}).map(([campo, valor]) => ({
        campo,
        valor: Array.isArray(valor) ? JSON.stringify(valor) : valor,
      })),
    );
    console.groupEnd();
    currentData = datos;

    // Datos generales
    if (ui.name) ui.name.textContent = datos.nombre || "Sin nombre";
    if (ui.breed) ui.breed.textContent = datos.razaTexto || datos.id_raza || "No especificada";
    if (ui.sex) ui.sex.textContent = datos.sexo || "-";
    if (ui.age) ui.age.textContent = datos.edad_aprox || "0";
    if (ui.size) ui.size.textContent = datos.tamano_medida ? `${datos.tamano_medida} cm` : "-";
    if (ui.status) ui.status.textContent = datos.estado || "Disponible";
    if (ui.description)
      ui.description.textContent = datos.descripcion || "Sin descripción proporcionada.";

    // Renderizado de Galería Multimedia
    const imagenes = datos.imagenes_urls || [];
    const archivos = datos.archivos_multimedia || [];
    const multimedia = imagenes.map((url, index) => {
      const archivo = archivos[index];
      const nombre = archivo?.name || obtenerNombreDesdeUrl(url);
      const extension = obtenerExtension(nombre);

      return {
        url,
        nombre,
        extension,
        esVideo: ["mov", "mp4"].includes(extension) || archivo?.type?.startsWith("video/"),
      };
    });
    const imagenPlaceholder =
      "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&q=80&w=800";

    if (multimedia.length > 0) {
      if (ui.thumbsContainer) {
        ui.thumbsContainer.replaceChildren();

        multimedia.forEach((recurso, index) => {
          const thumb = document.createElement(recurso.esVideo ? "video" : "img");
          thumb.src = recurso.url;
          thumb.className = index === 0 ? "active" : "";
          thumb.title = `${recurso.nombre} (.${recurso.extension.toUpperCase()})`;
          thumb.setAttribute(
            "aria-label",
            `${recurso.esVideo ? "Video" : "Imagen"} ${index + 1}, extensión .${recurso.extension}`,
          );
          if (recurso.esVideo) {
            thumb.muted = true;
            thumb.playsInline = true;
            thumb.preload = "metadata";
            thumb.style.cssText =
              "width:64px;height:64px;border-radius:10px;object-fit:cover;cursor:pointer;opacity:0.5;border:2px solid transparent;";
          }
          thumb.addEventListener("click", () => {
            mostrarRecursoMultimedia(recurso, ui);
            ui.thumbsContainer.querySelectorAll("img, video").forEach((elemento) => {
              const activo = elemento === thumb;
              elemento.classList.toggle("active", activo);
              if (elemento instanceof HTMLVideoElement) {
                elemento.style.opacity = activo ? "1" : "0.5";
                elemento.style.borderColor = activo
                  ? "var(--md-sys-color-secondary)"
                  : "transparent";
              }
            });
          });
          ui.thumbsContainer.append(thumb);
        });
      }

      mostrarRecursoMultimedia(multimedia[0], ui);
    } else {
      if (ui.mainImage) ui.mainImage.src = imagenPlaceholder;
      if (ui.mainImage) ui.mainImage.style.display = "block";
      if (ui.mainVideo) {
        ui.mainVideo.pause();
        ui.mainVideo.removeAttribute("src");
        ui.mainVideo.load();
        ui.mainVideo.style.display = "none";
      }
      if (ui.videoBadge) ui.videoBadge.style.display = "none";
      if (ui.thumbsContainer) ui.thumbsContainer.replaceChildren();
    }

    // Renderizado de Estado de Salud
    if (ui.sterilized) {
      ui.sterilized.textContent = datos.esterilizado ? "Esterilizado" : "No esterilizado";
      ui.sterilized.parentElement.style.opacity = datos.esterilizado ? "1" : "0.5";
    }
    if (ui.vaccinated) {
      ui.vaccinated.textContent = datos.vacunado ? "Vacunación Completa" : "Vacunación Incompleta";
      ui.vaccinated.parentElement.style.opacity = datos.vacunado ? "1" : "0.5";
    }

    // Renderizado de Badges (Características)
    if (ui.characteristicsContainer) {
      const caracteristicas = datos.caracteristicas || [];
      ui.characteristicsContainer.innerHTML =
        caracteristicas.length > 0
          ? caracteristicas.map((c) => `<span class="badge-item active">${c}</span>`).join("")
          : '<span class="badge-item">Sin características</span>';
    }

    // Renderizado de Badges (Cuidados Especiales)
    if (ui.careContainer) {
      const cuidados = datos.cuidados || [];
      ui.careContainer.innerHTML =
        cuidados.length > 0
          ? cuidados.map((c) => `<span class="badge-item active">${c}</span>`).join("")
          : '<span class="badge-item">Ninguno</span>';
    }
  };

  // 4. Método público de entrada
  const openWithData = (datos) => {
    populateSummary(datos);
    open();
  };

  // 5. Event Listeners (Signals)
  ui.btnClose?.addEventListener("click", close);

  ui.btnBack?.addEventListener("click", () => {
    close();
    if (typeof onBack === "function") onBack();
  });

  ui.btnConfirm?.addEventListener("click", async () => {
    if (!currentData) return;

    // Estado visual de carga (Deshabilitar para evitar múltiples envíos)
    ui.btnConfirm.disabled = true;
    ui.btnConfirm.innerHTML = `<span class="material-icons-round">sync</span> Guardando...`;
    let saveCompleted = false;

    try {
      // Llamada al Service desacoplado
      const urlsSubidas = [];

      if (currentData.archivos_multimedia && currentData.archivos_multimedia.length > 0) {
        const idPerroTemporal = `registro-${Date.now()}`;
        for (const file of currentData.archivos_multimedia) {
          const { uploadUrl, publicUrl } = await apiService.obtenerUrlFirmadaSupabase(
            idPerroTemporal,
            file.name,
            file.type,
          );

          await apiService.subirArchivoASupabase(uploadUrl, file);
          urlsSubidas.push(publicUrl);
        }
      }

      const payloadFinal = {
        ...currentData,
        imagenes_urls: urlsSubidas,
      };

      delete payloadFinal.archivos_multimedia;
      await apiService.guardarPerroBD(payloadFinal);
      saveCompleted = true;
    } catch (error) {
      console.error("Error al registrar el perro:", error);
      if (typeof onError === "function") onError(error, currentData);
      else alert(`Ocurrió un error al intentar guardar: ${error.message}`);
    } finally {
      ui.btnConfirm.disabled = false;
      ui.btnConfirm.innerHTML = `<span class="material-icons-round">cloud_upload</span> Confirmar y Registrar`;
    }

    if (saveCompleted) {
      close();
      if (typeof onSuccess === "function") onSuccess(currentData);
    }
  });

  // Retorno de la interfaz pública
  return {
    openWithData,
    close,
    ui,
  };
}

function mostrarRecursoMultimedia(recurso, ui) {
  if (recurso.esVideo && ui.mainVideo) {
    if (ui.mainImage) ui.mainImage.style.display = "none";
    ui.mainVideo.src = recurso.url;
    ui.mainVideo.style.display = "block";
    ui.mainVideo.load();
  } else {
    if (ui.mainVideo) {
      ui.mainVideo.pause();
      ui.mainVideo.removeAttribute("src");
      ui.mainVideo.load();
      ui.mainVideo.style.display = "none";
    }
    if (ui.mainImage) {
      ui.mainImage.src = recurso.url;
      ui.mainImage.style.display = "block";
    }
  }

  if (ui.videoBadge) {
    const icon = document.createElement("span");
    icon.className = "material-icons-round";
    icon.style.fontSize = "16px";
    icon.textContent = recurso.esVideo ? "play_circle" : "image";
    ui.videoBadge.replaceChildren(
      icon,
      document.createTextNode(
        `${recurso.esVideo ? "Video" : "Imagen"} · .${recurso.extension.toUpperCase()}`,
      ),
    );
    ui.videoBadge.style.display = "flex";
  }
}

function obtenerExtension(nombreArchivo) {
  return nombreArchivo?.split(".").pop()?.split("?")[0].toLowerCase() || "archivo";
}

function obtenerNombreDesdeUrl(url) {
  try {
    return new URL(url, window.location.href).pathname.split("/").pop() || "";
  } catch {
    return "";
  }
}
