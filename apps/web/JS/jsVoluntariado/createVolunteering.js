const API = "/api/voluntarios/programas";
const URL_VOLUNTARIADO =
  window.QARINO_VOLUNTARIADO_URL || "/HTML/htmlVoluntariado/adminVoluntariado.html";

const formulario = document.getElementById("programaForm");
const mensajeForm = document.getElementById("alerta");
const panelEmpalmes = document.getElementById("empalmesPanel");
const botonPublicar = document.getElementById("publicarBtn");
const botonGuardarTipo = document.getElementById("guardarTipoBtn");
const botonConfirmarEmpalme = document.getElementById("confirmarEmpalmeBtn");

let guardando = false;
let datosConfirmados = null;

document.getElementById("volverListado").href = URL_VOLUNTARIADO;
document.getElementById("cancelarBtn").href = URL_VOLUNTARIADO;

// Se envia una peticion al backend. Se agrega el token si existe y devuelve el JSON, en caso de fallar manda el error correspondiente
async function solicitarAPI(ruta, opciones = {}) {
  const { metodo = "GET", datos } = opciones;

  // Prepara los encabezados de la petición.
  const headers = {
    "Content-Type": "application/json",
  };

  // Configura el método y el envío de cookies.
  const configuracion = {
    method: metodo,
    credentials: "same-origin",
    headers: headers,
  };

  // Agrega los datos cuando sea una petición de escritura.
  if (datos !== undefined) {
    configuracion.body = JSON.stringify(datos);
  }

  // Primero ejecuta la petición al backend.
  const respuesta = await fetch(`${API}${ruta}`, configuracion);

  const esJSON = respuesta.headers.get("content-type")?.includes("application/json");
  const resultado = esJSON ? await respuesta.json() : {};

  if (!respuesta.ok) {
    let message;

    if (respuesta.status === 401) {
      message =
        "Tu sesion no esta activa. Inicia sesion para continuar. Si consideras que esto es un error comunicate con el administrador del sistema ";
    } else if (respuesta.status === 403) {
      message = "Necesitas permisos de administrador para realizar esta acción.";
    } else {
      message =
        resultado.message ||
        reusltado.error ||
        "No se pudo completar la solicitud, intenta nuevamente.";
    }

    const error = new Error(message);
    error.code = resultado.code;
    error.empalmes = resultado.empalmes;
    throw error;
  }

  if (!esJSON) throw new Error("El servidor no devolvió una respuesta válida.");

  return resultado;
}

// Llamadas HTTP del formulario: utilizado para obtener los tipos, crear tipos nuevos, obtener los empalmes y crear programas.
const programasAPI = {
  obtenerTipos: () => solicitarAPI("/tipos"),
  crearTipo: (datos) => solicitarAPI("/tipos", { metodo: "POST", datos }),
  obtenerEmpalmes: (datos) => solicitarAPI(`/empalmes?${new URLSearchParams(datos)}`),
  crearPrograma: (datos) => solicitarAPI("", { metodo: "POST", datos }),
};

