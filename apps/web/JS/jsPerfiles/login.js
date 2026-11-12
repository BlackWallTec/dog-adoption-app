import { perfilesApi } from "./perfiles.api.js";
import { createSessionView } from "./perfiles.session.js";
import { fieldError, byId } from "./utils.js";

const loginForm = byId("login-form");
const loginEmail = byId("login-email");
const loginPassword = byId("login-password");
const passwordToggle = byId("toggle-password");
const sessionView = createSessionView(perfilesApi);

let activeRequest = 0;

function validateEmail(input) {
  const message = !input.value.trim()
    ? "Ingresa tu correo."
    : input.validity.typeMismatch || input.validity.tooLong
      ? "Ingresa un correo válido, por ejemplo nombre@correo.com."
      : "";
  fieldError(input, message);
  return !message;
}

function feedback(id, message = "", variant = "danger") {
  const alert = byId(id);

  alert.className = `alert alert-${variant} py-3`;
  alert.textContent = message;
  alert.hidden = !message;

  if (message) alert.focus();
}

function setBusy(buttonId, busy, label) {
  const button = byId(buttonId);
  button.disabled = busy;

  button.querySelector(".spinner-border").hidden = !busy;
  button.querySelector("[data-button-label]").textContent = label;

  button.closest("form").setAttribute("aria-busy", String(busy));

  for (const input of [loginEmail, loginPassword, passwordToggle]) input.disabled = busy;
}

for (const input of [loginEmail, loginPassword]) {
  input.addEventListener("input", () => {
    fieldError(input);
    feedback("login-feedback");
  });
}

passwordToggle.addEventListener("click", () => {
  const show = loginPassword.type === "password";
  loginPassword.type = show ? "text" : "password";
  passwordToggle.setAttribute("aria-pressed", String(show));
  passwordToggle.setAttribute("aria-label", show ? "Ocultar contraseña" : "Mostrar contraseña");
});

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (byId("login-submit").disabled) return;

  feedback("login-feedback");

  const emailValid = validateEmail(loginEmail);
  const passwordValid = Boolean(loginPassword.value);

  fieldError(loginPassword, passwordValid ? "" : "Ingresa tu contraseña.");

  if (!emailValid || !passwordValid) {
    (emailValid ? loginPassword : loginEmail).focus();
    return;
  }

  const request = ++activeRequest;
  setBusy("login-submit", true, "Iniciando sesión…");

  try {
    const result = await perfilesApi.login({
      correo: loginEmail.value,
      contrasena: loginPassword.value,
    });
    if (request !== activeRequest) return;

    loginPassword.value = "";
    loginPassword.type = "password";

    passwordToggle.setAttribute("aria-pressed", "false");
    passwordToggle.setAttribute("aria-label", "Mostrar contraseña");

    await sessionView.show(result);
  } catch (error) {
    if (request !== activeRequest) return;

    feedback("login-feedback", error.message ? error.message : "Error al identificarse");
  } finally {
    if (request === activeRequest) setBusy("login-submit", false, "Iniciar sesión");
  }
});

async function restoreSession() {
  setBusy("login-submit", true, "Comprobando sesión…");
  try {
    await sessionView.show(await perfilesApi.getSession());
  } catch (error) {
    feedback("login-feedback", error.message ? error.message : "Error al comprobar sesión");
  } finally {
    setBusy("login-submit", false, "Iniciar sesión");
  }
}

void restoreSession();
