import {
  buildQueryString,
  estadoBadgeClass,
  estadoLabel,
  formatSolicitudDate,
  hasActiveFilters,
  pluralizeSolicitud,
} from "./adminSolicitudes.utils.js";
import { getMockDogs, getMockResponse } from "./adminSolicitudes.mock.js";
import { abrirDetalle } from "./adminSolicitudes.detalle.js";

const API_URL = "/api/solicitudes/admin/solicitudes";
const DOGS_API_URL = "/api/catalogo/admin/perros";
const LOGIN_URL = "/HTML/htmlPerfiles/login.html";
const USAR_MOCK = false;

const state = { perro: "todos", estado: "todos", fecha: "todas", page: 1 };
//We get the cache of the page
const solicitudesCache = new Map();
let abortController;
let requestId = 0;
let lastLoad;

const elements = {};

function cacheElements() {
  elements.body = document.getElementById("tabla-solicitudes-body");
  elements.chips = document.getElementById("status-chips");
  elements.estado = document.getElementById("filtro-estado");
  elements.fecha = document.getElementById("filtro-fecha");
  elements.perro = document.getElementById("filtro-perro");
  elements.paginationInfo = document.getElementById("pagination-info");
  elements.currentPage = document.getElementById("pagina-actual");
  elements.previous = document.getElementById("pagina-anterior");
  elements.next = document.getElementById("pagina-siguiente");
  elements.message = document.getElementById("status-message");
}

function setMessage(text = "", type = "", action) {
  elements.message.replaceChildren();
  elements.message.className = `status-message${text ? " visible" : ""}${type ? ` ${type}` : ""}`;
  if (!text) return;
  const message = document.createElement("span");
  message.textContent = text;
  elements.message.append(message);
  if (action) {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = action.label;
    button.addEventListener("click", action.handler);
    elements.message.append(button);
  }
}

function setTableMessage(text, action) {
  elements.body.replaceChildren();
  const row = document.createElement("tr");
  const cell = document.createElement("td");
  cell.colSpan = 6;
  cell.className = "empty-cell";
  cell.textContent = text;
  row.append(cell);
  if (action) {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = action.label;
    button.addEventListener("click", action.handler);
    cell.append(button);
  }
  elements.body.append(row);
}

function renderChips(summary = {}) {
  const chips = [
    ["todos", "Todas", summary.todas || 0, "chip-all"],
    ["pendientes", "Pendientes", summary.pendientes || 0, "chip-pending"],
    ["aprobada", "Aprobadas", summary.aprobadas || 0, "chip-approved"],
    ["rechazada", "Rechazadas", summary.rechazadas || 0, "chip-rejected"],
  ];
  elements.chips.replaceChildren();
  chips.forEach(([value, label, count, className]) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `status-chip ${className}${state.estado === value ? " active" : ""}`;
    button.textContent = `${label} · ${count}`;
    button.addEventListener("click", () => {
      state.estado = value;
      state.page = 1;
      elements.estado.value = value;
      void load();
    });
    elements.chips.append(button);
  });
}

