import { obtenerDonativosPendientes } from "./adminDonativos.js";
import { obtenerMensajeListaVacia, prepararFilaDonativo } from "./adminDonativos.utils.js";

const tablaBody = document.getElementById("tabla-donativos-body");
const counterSubtitle = document.getElementById("donativos-counter-subtitle");
const paginationInfo = document.getElementById("pagination-info");

function actualizarContador(total) {
  const mensaje = `Mostrando ${total} donativos pendientes`;

  if (counterSubtitle) {
    counterSubtitle.textContent = mensaje;
  }
  if (paginationInfo) {
    paginationInfo.textContent = mensaje;
  }
}

function mostrarMensaje(mensaje) {
  if (!tablaBody) return;

  tablaBody.replaceChildren();

  const fila = document.createElement("tr");
  const celda = document.createElement("td");
  celda.colSpan = 5;
  celda.textContent = mensaje;
  fila.append(celda);
  tablaBody.append(fila);
}

function renderizarTabla(donativos) {
  if (!tablaBody) return;

  tablaBody.replaceChildren();

  const mensajeListaVacia = obtenerMensajeListaVacia(donativos);
  if (mensajeListaVacia) {
    mostrarMensaje(mensajeListaVacia);
    return;
  }

  for (const donativo of donativos) {
    const fila = document.createElement("tr");
    const valores = prepararFilaDonativo(donativo);

    for (const valor of valores.slice(0, 4)) {
      const celda = document.createElement("td");
      celda.textContent = String(valor);
      fila.append(celda);
    }

    const celdaEstado = document.createElement("td");
    const etiqueta = document.createElement("span");
    etiqueta.className = "badge-estado badge-pendiente";
    etiqueta.textContent = valores[4];
    celdaEstado.append(etiqueta);
    fila.append(celdaEstado);
    tablaBody.append(fila);
  }
}

async function cargarDonativos() {
  mostrarMensaje("Cargando donativos pendientes...");

  try {
    const donativos = await obtenerDonativosPendientes();
    renderizarTabla(donativos);
    actualizarContador(donativos.length);
  } catch (error) {
    console.error("Error al cargar los donativos:", error);

    if (error.message === "NO_AUTENTICADO") {
      mostrarMensaje("Necesitas una sesión administrativa para consultar los donativos.");
    } else if (error.message === "SIN_PERMISOS") {
      mostrarMensaje("Tu cuenta no tiene permisos para consultar los donativos.");
    } else {
      mostrarMensaje("No fue posible cargar los donativos pendientes.");
    }

    if (counterSubtitle) {
      counterSubtitle.textContent = "No se pudieron cargar los donativos.";
    }
    if (paginationInfo) {
      paginationInfo.textContent = "Error al cargar los donativos";
    }
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const topbar = document.querySelector("admin-topbar");
  const titulo = topbar?.querySelector(".topbar-title");

  if (titulo) {
    titulo.textContent = "Donativos";
  }

  void cargarDonativos();
});
