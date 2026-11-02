import {
  buildQueryString,
  estadoBadgeClass,
  estadoLabel,
  formatSolicitudDate,
  hasActiveFilters,
  pluralizeSolicitud,
} from "./adminSolicitudes.utils.js";
import { getMockDogs, getMockResponse } from "./adminSolicitudes.mock.js";

const API_URL = "/api/solicitudes/admin/solicitudes";
const DOGS_API_URL = "/api/catalogo/admin/perros";
const LOGIN_URL = "/login";
const USAR_MOCK = false;

const state = { perro: "todos", estado: "todos", fecha: "todas", page: 1 };
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
    ["aprobadas", "Aprobadas", summary.aprobadas || 0, "chip-approved"],
    ["rechazadas", "Rechazadas", summary.rechazadas || 0, "chip-rejected"],
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
    const tableRow = document.createElement("tr");
    const folio = document.createElement("strong");
    folio.className = "folio";
    folio.textContent = row.folio || `QA-${String(row.id).padStart(4, "0")}`;
    const folioCell = document.createElement("td");
    folioCell.append(folio);

    const adopterCell = document.createElement("td");
    adopterCell.className = "adopter-cell";
    const adopterName = document.createElement("strong");
    adopterName.className = "adopter-name";
    adopterName.textContent =
      `${row.adoptante?.nombre || ""} ${row.adoptante?.apellidos || ""}`.trim();
    const email = document.createElement("span");
    email.className = "muted";
    email.textContent = row.adoptante?.email || "";
    adopterCell.append(adopterName, email);

    const dogCell = document.createElement("td");
    dogCell.className = "dog-cell";
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
    const detail = document.createElement("a");
    detail.className = "detail-button";
    detail.href = `/HTML/htmlSolicitudes/adminSolicitudDetalle.html?id=${encodeURIComponent(row.id)}`;
    detail.textContent = "Ver detalle";
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
}

document.addEventListener("DOMContentLoaded", () => {
  cacheElements();
  bindEvents();
  const title = document.querySelector(".topbar-title");
  if (title) title.textContent = "Solicitudes";
  void load();
});

export { state, renderRows, load };
