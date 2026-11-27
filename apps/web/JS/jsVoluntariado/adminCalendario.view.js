import { Calendar } from "fullcalendar";
import dayGridPlugin from "fullcalendar/daygrid";
import interactionPlugin from "fullcalendar/interaction";
import classicThemePlugin from "fullcalendar/themes/classic";
import esLocale from "fullcalendar/locales/es";
import "fullcalendar/skeleton.css";
import "fullcalendar/themes/classic/theme.css";
import "fullcalendar/themes/classic/palette.css";

import { obtenerCalendario } from "./adminCalendario.api.js";
import { rutaDe } from "../rutas.js";

// Calendario de programas HU30. Solo dibuja lo que calcula el servidor
const ZONA_HORARIA = "America/Mexico_City";
const MS_AVISO = 4000;

// Referencias a elementos del DOM
const $ = (id) => document.getElementById(id);
const el = {
  calendario: $("calendario"),
  tarjeta: document.querySelector(".calendario-card"),
  avisoFecha: $("aviso-fecha"),
  avisoVacio: $("aviso-vacio"),
  error: $("estado-error"),
  mensajeError: $("mensaje-error"),
  btnReintentar: $("btn-reintentar"),
  menu: $("menu-programa"),
  menuTitulo: $("menu-titulo"),
  menuDetalles: $("menu-detalles"),
};

const MENSAJES_ERROR = {
  0: "No se pudo conectar con el servidor.",
  400: "El periodo solicitado no es válido.",
  401: "Tu sesión expiró. Vuelve a iniciar sesión.",
  403: "No tienes permiso para ver esta información.",
};
const MENSAJE_ERROR_GENERAL = "No pudimos cargar el calendario. Intenta de nuevo.";

// Colores (variables de adminCalendario.css) y etiqueta de cada estado
const ESTILO_ESTADO = {
  Disponible: { color: "var(--cal-disponible)", texto: "var(--cal-disponible-texto)" },
  Lleno: { color: "var(--cal-lleno)", texto: "var(--cal-lleno-texto)", etiqueta: "Cupo lleno" },
  Finalizado: {
    color: "var(--cal-finalizado)",
    texto: "var(--cal-finalizado-texto)",
    etiqueta: "Finalizado",
  },
  Cancelado: {
    color: "var(--cal-cancelado)",
    texto: "var(--cal-cancelado-texto)",
    etiqueta: "Cancelado",
  },
};

let peticionActual = null;
let temporizadorAviso = null;

// ==================== FECHAS ====================

// Hoy en Querétaro como "AAAA-MM-DD", sin importar la zona de la computadora
function hoyEnMexico() {
  return new Intl.DateTimeFormat("sv-SE", { timeZone: ZONA_HORARIA }).format(new Date());
}

// FullCalendar da el fin exclusivo; la API espera "hasta" inclusivo
function diaAnterior(texto) {
  const fecha = new Date(`${texto}T00:00:00Z`);
  fecha.setUTCDate(fecha.getUTCDate() - 1);
  return fecha.toISOString().slice(0, 10);
}

// ==================== CONVERSION A EVENTOS ====================

function aEventoPrograma(programa) {
  const estilo = ESTILO_ESTADO[programa.estado] ?? ESTILO_ESTADO.Disponible;
  return {
    id: programa.idPrograma,
    title: programa.nombre,
    start: programa.inicio,
    end: programa.fin,
    color: estilo.color,
    contrastColor: estilo.texto,
    extendedProps: {
      tipo: programa.tipo,
      inscritos: programa.inscritos,
      cupoMaximo: programa.cupoMaximo,
      estado: programa.estado,
    },
  };
}

// Franja de lunes a lunes siguiente (el fin no se incluye, llega hasta el domingo)
function aFranjaSinCobertura(semana) {
  return {
    start: semana.inicio,
    end: semana.fin,
    allDay: true,
    display: "background",
    title: "Sin cobertura",
    color: "var(--cal-sin-cobertura)",
    className: "franja-sin-cobertura",
  };
}

// ==================== CONTENIDO DE CADA PROGRAMA ====================

function crear(etiqueta, clase, texto) {
  const nodo = document.createElement(etiqueta);
  nodo.className = clase;
  // textContent: nombre y tipo los escribe el Admin y no se interpretan como HTML
  nodo.textContent = texto;
  return nodo;
}

// Nombre · Tipo · 3/10 · horario + badge del estado
function contenidoPrograma(arg) {
  const { tipo, inscritos, cupoMaximo, estado } = arg.event.extendedProps;
  const cupo = cupoMaximo == null ? `${inscritos}` : `${inscritos}/${cupoMaximo}`;
  const detalle = [tipo, cupo, arg.timeText].filter(Boolean).join(" · ");

  const contenedor = crear("div", "evento-contenido", "");
  contenedor.append(crear("span", "evento-nombre", arg.event.title));
  contenedor.append(crear("span", "evento-detalle", detalle));

  const etiqueta = ESTILO_ESTADO[estado]?.etiqueta;
  if (etiqueta) {
    contenedor.append(crear("span", `badge-estado badge-${estado.toLowerCase()}`, etiqueta));
  }

  return { domNodes: [contenedor] };
}

// ==================== ESTADOS DE LA PANTALLA ====================

function mostrarCarga(cargando) {
  el.calendario.setAttribute("aria-busy", String(cargando));
}

