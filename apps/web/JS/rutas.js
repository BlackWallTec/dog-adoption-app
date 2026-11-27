// Rutas públicas del panel. La interfaz navega con `ruta`; `archivo` solo lo resuelve el servidor.

export const PANTALLAS = [
  {
    id: "inicio",
    etiqueta: "Inicio",
    icono: "grid_view",
    ruta: "/admin/inicio",
    archivo: "/HTML/htmlInicio/adminInicio.html",
  },
  {
    id: "perros",
    etiqueta: "Perros",
    icono: "pets",
    ruta: "/admin/perros",
    archivo: "/HTML/htmlCatalogo/adminPerros.html",
  },
  {
    id: "solicitudes",
    etiqueta: "Solicitudes",
    icono: "assignment",
    ruta: "/admin/solicitudes",
    archivo: "/HTML/htmlSolicitudes/adminSolicitudes.html",
  },
  {
    id: "voluntariado",
    etiqueta: "Voluntariado",
    icono: "volunteer_activism",
    ruta: "/admin/voluntariado",
    archivo: "/HTML/htmlVoluntariado/adminCalendario.html",
  },
  {
    id: "nuevaJornada",
    grupo: "voluntariado",
    titulo: "Voluntariado",
    ruta: "/admin/voluntariado/nueva",
    archivo: "/HTML/htmlVoluntariado/createVolunteering.html",
  },
  {
    id: "inscritos",
    grupo: "voluntariado",
    titulo: "Voluntariado",
    ruta: "/admin/voluntariado/inscritos",
    archivo: "/HTML/htmlVoluntariado/adminInscritos.html",
  },
  {
    id: "donativos",
    etiqueta: "Donativos",
    icono: "favorite",
    ruta: "/admin/donativos",
    archivo: "/HTML/htmlDonativos/adminDonativos.html",
  },
  {
    id: "adminSolicitudesCfdi",
    grupo: "donativos",
    titulo: "Donativos",
    ruta: "/admin/donativos/solicitudes-cfdi",
    archivo: "/HTML/htmlDonativos/adminSolicitudesCfdi.html",
  },
  {
    id: "resumenDonativos",
    titulo: "Donativos",
    ruta: "/donativos/resumen",
    archivo: "/HTML/htmlDonativos/resumenDonativos.html",
  },
  {
    id: "configuracion",
    etiqueta: "Configuración",
    icono: "settings",
    ruta: "/admin/configuracion",
    archivo: "/HTML/htmlPerfiles/adminPerfiles.html",
  },
  {
    id: "login",
    ruta: "/admin/login",
    archivo: "/HTML/htmlPerfiles/login.html",
  },
];

const porId = new Map(PANTALLAS.map((pantalla) => [pantalla.id, pantalla]));

export function normalizarRuta(pathname = "/") {
  const sinFragmento = String(pathname).split("#")[0];
  const path = sinFragmento.split("?")[0];
  if (path.length > 1 && path.endsWith("/")) return path.slice(0, -1);
  return path || "/";
}

export function rutaDe(id) {
  const pantalla = porId.get(id);
  if (!pantalla) throw new Error(`Ruta desconocida: ${id}`);
  return pantalla.ruta;
}

export function pantallaPorUbicacion(pathname) {
  const actual = normalizarRuta(pathname);
  return (
    PANTALLAS.find((pantalla) => actual === pantalla.ruta || actual === pantalla.archivo) ?? null
  );
}

export function idMenuActivo(pathname) {
  const pantalla = pantallaPorUbicacion(pathname);
  if (!pantalla) return "";
  return pantalla.grupo || pantalla.id;
}

export function tituloDe(pathname) {
  const pantalla = pantallaPorUbicacion(pathname);
  return pantalla?.titulo || pantalla?.etiqueta || "Panel";
}

export function archivoDeRuta(url = "") {
  const sinFragmento = String(url).split("#")[0];
  const corte = sinFragmento.indexOf("?");
  const path = corte === -1 ? sinFragmento : sinFragmento.slice(0, corte);
  const query = corte === -1 ? "" : sinFragmento.slice(corte);
  const pantalla = PANTALLAS.find((item) => item.archivo && item.ruta === normalizarRuta(path));
  if (!pantalla) return null;
  return `${pantalla.archivo}${query}`;
}
