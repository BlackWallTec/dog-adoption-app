import { iniciarSesionDonativos, obtenerTokenDonativos } from "./adminDonativos.auth.js";

const formulario = document.getElementById("donativos-login-form");
const contenedor = document.getElementById("donativos-login");
const mensaje = document.getElementById("donativos-login-error");

async function comprobarSesion() {
  try {
    const token = await obtenerTokenDonativos();

    if (token) {
      contenedor.hidden = true;
    }
  } catch (error) {
    console.error("Error al comprobar la sesión:", error);
  }
}

formulario.addEventListener("submit", async (event) => {
  event.preventDefault();

  const email = document.getElementById("donativos-email").value;
  const password = document.getElementById("donativos-password").value;
  const boton = formulario.querySelector('button[type="submit"]');

  boton.disabled = true;
  mensaje.textContent = "Iniciando sesión...";

  try {
    await iniciarSesionDonativos(email, password);

    mensaje.textContent = "";
    document.getElementById("donativos-password").value = "";

    contenedor.hidden = true;

    // Recargar la página para que el ViewModel consulte los donativos.
    window.location.reload();
  } catch (error) {
    mensaje.textContent = error.message;
  } finally {
    boton.disabled = false;
  }
});

void comprobarSesion();
