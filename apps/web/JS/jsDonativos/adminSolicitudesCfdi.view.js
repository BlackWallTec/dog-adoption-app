import {
  obtenerDetalleSolicitudCfdi,
  obtenerSolicitudesCfdiPendientes,
} from "./adminSolicitudesCfdi.js";

const cuerpoTabla = document.getElementById("solicitudes-cfdi-body");
const contador = document.getElementById("solicitudes-cfdi-contador");
const dialogo = document.getElementById("solicitud-cfdi-detalle");
const avisoDetalle = document.getElementById("solicitud-cfdi-aviso");

function formatearFecha(fecha) {
  if (!fecha) return "Sin fecha";
  const fechaValida = new Date(fecha);
  if (Number.isNaN(fechaValida.getTime())) return "Sin fecha";
  return new Intl.DateTimeFormat("es-MX", { dateStyle: "medium" }).format(fechaValida);
}

function mostrarMensaje(mensaje) {
  if (!cuerpoTabla) return;
  cuerpoTabla.replaceChildren();
  const fila = document.createElement("tr");
  const celda = document.createElement("td");
  celda.colSpan = 7;
  celda.textContent = mensaje;
  fila.append(celda);
  cuerpoTabla.append(fila);
}

function crearCelda(valor) {
  const celda = document.createElement("td");
  celda.textContent = valor;
  return celda;
}

function renderizarSolicitudes(solicitudes) {
  if (!cuerpoTabla) return;
  cuerpoTabla.replaceChildren();

  if (solicitudes.length === 0) {
    mostrarMensaje("No hay solicitudes CFDI pendientes.");
    return;
  }

  for (const solicitud of solicitudes) {
    const fila = document.createElement("tr");
    fila.append(
      crearCelda(String(solicitud.id_solicitud_cfdi)),
      crearCelda(String(solicitud.id_donativo ?? "—")),
      crearCelda(solicitud.donante || "Sin nombre"),
      crearCelda(formatearFecha(solicitud.fecha_solicitud)),
    );

    const celdaEstado = document.createElement("td");
    const estado = document.createElement("span");
    estado.className = "cfdi-state";
    estado.textContent = solicitud.estado || "Sin estado";
    celdaEstado.append(estado);
    fila.append(celdaEstado);

    const celdaRevision = document.createElement("td");
    const revision = document.createElement("span");
    revision.className = solicitud.requiere_revision ? "cfdi-review" : "cfdi-review cfdi-review-ok";
    revision.textContent = solicitud.requiere_revision ? "Requiere revisión" : "Sin alertas";
    celdaRevision.append(revision);
    fila.append(celdaRevision);

    const celdaAccion = document.createElement("td");
    const boton = document.createElement("button");
    boton.className = "cfdi-detail-button";
    boton.type = "button";
    boton.dataset.solicitudId = String(solicitud.id_solicitud_cfdi);
    boton.textContent = "Ver datos fiscales";
    celdaAccion.append(boton);
    fila.append(celdaAccion);

    cuerpoTabla.append(fila);
  }
}

function actualizarDetalle(detalle) {
  const setText = (id, value) => {
    const elemento = document.getElementById(id);
    if (elemento) elemento.textContent = value || "No disponible";
  };
  const donativo = detalle.donativo;
  const fiscal = detalle.datos_fiscales;

  setText("solicitud-cfdi-detalle-estado", `Estado: ${detalle.estado}`);
  setText("detalle-cfdi-donante", donativo?.donante);
  setText("detalle-cfdi-donativo", donativo?.id_donativo ? String(donativo.id_donativo) : null);
  setText(
    "detalle-cfdi-monto",
    donativo?.monto === null || donativo?.monto === undefined
      ? null
      : new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" }).format(
          donativo.monto,
        ),
  );
  setText("detalle-cfdi-fecha-donativo", formatearFecha(donativo?.fecha));
  setText("detalle-cfdi-estado-donativo", donativo?.estado);
  setText("detalle-cfdi-rfc", fiscal?.rfc);
  setText("detalle-cfdi-razon-social", fiscal?.razon_social);
  setText("detalle-cfdi-regimen", fiscal?.regimen_fiscal);
  setText("detalle-cfdi-codigo-postal", fiscal?.codigo_postal);
  setText("detalle-cfdi-correo", fiscal?.correo_facturacion);

  if (avisoDetalle) {
    avisoDetalle.hidden = detalle.listo_para_emitir === true;
    avisoDetalle.textContent = detalle.listo_para_emitir
      ? ""
      : `Requiere revisión: ${(detalle.motivos_revision ?? []).join(" ") || "la información está incompleta."}`;
  }

  const enlaceConstancia = document.getElementById("detalle-cfdi-constancia");
  const constanciaFaltante = document.getElementById("detalle-cfdi-constancia-faltante");
  const urlConstancia = fiscal?.constancia_url;
  if (enlaceConstancia && constanciaFaltante) {
    enlaceConstancia.hidden = !urlConstancia;
    constanciaFaltante.hidden = Boolean(urlConstancia);
    if (urlConstancia) enlaceConstancia.href = urlConstancia;
    else enlaceConstancia.removeAttribute("href");
  }
}

function mostrarError(error) {
  if (error.message === "NO_AUTENTICADO") {
    return "Tu sesión administrativa expiró. Inicia sesión para consultar las solicitudes.";
  }
  if (error.message === "SIN_PERMISOS") {
    return "Tu cuenta no tiene permisos para consultar solicitudes CFDI.";
  }
  return "No fue posible cargar las solicitudes. Intenta actualizar la página.";
}

async function cargarSolicitudes() {
  if (!cuerpoTabla) return;
  mostrarMensaje("Cargando solicitudes CFDI pendientes...");
  if (contador) contador.textContent = "Cargando solicitudes...";

  try {
    const solicitudes = await obtenerSolicitudesCfdiPendientes();
    renderizarSolicitudes(solicitudes);
    if (contador) {
      contador.textContent = `${solicitudes.length} solicitud${solicitudes.length === 1 ? "" : "es"} pendiente${solicitudes.length === 1 ? "" : "s"}`;
    }
  } catch (error) {
    console.error("Error al cargar solicitudes CFDI:", error);
    mostrarMensaje(mostrarError(error));
    if (contador) contador.textContent = "No se pudieron cargar las solicitudes.";
  }
}

cuerpoTabla?.addEventListener("click", async (evento) => {
  const boton = evento.target.closest("[data-solicitud-id]");
  if (!boton) return;

  boton.disabled = true;
  try {
    const detalle = await obtenerDetalleSolicitudCfdi(boton.dataset.solicitudId);
    actualizarDetalle(detalle);
    dialogo?.showModal();
  } catch (error) {
    console.error("Error al cargar detalle de solicitud CFDI:", error);
    window.alert(mostrarError(error));
  } finally {
    boton.disabled = false;
  }
});

document.getElementById("solicitudes-cfdi-recargar")?.addEventListener("click", () => {
  void cargarSolicitudes();
});

document.getElementById("solicitud-cfdi-cerrar")?.addEventListener("click", () => {
  dialogo?.close();
});

document.getElementById("solicitud-cfdi-cerrar-pie")?.addEventListener("click", () => {
  dialogo?.close();
});

document.addEventListener("DOMContentLoaded", () => {
  const titulo = document.querySelector("admin-topbar .topbar-title");
  if (titulo) titulo.textContent = "Solicitudes CFDI";
});

void cargarSolicitudes();
