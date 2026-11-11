// Carga de modales de manera dinámica en el DOM
import { loadModal } from "../utils/modalLoader.js";
import { initDogRegisterModal } from "./ModalsJS/registerModal.controller.js";
import { initDogSummaryModal } from "./ModalsJS/summaryModal.controller.js";
import { abrirEdicion } from "./adminPerros.edit.js";
import { escaparHtml } from "./perroForm.js";
import * as apiService from "./ModalsJS/Api.service.js";

// referencias globales de modulo
let feedback = null;

document.addEventListener("DOMContentLoaded", async () => {
  // 1. Cargar los modales de manera dinámica
  await Promise.all([
    loadModal("/HTML/htmlCatalogo/registerDogModal.html", "#modal-container1"),
    loadModal("/HTML/htmlCatalogo/dogSummaryModal.html", "#modal-container2"),
    loadModal("/HTML/htmlCatalogo/confirmationModal.html", "#modal-container3"),
  ]);

  //2. Inicializar controladores de modales
  let registerModalController = null;
  let summaryModalController = null;
  feedback = initFeedbackModal();

  // 1. Inicializamos el Modal de Resumen
  summaryModalController = initDogSummaryModal({
    // SIGNAL: Cuando el usuario le da a "Atrás" en el Resumen:
    onBack: () => {
      registerModalController.open(); // Reabrimos el formulario intacto
    },
    // SIGNAL: Cuando se guardó con éxito:
    onSuccess: (datosPerro) => {
      // Limpiamos el formulario de registro y refrescamos la tabla
      registerModalController.close();
      registerModalController.reset();
      feedback.showSuccess(datosPerro?.nombre);
      cargarPerros();
    },
    // SIGNAL: El backend rechazó el registro o ocurrió un error al guardar:
    onError: (error, datosPerro) => {
      feedback.showError(error, datosPerro?.nombre);
    },
  });

  // 2. Inicializamos el Modal de Registro
  registerModalController = initDogRegisterModal({
    // SIGNAL: Cuando le dan a "Continuar" en el Formulario:
    onContinue: (datosRecolectados) => {
      // Le pasamos los datos al Resumen y lo abrimos
      summaryModalController.openWithData(datosRecolectados);
    },
  });

  // 3. Botón "Registrar Perro" de la pantalla principal
  document.querySelector(".button-registrar")?.addEventListener("click", () => {
    registerModalController.open();
  });

  //4. Inicializar Bulking setup
  inicializarSeleccionLote();
});

function initFeedbackModal() {
  const overlay = document.querySelector("#modalFeedback");
  const card = document.querySelector("#feedbackCard");
  const icon = document.querySelector("#feedbackIcon");
  const title = document.querySelector("#feedbackTitle");
  const message = document.querySelector("#feedbackMessage");
  const closeButtons = [
    document.querySelector("#btnFeedbackCloseTop"),
    document.querySelector("#btnFeedbackAction"),
  ];

  const close = () => overlay?.classList.remove("active");
  closeButtons.forEach((button) => button?.addEventListener("click", close));

  const show = ({ mode, titleText, messageText, iconName, actionText }) => {
    if (!overlay || !card || !icon || !title || !message) {
      console.error("No se pudo mostrar el resultado: faltan elementos del modal de feedback.");
      return;
    }

    card.classList.remove("modal-success", "modal-error");
    card.classList.add(mode === "success" ? "modal-success" : "modal-error");
    icon.textContent = iconName;
    title.textContent = titleText;
    message.textContent = messageText;

    const actionButton = document.querySelector("#btnFeedbackAction");
    if (actionButton) {
      actionButton.textContent = actionText;
      actionButton.classList.toggle("btn-error", mode === "error");
    }

    overlay.classList.add("active");
  };

  return {
    show,
    showSuccess(nombre) {
      const nombrePerro = nombre || "el perro";
      show({
        mode: "success",
        titleText: "¡Registro exitoso!",
        messageText: `El expediente de ${nombrePerro} se guardó correctamente en la base de datos del refugio.`,
        iconName: "check_circle",
        actionText: "Continuar",
      });
    },
    showError(error, nombre) {
      const nombrePerro = nombre || "el perro";
      const detalle = error instanceof Error ? error.message : String(error);
      show({
        mode: "error",
        titleText: `No se pudo registrar a ${nombrePerro}`,
        messageText: `No se guardaron los datos. ${detalle} Puedes volver al resumen e intentarlo de nuevo.`,
        iconName: "error",
        actionText: "Volver al resumen",
      });
    },
  };
}

// Referencias a elementos del DOM
const tablaBody = document.getElementById("tabla-perros-body");
const counterSubtitle = document.getElementById("perros-counter-subtitle");
const paginationInfo = document.getElementById("pagination-info");

