//Open Modal with the data from the database
document.addEventListener("click", async (e) => {
  if (e.target.classList.contains("btn-ver-detalle")) {
    const id = e.target.dataset.id;
    await abrirModal(id);
  }
});

//We open and show data in the modal based on the id
async function abrirModal(id) {
  try {
    const res = await fetch(`/api/adopciones/${id}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
    });
    if (!res.ok) throw new Error("Error al cargar la solicitud");
    const data = await res.json();

    document.getElementById("modal-folio").textContent = data.id;
    document.getElementById("modal-adoptante").textContent =
      `${data.usuarios.nombre} ${data.usuarios.apellidos}`;
    document.getElementById("modal-correo").textContent = data.usuarios.correo;
    document.getElementById("modal-telefono").textContent = data.usuarios.telefono;
    document.getElementById("modal-direccion").textContent = data.usuarios.direccion;
    document.getElementById("modal-perro").textContent = data.perros.nombre;
    document.getElementById("modal-estado").textContent = data.estado;

    const bloqueResolucion = document.getElementById("bloque-resolucion");
    const bloqueLectura = document.getElementById("bloque-lectura");
    bloqueResolucion.dataset.solicitudId = data.id;
    document.getElementById("error-resolucion").style.display = "none";

    if (data.estado === "En revisión") {
      bloqueResolucion.style.display = "block";
      bloqueLectura.style.display = "none";
      document.getElementById("comentario-resolucion").value = "";
    } else {
      bloqueResolucion.style.display = "none";
      bloqueLectura.style.display = "block";
      document.getElementById("modal-comentario-guardado").textContent =
        data.comentario || "Sin comentarios";
    }

    document.getElementById("modal-detalle").style.display = "flex";
  } catch (error) {
    alert(error.message);
  }
}

//Close modal
document.getElementById("btn-cerrar-modal").addEventListener("click", () => {
  document.getElementById("modal-detalle").style.display = "none";
});

//Send status with comment whenn it is rejected
document
  .getElementById("btn-aprobar")
  .addEventListener("click", () => enviarResolucion("Aprobada"));
document
  .getElementById("btn-rechazar")
  .addEventListener("click", () => enviarResolucion("Rechazada"));

//We send JSON with the datas
async function enviarResolucion(decision) {
  const solicitudId = document.getElementById("bloque-resolucion").dataset.solicitudId;
  const comentario = document.getElementById("comentario-resolucion").value.trim();
  const errorBox = document.getElementById("error-resolucion");
  const btnAprobar = document.getElementById("btn-aprobar");
  const btnRechazar = document.getElementById("btn-rechazar");

  btnAprobar.disabled = true;
  btnRechazar.disabled = true;
  errorBox.style.display = "none";

  //Start of promise
  try {
    //We get the URL for the post
    const response = await fetch(`/api/adopciones/${solicitudId}/resolucion`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
      body: JSON.stringify({ state: decision, comment: comentario }),
    });

    const result = await response.json();

    if (!response.ok) {
      errorBox.textContent = result.message || "No se pudo procesar la solicitud.";
      errorBox.style.display = "block";
      return;
    }

    document.getElementById("modal-detalle").style.display = "none";
    location.reload(); //Reloads the page
  } catch (error) {
    errorBox.textContent = "Error de conexión con el servidor.";
    errorBox.style.display = "block";
  } finally {
    btnAprobar.disabled = false;
    btnRechazar.disabled = false;
  }
}
