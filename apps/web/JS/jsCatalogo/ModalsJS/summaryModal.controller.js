// summaryModalJS/summaryModal.controller.js
import * as apiService from "./Modals.service.js";

export function initDogSummaryModal({ onBack, onSuccess }) {
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
    thumbsContainer: document.querySelector("#resMiniaturas"),
  };

  let currentData = null;

  // 2. Control de visibilidad
  const open = () => ui.modalOverlay?.classList.add("active");
  const close = () => ui.modalOverlay?.classList.remove("active");

  // 3. Renderizador de datos (Poblado de UI)
  const populateSummary = (datos) => {
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
    const imagenPlaceholder =
      "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&q=80&w=800";

    if (imagenes.length > 0) {
      // 1. La primera imagen pasa a ser la foto principal
      if (ui.mainImage) ui.mainImage.src = imagenes[0];

      // 2. Renderizar carrusel de miniaturas
      if (ui.thumbsContainer) {
        ui.thumbsContainer.innerHTML = imagenes
          .map(
            (url, index) => `
            <img 
              src="${url}" 
              class="${index === 0 ? "active" : ""}" 
              alt="Miniatura ${index + 1}"
              data-index="${index}"
            />
          `,
          )
          .join("");

        // 3. Activar listener de intercambio al hacer clic en cualquier miniatura
        attachGalleryInteractivity(imagenes);
      }
    } else {
      // Imagen default por si el usuario no subió nada
      if (ui.mainImage) ui.mainImage.src = imagenPlaceholder;
      if (ui.thumbsContainer) ui.thumbsContainer.innerHTML = "";
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
    const originalText = ui.btnConfirm.innerHTML;
    ui.btnConfirm.innerHTML = `<span class="material-icons-round">sync</span> Guardando...`;

    try {
      // Llamada al Service desacoplado
      const urlsSubidas = [];

      // 1. AHORA SÍ: Subimos los archivos retenidos a Supabase uno por uno
      if (currentData.archivos_multimedia && currentData.archivos_multimedia.length > 0) {
        for (const file of currentData.archivos_multimedia) {
          // A. Pide la URL firmada a tu backend
          const { uploadUrl, publicUrl } = await apiService.obtenerUrlFirmadaSupabase(
            file.name,
            file.type,
          );

          // B. Sube el binario directo al Bucket
          await apiService.subirArchivoASupabase(uploadUrl, file);

          // C. Guarda la URL pública final devuelta por el servidor
          urlsSubidas.push(publicUrl);
        }
      }

      // 2. Construimos el payload definitivo para la Base de Datos con las URLs reales de Supabase
      const payloadFinal = {
        currentData,
        imagenes_urls: urlsSubidas, // por las públicas de Supabase
      };

      // Quitamos los binarios para no mandar basura pesada en el JSON del backend
      delete payloadFinal.archivos_multimedia;

      await apiService.guardarPerroBD(payloadFinal);
      close();
      if (typeof onSuccess === "function") onSuccess();
    } catch (error) {
      console.error("Error al registrar el perro:", error);
      alert("Ocurrió un error al intentar guardar en la base de datos.");
    } finally {
      ui.btnConfirm.disabled = false;
      ui.btnConfirm.innerHTML = `<span class="material-icons-round">cloud_upload</span> Confirmar y Registrar`;
    }
  });

  // Retorno de la interfaz pública
  return {
    openWithData,
    close,
    ui,
  };
}

// ======== Función Generada con IA ===========
/**
 * Permite alternar la imagen principal al hacer clic sobre una miniatura del carrusel
 */
function attachGalleryInteractivity(imagenes) {
  const thumbsContainer = document.querySelector("#resMiniaturas");
  const mainImage = document.querySelector("#resImagenPrincipal");

  if (!thumbsContainer || !mainImage) return;

  thumbsContainer.querySelectorAll("img").forEach((thumb) => {
    thumb.addEventListener("click", (e) => {
      // Remover clase active de todas las miniaturas
      thumbsContainer.querySelectorAll("img").forEach((img) => img.classList.remove("active"));

      // Asignar active a la seleccionada
      const selectedThumb = e.currentTarget;
      selectedThumb.classList.add("active");

      // Actualizar src de la imagen principal con animación suave
      const newUrl = selectedThumb.src;
      mainImage.style.opacity = "0.4";
      setTimeout(() => {
        mainImage.src = newUrl;
        mainImage.style.opacity = "1";
      }, 150);
    });
  });
}

// ======== Fin Función Generada con IA ===========
