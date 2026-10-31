import { donativosDemo } from "./adminDonativos.js";

const tablaBody = document.getElementById("tabla-donativos-body");
const counterSubtitle = document.getElementById("donativos-counter-subtitle");
const paginationInfo = document.getElementById("pagination-info");

function formatearMonto(monto) {
  return new Intl.NumberFormat("es-MX", {
    style: "currency",
    currency: "MXN",
  }).format(monto);
}

function formatearFecha(fecha) {
  return new Intl.DateTimeFormat("es-MX", {
    dateStyle: "medium",
    timeZone: "UTC",
  }).format(new Date(`${fecha}T12:00:00Z`));
}

function renderizarTabla(donativos) {
  if (!tablaBody) return;

  if (donativos.length === 0) {
    tablaBody.innerHTML = `
            <tr>
                <td colspan="5">No se encontraron donativos.</td>
            </tr>
        `;
    return;
  }

  tablaBody.innerHTML = donativos
    .map((donativo) => {
      const estado = donativo.estado;
      const claseEstado = estado === "Confirmado" ? "badge-confirmado" : "badge-pendiente";

      return `
                <tr>
                    <td>${donativo.id}</td>
                    <td>${donativo.donante}</td>
                    <td>${formatearMonto(donativo.monto)}</td>
                    <td>${formatearFecha(donativo.fecha)}</td>
                    <td>
                        <span class="badge-estado ${claseEstado}">
                            ${estado}
                        </span>
                    </td>
                </tr>
            `;
    })
    .join("");
}

function cargarDonativos() {
  const total = donativosDemo.length;

  if (counterSubtitle) {
    counterSubtitle.textContent = `Mostrando ${total} de ${total} donativos`;
  }

  if (paginationInfo) {
    paginationInfo.textContent = `Mostrando ${total} de ${total} donativos`;
  }

  renderizarTabla(donativosDemo);
}

document.addEventListener("DOMContentLoaded", cargarDonativos);