// Estado local de la vista
let catalogoPerros = [];

// Función para obtener la clase de badge según el estado
function obtenerClaseBadge(estado) {
  const normalizado = (estado || "").toLowerCase().trim();
  if (normalizado === "disponible") return "badge-disponible";
  if (normalizado === "adoptado" || normalizado === "adoptada") return "badge-adoptado";
  return "badge-tratamiento";
}

function textoRelacion(valor, fallback) {
  if (!valor) return fallback;
  if (typeof valor === "object") return valor.nombre || fallback;
  return valor;
}

function textoEdad(perro) {
  const edad = perro.edad ?? perro.edad_aprox;
  if (edad === null || edad === undefined || edad === "") return "No especificada";
  if (typeof edad === "number" || /^\d+(\.\d+)?$/.test(String(edad).trim())) {
    return `${edad} años`;
  }
  return String(edad);
}

// Función para renderizar la tabla de perros
function renderizarTabla(perros) {
  if (!tablaBody) return;

  if (perros.length === 0) {
    tablaBody.innerHTML = `
           <tr>
                <td colspan="7" style="text-align: center; padding: 32px; color: var(--md-sys-color-on-surface-variant);">
                No se encontraron perros registrados.
                </td>
            </tr>
        `;
    return;
  }

  tablaBody.innerHTML = perros
    .map((perro) => {
      // Foto fallback en caso de no tener URL cargada
      const fotoUrl = escaparHtml(perro.foto_url || perro.foto || "/public/favicon.svg");
      const raza = escaparHtml(textoRelacion(perro.raza, "Mestizo"));
      const edadTexto = escaparHtml(textoEdad(perro));
      const tamano = escaparHtml(textoRelacion(perro.tamano || perro.tamanio, "Mediano"));
      const estado = escaparHtml(perro.estado || "Disponible");
      const badgeClase = obtenerClaseBadge(estado);
      const id = escaparHtml(perro.id ?? perro.id_perro ?? "");
      const nombre = escaparHtml(perro.nombre);

      return `
      <tr data-id="${id}">
        <td style="text-align: center;">
          <input type="checkbox" class="perro-select-checkbox" value="${id}">
        </td>
        <td>
          <img src="${fotoUrl}" alt="${nombre}" class="dog-photo" onerror="this.onerror=null; this.src='/public/favicon.svg'">
        </td>
        <td>
          <div class="dog-name-cell">
            <span class="dog-name">${nombre}</span>
            <span class="dog-meta">${raza}</span>
          </div>
        </td>
        <td>${edadTexto}</td>
        <td>${tamano}</td>
        <td>
          <span class="badge-estado ${badgeClase}">${estado}</span>
        </td>
        <td>
          <div class="actions-cell">
            <button type="button" class="action-btn action-edit" title="Editar">
              <span class="material-symbols-rounded">edit</span>
              Editar
            </button>
            <button type="button" class="action-btn action-delete" title="Eliminar">
              <span class="material-symbols-rounded">delete</span>
              Eliminar
            </button>
          </div>
        </td>
      </tr>
    `;
    })
    .join("");
}

