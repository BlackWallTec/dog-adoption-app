import { obtenerDonativosPendientes } from "./adminDonativos.js";

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

function formatearMonto(monto) {
  return new Intl.NumberFormat("es-MX", {
    style: "currency",
    currency: "MXN",
  }).format(monto);
}

function formatearFecha(fecha) {
  const fechaValida = new Date(fecha);

  if (Number.isNaN(fechaValida.getTime())) {
    return "Sin fecha";
  }

  return new Intl.DateTimeFormat("es-MX", {
    dataStyle: "medium",
  }).format(fechaValida);
}

function renderizarTabla(donativos) {
  if (!tablaBody) return;

  tablaBody.replaceChildren();

  if (donativos.length === 0) {
    mostrarMensaje("No hay donativos pendientes.");
    return;
  }

  for (const donativo of donativos) {
    const fila = document.createElement("tr");

    const valores = [
      donativo.id,
      donativo.donante,
      formatearMonto(donativo.monto),
      formatearFecha(donativo.fecha),
    ];

    for (const valor of valores) {
      const celda = document.createElement("td");
      celda.textContent = valor ?? "";
      fila.append(celda);
    }

    const celdaEstado = document.createElement("td");
    const etiqueta = document.createElement("span");
    etiqueta.className = "badge-estado badge-pendiente";
    etiqueta.textContent = donativo.estado;
    celdaEstado.append(etiqueta);
    fila.append(celdaEstado);

    tablaBody.append(fila);
  }
}

async function cargarDonativos() {
  mostrarMensaje("Cargando donativos pendientes...");
  actualizarContador(0);

  try {
    const donativos = await obtenerDonativosPendientes();
    renderizarTabla(donativos);
    actualizarContador(donativos.length);
  } catch (error) {
    console.error("Error al cargar los donativos:", error);
    mostrarMensaje("No fue posible cargar los donativos pendientes.");
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
