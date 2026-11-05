import { API_MODE, PerfilesApiError, perfilesApi } from "./perfiles.api.js";

const byId = (id) => document.getElementById(id);
const loginForm = byId("login-form");
const recoveryForm = byId("recovery-form");
const loginEmail = byId("login-email");
const loginPassword = byId("login-password");
const recoveryEmail = byId("recovery-email");
const passwordToggle = byId("toggle-password");
let activeRequest = 0;

byId("demo-badge").hidden = API_MODE !== "mock";

function setFieldError(input, message = "") {
  input.classList.toggle("is-invalid", Boolean(message));
  input.setAttribute("aria-invalid", String(Boolean(message)));
  byId(`${input.id}-error`).textContent = message;
}

function validateEmail(input) {
  const message = !input.value.trim()
    ? "Ingresa tu correo."
    : input.validity.typeMismatch || input.validity.tooLong
      ? "Ingresa un correo válido, por ejemplo nombre@correo.com."
      : "";
  setFieldError(input, message);
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
}

function showPanel(recovery) {
  activeRequest++;
  byId("login-panel").hidden = recovery;
  byId("recovery-panel").hidden = !recovery;
  feedback("login-feedback");
  feedback("recovery-feedback");
  setBusy("login-submit", false, "Iniciar sesión");
  setBusy("recovery-submit", false, "Enviar enlace");
  if (recovery) {
    recoveryEmail.value = loginEmail.value.trim();
    setFieldError(recoveryEmail);
    byId("recovery-title").focus();
  } else {
    byId("login-title").focus();
  }
}

function errorMessage(error) {
  return error instanceof PerfilesApiError
    ? error.message
    : "Ocurrió un problema. Inténtalo de nuevo.";
}

for (const input of [loginEmail, loginPassword, recoveryEmail]) {
  input.addEventListener("input", () => {
    setFieldError(input);
    feedback(input === recoveryEmail ? "recovery-feedback" : "login-feedback");
  });
}

passwordToggle.addEventListener("click", () => {
  const show = loginPassword.type === "password";
  loginPassword.type = show ? "text" : "password";
  passwordToggle.setAttribute("aria-pressed", String(show));
  passwordToggle.setAttribute("aria-label", show ? "Ocultar contraseña" : "Mostrar contraseña");
});

byId("show-recovery").addEventListener("click", () => showPanel(true));
byId("cancel-recovery").addEventListener("click", () => showPanel(false));

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (byId("login-submit").disabled) return;
  feedback("login-feedback");
  const emailValid = validateEmail(loginEmail);
  const passwordValid = Boolean(loginPassword.value);
  setFieldError(loginPassword, passwordValid ? "" : "Ingresa tu contraseña.");

  if (!emailValid || !passwordValid) {
    (emailValid ? loginPassword : loginEmail).focus();
    return;
  }

  const request = ++activeRequest;
  setBusy("login-submit", true, "Iniciando sesión…");
  try {
    const result = await perfilesApi.login({
      email: loginEmail.value,
      passwd: loginPassword.value,
    });
    if (request !== activeRequest) return;

    loginPassword.value = "";
    loginPassword.type = "password";
    passwordToggle.setAttribute("aria-pressed", "false");
    passwordToggle.setAttribute("aria-label", "Mostrar contraseña");
    feedback(
      "login-feedback",
      result.simulated
        ? "Inicio de sesión simulado correctamente. No se creó una sesión real."
        : "Sesión iniciada correctamente.",
      "success",
    );
  } catch (error) {
    if (request !== activeRequest) return;
    feedback("login-feedback", errorMessage(error));
  } finally {
    if (request === activeRequest) setBusy("login-submit", false, "Iniciar sesión");
  }
});

recoveryForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (byId("recovery-submit").disabled) return;
  feedback("recovery-feedback");
  if (!validateEmail(recoveryEmail)) {
    recoveryEmail.focus();
    return;
  }

  const request = ++activeRequest;
  setBusy("recovery-submit", true, "Enviando…");
  try {
    await perfilesApi.requestPasswordReset({ email: recoveryEmail.value });
    if (request !== activeRequest) return;
    feedback(
      "recovery-feedback",
      "Solicitud de recuperación simulada correctamente. No se ha enviado ningún correo.",
      "success",
    );
  } catch (error) {
    if (request !== activeRequest) return;
    feedback("recovery-feedback", errorMessage(error));
  } finally {
    if (request === activeRequest) setBusy("recovery-submit", false, "Enviar enlace");
  }
});
