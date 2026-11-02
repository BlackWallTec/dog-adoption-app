const ESTADO_LABELS = {
  pendientes: "Pendientes",
  por_revisar: "Por revisar",
  en_revision: "En revisión",
  aprobada: "Aprobada",
  rechazada: "Rechazada",
};

export function buildQueryString({ perro = "todos", estado = "todos", fecha = "todas", page = 1 }) {
  const params = new URLSearchParams();
  if (perro !== "todos") params.set("perro", perro);
  if (estado !== "todos") params.set("estado", estado);
  if (fecha !== "todas") params.set("fecha", fecha);
  params.set("page", String(page));
  return params.toString();
}

export function formatSolicitudDate(value) {
  if (!value) return "Sin fecha";
  const dateOnly = /^\d{4}-\d{2}-\d{2}$/.test(String(value));
  const date = dateOnly ? new Date(`${value}T12:00:00-06:00`) : new Date(value);
  if (Number.isNaN(date.getTime())) return "Sin fecha";
  const parts = new Intl.DateTimeFormat("es-MX", {
    timeZone: "America/Mexico_City",
    day: "numeric",
    month: "short",
    year: "numeric",
  }).formatToParts(date);
  const values = Object.fromEntries(parts.map(({ type, value: part }) => [type, part]));
  const month = {
    ene: "ene.",
    feb: "feb.",
    mar: "mar.",
    abr: "abr.",
    may: "may.",
    jun: "jun.",
    jul: "jul.",
    ago: "ago.",
    sep: "sep.",
    oct: "oct.",
    nov: "nov.",
    dic: "dic.",
  };
  return `${values.day} ${month[values.month] || values.month} ${values.year}`;
}

export function estadoLabel(estado) {
  return ESTADO_LABELS[estado] || String(estado || "Sin estado");
}

export function estadoBadgeClass(estado) {
  if (estado === "aprobada") return "badge-aprobada";
  if (estado === "rechazada") return "badge-rechazada";
  return "badge-pendiente";
}

export function hasActiveFilters({ perro, estado, fecha }) {
  return perro !== "todos" || estado !== "todos" || fecha !== "todas";
}

export function pluralizeSolicitud(count) {
  return `${count} solicitud${count === 1 ? "" : "es"}`;
}

export { ESTADO_LABELS };
