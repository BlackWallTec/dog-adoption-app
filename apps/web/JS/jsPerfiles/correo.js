import { perfilesApi } from "./perfiles.api.js";
import { byId } from "./utils.js";

// El token sólo vive en memoria: se retira del historial antes de solicitar recursos a la API.
const fragmento = new URLSearchParams(location.hash.slice(1));
const token = fragmento.get("token");
const accion = fragmento.get("accion") || new URLSearchParams(location.search).get("accion");
const recuperar = accion === "recuperar";
history.replaceState(
  null,
  "",
  `${location.pathname}${recuperar ? "?accion=recuperar" : "?accion=confirmar"}`,
);
byId("correo-destino").value = sessionStorage.getItem("perfiles-correo") || "";
byId("solicitud-form").hidden = Boolean(token);
byId("confirmacion-panel").hidden = !token || recuperar;
byId("password-form").hidden = !token || !recuperar;
if (recuperar) {
  byId("correo-titulo").textContent = token
    ? "Elige tu nueva contraseña"
    : "Recupera tu contraseña";
  byId("correo-descripcion").textContent = token
    ? "El enlace se utiliza una sola vez. Después podrás iniciar sesión con tu nueva contraseña."
    : "Te enviaremos un enlace válido por 30 minutos para elegir una nueva contraseña.";
  byId("solicitar-correo").textContent = "Enviar enlace de recuperación";
}

function mostrar(mensaje, correcto = false) {
  const feedback = byId("correo-feedback");
  feedback.textContent = mensaje;
  feedback.className = `alert alert-${correcto ? "success" : "danger"}`;
  feedback.hidden = false;
  feedback.focus();
}

async function actualizarBandeja() {
  const { mensajes } = await perfilesApi.getBandejaCorreo();
  const lista = byId("bandeja-mensajes");
  lista.replaceChildren();
  for (const mensaje of mensajes) {
    // Se crea texto, nunca HTML procedente del destinatario o del proveedor de correo.
    const item = document.createElement("div");
    item.className = "border rounded p-3";
    const titulo = document.createElement("p");
    titulo.className = "mb-1 fw-semibold";
    titulo.textContent = mensaje.asunto;
    const para = document.createElement("p");
    para.className = "small text-secondary text-break mb-2";
    para.textContent = `${mensaje.para} · ${new Date(mensaje.fecha).toLocaleString("es-MX")}`;
    const enlace = document.createElement("a");
    enlace.href = mensaje.enlace;
    enlace.textContent =
      mensaje.tipo === "confirmar" ? "Abrir confirmación" : "Elegir nueva contraseña";
    item.append(titulo, para, enlace);
    lista.append(item);
  }
  if (!mensajes.length)
    lista.textContent = "Aún no hay mensajes. Crea una cuenta o solicita un enlace.";
}

async function ejecutar(boton, operacion, mensaje) {
  if (boton.disabled) return;
  boton.disabled = true;
  try {
    await operacion();
    mostrar(mensaje, true);
    if (byId("bandeja-panel").hidden === false) await actualizarBandeja();
    return true;
  } catch (error) {
    mostrar(error.message);
    if (error.apiCode === "INVALID_EMAIL_TOKEN") byId("solicitud-form").hidden = false;
    return false;
  } finally {
    boton.disabled = false;
  }
}

byId("solicitud-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const correo = byId("correo-destino").value.trim().toLowerCase();
  sessionStorage.setItem("perfiles-correo", correo);
  await ejecutar(
    byId("solicitar-correo"),
    () => perfilesApi.solicitarCorreo(correo, recuperar),
    "Si la cuenta corresponde, recibirás un enlace. Revisa tu bandeja y espera un minuto antes de solicitar otro.",
  );
});

byId("confirmar-correo").addEventListener("click", async (event) => {
  if (
    await ejecutar(
      event.currentTarget,
      () => perfilesApi.confirmarCorreo(token),
      "Tu correo está confirmado. Ya puedes iniciar sesión.",
    )
  ) {
    byId("confirmacion-panel").hidden = true;
  }
});

byId("password-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const password = byId("nueva-contrasena").value;
  const repetida = byId("repetir-contrasena").value;
  if (password !== repetida) return mostrar("Las contraseñas no coinciden.");
  if (new TextEncoder().encode(password).length > 72)
    return mostrar("La contraseña supera el máximo de 72 bytes. Usa una más corta.");
  if (
    await ejecutar(
      event.currentTarget.querySelector("button"),
      () => perfilesApi.restablecerContrasena(token, password, repetida),
      "Contraseña actualizada. Inicia sesión con tu nueva contraseña.",
    )
  ) {
    byId("password-form").reset();
    byId("password-form").hidden = true;
  }
});

byId("actualizar-bandeja").addEventListener("click", (event) =>
  ejecutar(event.currentTarget, actualizarBandeja, "Bandeja actualizada."),
);
async function iniciar() {
  try {
    const config = await perfilesApi.getCorreoConfig();
    if (!config.habilitado) {
      mostrar("El servicio de correo todavía no está configurado en este entorno.");
      for (const boton of document.querySelectorAll("form button, #confirmar-correo"))
        boton.disabled = true;
    }
    byId("bandeja-panel").hidden = !config.demo;
    if (config.demo) await actualizarBandeja();
  } catch (error) {
    mostrar(error.message);
  }
}
void iniciar();
