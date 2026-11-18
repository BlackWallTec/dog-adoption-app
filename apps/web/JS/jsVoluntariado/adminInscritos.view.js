import { obtenerInscritos } from "./adminInscritos.js";
import { obtenerProgramaAdmin, cancelarProgramaAdmin } from "./programaAdmin.api.js";
import { rutaDe } from "../rutas.js";

// Referencias a elementos del DOM
const $ = (id) => document.getElementById(id);
const el = {
  nombre: $("programa-nombre"),
  tipo: $("programa-tipo"),
  estado: $("programa-estado"),
  aviso: $("aviso-estado"),
  avisoExito: $("aviso-exito"),
  resumen: $("resumen-programa"),
  fecha: $("programa-fecha"),
  ubicacion: $("programa-ubicacion"),
  cupo: $("programa-cupo"),
  tabla: $("tabla-inscritos"),
  tablaBody: $("tabla-inscritos-body"),
  error: $("estado-error"),
  mensajeError: $("mensaje-error"),
  btnReintentar: $("btn-reintentar"),
  // HU38
  btnEditar: $("btn-editar"),
  btnCancelar: $("btn-cancelar"),
  dialogo: $("dialogo-cancelar"),
  dialogoTexto: $("dialogo-texto"),
  motivo: $("motivo-cancelacion"),
  contador: $("contador-motivo"),
  errorCancelar: $("error-cancelar"),
  btnVolverDialogo: $("btn-volver-dialogo"),
  btnConfirmarCancelar: $("btn-confirmar-cancelar"),
};

// El programa llega en la URL adminInscritos.html?id=<id_programa>
const parametros = new URLSearchParams(window.location.search);
const idPrograma = parametros.get("id");

const MAX_MOTIVO = 200;
const FECHA_HORA = { dateStyle: "medium", timeStyle: "short" };

const AVISOS = {
  cancelado: "Programa cancelado. Usa esta lista para avisar a los inscritos.",
  finalizado: "Programa finalizado. La lista se muestra como histórico.",
};

const ASISTENCIA = {
  true: '<span class="asistencia-si">Asistió</span>',
  false: '<span class="asistencia-no">No asistió</span>',
  null: '<span class="sin-dato">Sin pase de lista</span>',
};

const MENSAJES_ERROR = {
  401: "Tu sesión expiró. Vuelve a iniciar sesión.",
  403: "No tienes permiso para ver esta información.",
  404: "No encontramos este programa.",
};

// Inscritos activos del programa, para el texto del dialogo de cancelacion
let inscritosActivos = 0;

