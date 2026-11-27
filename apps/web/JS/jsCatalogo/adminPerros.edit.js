// HU19C: el administrador modifica la ficha de un perro: datos, estado, características,
// cuidado especial (texto, HU20) y fotos/videos (agregar y quitar).
import {
  cambiosEnRelaciones,
  cambiosRespectoA,
  conectarAlturaYTamano,
  leerFormulario,
  mostrarErrores,
  pintarFormulario,
  validarFormulario,
} from "./perroForm.js";
import { clasificarArchivo, montarGaleria, nombreSeguro } from "./perroMultimedia.js";
import { obtenerUrlFirmadaSupabase, subirArchivoASupabase } from "./ModalsJS/Api.service.js";

const dialogo = document.getElementById("dialog-editar-perro");
const cuerpo = document.getElementById("editar-perro-cuerpo");
const aviso = document.getElementById("editar-perro-aviso");
const btnGuardar = document.getElementById("editar-perro-guardar");
const btnCancelar = document.getElementById("editar-perro-cancelar");
const titulo = document.getElementById("editar-perro-titulo");
const toast = document.getElementById("toast");

let perroActual = null; // ficha tal como llegó del API
let idActual = null;
let galeria = null; // fotos y videos de la ficha en pantalla
let guardando = false;
let alGuardar = () => {};
// Archivos nuevos ya subidos al bucket (File -> url pública). Si el guardado falla después de
// subir, el reintento no los vuelve a subir.
let archivosSubidos = new Map();

// fetch que devuelve { ok, status, json }. Un fallo de red lanza una excepción.
async function api(ruta, opciones) {
  const respuesta = await fetch(`/api/catalogo${ruta}`, {
    credentials: "same-origin",
    headers: { "Content-Type": "application/json" },
    ...opciones,
  });
  const json = await respuesta.json().catch(() => ({}));
  return { ok: respuesta.ok, status: respuesta.status, json };
}

function mostrarAviso(texto, tipo = "error") {
  aviso.textContent = texto;
  aviso.className = `form-notice ${texto ? tipo : ""}`;
  aviso.hidden = !texto;
}

function mostrarToast(texto) {
  toast.textContent = texto;
  toast.hidden = false;
  setTimeout(() => (toast.hidden = true), 3500);
}

function mensajeDeEstado(status, json) {
  if (status === 401) return "Tu sesión terminó. Inicia sesión de nuevo para continuar.";
  if (status === 403) return "No tienes permisos para modificar fichas.";
  return json.message || "No se pudo completar la operación.";
}

function liberarGaleria() {
  galeria?.destruir();
  galeria = null;
}

function estadoCargando() {
  liberarGaleria();
  cuerpo.innerHTML = `<p class="dialog-status">Cargando ficha…</p>`;
  btnGuardar.disabled = true;
  mostrarAviso("");
}

function estadoError(texto) {
  cuerpo.innerHTML = `
    <p class="dialog-status error">${texto}</p>
    <button type="button" class="btn-secundario" id="editar-perro-reintentar">Reintentar</button>`;
  document.getElementById("editar-perro-reintentar").onclick = () => cargar();
  btnGuardar.disabled = true;
}

async function cargar() {
  estadoCargando();
  try {
    // Ficha y catálogos en paralelo (flujo principal pasos 2 y 3 del análisis)
    const [ficha, razas, tamanos, caracteristicas] = await Promise.all([
      api(`/admin/perros/${encodeURIComponent(idActual)}`),
      api("/admin/razas"),
      api("/admin/tamanos"),
      api("/admin/caracteristicas"),
    ]);

    if (ficha.status === 404) {
      // CA-06: el perro ya no existe → avisar y refrescar la lista
      dialogo.close();
      mostrarToast("Ese perro ya no existe. Se actualizó la lista.");
      alGuardar();
      return;
    }
    for (const r of [ficha, razas, tamanos, caracteristicas]) {
      if (!r.ok) return estadoError(mensajeDeEstado(r.status, r.json));
    }

    perroActual = ficha.json.data;
    titulo.textContent = `Editar a ${perroActual.nombre}`;
    pintarFormulario(cuerpo, {
      perro: perroActual,
      razas: razas.json.data,
      tamanos: tamanos.json.data,
      caracteristicas: caracteristicas.json.data,
    });
    conectarAlturaYTamano(cuerpo, tamanos.json.data);
    galeria = montarGaleria(cuerpo.querySelector("[data-galeria]"), {
      multimedia: perroActual.multimedia,
    });
    btnGuardar.disabled = false;
  } catch {
    estadoError("No se pudo conectar con el servidor. Revisa tu conexión.");
  }
}

