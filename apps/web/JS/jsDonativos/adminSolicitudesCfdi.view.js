import {
  cargarArchivosCfdi,
  obtenerDetalleSolicitudCfdi,
  obtenerSolicitudesCfdiPendientes,
  reintentarCorreoCfdi,
  validarArchivosCfdi,
} from "./adminSolicitudesCfdi.js";

const cuerpoTabla = document.getElementById("solicitudes-cfdi-body");
const contador = document.getElementById("solicitudes-cfdi-contador");
const dialogo = document.getElementById("solicitud-cfdi-detalle");
const avisoDetalle = document.getElementById("solicitud-cfdi-aviso");
const formCargaCfdi = document.getElementById("cfdi-upload-form");
const inputPdf = document.getElementById("cfdi-pdf");
const inputXml = document.getElementById("cfdi-xml");
const previewCfdi = document.getElementById("cfdi-preview");
const mensajeCarga = document.getElementById("cfdi-carga-mensaje");
const botonValidar = document.getElementById("cfdi-validar");
const botonGuardar = document.getElementById("cfdi-confirmar-carga");
const confirmacionPdf = document.getElementById("cfdi-confirmar-pdf");
const seccionCarga = document.getElementById("cfdi-upload-section");
const seccionEntrega = document.getElementById("cfdi-delivery-section");
const estadoCorreo = document.getElementById("cfdi-correo-estado");
const botonReintentar = document.getElementById("cfdi-reintentar-correo");
const visorPdf = document.getElementById("cfdi-pdf-preview");
let solicitudSeleccionada = null;
let pdfPreviewUrl = null;
let validacionLista = false;

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

  solicitudSeleccionada = detalle.id_solicitud_cfdi;
  formCargaCfdi?.reset();
  const archivosCargados = detalle.cfdi?.archivos_cargados === true;
  if (seccionCarga) {
    seccionCarga.hidden = archivosCargados || detalle.listo_para_emitir !== true;
  }
  if (seccionEntrega) seccionEntrega.hidden = !archivosCargados;
  if (estadoCorreo) {
    const estado = detalle.cfdi?.correo_estado;
    estadoCorreo.textContent =
      estado === "Enviado"
        ? "El CFDI está guardado y se envió al correo de facturación."
        : estado === "Error"
          ? detalle.cfdi?.correo_error ||
            "El CFDI está guardado, pero no se pudo enviar el correo. Puedes reintentarlo."
          : "El CFDI está guardado y el envío por correo está pendiente.";
    estadoCorreo.dataset.error = String(estado === "Error");
  }
  if (botonReintentar) {
    botonReintentar.hidden = !["Error", "Pendiente de envío"].includes(detalle.cfdi?.correo_estado);
  }
  limpiarValidacionCfdi();
}

function limpiarValidacionCfdi() {
  validacionLista = false;
  if (previewCfdi) previewCfdi.hidden = true;
  if (confirmacionPdf) confirmacionPdf.checked = false;
  if (botonGuardar) botonGuardar.disabled = true;
  if (mensajeCarga) {
    mensajeCarga.textContent = "";
    mensajeCarga.dataset.error = "";
  }
  if (visorPdf) visorPdf.removeAttribute("src");
  if (pdfPreviewUrl) URL.revokeObjectURL(pdfPreviewUrl);
  pdfPreviewUrl = null;
}

function obtenerFormData(confirmarPdf = false) {
  const formData = new FormData();
  if (inputPdf?.files?.[0]) formData.append("pdf", inputPdf.files[0]);
  if (inputXml?.files?.[0]) formData.append("xml", inputXml.files[0]);
  if (confirmarPdf) formData.append("confirmar_pdf", "true");
  return formData;
}

function mostrarMensajeCarga(mensaje, esError = false) {
  if (!mensajeCarga) return;
  mensajeCarga.textContent = mensaje;
  mensajeCarga.dataset.error = String(esError);
}

function formatearMonto(monto) {
  return new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" }).format(monto);
}

