// utils/feedbackHelper.js
let feedbackInstance = null;

// 1. El GodScript llama a esta función una sola vez en su DOMContentLoaded
export function initFeedbackModal() {
  const overlay = document.querySelector("#modalFeedback");
  const card = document.querySelector("#feedbackCard");
  const icon = document.querySelector("#feedbackIcon");
  const title = document.querySelector("#feedbackTitle");
  const message = document.querySelector("#feedbackMessage");
  const closeButtons = [
    document.querySelector("#btnFeedbackCloseTop"),
    document.querySelector("#btnFeedbackAction"),
  ];

  const close = () => overlay?.classList.remove("active");
  closeButtons.forEach((button) => button?.addEventListener("click", close));

  const show = ({ mode, titleText, messageText, iconName, actionText }) => {
    if (!overlay || !card || !icon || !title || !message) {
      console.error("No se pudo mostrar el resultado: faltan elementos del modal de feedback.");
      return;
    }

    card.classList.remove("modal-success", "modal-error");
    card.classList.add(mode === "success" ? "modal-success" : "modal-error");
    icon.textContent = iconName;
    title.textContent = titleText;
    message.textContent = messageText;

    const actionButton = document.querySelector("#btnFeedbackAction");
    if (actionButton) {
      actionButton.textContent = actionText;
      actionButton.classList.toggle("btn-error", mode === "error");
    }

    overlay.classList.add("active");
  };

  // Guardamos la instancia internamente y la retornamos por si el GodScript la sigue usando
  feedbackInstance = {
    show,
    showSuccess(nombre) {
      const nombrePerro = nombre || "el perro";
      show({
        mode: "success",
        titleText: "¡Registro exitoso!",
        messageText: `El expediente de ${nombrePerro} se guardó correctamente en la base de datos del refugio.`,
        iconName: "check_circle",
        actionText: "Continuar",
      });
    },
    showError(error, nombre) {
      // Aprovechamos también aquí para atrapar 401/403 si falla el registro
      if (error?.status === 401 || error?.status === 403) {
        mostrarErrorFeedback(error);
        return;
      }

      const nombrePerro = nombre || "el perro";
      const detalle = error instanceof Error ? error.message : String(error);
      show({
        mode: "error",
        titleText: `No se pudo registrar a ${nombrePerro}`,
        messageText: `No se guardaron los datos. ${detalle} Puedes volver al resumen e intentarlo de nuevo.`,
        iconName: "error",
        actionText: "Volver al resumen",
      });
    },
  };

  return feedbackInstance;
}

// 2. Cualquier controlador (GodScript o modales hijos) importa y llama a esta función
export function mostrarErrorFeedback(error, tituloPorDefecto = "Ocurrió un error") {
  if (!feedbackInstance) {
    console.error("El modal de feedback no se ha inicializado:", error);
    return;
  }

  // Escenario AuthN (401)
  if (error?.status === 401) {
    feedbackInstance.show({
      mode: "error",
      titleText: "Sesión no iniciada",
      messageText: "Tu sesión ha expirado o no has iniciado sesión. Inicia sesión para continuar.",
      iconName: "lock",
      actionText: "Entendido",
    });
    return;
  }

  // Escenario AuthZ (403)
  if (error?.status === 403) {
    feedbackInstance.show({
      mode: "error",
      titleText: "Acceso denegado",
      messageText: "No tienes permisos de administrador para realizar esta acción.",
      iconName: "block",
      actionText: "Cerrar",
    });
    return;
  }

  // Cualquier otro error (500, 400, red)
  feedbackInstance.show({
    mode: "error",
    titleText: tituloPorDefecto,
    messageText: error instanceof Error ? error.message : String(error),
    iconName: "error",
    actionText: "Cerrar",
  });
}

// Opcional: por si algún modal hijo quiere mostrar un éxito personalizado sin pasar por el GodScript
export function getFeedback() {
  return feedbackInstance;
}
