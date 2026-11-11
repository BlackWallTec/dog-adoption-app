import { perfilesApi } from "./perfiles.api.js";
import { initCodigoPostal } from "./registro.cp.js";
import { VALIDATION, UNAUTHENTICATED } from "../../../shared/errores.js";
import { validateRegistration } from "../../../shared/perfiles/registro.validation.js";

const byId = (id) => document.getElementById(id);
const form = byId("register-form");
const submit = byId("register-submit");
const toggle = byId("register-toggle");
const feedback = byId("register-feedback");

function fieldError(input, message = "") {
  input.classList.toggle("is-invalid", Boolean(message));
  input.setAttribute("aria-invalid", String(Boolean(message)));
  byId(`${input.id}-error`).textContent = message;
}

function busy(active, label) {
  form.setAttribute("aria-busy", String(active));
  for (const input of form.querySelectorAll("input, button")) input.disabled = active;
  submit.querySelector(".spinner-border").hidden = !active;
  submit.querySelector("[data-button-label]").textContent = label;
}

function showError(message) {
  feedback.textContent = message;
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
    } else if (error instanceof EMAIL_EXISTS) {
      fieldError(byId("register-correo"), error.message);
    }
    showError(error.message || "No pudimos crear tu cuenta. Inténtalo de nuevo.");
    return;
  }

  busy(true, "Creando tu cuenta…");

  try {
    await perfilesApi.register(values);
    form.reset();
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

async function checkSession() {
  busy(true, "Comprobando sesión…");
  try {
    await perfilesApi.getSession();
    window.location.replace("login.html");
  } catch (error) {
    if (!(error instanceof UNAUTHENTICATED)) showError(error.message);
  } finally {
    busy(false, "Crear mi cuenta");
  }
}
void checkSession();