// CA-11: sin calendario vacío ni franjas, solo el mensaje y "Reintentar"
function mostrarError(error) {
  el.mensajeError.textContent = MENSAJES_ERROR[error?.status] ?? MENSAJE_ERROR_GENERAL;
  el.error.hidden = false;
  el.tarjeta.hidden = true;
  el.avisoVacio.hidden = true;
}

function ocultarError() {
  el.error.hidden = true;
  el.tarjeta.hidden = false;
}

// CA-09: aviso temporal al elegir un día pasado
function mostrarAvisoFecha() {
  el.avisoFecha.hidden = false;
  clearTimeout(temporizadorAviso);
  temporizadorAviso = setTimeout(() => {
    el.avisoFecha.hidden = true;
  }, MS_AVISO);
}

// ==================== CARGA DE DATOS ====================

// FullCalendar llama esta función cada vez que cambia el mes, la semana o la vista
function cargarEventos(info, exito, falla) {
  // Si el Admin navega rápido, se cancela la petición anterior
  peticionActual?.abort();
  const peticion = new AbortController();
  peticionActual = peticion;

  const desde = info.startStr.slice(0, 10);
  const hasta = diaAnterior(info.endStr.slice(0, 10));

  mostrarCarga(true);

  obtenerCalendario(desde, hasta, { signal: peticion.signal })
    .then((data) => {
      ocultarError();
      el.avisoVacio.hidden = data.programas.length > 0;
      exito([
        ...data.programas.map(aEventoPrograma),
        ...data.semanasSinCobertura.map(aFranjaSinCobertura),
      ]);
    })
    .catch((error) => {
      if (error?.name === "AbortError") return;
      // Detalle técnico a la consola, mensaje entendible a la pantalla
      console.error("[voluntarios] calendario:", error);
      mostrarError(error);
      falla(error);
    })
    .finally(() => {
      if (peticion === peticionActual) mostrarCarga(false);
    });
}

// ==================== MENU DEL PROGRAMA (CA-08) ====================

function abrirMenu(evento, clic) {
  const id = encodeURIComponent(evento.id);
  el.menuTitulo.textContent = evento.title;
  // Los detalles muestran los inscritos y las acciones de editar y cancelar
  el.menuDetalles.href = `${rutaDe("inscritos")}?id=${id}`;

  // Se muestra junto al clic sin salirse de la pantalla
  el.menu.hidden = false;
  const { width, height } = el.menu.getBoundingClientRect();
  el.menu.style.left = `${Math.min(clic.clientX, window.innerWidth - width - 8)}px`;
  el.menu.style.top = `${Math.min(clic.clientY, window.innerHeight - height - 8)}px`;
  el.menuDetalles.focus();
}

function cerrarMenu() {
  el.menu.hidden = true;
}

document.addEventListener("click", (event) => {
  if (el.menu.hidden) return;
  if (el.menu.contains(event.target) || event.target.closest(".evento")) return;
  cerrarMenu();
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") cerrarMenu();
});

document.querySelector(".content-area").addEventListener("scroll", cerrarMenu);

// La ruta sale de rutas.js el href del HTML queda como respaldo
document.getElementById("btn-crear-jornada").href = rutaDe("nuevaJornada");

// ==================== CALENDARIO ====================

const calendario = new Calendar(el.calendario, {
  plugins: [dayGridPlugin, interactionPlugin, classicThemePlugin],
  locale: esLocale,
  timeZone: ZONA_HORARIA,
  firstDay: 1,
  initialView: "dayGridMonth",
  headerToolbar: {
    start: "prev,next today",
    center: "title",
    end: "dayGridMonth,dayGridWeek",
  },
  height: "auto",

  // Todos los programas como barra; "+N más" cuando no caben (CA-10)
  eventDisplay: "block",
  dayMaxEvents: true,
  displayEventEnd: true,
  eventTimeFormat: { hour: "2-digit", minute: "2-digit", hour12: false },

  events: cargarEventos,

  // Clases propias (adminCalendario.css); en la v7 ya no hay clases fc-*
  // Flechas circulares, "Hoy" con contorno y Mes/Semana como selector
  buttonGroupClass: (grupo) => (grupo.hasSelection ? "cal-selector" : "cal-grupo"),
  buttonClass: (boton) =>
    [
      "cal-boton",
      boton.isIconOnly ? "cal-boton-icono" : "cal-boton-texto",
      boton.isSelected && "cal-boton-activo",
    ]
      .filter(Boolean)
      .join(" "),
  toolbarTitleClass: "calendario-titulo",
  eventClass: (arg) => `evento evento-${arg.event.extendedProps.estado.toLowerCase()}`,
  eventContent: contenidoPrograma,
  backgroundEventTitleClass: "franja-etiqueta",

  // CA-07 y CA-09: crear solo de hoy en adelante
  dateClick: (info) => {
    const fecha = info.dateStr.slice(0, 10);
    if (fecha < hoyEnMexico()) {
      mostrarAvisoFecha();
      return;
    }
    window.location.href = `${rutaDe("nuevaJornada")}?fecha=${fecha}`;
  },

  eventClick: (info) => {
    info.jsEvent.preventDefault();
    abrirMenu(info.event, info.jsEvent);
  },

  // Al cambiar de periodo se cierra el menú abierto
  datesSet: cerrarMenu,
});

calendario.render();

el.btnReintentar.addEventListener("click", () => {
  ocultarError();
  calendario.updateSize();
  calendario.refetchEvents();
});
