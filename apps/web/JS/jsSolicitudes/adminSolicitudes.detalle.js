import { estadoBadgeClass, estadoLabel, formatSolicitudDate } from "./adminSolicitudes.utils.js";

const DETALLE_URL = "/api/solicitudes/admin/solicitudes";

const TIPOS_DOCUMENTO = {
  identificacion: "Identificación oficial",
  ine: "Identificación oficial",
  comprobante_domicilio: "Comprobante de domicilio",
};

const el = {};
let iniciado = false;
let peticionActual = 0;

// Funciones Helpers

function crear(tag, className, texto) {
  const nodo = document.createElement(tag);
  if (className) nodo.className = className;
  if (texto !== undefined && texto !== null) nodo.textContent = String(texto);
  return nodo;
}

const vacio = (valor) => valor === null || valor === undefined || String(valor).trim() === "";
const respuestaTexto = (valor) => (vacio(valor) ? "Sin respuesta" : String(valor));

function edadDesde(fecha) {
  if (!fecha) return null;
  const nacimiento = new Date(fecha);
  if (Number.isNaN(nacimiento.getTime())) return null;
  const hoy = new Date();
  let edad = hoy.getFullYear() - nacimiento.getFullYear();
  const aunNoCumple =
    hoy.getMonth() < nacimiento.getMonth() ||
    (hoy.getMonth() === nacimiento.getMonth() && hoy.getDate() < nacimiento.getDate());
  if (aunNoCumple) edad -= 1;
  return edad;
}

// convierte las fechas de 1995-03-15T00:00:00 a 15/03/1995
function fechaCorta(fecha) {
  const partes = String(fecha || "")
    .slice(0, 10)
    .split("-");
  return partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : null;
}

function nombreArchivo(url) {
  try {
    return decodeURIComponent(String(url).split("/").pop() || "Documento");
  } catch {
    return "Documento";
  }
}

// las secciones que tiene el pop-up

function seccionPerro(perro) {
  const tarjeta = crear("div", "detalle-perro");

  const foto = crear("img", "detalle-perro-foto");
  foto.src = perro.foto_url || "/favicon.svg";
  foto.alt = perro.nombre ? `Foto de ${perro.nombre}` : "Perro";
  foto.addEventListener("error", () => (foto.src = "/favicon.svg"), { once: true });

  const info = crear("div", "detalle-perro-info");
  const datos = [
    vacio(perro.edad_aprox)
      ? null
      : `${perro.edad_aprox} ${perro.edad_aprox === 1 ? "año" : "años"}`,
    perro.sexo,
    perro.tamano,
    perro.raza,
  ].filter((dato) => !vacio(dato));
  info.append(
    crear("strong", "detalle-perro-nombre", perro.nombre || "Sin nombre"),
    crear("span", "detalle-muted", datos.join(" · ")),
  );

  tarjeta.append(foto, info);
  if (perro.estado) tarjeta.append(crear("span", "detalle-chip", perro.estado));
  return tarjeta;
}

function seccionAdoptante(adoptante) {
  const bloque = crear("section", "detalle-adoptante");

  const izquierda = crear("div");
  const edad = edadDesde(adoptante.fecha_nacimiento);
  const linea1 = [
    edad === null ? null : `${edad} años`,
    fechaCorta(adoptante.fecha_nacimiento),
    `Ocupación: ${respuestaTexto(adoptante.ocupacion)}`,
  ].filter(Boolean);
  const linea2 = [adoptante.correo, adoptante.telefono].filter((dato) => !vacio(dato));
  izquierda.append(
    crear("h3", "detalle-nombre", adoptante.nombre_completo || "Sin nombre"),
    crear("p", "detalle-muted", linea1.join(" · ")),
    crear("p", "detalle-muted", linea2.join(" · ")),
  );

  const derecha = crear("div", "detalle-domicilio");
  derecha.append(
    crear("span", "detalle-etiqueta", "Domicilio"),
    crear("p", "", respuestaTexto(adoptante.domicilio)),
  );

  bloque.append(izquierda, derecha);
  return bloque;
}

function seccionRespuestas(secciones) {
  const bloque = crear("section");
  bloque.append(crear("h3", "detalle-seccion-titulo", "Respuestas del adoptante"));

  secciones.forEach((seccion) => {
    bloque.append(crear("h4", "detalle-subseccion", seccion.titulo));
    const grid = crear("dl", "detalle-respuestas");
    seccion.respuestas.forEach(({ pregunta, respuesta }) => {
      const item = crear("div", "detalle-respuesta");
      item.append(
        crear("dt", "detalle-etiqueta", pregunta),
        crear("dd", vacio(respuesta) ? "detalle-sin-respuesta" : "", respuestaTexto(respuesta)),
      );
      grid.append(item);
    });
    bloque.append(grid);
  });

  return bloque;
}