function renderRows(rows) {
  elements.body.replaceChildren();
  rows.forEach((row) => {
    //We load the cache
    solicitudesCache.set(String(row.id), row);
    const tableRow = document.createElement("tr");
    const folio = document.createElement("strong");
    folio.className = "folio";
    folio.textContent = row.folio || `QA-${String(row.id).padStart(4, "0")}`;
    const folioCell = document.createElement("td");
    folioCell.append(folio);

    const adopterCell = document.createElement("td");
    const adopterName = document.createElement("strong");
    adopterName.className = "adopter-name";
    adopterName.textContent =
      `${row.adoptante?.nombre || ""} ${row.adoptante?.apellidos || ""}`.trim();
    const email = document.createElement("span");
    email.className = "muted";
    email.textContent = row.adoptante?.email || "";
    adopterCell.append(adopterName, email);

    const dogCell = document.createElement("td");
    const dogInfo = document.createElement("div");
    dogInfo.className = "dog-info";
    const photo = document.createElement("img");
    photo.className = "dog-photo";
    photo.src = row.perro?.foto_url || "/favicon.svg";
    photo.alt = row.perro?.nombre || "Perro";
    photo.addEventListener(
      "error",
      () => {
        photo.src = "/favicon.svg";
      },
      { once: true },
    );
    const dogName = document.createElement("strong");
    dogName.className = "dog-name";
    dogName.textContent = row.perro?.nombre || "Sin nombre";
    dogCell.append(photo, dogName);

    const dateCell = document.createElement("td");
    dateCell.textContent = formatSolicitudDate(row.fecha);
    const statusCell = document.createElement("td");
    const badge = document.createElement("span");
    badge.className = `badge-estado ${estadoBadgeClass(row.estado)}`;
    badge.textContent = estadoLabel(row.estado);
    statusCell.append(badge);
    const actionCell = document.createElement("td");
    const detail = document.createElement("button");
    detail.type = "button";
    detail.className = "detail-button";
    detail.dataset.id = row.id;
    detail.textContent = "Ver detalle";
    detail.addEventListener("click", () => abrirDetalle(row.id)); // HU05: abre el pop-up del detalle
    actionCell.append(detail);
    tableRow.append(folioCell, adopterCell, dogCell, dateCell, statusCell, actionCell);
    elements.body.append(tableRow);
  });
}

function renderPagination(pagination, count) {
  const page = pagination?.page || state.page;
  const pageSize = pagination?.page_size || 10;
  const total = pagination?.total ?? count;
  const start = total === 0 ? 0 : (page - 1) * pageSize + 1;
  const end = Math.min(page * pageSize, total);
  elements.paginationInfo.textContent = `Mostrando ${start}–${end} de ${pluralizeSolicitud(total)}`;
  elements.currentPage.textContent = String(page);
  elements.previous.disabled = page <= 1;
  elements.next.disabled = end >= total;
}

function renderResult(result) {
  renderChips(result.resumen);
  const rows = result.data || [];
  if (rows.length === 0) {
    const emptyAction = hasActiveFilters(state)
      ? { label: "Limpiar filtros", handler: resetFilters }
      : undefined;
    setTableMessage(
      hasActiveFilters(state) ? "No hay solicitudes con estos filtros." : "Aún no hay solicitudes.",
      emptyAction,
    );
  } else {
    renderRows(rows);
  }
  renderPagination(result.paginacion, rows.length);
  const pending = result.resumen?.pendientes || 0;
  setMessage(
    pending > 0
      ? `Hay ${pending} solicitudes pendientes. Revisa las respuestas y los documentos antes de tomar una decisión.`
      : "",
    pending > 0 ? "info" : "",
  );
}

function resetFilters(shouldLoad = true) {
  Object.assign(state, { perro: "todos", estado: "todos", fecha: "todas", page: 1 });
  elements.estado.value = state.estado;
  elements.fecha.value = state.fecha;
  elements.perro.value = state.perro;
  if (shouldLoad) void load();
}

async function fetchJson(url, signal) {
  const response = await fetch(url, { signal });
  const contentType = response.headers.get("content-type") || "";
  let payload = {};
  if (contentType.includes("application/json")) payload = await response.json();
  if (!response.ok) {
    const error = new Error(payload.error || `Solicitud fallida (${response.status})`);
    error.status = response.status;
    throw error;
  }
  if (!contentType.includes("application/json")) throw new Error("La respuesta no es JSON.");
  return payload;
}

async function fetchDogs(signal) {
  if (USAR_MOCK) return getMockDogs();
  const payload = await fetchJson(DOGS_API_URL, signal);
  const dogs = Array.isArray(payload) ? payload : payload.data || payload.perros || [];
  return dogs
    .map((dog) => ({
      id: dog.id ?? dog.id_perro,
      nombre: dog.nombre,
      foto_url: dog.foto_url || dog.foto,
    }))
    .sort((a, b) => String(a.nombre).localeCompare(String(b.nombre), "es"));
}

