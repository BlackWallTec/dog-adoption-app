// Galería de fotos y videos de la ficha (HU19C).
// Muestra lo que el perro ya tiene, deja marcar archivos para quitar y agregar archivos nuevos.
// No hace peticiones: solo lleva la cuenta de qué quitar y qué subir. Quien la usa (la edición)
// sube los archivos nuevos y manda los cambios al servidor al guardar; si se cancela, nada se
// toca.

export const MAX_NUEVOS = 20; // mismo tope que el servidor por edición

const MIME_POR_EXTENSION = {
  jpg: "image/jpeg",
  jpeg: "image/jpeg",
  mp4: "video/mp4",
  mov: "video/quicktime",
};

export const ACCEPT = ".jpg,.jpeg,.mp4,.mov,image/jpeg,video/mp4,video/quicktime";

function extensionDe(nombre) {
  return String(nombre ?? "")
    .split("?")[0]
    .split(".")
    .pop()
    .toLowerCase();
}

// Devuelve { extension, mime, tipo } si es JPG, MP4 o MOV (mismos formatos que el alta);
// si no, null. Se rechaza también cuando el navegador dice un tipo que contradice la extensión.
export function clasificarArchivo(archivo) {
  const extension = extensionDe(archivo?.name);
  const mime = MIME_POR_EXTENSION[extension];
  if (!mime) return null;
  const reportado = archivo.type;
  const compatible =
    !reportado ||
    reportado === mime ||
    reportado === "application/octet-stream" ||
    (mime === "video/quicktime" && reportado === "video/x-quicktime");
  if (!compatible) return null;
  return { extension, mime, tipo: mime.startsWith("video/") ? "video" : "imagen" };
}

// Nombre sin acentos, espacios ni símbolos para la ruta en el bucket (Supabase Storage
// rechaza algunos caracteres). Conserva la extensión.
export function nombreSeguro(nombre) {
  const limpio = String(nombre ?? "")
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .replace(/[^a-zA-Z0-9._-]+/g, "_");
  return limpio || "archivo";
}

// Un archivo de la ficha es video si la BD lo dice o, si no, por su extensión.
export function esVideo(item) {
  if (item?.tipo) return String(item.tipo).trim().toLowerCase() === "video";
  const extension = extensionDe(item?.url);
  return extension === "mp4" || extension === "mov" || extension === "webm";
}

function el(etiqueta, props = {}, ...hijos) {
  const nodo = document.createElement(etiqueta);
  Object.assign(nodo, props);
  nodo.append(...hijos.filter(Boolean));
  return nodo;
}

function crearMedia({ url, video, texto }) {
  if (video) {
    return el("video", { src: url, muted: true, playsInline: true, preload: "metadata" });
  }
  return el("img", { src: url, alt: texto, loading: "lazy" });
}