async function cargarPerros() {
  try {
    const respuesta = await fetch("/api/catalogo/admin/perros");

    const contentType = respuesta.headers.get("content-type") || "";
    if (!respuesta.ok || !contentType.includes("application/json")) {
      throw new Error(`La ruta /api/catalogo/admin/perros no devolvió JSON (${respuesta.status}).`);
    }

    const data = await respuesta.json();
    catalogoPerros = Array.isArray(data) ? data : data.data || data.perros || [];

    // Actualizar contador en el encabezado y paginación
    const total = catalogoPerros.length;
    if (counterSubtitle) {
      counterSubtitle.textContent = `Mostrando ${total} perro${total === 1 ? "" : "s"} bajo cuidado`;
    }
    if (paginationInfo) {
      paginationInfo.textContent =
        total === 0 ? "Mostrando 0 de 0 perros" : `Mostrando 1-${total} de ${total} perros`;
    }

    renderizarTabla(catalogoPerros);
  } catch (error) {
    console.error("Error al cargar los perros:", error);
    if (tablaBody) {
      tablaBody.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align: center; padding: 28px; color: var(--md-sys-color-error);">
                        Error al conectar con la base de datos. Asegúrate de que el servidor backend esté corriendo.
                    </td>
                </tr>
            `;
    }
  }
}

tablaBody?.addEventListener("click", async (event) => {
  const boton = event.target.closest(".action-delete");
  if (!boton) return;

  const id = boton.closest("tr")?.dataset.id;
  if (!id) return;

  const perro = catalogoPerros.find((item) => String(item.id_perro ?? item.id) === id);
  const nombre = perro?.nombre || "este perro";

  if (!window.confirm(`¿Desactivar a ${nombre}? Dejará de aparecer en el catálogo.`)) {
    return;
  }

  boton.disabled = true;

  try {
    const respuesta = await fetch(`/api/catalogo/admin/perros/${id}/desactivar`, {
      method: "PATCH",
    });
    const data = await respuesta.json().catch(() => ({}));

    if (!respuesta.ok) {
      throw new Error(data.message || `No se pudo desactivar el perro (${respuesta.status}).`);
    }

    await cargarPerros();
  } catch (error) {
    console.error("Error al desactivar el perro:", error);
    window.alert(error instanceof Error ? error.message : "No se pudo desactivar el perro.");
    boton.disabled = false;
  }
});

// Carga inicial al montar la página
document.addEventListener("DOMContentLoaded", cargarPerros);

// HU19C: botón Editar de cada fila (delegación de eventos porque las filas se re-pintan)
tablaBody?.addEventListener("click", (evento) => {
  const boton = evento.target.closest(".action-edit");
  if (!boton) return;
  const id = boton.closest("tr")?.dataset.id;
  if (id) abrirEdicion(id, cargarPerros);
});

// ========== HU27 ========= //
// 1. Inicializador de eventos para selección masiva e individual
function inicializarSeleccionLote() {
  const selectAllHeader = document.getElementById("select-all-perros");
  const tablaBody = document.getElementById("tabla-perros-body");

  if (!tablaBody || !selectAllHeader) return;

  // Evento: Seleccionar/Desmarcar todos los checkboxes
  selectAllHeader.addEventListener("change", (e) => {
    const isChecked = e.target.checked;
    const checkboxes = tablaBody.querySelectorAll(".perro-select-checkbox");

    checkboxes.forEach((cb) => {
      cb.checked = isChecked;
      const row = cb.closest("tr");
      row.classList.toggle("selected-row", isChecked);
    });

    actualizarBarraFlotante();
  });

  // Evento delegado: Al marcar individualmente un checkbox en la tabla
  tablaBody.addEventListener("change", (e) => {
    if (e.target.classList.contains("perro-select-checkbox")) {
      const row = e.target.closest("tr");
      row.classList.toggle("selected-row", e.target.checked);

      // Desmarcar 'seleccionar todos' si alguno se desmarca individualmente
      const checkboxes = Array.from(tablaBody.querySelectorAll(".perro-select-checkbox"));
      const todosSeleccionados =
        checkboxes.length > 0 &&
        checkboxes.every((cb) => {
          cb.checked;
          actualizarBarraFlotante();
        });
      selectAllHeader.checked = todosSeleccionados;
    }
  });
}

/**
 * 2. Función helper para obtener los IDs seleccionados
 * @returns {Array<string>} Arreglo con los IDs de los perros marcados
 */
function obtenerIdsSeleccionados() {
  const checkboxesMarcados = document.querySelectorAll(".perro-select-checkbox:checked");
  return Array.from(checkboxesMarcados).map((cb) => cb.value);
}

// == Bulking mini-menu ==
const bulkBar = document.getElementById("bulk-actions-bar");
const bulkCounter = document.getElementById("bulk-counter-text");
const btnBulkDesactivar = document.getElementById("btn-bulk-desactivar");
const btnBulkActivar = document.getElementById("btn-bulk-activar");
const btnBulkDeselect = document.getElementById("btn-bulk-deselect");

function actualizarBarraFlotante() {
  const idsSeleccionados = obtenerIdsSeleccionados();
  const total = idsSeleccionados.length;
  console.log("IDs seleccionado: ", idsSeleccionados);

  if (total > 1) {
    bulkCounter.textContent = `${total} seleccionados`;
    bulkBar.classList.add("active");
  } else {
    bulkBar.classList.remove("active");
  }
}

// Event Listeners para botones de lote
btnBulkDesactivar.addEventListener("click", () => {
  const selectedIds = obtenerIdsSeleccionados();
  const action = false;
  //console.log("Ejecutar POST/PUT Desactivar Lote para IDs:", ids);
  showConfirmModal(
    "Desactivar Perros",
    `¿Estás seguro de que deseas desactivar a los <strong>${selectedIds.length} perros</strong> seleccionados?`,
    "Sí, desactivar",
    action, // Es acción destructiva (Rojo)
    async () => {
      // ====== Fetch bulk =====
      try {
        const response = await apiService.fetchBulkActivationToggle(selectedIds, action);

        console.log("Activación exitosa:", response);

        //Recargamos a los perros
        await cargarPerros();
        btnBulkDeselect.click();

        feedback.show({
          mode: "success",
          titleText: "Perros desactivados",
          messageText: `Se desactivaron ${selectedIds.length} perros correctamente.`,
          iconName: "check_circle",
          actionText: "Continuar",
        });
      } catch (error) {
        console.log("Error al hacer Bulk: ", error);
        feedback.show({
          mode: "error",
          titleText: "No se pudo completar la acción",
          messageText: error instanceof Error ? error.message : String(error),
          iconName: "error",
          actionText: "Cerrar",
        });
      }
    },
  );
});

btnBulkActivar.addEventListener("click", () => {
  const selectedIds = obtenerIdsSeleccionados();
  const action = false;
  //console.log("Ejecutar POST/PUT Activar Lote para IDs:", ids);
  showConfirmModal(
    "Activar Perros",
    `¿Deseas activar el perfil de los <strong>${selectedIds.length} perros</strong> seleccionados para que estén visibles?`,
    "Sí, activar",
    action, // Acción estándar (Verde/Azul)
    async () => {
      console.log("Hacer fetch POST/PUT a la API para activar:", selectedIds);
      try {
        //¡Esto es una llamada Async!
        const response = await apiService.fetchBulkActivationToggle(selectedIds, action);
        console.log("Activación exitosa:", response);
        //Llamar al modal de Success

        await cargarPerros();
        btnBulkDeselect.click();

        feedback.show({
          mode: "success",
          titleText: "Perros Activados",
          messageText: `Se activaron ${selectedIds.length} perros correctamente.`,
          iconName: "check_circle",
          actionText: "Continuar",
        });
      } catch (error) {
        console.log("Error al hacer Bulk: ", error);
        feedback.show({
          mode: "error",
          titleText: "No se pudo completar la acción",
          messageText: error instanceof Error ? error.message : String(error),
          iconName: "error",
          actionText: "Cerrar",
        });
      }
    },
  );
});

btnBulkDeselect.addEventListener("click", () => {
  const selectAllHeader = document.getElementById("select-all-perros");
  const checkboxes = document.querySelectorAll(".perro-select-checkbox");

  if (selectAllHeader) selectAllHeader.checked = false;
  checkboxes.forEach((cb) => {
    cb.checked = false;
    cb.closest("tr")?.classList.remove("selected-row");
  });

  actualizarBarraFlotante();
});

//=== Confirmation Ids =====
// Probar nueva forma de ids en lugar de const infinitos.
// Es como un nodo de Godot

function showConfirmModal(title, content, buttonText, isActivate = false, onConfirm = null) {
  const CONFIRM_MODAL_NODES = {
    overlay: document.getElementById("modalConfirmacionGlobal"),
    card: document.getElementById("confirmCard"),
    title: document.getElementById("confirmTitle"),
    message: document.getElementById("confirmMessage"),
    icon: document.getElementById("confirmIcon"),
    btnAccept: document.getElementById("btnConfirmAccept"),
    btnReject: document.getElementById("btnConfirmReject"),
    btnCloseTop: document.getElementById("btnConfirmCloseTop"),
  };

  console.log(`¡${title}!:${content}. --> ${buttonText}`);
  // 1. Inyectar valores al DOM
  CONFIRM_MODAL_NODES.title.textContent = title;
  CONFIRM_MODAL_NODES.message.innerHTML = content;
  CONFIRM_MODAL_NODES.btnAccept.textContent = buttonText;

  // 2. Alternar variante destructiva (Rojo para Desactivar) o estándar (Azul para Activar)
  if (!isActivate) {
    CONFIRM_MODAL_NODES.card.classList.add("confirm-danger");
    CONFIRM_MODAL_NODES.icon.textContent = "warning_amber";
  } else {
    CONFIRM_MODAL_NODES.card.classList.remove("confirm-danger");
    CONFIRM_MODAL_NODES.icon.textContent = "help_outline";
  }

  // 3. Handlers para cerrar y confirmar
  const close = () => {
    CONFIRM_MODAL_NODES.overlay.classList.remove("active");
    CONFIRM_MODAL_NODES.btnAccept.onclick = null;
    CONFIRM_MODAL_NODES.btnReject.onclick = null;
    CONFIRM_MODAL_NODES.btnCloseTop.onclick = null;
  };

  CONFIRM_MODAL_NODES.btnAccept.onclick = () => {
    close();
    if (typeof onConfirm === "function") onConfirm();
  };

  CONFIRM_MODAL_NODES.btnReject.onclick = close;
  CONFIRM_MODAL_NODES.btnCloseTop.onclick = close;

  // 4. Mostrar modal
  CONFIRM_MODAL_NODES.overlay.classList.add("active");
}

// ======== FIN DE HU27 ===== //