// Mensaje de exito debajo del formulario
function mostrarMensaje(texto, exito = false) {
  mensajeForm.textContent = texto || "";
  mensajeForm.classList.toggle("success", exito);
  mensajeForm.hidden = !texto;
  if (texto) mensajeForm.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

// Bloquea los botones mientras se realiza una operacion, evitando envios repetidos.
function cambiarEstadoGuardado(estado) {
  guardando = estado;
  botonPublicar.disabled = estado;
  botonGuardarTipo.disabled = estado;
  botonConfirmarEmpalme.disabled = estado;
  botonPublicar.textContent = estado ? "Publicando..." : "Publicar programa";
}

// Captura los valores de las casillas del formulario
function obtenerDatosForm() {
  const cupo = document.getElementById("cupoMaximo").value;
  return {
    nombre: document.getElementById("nombre").value.trim(),
    tipo: document.getElementById("tipo").value.trim(),
    descripcion: document.getElementById("descripcion").value.trim(),
    fechaInicio: document.getElementById("fechaInicio").value,
    horaInicio: document.getElementById("horaInicio").value,
    fechaFin: document.getElementById("fechaFin").value,
    horaFin: document.getElementById("horaFin").value,
    ubicacion: document.getElementById("ubicacion").value.trim(),
    cupoMaximo: cupo === "" ? NaN : Number(cupo),
  };
}

// Revisa los campos, cupos y el horario bajo las reglas y condiciones establecidas
function validarPrograma(datos) {
  if (
    !datos.nombre ||
    datos.nombre.length > 50 ||
    !datos.tipo ||
    datos.tipo.length > 20 ||
    !datos.descripcion ||
    datos.descripcion.length > 1000 ||
    !datos.ubicacion ||
    datos.ubicacion.length > 100
  ) {
    return "Completa los campos obligatorios respetando el límite de caracteres.";
  }

  if (!datos.fechaInicio || !datos.horaInicio || !datos.fechaFin || !datos.horaFin) {
    return "Completa las fechas y los horarios.";
  }

  if (!Number.isFinite(datos.cupoMaximo) || datos.cupoMaximo % 1 !== 0 || datos.cupoMaximo <= 0) {
    return "El cupo máximo debe ser un número entero mayor que cero.";
  }

  const inicio = new Date(`${datos.fechaInicio} ${datos.horaInicio}:00`);
  const fin = new Date(`${datos.fechaFin} ${datos.horaFin}:00`);
  if (Number.isNaN(inicio.getTime()) || Number.isNaN(fin.getTime())) {
    return "Fecha u hora inválida.";
  }
  if (inicio <= new Date()) return "La jornada debe iniciar en el futuro.";
  if (fin <= inicio) return "La fecha de fin debe ser posterior al inicio.";
  return null;
}

// Consulta de tipos de voluntariado y llena el selector de tipos del formulario
async function cargarTipos(seleccionado = "") {
  const selector = document.getElementById("tipo");
  selector.replaceChildren(new Option("Cargando tipos...", ""));

  try {
    const { tipos } = await programasAPI.obtenerTipos();
    selector.replaceChildren(new Option("Selecciona un tipo", ""));

    for (const tipo of tipos) {
      selector.add(new Option(tipo.nombre, tipo.nombre));
    }
    if (seleccionado) selector.value = seleccionado;
    if (tipos.length === 0) selector.options[0].textContent = "No hay tipos; registra uno";
  } catch (error) {
    selector.replaceChildren(new Option("Error al cargar tipos", ""));
    console.error("Error al cargar tipos:", error);
    mostrarMensaje(error.message);
  }
}

// Muestra jornadas con empalmes en  el horario elegido
function mostrarEmpalmes(empalmes) {
  const lista = document.getElementById("empalmesLista");
  lista.replaceChildren();

  for (const programa of empalmes) {
    const elemento = document.createElement("li");
    elemento.textContent =
      `${programa.nombre} — ${programa.fechaInicio} ${programa.horaInicio} a ` +
      `${programa.fechaFin} ${programa.horaFin} — Cupo: ${programa.cupoMaximo}`;
    lista.appendChild(elemento);
  }
  panelEmpalmes.hidden = false;
  panelEmpalmes.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

async function guardarPrograma(datos, confirmarEmpalme = false) {
  await programasAPI.crearPrograma({ ...datos, confirmarEmpalme });
  datosConfirmados = null;
  panelEmpalmes.hidden = true;
  formulario.reset();
  actualizarContadorDescripcion();
  mostrarMensaje("La jornada se publicó correctamente.", true);
}

function manejarErrorGuardado(error, datos) {
  if (error.code === "OVERLAP" && Array.isArray(error.empalmes) && error.empalmes.length) {
    datosConfirmados = datos;
    mostrarEmpalmes(error.empalmes);
  } else {
    console.error("Error al publicar jornada:", error);
    mostrarMensaje(error.message);
  }
}

async function enviarForm(evento) {
  evento.preventDefault();
  if (guardando) return;

  mostrarMensaje("");
  panelEmpalmes.hidden = true;
  datosConfirmados = null;

  const datos = obtenerDatosForm();
  const error = validarPrograma(datos);
  if (error) return mostrarMensaje(error);

  cambiarEstadoGuardado(true);
  try {
    const { empalmes } = await programasAPI.obtenerEmpalmes({
      fechaInicio: datos.fechaInicio,
      horaInicio: datos.horaInicio,
      fechaFin: datos.fechaFin,
      horaFin: datos.horaFin,
    });

    if (empalmes.length > 0) {
      datosConfirmados = datos;
      mostrarEmpalmes(empalmes);
    } else {
      await guardarPrograma(datos);
    }
  } catch (fallo) {
    manejarErrorGuardado(fallo, datos);
  } finally {
    cambiarEstadoGuardado(false);
  }
}

async function confirmarPublicacionConEmpalme() {
  if (guardando || !datosConfirmados) return;

  if (JSON.stringify(obtenerDatosForm()) !== JSON.stringify(datosConfirmados)) {
    panelEmpalmes.hidden = true;
    datosConfirmados = null;
    return mostrarMensaje("Cambiaste los datos. Vuelve a presionar Publicar programa.");
  }

  cambiarEstadoGuardado(true);
  try {
    await guardarPrograma(datosConfirmados, true);
  } catch (error) {
    manejarErrorGuardado(error, datosConfirmados);
  } finally {
    cambiarEstadoGuardado(false);
  }
}

function actualizarContadorDescripcion() {
  document.getElementById("descCount").textContent =
    document.getElementById("descripcion").value.length;
}

function alternarNuevoTipo() {
  const panel = document.getElementById("nuevoTipoPanel");
  panel.hidden = !panel.hidden;
  document.getElementById("nuevoTipoBtn").setAttribute("aria-expanded", String(!panel.hidden));
  if (!panel.hidden) document.getElementById("nuevoTipoNombre").focus();
}

async function guardarNuevoTipo() {
  if (guardando) return;
  const nombre = document.getElementById("nuevoTipoNombre").value.trim();
  const descripcion = document.getElementById("nuevoTipoDescripcion").value.trim();

  if (!nombre || nombre.length > 20 || descripcion.length > 200) {
    return mostrarMensaje("Nombre obligatorio (máximo 20 caracteres) y descripción de hasta 200.");
  }

  cambiarEstadoGuardado(true);
  try {
    const { tipo } = await programasAPI.crearTipo({ nombre, descripcion });
    await cargarTipos(tipo.nombre);
    document.getElementById("nuevoTipoPanel").hidden = true;
    document.getElementById("nuevoTipoBtn").setAttribute("aria-expanded", "false");
    document.getElementById("nuevoTipoNombre").value = "";
    document.getElementById("nuevoTipoDescripcion").value = "";
    mostrarMensaje("Nuevo tipo registrado.", true);
  } catch (error) {
    console.error("Error al guardar el tipo:", error);
    mostrarMensaje(error.message);
  } finally {
    cambiarEstadoGuardado(false);
  }
}

formulario.addEventListener("submit", enviarForm);
document
  .getElementById("confirmarEmpalmeBtn")
  .addEventListener("click", confirmarPublicacionConEmpalme);
document.getElementById("ajustarHorarioBtn").addEventListener("click", () => {
  panelEmpalmes.hidden = true;
  datosConfirmados = null;
  document.getElementById("fechaInicio").focus();
});
document.getElementById("descripcion").addEventListener("input", actualizarContadorDescripcion);
document.getElementById("nuevoTipoBtn").addEventListener("click", alternarNuevoTipo);
document.getElementById("guardarTipoBtn").addEventListener("click", guardarNuevoTipo);

actualizarContadorDescripcion();
void cargarTipos();