// Sube al bucket los archivos nuevos (uno por uno) y devuelve [{ url, tipo }] para guardarlos
// en la ficha. Lanza un Error con mensaje legible si alguno falla.
async function subirArchivosNuevos(archivos) {
  const subidos = [];
  for (const [indice, archivo] of archivos.entries()) {
    btnGuardar.textContent = `Subiendo ${indice + 1}/${archivos.length}…`;
    let url = archivosSubidos.get(archivo);
    if (!url) {
      const { uploadUrl, publicUrl } = await obtenerUrlFirmadaSupabase(
        idActual,
        nombreSeguro(archivo.name),
        archivo.type,
      );
      await subirArchivoASupabase(uploadUrl, archivo);
      archivosSubidos.set(archivo, publicUrl);
      url = publicUrl;
    }
    subidos.push({ url, tipo: clasificarArchivo(archivo)?.tipo ?? "imagen" });
  }
  return subidos;
}

function ponerGuardando(valor) {
  guardando = valor;
  btnGuardar.disabled = valor;
  btnCancelar.disabled = valor;
  btnGuardar.textContent = valor ? "Guardando…" : "Guardar cambios";
}

// Junta el aviso del servidor con los errores de campos que el formulario no tiene a la vista.
function textoDeErrores(json) {
  const errores = json.errors && !Array.isArray(json.errors) ? json.errors : {};
  const sinLugar = Object.entries(errores)
    .filter(([campo]) => !cuerpo.querySelector(`[data-error-for="${campo}"]`))
    .map(([, mensaje]) => mensaje);
  return [json.message || "Revisa los campos marcados.", ...sinLugar].join(" ");
}

async function guardar() {
  if (guardando) return;
  mostrarAviso("");
  const datos = leerFormulario(cuerpo);

  const errores = validarFormulario(datos);
  mostrarErrores(cuerpo, errores);
  if (Object.keys(errores).length) return; // CA-03

  const fotos = galeria?.obtenerCambios() ?? { eliminar: [], nuevos: [] };
  const cambios = {
    ...cambiosRespectoA(perroActual, datos),
    ...cambiosEnRelaciones(perroActual, datos),
  };
  if (fotos.eliminar.length > 0) cambios.multimedia_eliminar = fotos.eliminar;

  if (Object.keys(cambios).length === 0 && fotos.nuevos.length === 0) {
    mostrarAviso("No hay cambios que guardar.", "info"); // CA-04
    return;
  }

  ponerGuardando(true);
  try {
    if (fotos.nuevos.length > 0) {
      try {
        cambios.multimedia_agregar = await subirArchivosNuevos(fotos.nuevos);
      } catch (error) {
        // No se guardó nada de la ficha: el formulario conserva lo capturado para reintentar.
        mostrarErrores(cuerpo, { multimedia_agregar: error.message });
        mostrarAviso("No se pudieron subir las fotos. Intenta de nuevo.");
        return;
      }
    }

    btnGuardar.textContent = "Guardando…";
    const r = await api(`/admin/perros/${encodeURIComponent(idActual)}`, {
      method: "PATCH",
      body: JSON.stringify(cambios),
    });

    if (r.ok) {
      dialogo.close();
      mostrarToast("Ficha actualizada.");
      alGuardar(); // CA-02: la lista se vuelve a cargar con los datos nuevos
    } else if (r.status === 404) {
      dialogo.close();
      mostrarToast("Ese perro ya no existe. Se actualizó la lista.");
      alGuardar();
    } else if (r.status === 400 && r.json.errors) {
      mostrarErrores(cuerpo, r.json.errors);
      mostrarAviso(textoDeErrores(r.json));
    } else {
      mostrarAviso(mensajeDeEstado(r.status, r.json)); // conserva lo capturado
    }
  } catch {
    // CA-05: error de red, el formulario conserva lo capturado y se puede reintentar
    mostrarAviso("No se pudo conectar con el servidor. Intenta de nuevo.");
  } finally {
    ponerGuardando(false);
  }
}

btnGuardar?.addEventListener("click", guardar);
btnCancelar?.addEventListener("click", () => dialogo.close());
// Con "Esc" no se puede cerrar mientras se sube o se guarda.
dialogo?.addEventListener("cancel", (evento) => {
  if (guardando) evento.preventDefault();
});
// Al cerrar (guardar, cancelar o Esc) se liberan las vistas previas de archivos nuevos.
// "close" llega de forma asíncrona: si el diálogo ya se volvió a abrir, no se toca la galería nueva.
dialogo?.addEventListener("close", () => {
  if (!dialogo.open) liberarGaleria();
});

// Punto de entrada: lo llama la lista al presionar "Editar".
export function abrirEdicion(id, callbackAlGuardar) {
  idActual = id;
  perroActual = null;
  archivosSubidos = new Map();
  alGuardar = callbackAlGuardar || (() => {});
  titulo.textContent = "Editar perro";
  dialogo.showModal();
  cargar();
}