// Los nombres y correos los escriben los usuarios, se escapan antes de usar innerHTML
function escapar(texto) {
  return String(texto)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

// Las fechas vienen sin zona horaria y se muestran tal como estan guardadas
function formatearFecha(valor, opciones) {
  if (!valor) return "Sin fecha";
  return new Date(valor).toLocaleString("es-MX", opciones);
}

function textoHorario(inicio, fin) {
  const mismoDia = inicio?.slice(0, 10) === fin?.slice(0, 10);
  if (mismoDia) {
    const dia = formatearFecha(inicio, { dateStyle: "long" });
    const horaInicio = formatearFecha(inicio, { timeStyle: "short" });
    const horaFin = formatearFecha(fin, { timeStyle: "short" });
    return `${dia}, ${horaInicio} – ${horaFin}`;
  }
  return `${formatearFecha(inicio, FECHA_HORA)} – ${formatearFecha(fin, FECHA_HORA)}`;
}

function celda(valor) {
  return valor ? escapar(valor) : '<span class="sin-dato">No registrado</span>';
}

function textoAviso(estado, detalle) {
  const aviso = AVISOS[estado];
  if (estado === "cancelado" && detalle?.motivoCancelacion) {
    return `${aviso} Motivo: ${detalle.motivoCancelacion}`;
  }
  return aviso ?? "";
}

function pintarPrograma(programa, total, detalle) {
  const estado = (programa.estado ?? "").toLowerCase();

  el.nombre.textContent = programa.nombre;
  el.tipo.textContent = programa.tipo ?? "";

  el.estado.textContent = programa.estado ?? "";
  el.estado.className = `badge-estado badge-${estado}`;
  el.estado.hidden = !programa.estado;

  // textContent: el motivo lo escribe el Admin y no se interpreta como HTML
  el.aviso.textContent = textoAviso(estado, detalle);
  el.aviso.hidden = !AVISOS[estado];

  el.fecha.textContent = textoHorario(programa.fechaInicio, programa.fechaFin);
  el.ubicacion.textContent = programa.ubicacion ?? "Sin ubicación";
  el.cupo.textContent = programa.cupoMaximo ? `${total} / ${programa.cupoMaximo}` : `${total}`;
  el.resumen.hidden = false;
}

// Editar y cancelar solo aparecen si el programa no esta cancelado ni finalizado
function pintarAcciones(detalle) {
  const editable = Boolean(detalle?.editable);
  inscritosActivos = detalle?.inscritosActivos ?? 0;

  el.btnEditar.href = `${rutaDe("nuevaJornada")}?id=${encodeURIComponent(idPrograma)}`;
  el.btnEditar.hidden = !editable;
  el.btnCancelar.hidden = !editable;
}

function pintarInscritos(inscritos) {
  if (inscritos.length === 0) {
    el.tablaBody.innerHTML =
      '<tr><td colspan="6" class="fila-vacia">Aún no hay voluntarios inscritos.</td></tr>';
  } else {
    el.tablaBody.innerHTML = inscritos
      .map(
        (inscrito, indice) => `
        <tr>
          <td>${indice + 1}</td>
          <td>${celda(inscrito.nombreCompleto)}</td>
          <td>${celda(inscrito.correo)}</td>
          <td>${celda(inscrito.telefono)}</td>
          <td>${escapar(formatearFecha(inscrito.fechaInscripcion, FECHA_HORA))}</td>
          <td>${ASISTENCIA[String(inscrito.asistencia)]}</td>
        </tr>`,
      )
      .join("");
  }
  el.tabla.hidden = false;
}

function mostrarError(status) {
  el.nombre.textContent = "Inscritos";
  el.mensajeError.textContent =
    MENSAJES_ERROR[status] ?? "No se pudo cargar la lista de inscritos.";
  // Reintentar solo tiene sentido si fallo la conexion o el servidor.
  el.btnReintentar.hidden = Boolean(MENSAJES_ERROR[status]);
  el.error.hidden = false;
}

function mostrarExito(texto) {
  el.avisoExito.textContent = texto;
  el.avisoExito.hidden = false;
}

async function cargar() {
  el.error.hidden = true;
  el.resumen.hidden = true;
  el.tabla.hidden = true;
  el.btnEditar.hidden = true;
  el.btnCancelar.hidden = true;
  el.nombre.textContent = "Cargando programa...";

  if (!idPrograma) {
    mostrarError(404);
    return;
  }

  try {
    const [{ programa, totalInscritos, inscritos }, detalle] = await Promise.all([
      obtenerInscritos(idPrograma),
      obtenerProgramaAdmin(idPrograma),
    ]);
    pintarPrograma(programa, totalInscritos, detalle);
    pintarAcciones(detalle);
    pintarInscritos(inscritos);
  } catch (error) {
    console.error("Error al cargar los inscritos:", error);
    mostrarError(error.status);
  }
}

// ==================== Mensaje al volver de la edicion ====================

function mostrarMensajeGuardado() {
  if (parametros.get("guardado") !== "1") return;

  const avisar = Number(parametros.get("avisar") ?? 0);

  let texto = "Cambios guardados.";
  if (avisar === 1) {
    texto = "Cambios guardados. Hay 1 persona inscrita, avísale del cambio con esta lista.";
  } else if (avisar > 1) {
    texto = `Cambios guardados. Hay ${avisar} personas inscritas, avísales del cambio con esta lista.`;
  }
  mostrarExito(texto);

  // Se limpia la URL para que el mensaje no vuelva a salir al recargar
  window.history.replaceState(null, "", `?id=${encodeURIComponent(idPrograma)}`);
}

// ==================== Dialogo de cancelacion ====================

function textoDialogo() {
  if (inscritosActivos === 0) return "Este programa no tiene inscritos.";
  if (inscritosActivos === 1) {
    return "Hay 1 persona inscrita. Después de cancelar, avísale con esta lista.";
  }
  return `Hay ${inscritosActivos} personas inscritas. Después de cancelar, avísales con esta lista.`;
}

function actualizarContador() {
  el.contador.textContent = `${el.motivo.value.length}/${MAX_MOTIVO}`;
}

function mostrarErrorCancelar(texto) {
  el.errorCancelar.textContent = texto;
  el.errorCancelar.hidden = !texto;
}

function bloquearDialogo(bloqueado) {
  el.btnConfirmarCancelar.disabled = bloqueado;
  el.btnVolverDialogo.disabled = bloqueado;
  el.btnConfirmarCancelar.textContent = bloqueado ? "Cancelando..." : "Cancelar programa";
}

function abrirDialogo() {
  el.dialogoTexto.textContent = textoDialogo();
  el.motivo.value = "";
  actualizarContador();
  mostrarErrorCancelar("");
  bloquearDialogo(false);
  el.dialogo.showModal();
}

async function confirmarCancelacion() {
  const motivo = el.motivo.value.trim();

  if (motivo.length > MAX_MOTIVO) {
    mostrarErrorCancelar(`El motivo no puede tener más de ${MAX_MOTIVO} caracteres.`);
    return;
  }

  bloquearDialogo(true);
  mostrarErrorCancelar("");

  try {
    const resultado = await cancelarProgramaAdmin(idPrograma, motivo || null);
    el.dialogo.close();
    await cargar();

    const total = resultado.inscritosActivos;
    mostrarExito(
      total > 0
        ? `Programa cancelado. Avisa a ${total === 1 ? "la persona inscrita" : `las ${total} personas inscritas`} con esta lista.`
        : "Programa cancelado.",
    );
  } catch (error) {
    console.error("Error al cancelar el programa:", error);

    if (error.status === 409) {
      el.dialogo.close();
      await cargar();
      mostrarExito("Este programa ya no se puede cancelar.");
      return;
    }

    if (error.status === 401 || error.status === 403) {
      el.dialogo.close();
      mostrarError(error.status);
      return;
    }

    mostrarErrorCancelar(error.message ?? "No se pudo cancelar el programa. Intenta de nuevo.");
    bloquearDialogo(false);
  }
}

// ==================== Eventos ====================

el.btnReintentar.addEventListener("click", cargar);
el.btnCancelar.addEventListener("click", abrirDialogo);
el.btnVolverDialogo.addEventListener("click", () => el.dialogo.close());
el.motivo.addEventListener("input", actualizarContador);
el.btnConfirmarCancelar.addEventListener("click", () => {
  void confirmarCancelacion();
});

// El script es type="module" asi que el DOM ya esta listo cuando se ejecuta
mostrarMensajeGuardado();
void cargar();