function seccionDocumentos(documentos) {
  const bloque = crear("section");
  const encabezado = crear("div", "detalle-seccion-encabezado");
  encabezado.append(
    crear("h3", "detalle-seccion-titulo", "Documentos adjuntos"),
    crear(
      "span",
      "detalle-muted",
      `${documentos.length} archivo${documentos.length === 1 ? "" : "s"}`,
    ),
  );
  bloque.append(encabezado);

  if (documentos.length === 0) {
    bloque.append(crear("p", "detalle-sin-respuesta", "El adoptante no ha subido documentos."));
    return bloque;
  }

  const lista = crear("div", "detalle-documentos");
  documentos.forEach((doc) => {
    const item = crear("div", "detalle-documento");
    const icono = crear("span", "material-symbols-rounded detalle-documento-icono", "description");
    const info = crear("div", "detalle-documento-info");
    info.append(
      crear("strong", "", TIPOS_DOCUMENTO[doc.tipo.toLowerCase()] || doc.tipo || "Documento"),
      crear("span", "detalle-muted", nombreArchivo(doc.url)),
    );
    item.append(icono, info);

    if (doc.url) {
      const abrir = crear("a", "detail-button", "Abrir");
      abrir.href = doc.url;
      abrir.target = "_blank";
      abrir.rel = "noopener noreferrer";
      item.append(abrir);
    }
    lista.append(item);
  });
  bloque.append(lista);
  return bloque;
}

// Estados del pop-up

function mostrarMensaje(texto, accion) {
  el.contenido.replaceChildren();
  const mensaje = crear("div", "detalle-mensaje");
  mensaje.append(crear("p", "", texto));
  if (accion) {
    const boton = crear("button", "detail-button", accion.label);
    boton.type = "button";
    boton.addEventListener("click", accion.handler);
    mensaje.append(boton);
  }
  el.contenido.append(mensaje);
}

function pintarDetalle(detalle) {
  el.titulo.textContent = `Solicitud ${detalle.folio}`;
  el.subtitulo.textContent = `Recibida el ${formatSolicitudDate(detalle.fecha)}`;
  el.estado.className = `badge-estado ${estadoBadgeClass(detalle.estado)}`;
  el.estado.textContent = estadoLabel(detalle.estado);

  const expediente = crear("div", "detalle-expediente");
  expediente.append(
    seccionPerro(detalle.perro),
    seccionAdoptante(detalle.adoptante),
    seccionRespuestas(detalle.secciones || []),
    seccionDocumentos(detalle.documentos || []),
    crear(
      "p",
      "detalle-nota",
      "El adoptante aceptó los requisitos de adopción y el compromiso de cuidado y seguimiento.",
    ),
  );
  el.contenido.replaceChildren(expediente);
}

async function cargar(id) {
  const peticion = ++peticionActual;
  el.titulo.textContent = "Solicitud";
  el.subtitulo.textContent = "";
  el.estado.className = "badge-estado";
  el.estado.textContent = "";
  mostrarMensaje("Cargando detalle...");

  try {
    const respuesta = await fetch(`${DETALLE_URL}/${encodeURIComponent(id)}`);
    const payload = await respuesta.json().catch(() => ({}));
    if (peticion !== peticionActual) return; // el admin ya abrio otra

    if (!respuesta.ok) {
      const mensaje =
        respuesta.status === 404
          ? "No se encontró la solicitud."
          : payload.error || "No pudimos cargar el detalle.";
      mostrarMensaje(mensaje, { label: "Reintentar", handler: () => cargar(id) });
      return;
    }

    pintarDetalle(payload.data);
  } catch (error) {
    if (peticion !== peticionActual) return;
    console.error("Error al cargar el detalle:", error);
    mostrarMensaje("No pudimos cargar el detalle.", {
      label: "Reintentar",
      handler: () => cargar(id),
    });
  }
}

function iniciar() {
  if (iniciado) return;
  el.modal = document.getElementById("modal-detalle");
  el.titulo = document.getElementById("detalle-titulo");
  el.subtitulo = document.getElementById("detalle-subtitulo");
  el.estado = document.getElementById("detalle-estado");
  el.contenido = document.getElementById("detalle-contenido");

  document.getElementById("detalle-cerrar").addEventListener("click", () => el.modal.close());
  // Clic fuera de la tarjeta tambien cierra el pop up (como en Maree)
  el.modal.addEventListener("click", (evento) => {
    if (evento.target === el.modal) el.modal.close();
  });
  iniciado = true;
}

// loo que usa la tabla
export function abrirDetalle(id) {
  iniciar();
  el.modal.showModal();
  el.contenido.scrollTop = 0;
  void cargar(id);
}