botonValidar?.addEventListener("click", async () => {
  if (!solicitudSeleccionada || !inputPdf?.files?.[0] || !inputXml?.files?.[0]) {
    mostrarMensajeCarga("Selecciona el PDF y el XML antes de validar.", true);
    return;
  }

  botonValidar.disabled = true;
  limpiarValidacionCfdi();
  mostrarMensajeCarga("Validando archivos y comparando el XML con el donativo...");
  try {
    const resultado = await validarArchivosCfdi(solicitudSeleccionada, obtenerFormData());
    document.getElementById("cfdi-preview-uuid").textContent = resultado.uuid;
    document.getElementById("cfdi-preview-rfc").textContent = resultado.rfc_receptor;
    document.getElementById("cfdi-preview-total").textContent = formatearMonto(resultado.monto);
    pdfPreviewUrl = URL.createObjectURL(inputPdf.files[0]);
    if (visorPdf) visorPdf.src = pdfPreviewUrl;
    if (previewCfdi) previewCfdi.hidden = false;
    validacionLista = true;
    mostrarMensajeCarga(
      `El XML coincide con el RFC y el monto del donativo (${formatearMonto(resultado.donativo.monto)}). Revisa el PDF antes de confirmar.`,
    );
  } catch (error) {
    mostrarMensajeCarga(error.message, true);
  } finally {
    botonValidar.disabled = false;
  }
});

for (const input of [inputPdf, inputXml]) {
  input?.addEventListener("change", limpiarValidacionCfdi);
}

confirmacionPdf?.addEventListener("change", () => {
  if (botonGuardar) botonGuardar.disabled = !validacionLista || !confirmacionPdf.checked;
});

formCargaCfdi?.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  if (!solicitudSeleccionada || !validacionLista || !confirmacionPdf?.checked) return;

  if (botonGuardar) botonGuardar.disabled = true;
  if (botonValidar) botonValidar.disabled = true;
  mostrarMensajeCarga("Guardando los archivos y enviando el CFDI...");
  try {
    const resultado = await cargarArchivosCfdi(solicitudSeleccionada, obtenerFormData(true));
    const archivosCargados = document.getElementById("cfdi-upload-section");
    if (archivosCargados) archivosCargados.hidden = true;
    if (seccionEntrega) seccionEntrega.hidden = false;
    if (estadoCorreo) {
      const enviado = resultado.correo.estado === "Enviado";
      estadoCorreo.textContent = enviado
        ? "El CFDI quedó guardado y se envió al correo de facturación."
        : resultado.correo.mensaje;
      estadoCorreo.dataset.error = String(!enviado);
    }
    if (botonReintentar) botonReintentar.hidden = resultado.correo.estado !== "Error";
    mostrarMensajeCarga("Los archivos se guardaron correctamente.");
    await cargarSolicitudes();
  } catch (error) {
    let detalleActualizado = null;
    try {
      detalleActualizado = await obtenerDetalleSolicitudCfdi(solicitudSeleccionada);
      actualizarDetalle(detalleActualizado);
    } catch (errorActualizacion) {
      console.error("No se pudo actualizar el estado de la carga CFDI:", errorActualizacion);
    }

    if (detalleActualizado?.cfdi?.archivos_cargados) {
      if (estadoCorreo) {
        estadoCorreo.textContent =
          "Los archivos quedaron guardados. Revisa el estado del correo y reinténtalo si sigue pendiente.";
        estadoCorreo.dataset.error = "true";
      }
      if (botonReintentar) {
        botonReintentar.hidden = !["Error", "Pendiente de envío"].includes(
          detalleActualizado.cfdi.correo_estado,
        );
      }
      if (seccionEntrega) seccionEntrega.hidden = false;
      if (seccionCarga) seccionCarga.hidden = true;
    } else {
      mostrarMensajeCarga(error.message, true);
      if (botonGuardar) botonGuardar.disabled = !confirmacionPdf.checked;
    }
  } finally {
    if (botonValidar) botonValidar.disabled = false;
  }
});

botonReintentar?.addEventListener("click", async () => {
  if (!solicitudSeleccionada) return;
  botonReintentar.disabled = true;
  if (estadoCorreo) {
    estadoCorreo.textContent = "Reintentando envío...";
    estadoCorreo.dataset.error = "false";
  }
  try {
    const resultado = await reintentarCorreoCfdi(solicitudSeleccionada);
    const enviado = resultado.correo.estado === "Enviado";
    if (estadoCorreo) {
      estadoCorreo.textContent = enviado
        ? "El CFDI se envió al correo de facturación."
        : resultado.correo.mensaje;
      estadoCorreo.dataset.error = String(!enviado);
    }
    botonReintentar.hidden = enviado;
    await cargarSolicitudes();
  } catch (error) {
    if (estadoCorreo) {
      estadoCorreo.textContent = error.message;
      estadoCorreo.dataset.error = "true";
    }
  } finally {
    botonReintentar.disabled = false;
  }
});

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
