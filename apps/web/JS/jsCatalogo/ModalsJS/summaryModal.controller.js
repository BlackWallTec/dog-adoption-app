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
      await apiService.guardarPerroBD(currentData);
      close();
      if (typeof onSuccess === "function") onSuccess();
    } catch (error) {
      console.error("Error al registrar el perro:", error);
      alert("Ocurrió un error al intentar guardar en la base de datos.");
    } finally {
      ui.btnConfirm.disabled = false;
      ui.btnConfirm.innerHTML = originalText;
    }
  });

  // Retorno de la interfaz pública
  return {
    openWithData,
    close,
    ui,
  };
}
