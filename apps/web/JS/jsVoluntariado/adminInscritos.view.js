import { obtenerInscritos } from "./adminInscritos.js";

// Referencias a elementos del DOM
const $ = (id) => document.getElementById(id);
const el = {
  nombre: $("programa-nombre"),
  tipo: $("programa-tipo"),
  estado: $("programa-estado"),
  aviso: $("aviso-estado"),
  resumen: $("resumen-programa"),
  fecha: $("programa-fecha"),
  ubicacion: $("programa-ubicacion"),
  cupo: $("programa-cupo"),
  tabla: $("tabla-inscritos"),
  tablaBody: $("tabla-inscritos-body"),
  error: $("estado-error"),
  mensajeError: $("mensaje-error"),
  btnReintentar: $("btn-reintentar"),
};

// El programa llega en la URL adminInscritos.html?id=<id_programa>
const idPrograma = new URLSearchParams(window.location.search).get("id");

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

function pintarPrograma(programa, total) {
  const estado = (programa.estado ?? "").toLowerCase();

  el.nombre.textContent = programa.nombre;
  el.tipo.textContent = programa.tipo ?? "";

  el.estado.textContent = programa.estado ?? "";
  el.estado.className = `badge-estado badge-${estado}`;
  el.estado.hidden = !programa.estado;

  el.aviso.textContent = AVISOS[estado] ?? "";
  el.aviso.hidden = !AVISOS[estado];

  el.fecha.textContent = textoHorario(programa.fechaInicio, programa.fechaFin);
  el.ubicacion.textContent = programa.ubicacion ?? "Sin ubicación";
  el.cupo.textContent = programa.cupoMaximo ? `${total} / ${programa.cupoMaximo}` : `${total}`;
  el.resumen.hidden = false;
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

async function cargar() {
  el.error.hidden = true;
  el.resumen.hidden = true;
  el.tabla.hidden = true;
  el.nombre.textContent = "Cargando programa...";

  if (!idPrograma) {
    mostrarError(404);
    return;
  }

  try {
    const { programa, totalInscritos, inscritos } = await obtenerInscritos(idPrograma);
    pintarPrograma(programa, totalInscritos);
    pintarInscritos(inscritos);
  } catch (error) {
    console.error("Error al cargar los inscritos:", error);
    mostrarError(error.status);
  }
}

el.btnReintentar.addEventListener("click", cargar);

// El script es type="module" asi que el DOM ya está listo cuando se ejecuta
void cargar();