async function fetchList(filters, signal) {
  const mode = new URLSearchParams(window.location.search).get("mock") || "ok";
  if (USAR_MOCK) {
    if (mode === "lento") await new Promise((resolve) => setTimeout(resolve, 600));
    const response = getMockResponse(filters, mode);
    if (response.status) {
      const error = new Error(`Mock ${response.status}`);
      error.status = response.status;
      throw error;
    }
    return response;
  }
  return fetchJson(`${API_URL}?${buildQueryString(filters)}`, signal);
}

async function load() {
  abortController?.abort();
  abortController = new AbortController();
  const currentRequest = ++requestId;
  lastLoad = load;
  elements.previous.disabled = true;
  elements.next.disabled = true;
  elements.body.setAttribute("aria-busy", "true");
  setTableMessage("Cargando solicitudes...");
  setMessage("");
  try {
    const [result, dogs] = await Promise.all([
      fetchList({ ...state }, abortController.signal),
      fetchDogs(abortController.signal).catch((error) => {
        console.error("No se pudieron cargar los perros:", error);
        return [];
      }),
    ]);
    if (currentRequest !== requestId) return;
    elements.perro.replaceChildren(new Option("Perro: Todos", "todos"));
    dogs.forEach((dog) => elements.perro.append(new Option(`Perro: ${dog.nombre}`, dog.id)));
    elements.perro.value = state.perro;
    renderResult(result);
  } catch (error) {
    if (error.name === "AbortError" || currentRequest !== requestId) return;
    elements.body.removeAttribute("aria-busy");
    if (error.status === 401) {
      setTableMessage("Tu sesión expiró. Inicia sesión de nuevo.");
      window.location.assign(LOGIN_URL);
    } else if (error.status === 403) {
      setTableMessage("No tienes permiso para ver esta sección.");
    } else if (error.status === 400) {
      resetFilters(false);
      setTableMessage("Los filtros no son válidos.");
    } else {
      setTableMessage("No pudimos cargar las solicitudes.", {
        label: "Reintentar",
        handler: lastLoad,
      });
    }
  } finally {
    if (currentRequest === requestId) elements.body.removeAttribute("aria-busy");
  }
}

function bindEvents() {
  elements.estado.addEventListener("change", () => {
    state.estado = elements.estado.value;
    state.page = 1;
    void load();
  });
  elements.fecha.addEventListener("change", () => {
    state.fecha = elements.fecha.value;
    state.page = 1;
    void load();
  });
  elements.perro.addEventListener("change", () => {
    state.perro = elements.perro.value;
    state.page = 1;
    void load();
  });
  elements.previous.addEventListener("click", () => {
    if (state.page > 1) {
      state.page -= 1;
      void load();
    }
  });
  elements.next.addEventListener("click", () => {
    state.page += 1;
    void load();
  });
  //When you click the button y opens the modal
  elements.body.addEventListener("click", (event) => {
    const button = event.target.closest(".detail-button");
    if (!button) return;
    const solicitudId = button.dataset.id;
    //based on the id
    if (solicitudId) void abrirDetalleSolicitud(solicitudId);
  });
}

