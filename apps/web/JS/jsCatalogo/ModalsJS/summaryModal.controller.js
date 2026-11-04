export function initDogRegisterModal() {
  // Mapa de nodos UI centralizados (variables @export)
  const ui = {
    modalOverlay: document.querySelector("#modalResumenPerro"),
    btnBack: document.querySelector("#back-2-register"),
    btnSubmit: document.querySelector("#btn-submit-modal"),
  };

  const open = () => ui.modalOverlay.classList.add("active");
  const volverAlFormulario = () => ui.modalOverlay.classList.remove("active");
  const close = () => ui.modalOverlay.classList.remove("active");

  // Event Listeners
  ui.btnBack.addEventListener("click", volverAlFormulario);

  ui.btnSubmit.addEventListener("click", (e) => {
    e.preventDefault();
    console.log("Enviando datos a la API...");
    close();
  });

  return { open, close, ui };
}
