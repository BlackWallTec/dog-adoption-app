// HU19C: el administrador modifica las características de la ficha de un perro.
import {
  cambiosRespectoA,
  conectarAlturaYTamano,
  leerFormulario,
  mostrarErrores,
  pintarFormulario,
  validarFormulario,
} from "./perroForm.js";

const dialogo = document.getElementById("dialog-editar-perro");
const cuerpo = document.getElementById("editar-perro-cuerpo");
const aviso = document.getElementById("editar-perro-aviso");
const btnGuardar = document.getElementById("editar-perro-guardar");
const btnCancelar = document.getElementById("editar-perro-cancelar");
const titulo = document.getElementById("editar-perro-titulo");
const toast = document.getElementById("toast");

let perroActual = null; // ficha tal como llegó del API
let idActual = null;
let alGuardar = () => {};

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

function estadoCargando() {
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
    const [ficha, razas, tamanos] = await Promise.all([
      api(`/admin/perros/${encodeURIComponent(idActual)}`),
      api("/admin/razas"),
      api("/admin/tamanos"),
    ]);

    if (ficha.status === 404) {
      // CA-06: el perro ya no existe → avisar y refrescar la lista
      dialogo.close();
      mostrarToast("Ese perro ya no existe. Se actualizó la lista.");
      alGuardar();
      return;
    }
    for (const r of [ficha, razas, tamanos]) {
      if (!r.ok) return estadoError(mensajeDeEstado(r.status, r.json));
    }

    perroActual = ficha.json.data;
    titulo.textContent = `Editar a ${perroActual.nombre}`;
    pintarFormulario(cuerpo, {
      perro: perroActual,
      razas: razas.json.data,
      tamanos: tamanos.json.data,
    });
    conectarAlturaYTamano(cuerpo, tamanos.json.data);
    btnGuardar.disabled = false;
  } catch {
    estadoError("No se pudo conectar con el servidor. Revisa tu conexión.");
  }
}

async function guardar() {
  mostrarAviso("");
  const datos = leerFormulario(cuerpo);

  const errores = validarFormulario(datos);
  mostrarErrores(cuerpo, errores);
  if (Object.keys(errores).length) return; // CA-03

  const cambios = cambiosRespectoA(perroActual, datos);
  if (Object.keys(cambios).length === 0) {
    mostrarAviso("No hay cambios que guardar.", "info"); // CA-04
    return;
  }

  btnGuardar.disabled = true;
  btnGuardar.textContent = "Guardando…";
  try {
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
      mostrarAviso(r.json.message || "Revisa los campos marcados.");
    } else {
      mostrarAviso(mensajeDeEstado(r.status, r.json)); // conserva lo capturado
    }
  } catch {
    // CA-05: error de red, el formulario conserva lo capturado y se puede reintentar
    mostrarAviso("No se pudo conectar con el servidor. Intenta de nuevo.");
  } finally {
    btnGuardar.disabled = false;
    btnGuardar.textContent = "Guardar cambios";
  }
}

btnGuardar?.addEventListener("click", guardar);
btnCancelar?.addEventListener("click", () => dialogo.close());

// Punto de entrada: lo llama la lista al presionar "Editar".
export function abrirEdicion(id, callbackAlGuardar) {
  idActual = id;
  perroActual = null;
  alGuardar = callbackAlGuardar || (() => {});
  titulo.textContent = "Editar perro";
  dialogo.showModal();
  cargar();
}