document.addEventListener("DOMContentLoaded", () => {
  cacheElements();
  bindEvents();
  const title = document.querySelector(".topbar-title");
  if (title) title.textContent = "Solicitudes";
  void load();
});
// <----------------- Begin Detail modal --------------------->
//we assign a variable to everything that we get from the view }
const modal = document.getElementById("modal-detalle-solicitud");
const bloqueResolucion = document.getElementById("bloque-resolucion");
const selectEstado = document.getElementById("select-nuevo-estado");
const estadoDot = document.getElementById("estado-dot");
const contenedorComentario = document.getElementById("contenedor-comentario-rechazo");
const textareaComentario = document.getElementById("comentario-resolucion");
const contadorCaracteres = document.getElementById("contador-caracteres");
const errorResolucion = document.getElementById("error-resolucion");
const modalConfirmacion = document.getElementById("modal-confirmacion-estado");
const iconoConfirmacion = document.getElementById("confirmacion-icono");
const tituloConfirmacion = document.getElementById("titulo-confirmacion-estado");
const mensajeConfirmacion = document.getElementById("mensaje-confirmacion-estado");

//It acompanies the color of the state
function actualizarIndicadorEstado(valor) {
  //It updates the view of colors
  estadoDot.className = "status-dot";
  if (valor === "aprobada") estadoDot.classList.add("dot-aprobada");
  else if (valor === "rechazada") estadoDot.classList.add("dot-rechazada");
  else if (valor === "periodo_prueba") estadoDot.classList.add("dot-prueba");
  else estadoDot.classList.add("dot-revision");
}

//Comentary when an adoption is not accepted
function actualizarContador() {
  const longitud = textareaComentario.value.length;
  contadorCaracteres.textContent = `${longitud.toLocaleString("es-MX")} / 1,000`;
}

//We update the state of the adoption in the main dashboard
function sincronizarFormularioEstado() {
  //reads the value that it is selected in the menu
  const estado = selectEstado.value;
  //we get a boolean value to see if the adoption status is actually rejected
  const esRechazada = estado === "rechazada";
  //Function to change of color
  actualizarIndicadorEstado(estado);
  contenedorComentario.hidden = !esRechazada;
  //If it is rejected then you show the comentary box
  if (!esRechazada) {
    textareaComentario.value = "";
  }

  //Recalculates the length of the text that is written
  actualizarContador();
  //We hide every other message of validation before
  errorResolucion.hidden = true;
  errorResolucion.textContent = "";
}

//Filter of security befire the data is sent, we verify that everything has been written correctly
function validarResolucion() {
  const estado = selectEstado.value;
  const comentario = textareaComentario.value.trim();
  const estadosValidos = ["en_revision", "periodo_prueba", "aprobada", "rechazada"];

  if (!estadosValidos.includes(estado)) {
    return { ok: false, mensaje: "Selecciona un estado válido." };
  }
  //If a comment is not written you need to show an error
  if (estado === "rechazada" && !comentario) {
    return {
      ok: false,
      mensaje: "Debes escribir un comentario interno cuando la solicitud es rechazada.",
    };
  }

  //If it is bigger than a 1000 characters it shows an error
  if (comentario.length > 1000) {
    return { ok: false, mensaje: "El comentario no puede superar los 1,000 caracteres." };
  }

  return {
    ok: true,
    datos: {
      state: estado,
      comment: estado === "rechazada" ? comentario : null,
    },
  };
}

