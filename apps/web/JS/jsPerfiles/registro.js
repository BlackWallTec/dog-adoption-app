import { perfilesApi } from "./perfiles.api.js";
import { initCodigoPostal } from "./registro.cp.js";
import { VALIDATION, UNAUTHENTICATED } from "@qarino/shared/errores.js";
import { validateRegistration } from "@qarino/shared/perfiles/registro.validation.js";
import { fieldError, byId } from "./utils.js";

const form = byId("register-form");
const submit = byId("register-submit");
const toggle = byId("register-toggle");
const feedback = byId("register-feedback");

function busy(active, label) {
  form.setAttribute("aria-busy", String(active));
  for (const input of form.querySelectorAll("input, button")) input.disabled = active;
  submit.querySelector(".spinner-border").hidden = !active;
  submit.querySelector("[data-button-label]").textContent = label;
}

function showError(message, correcto = false) {
  feedback.textContent = message;
  feedback.className = `alert alert-${correcto ? "success" : "danger"}`;
  feedback.hidden = !message;
  if (message) feedback.focus();
}

for (const input of form.querySelectorAll("input, select")) {
  input.addEventListener("input", () => {
    fieldError(input);
    showError("");
  });
}

toggle.addEventListener("click", () => {
  const visible = byId("register-contrasena").type === "password";

  for (const field of ["contrasena", "confirmContrasena"])
    byId(`register-${field}`).type = visible ? "text" : "password";

  toggle.textContent = visible ? "Ocultar" : "Ver";
  toggle.setAttribute("aria-pressed", String(visible));
  toggle.setAttribute("aria-label", visible ? "Ocultar contraseñas" : "Mostrar contraseñas");
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (submit.disabled) return;
  showError("");

  const values = Object.fromEntries(new FormData(form));
  values.privacidad = byId("register-privacidad").checked;

  try {
    validateRegistration(values);
  } catch (error) {
    if (error instanceof VALIDATION) {
      for (const [field, message] of Object.entries(error.getFields() ?? {})) {
        const input = byId(`register-${field}`);
        if (input && typeof message === "string") fieldError(input, message);
      }
    }
    showError(error.message || "No pudimos crear tu cuenta. Inténtalo de nuevo.");
    return;
  }

  busy(true, "Creando tu cuenta…");

  try {
    const resultado = await perfilesApi.registerAdmin(values);
    form.reset();
    // Root conserva su sesión; la confirmación se envía al nuevo Administrador.
    if (resultado.requiereConfirmacion) {
      showError(
        "Cuenta creada. La persona debe confirmar su correo antes de iniciar sesión.",
        true,
      );
      return;
    }
    window.location.replace("login.html");
  } catch (error) {
    for (const [field, message] of Object.entries(error.fields ?? {})) {
      const input = byId(`register-${field}`);
      if (input && typeof message === "string") fieldError(input, message);
    }
    showError(error.message || "No pudimos crear tu cuenta. Inténtalo de nuevo.");
  } finally {
    busy(false, "Crear mi cuenta");
  }
});

initCodigoPostal();

// Esta página es sólo para Root: sin sesión, o con otro rol, se regresa al login.
// (El backend también lo exige: /register-admin devuelve 401/403.)
async function checkSession() {
  busy(true, "Comprobando sesión…");
  try {
    const { user } = await perfilesApi.getSession();
    if (user.rol !== "Root") {
      window.location.replace("login.html");
      return;
    }
  } catch (error) {
    if (error instanceof UNAUTHENTICATED) {
      window.location.replace("login.html");
      return;
    }
    showError(error.message);
  } finally {
    busy(false, "Crear mi cuenta");
  }
}
void checkSession();
