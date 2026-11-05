import * as apiService from "./Modals.service.js";
import { initDropzone } from "./dropZone.controller.js";

let archivosMultimediaLocales = [];

export function initDogRegisterModal({ onContinue }) {
  // Variables

  // Inicializar Dropzone
  const dropzoneController = initDropzone({
    onImageUploaded: (archivosPuros) => {
      archivosMultimediaLocales = archivosPuros;
    },
  });

  // Mapa de nodos UI centralizados (variables @export)
  const ui = {
    modalOverlay: document.querySelector("#modalAltaPerro"),
    btnClose: document.querySelector("#btn-cerrar-modal"),
    btnCancel: document.querySelector("#btn-cancelar-modal"),
    btnContinue: document.querySelector("#btn-continuar-modal"),
  };

  const open = async () => {
    ui.modalOverlay.classList.add("active");
    await loadModelData();
  };
  const close = () => ui.modalOverlay.classList.remove("active");
  const onContinueButtonClicked = (event) => {
    event.preventDefault();
    // 1. Recolectamos los datos del formulario
    const datosPerro = recollectFormData();
    if (!datosPerro) {
      alert("Por favor, completa todos los campos obligatorios.");
      return;
    }

    // 2. Aquí puedes pasar los datos al modal de resumen o a la API según tu flujo
    console.log("Datos recolectados del formulario:", datosPerro);

    //3. Emitir la "Signal" al Padre con los datos recolectados
    if (typeof onContinue === "function") {
      onContinue(datosPerro);
    }
  };

  // Event Listeners
  ui.btnClose.addEventListener("click", close);
  ui.btnCancel.addEventListener("click", close);
  ui.btnContinue.addEventListener("click", onContinueButtonClicked);

  return { open, close, ui };
}

function recollectFormData() {
  // 1. Validar campos requeridos de manera manual (ya que no hay etiqueta form nativa)
  const camposObligatorios = [
    { el: document.getElementById("nombre"), nombre: "Nombre" },
    { el: document.getElementById("sexo"), nombre: "Sexo" },
    { el: document.getElementById("id_raza"), nombre: "Raza" },
    { el: document.getElementById("id_tamano"), nombre: "Tamaño Categoría" },
  ];

  for (const campo of camposObligatorios) {
    if (!campo.el || !campo.el.value || campo.el.value.trim() === "") {
      alert(`Por favor, completa el campo obligatorio: ${campo.nombre}`);
      campo.el?.focus();
      return null; // Detiene la recolección si falta algo
    }
  }

  // 2. Extraer IDs de Badges con clase '.active'
  const obtenerIdsDeBadges = (containerId) => {
    const contenedor = document.getElementById(containerId);
    if (!contenedor) return [];
    const badgesActivos = contenedor.querySelectorAll(".badge-item.active");
    return Array.from(badgesActivos).map((badge) => badge.getAttribute("data-id"));
  };

  // 3. Obtener los archivos de imágenes cargados
  const inputMultimedia = document.getElementById("multimediaInput");
  const listaArchivos =
    inputMultimedia && inputMultimedia.files ? Array.from(inputMultimedia.files) : [];

  // 4. Construcción de la Estructura Final del Objeto
  const datosPerro = {
    // Información General
    nombre: document.getElementById("nombre").value.trim(),
    sexo: document.getElementById("sexo").value,
    edad_aprox: document.getElementById("edad_aprox").value
      ? parseInt(document.getElementById("edad_aprox").value, 10)
      : null,
    tamano_medida: document.getElementById("tamano_medida").value
      ? parseFloat(document.getElementById("tamano_medida").value)
      : null,
    estado: document.getElementById("estado").value,
    descripcion: document.getElementById("descripcion").value.trim(),

    // Clasificación de BD (Llaves foráneas numéricas)
    id_raza: document.getElementById("id_raza").value, // string directo
    id_tamano: document.getElementById("id_tamano").value, // string directo

    // Estado de Salud (Booleanos de los Checkboxes)
    esterilizado: document.getElementById("esterilizado").checked,
    vacunado: document.getElementById("vacunado").checked,

    // Relaciones Muchos a Muchos (Arreglos de IDs)
    caracteristicas_ids: obtenerIdsDeBadges("caracteristicasContainer"),
    cuidados_ids: obtenerIdsDeBadges("cuidadosContainer"),

    // Archivos Binarios (Pasará los elementos File listos para un FormData en el otro JS)
    archivos_multimedia: archivosMultimediaLocales,
    imagenes: archivosMultimediaLocales.map((file) => URL.createObjectURL(file)),
  };

  return datosPerro;
}

// ==========================================
// FUNCIONES AUXILIARES DE RENDERIZADO (UI)
// ==========================================

/**
 * Llena un elemento <select> con las opciones de la API.
 */
function populateSelect(selectId, apiGroup, valueKey, labelKey) {
  const select = document.getElementById(selectId);
  if (!select) return;

  // Extraemos la lista desde apiGroup.data
  const items = apiGroup?.data || [];

  const defaultOption = `<option value="" disabled selected>Selecciona una opción</option>`;
  const optionsHTML = items
    .map(
      (item) => `
    <option value="${item[valueKey]}">${item[labelKey]}</option>
  `,
    )
    .join("");

  select.innerHTML = defaultOption + optionsHTML;
}

/**
 * Genera dinámicamente los Badges interactivos para Selección Múltiple
 */
function populateBadges(containerId, apiGroup, idKey, labelKey) {
  const container = document.getElementById(containerId);
  if (!container) return;

  const items = apiGroup?.data || [];

  container.innerHTML = items
    .map(
      (item) => `
    <span class="badge-item" data-id="${item[idKey]}">
      ${item[labelKey]}
    </span>
  `,
    )
    .join("");

  // Re-adjuntamos el listener de toggle (activar/desactivar badge al hacer clic)
  attachBadgeToggleListener(container);
}

/**
 * Permite hacer clic en los badges recién renderizados para seleccionar/deseleccionar
 */
function attachBadgeToggleListener(container) {
  container.querySelectorAll(".badge-item").forEach((badge) => {
    badge.addEventListener("click", () => {
      badge.classList.toggle("active");
    });
  });
}

async function loadModelData() {
  try {
    // 1. Llamamos al servicio de API para obtener los datos necesarios para el modal
    const response = await fetchDataFromAPI();

    if (!response) {
      console.error("onLoad: No se pudieron obtener los datos de la API.");
      return;
    }

    // 2. Llenar los selectores y badges con los datos obtenidos
    populateSelect("id_tamano", response.tamanos, "id", "nombre");
    populateSelect("id_raza", response.razas, "id", "nombre");
    populateBadges("caracteristicasContainer", response.caracteristicas, "id_caracter", "nombre");
    populateBadges("cuidadosContainer", response.cuidados, "id_cuidado", "descripcion");
  } catch (error) {
    // Captura cualquier error de red o de ejecución dentro del bloque try
    console.error("onLoad: Ocurrió un error al procesar la solicitud.", error);
  }
}

async function fetchDataFromAPI() {
  //1. Llamamos al servicio de API para obtener los datos necesarios para el modal
  try {
    const response = await apiService.fetchRegisterData();
    console.log("fetchDataFromAPI: Datos obtenidos de la API:", response);
    if (!response || typeof response !== "object") {
      throw new Error("fetchDataFromAPI: Respuesta de la API no es un objeto válido.");
    }
    return response;
  } catch (erro) {
    console.error("Error al pedir datos de la BD");
  }
}