// Monta la galería dentro de `contenedor`. `multimedia` es la lista de la ficha
// ([{ id_multimedia, url, tipo }]). Devuelve:
//   obtenerCambios() -> { eliminar: [id_multimedia], nuevos: [File] }
//   destruir()       -> libera las vistas previas temporales
export function montarGaleria(contenedor, { multimedia = [] } = {}) {
  const existentes = multimedia.filter((m) => m?.id_multimedia && m?.url);
  const quitadas = new Set();
  let nuevos = []; // { id, file, url, video }
  let contador = 0;

  const lista = el("div", { className: "galeria-lista" });
  const vacia = el("p", {
    className: "galeria-vacia",
    textContent: "Este perro todavía no tiene fotos ni videos.",
  });
  const aviso = el("p", { className: "galeria-aviso" });
  aviso.setAttribute("role", "status");
  aviso.hidden = true;
  const entrada = el("input", { type: "file", multiple: true, accept: ACCEPT, hidden: true });
  const agregar = el("button", {
    type: "button",
    className: "btn-secundario galeria-agregar",
    textContent: "Agregar fotos o videos",
  });

  function mostrarAviso(texto) {
    aviso.textContent = texto;
    aviso.hidden = !texto;
  }

  function tarjeta({ url, video, texto, etiqueta, accion, onAccion, quitada }) {
    return el(
      "figure",
      { className: `galeria-item ${quitada ? "quitada" : ""}` },
      crearMedia({ url, video, texto }),
      etiqueta ? el("span", { className: "galeria-etiqueta", textContent: etiqueta }) : null,
      el("button", {
        type: "button",
        className: "galeria-quitar",
        textContent: accion,
        onclick: onAccion,
      }),
    );
  }

  function pintar() {
    const tarjetas = [];
    existentes.forEach((item, indice) => {
      const quitada = quitadas.has(item.id_multimedia);
      tarjetas.push(
        tarjeta({
          url: item.url,
          video: esVideo(item),
          texto: `Archivo ${indice + 1} de la ficha`,
          etiqueta: quitada ? "Se quitará" : "",
          accion: quitada ? "Deshacer" : "Quitar",
          quitada,
          onAccion: () => {
            if (quitadas.has(item.id_multimedia)) quitadas.delete(item.id_multimedia);
            else quitadas.add(item.id_multimedia);
            mostrarAviso("");
            pintar();
          },
        }),
      );
    });
    for (const nuevo of nuevos) {
      tarjetas.push(
        tarjeta({
          url: nuevo.url,
          video: nuevo.video,
          texto: `Vista previa de ${nuevo.file.name}`,
          etiqueta: "Nueva",
          accion: "Quitar",
          onAccion: () => {
            URL.revokeObjectURL(nuevo.url);
            nuevos = nuevos.filter((n) => n.id !== nuevo.id);
            mostrarAviso("");
            pintar();
          },
        }),
      );
    }
    lista.replaceChildren(...tarjetas);
    vacia.hidden = tarjetas.length > 0;
    agregar.disabled = nuevos.length >= MAX_NUEVOS;
  }

  function agregarArchivos(archivos) {
    const rechazados = [];
    let sobrantes = 0;
    for (const archivo of archivos) {
      const info = clasificarArchivo(archivo);
      if (!info) {
        rechazados.push(archivo.name);
      } else if (nuevos.length >= MAX_NUEVOS) {
        sobrantes += 1;
      } else {
        // Se fija el tipo según la extensión: algunos navegadores no reportan el de .mov
        // y Storage lo serviría mal.
        const normalizado =
          archivo.type === info.mime
            ? archivo
            : new File([archivo], archivo.name, {
                type: info.mime,
                lastModified: archivo.lastModified,
              });
        contador += 1;
        nuevos.push({
          id: `nuevo-${contador}`,
          file: normalizado,
          url: URL.createObjectURL(normalizado),
          video: info.tipo === "video",
        });
      }
    }

    const mensajes = [];
    if (rechazados.length > 0) {
      mensajes.push(`Formato no compatible: ${rechazados.join(", ")}. Solo JPG, MP4 y MOV.`);
    }
    if (sobrantes > 0) {
      mensajes.push(`Solo se pueden agregar ${MAX_NUEVOS} archivos por edición.`);
    }
    mostrarAviso(mensajes.join(" "));
    pintar();
  }

  agregar.addEventListener("click", () => entrada.click());
  entrada.addEventListener("change", () => {
    agregarArchivos(Array.from(entrada.files ?? []));
    entrada.value = "";
  });

  contenedor.replaceChildren(
    el("div", { className: "galeria" }, vacia, lista, aviso, agregar, entrada),
  );
  pintar();

  return {
    obtenerCambios: () => ({
      eliminar: [...quitadas],
      nuevos: nuevos.map((n) => n.file),
    }),
    destruir: () => {
      nuevos.forEach((n) => URL.revokeObjectURL(n.url));
      nuevos = [];
      contenedor.replaceChildren();
    },
  };
}