//Function to open modal of details
async function abrirDetalleSolicitud(solicitudId) {
  try {
    const respuesta = await fetchJson(`${API_URL}/${solicitudId}`);
    const solicitud = respuesta.data || respuesta;

    document.getElementById("modal-folio").textContent =
      solicitud.folio || `QA-${String(solicitudId).slice(0, 8).toUpperCase()}`;

    const nombreAdoptante =
      typeof solicitud.adoptante === "object" && solicitud.adoptante !== null
        ? `${solicitud.adoptante.nombre || ""} ${solicitud.adoptante.apellidos || ""}`.trim()
        : solicitud.adoptante || "Sin nombre";
    const correoAdoptante = solicitud.adoptante?.email || solicitud.correo || "Sin correo";
    const nombrePerro =
      typeof solicitud.perro === "object" && solicitud.perro !== null
        ? solicitud.perro.nombre
        : solicitud.perro || "Sin perro";

    document.getElementById("modal-datos-solicitud").innerHTML = `
      <p><strong>Adoptante:</strong> ${nombreAdoptante}</p>
      <p><strong>Correo:</strong> ${correoAdoptante}</p>
      <p><strong>Perro:</strong> ${nombrePerro}</p>
      <p><strong>Estado actual:</strong> ${estadoLabel(solicitud.estado)}</p>
    `;

    bloqueResolucion.dataset.solicitudId = solicitudId;

    const estadoRaw = String(solicitud.estado || "").toLowerCase();
    if (estadoRaw.includes("aprob")) selectEstado.value = "aprobada";
    else if (estadoRaw.includes("rechaz")) selectEstado.value = "rechazada";
    else if (estadoRaw.includes("prueba")) selectEstado.value = "periodo_prueba";
    else selectEstado.value = "en_revision";

    textareaComentario.value = solicitud.comentario_admin || "";
    sincronizarFormularioEstado();

    modal.hidden = false;
    modal.style.display = "flex";
  } catch (err) {
    setMessage(err.message || "No se pudo cargar el detalle de la solicitud.", "error");
  }
}

selectEstado?.addEventListener("change", sincronizarFormularioEstado);
textareaComentario?.addEventListener("input", actualizarContador);

document.getElementById("btn-cerrar-modal")?.addEventListener("click", () => {
  modal.hidden = true;
  modal.style.display = "none";
});

document.getElementById("btn-cancelar-modal")?.addEventListener("click", () => {
  modal.hidden = true;
  modal.style.display = "none";
});

//Modal to show confirmation
function mostrarModalConfirmacion(estadoGuardado) {
  if (!modalConfirmacion) return;

  iconoConfirmacion.className = "confirmacion-icono";
  iconoConfirmacion.textContent = "✓";

  if (estadoGuardado === "rechazada") {
    iconoConfirmacion.classList.add("rechazada");
  } else if (estadoGuardado === "en_revision") {
    iconoConfirmacion.classList.add("revision");
  }

  tituloConfirmacion.textContent = "El estado de la solicitud se actualizó correctamente";
  mensajeConfirmacion.textContent = "Los cambios ya se reflejan en el listado de solicitudes.";

  modalConfirmacion.hidden = false;
  modalConfirmacion.style.display = "flex";
}
document.getElementById("btn-cerrar-confirmacion")?.addEventListener("click", () => {
  modalConfirmacion.hidden = true;
  modalConfirmacion.style.display = "none";
});

modalConfirmacion?.addEventListener("click", (event) => {
  if (event.target === modalConfirmacion) {
    modalConfirmacion.hidden = true;
    modalConfirmacion.style.display = "none";
  }
});

document.getElementById("btn-guardar-estado")?.addEventListener("click", async () => {
  const resultado = validarResolucion();
  //
  if (!resultado.ok) {
    errorResolucion.textContent = resultado.mensaje;
    errorResolucion.hidden = false;
    if (selectEstado.value === "rechazada") textareaComentario.focus();
    return;
  }

  const solicitudId = bloqueResolucion.dataset.solicitudId;
  //Start of the promise, we send the data to be updated in the modal
  try {
    const response = await fetch(`${API_URL}/${solicitudId}/resolucion`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(resultado.datos),
    });
    const payload = await response.json().catch(() => ({}));
    //If there is an error or it has been done incorrectly
    if (!response.ok) {
      throw new Error(payload.error || "No se pudo guardar el cambio.");
    }

    //We close the modal of detail
    modal.hidden = true;
    modal.style.display = "none";

    //We get the modal of confirmation when the update has been done succesfully
    mostrarModalConfirmacion(resultado.datos.state);

    //3. The page is reoloaded, and the numbers are updated
    await load();
  } catch (err) {
    errorResolucion.textContent = err.message || "No se pudo guardar el cambio.";
    errorResolucion.hidden = false;
  }
});
// <----------------- End Detail modal --------------------->
export { state, renderRows, load };
