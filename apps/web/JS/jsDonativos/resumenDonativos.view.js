import { obtenerResumenPublicoDonativos } from "./resumenDonativos.js";

const totalElement = document.getElementById("total-pendientes");
const statusElement = document.getElementById("resumen-estado");

async function cargarResumen() {
  try {
    const total = await obtenerResumenPublicoDonativos();
    totalElement.textContent = new Intl.NumberFormat("es-MX").format(total);
    statusElement.textContent = "Resumen actualizado";
  } catch (error) {
    console.error("Error al cargar el resumen público de donativos:", error);
    totalElement.textContent = "—";
    statusElement.textContent = "No fue posible cargar el resumen.";
  }
}

void cargarResumen();
