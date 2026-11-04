export function initDogRegisterModal() {
  // Mapa de nodos UI centralizados (variables @export)
  const ui = {
    modalOverlay: document.querySelector("#modalAltaPerro"),
    btnClose: document.querySelector("#btn-cerrar-modal"),
    btnCancel: document.querySelector("#btn-cancelar-modal"),
    btnContinue: document.querySelector("#btn-continuar-modal"),
  };

  const open = () => ui.modalOverlay.classList.add("active");
  const close = () => ui.modalOverlay.classList.remove("active");
  const onContinueButtonClicked = (event) => {
    event.preventDefault();
    // 1. Recolectamos los datos del formulario
    const datosPerro = recollectFormData();
    if (!datosPerro) {
      alert("Por favor, completa todos los campos obligatorios.");
      return;
    }

    // 2. Aquí puedes pasar los datos al modal de resumen o a la API según tu flujo
    console.log("Datos recolectados del formulario:", datosPerro);

    //3. Emitir la "Signal" al Padre con los datos recolectados
    if (typeof onContinue === "function") {
      onContinue(datosPerro);
    }
  };

  // Event Listeners
  ui.btnClose.addEventListener("click", close);
  ui.btnCancel.addEventListener("click", close);
  ui.btnContinue.addEventListener("click", onContinueButtonClicked);

  return { open, close, ui };
}

function recollectFormData() {
  // 1. Validar campos requeridos de manera manual (ya que no hay etiqueta form nativa)
  const camposObligatorios = [
    { el: document.getElementById("nombre"), nombre: "Nombre" },
    { el: document.getElementById("sexo"), nombre: "Sexo" },
    { el: document.getElementById("id_raza"), nombre: "Raza" },
    { el: document.getElementById("id_tamano"), nombre: "Tamaño Categoría" },
  ];

  for (const campo of camposObligatorios) {
    if (!campo.el || !campo.el.value || campo.el.value.trim() === "") {
      alert(`Por favor, completa el campo obligatorio: ${campo.nombre}`);
      campo.el?.focus();
      return null; // Detiene la recolección si falta algo
    }
  }

  // 2. Extraer IDs de Badges con clase '.active'
  const obtenerIdsDeBadges = (containerId) => {
    const contenedor = document.getElementById(containerId);
    if (!contenedor) return [];
    const badgesActivos = contenedor.querySelectorAll(".badge-item.active");
    return Array.from(badgesActivos).map((badge) => parseInt(badge.getAttribute("data-id"), 10));
  };

  // 3. Obtener los archivos de imágenes cargados
  const inputMultimedia = document.getElementById("multimediaInput");
  const listaArchivos =
    inputMultimedia && inputMultimedia.files ? Array.from(inputMultimedia.files) : [];

  // 4. Construcción de la Estructura Final del Objeto
  const datosPerro = {
    // Información General
    nombre: document.getElementById("nombre").value.trim(),
    sexo: document.getElementById("sexo").value,
    edad_aprox: document.getElementById("edad_aprox").value
      ? parseInt(document.getElementById("edad_aprox").value, 10)
      : null,
    tamano_medida: document.getElementById("tamano_medida").value
      ? parseFloat(document.getElementById("tamano_medida").value)
      : null,
    estado: document.getElementById("estado").value,
    descripcion: document.getElementById("descripcion").value.trim(),

    // Clasificación de BD (Llaves foráneas numéricas)
    id_raza: parseInt(document.getElementById("id_raza").value, 10),
    id_tamano: parseInt(document.getElementById("id_tamano").value, 10),

    // Estado de Salud (Booleanos de los Checkboxes)
    esterilizado: document.getElementById("esterilizado").checked,
    vacunado: document.getElementById("vacunado").checked,

    // Relaciones Muchos a Muchos (Arreglos de IDs)
    caracteristicas_ids: obtenerIdsDeBadges("caracteristicasContainer"),
    cuidados_ids: obtenerIdsDeBadges("cuidadosContainer"),

    // Archivos Binarios (Pasará los elementos File listos para un FormData en el otro JS)
    imagenes: listaArchivos,
  };

  return datosPerro;
}
