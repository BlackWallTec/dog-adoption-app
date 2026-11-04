import * as apiService from "./Modals.service.js";

export function initDogRegisterModal() {
  // Mapa de nodos UI centralizados (variables @export)
  const ui = {
    modalOverlay: document.querySelector("#modalAltaPerro"),
    btnClose: document.querySelector("#btn-cerrar-modal"),
    btnCancel: document.querySelector("#btn-cancelar-modal"),
  };

  const open = () => ui.modalOverlay.classList.add("active");
  const close = () => ui.modalOverlay.classList.remove("active");

  // Event Listeners
  ui.btnClose.addEventListener("click", close);
  ui.btnCancel.addEventListener("click", close);

  return { open, close, ui };
}

export function onFormSubmit(event) {
  //1. Recuperamos los datos del formulario
  //2. Validamos los datos (opcional)
  //3. Llamamos a modal de Resumen de datos
  //4. Nos cerramos pero recordamos los datos del formulario.
  // 5. Abrimos el modal de resumen de datos para que el usuario confirme la información antes de enviarla a la API.
}
