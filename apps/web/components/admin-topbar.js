import { FORBIDDEN, UNAUTHENTICATED } from "@qarino/shared/errores.js";
import { tituloDe } from "../JS/rutas.js";
import { rutaAccesoDenegado, usuarioDePanel } from "../JS/jsPerfiles/accesoPanel.js";
import { iniciales, nombreCompleto } from "../JS/jsPerfiles/usuarioActual.js";

const SIN_DATO = "—";

class AdminTopbar extends HTMLElement {
  connectedCallback() {
    const titulo = tituloDe(window.location.pathname);

    this.innerHTML = `
        <header class="topbar">
        <h2 class="topbar-title">${titulo}</h2>

        <div class="topbar-right">
          <div class="global-search">
            <span class="material-symbols-rounded">search</span>
            <input type="text" placeholder="Buscar en el panel" />
          </div>

          <button type="button" class="icon-btn" aria-label="Notificaciones">
            <span class="material-symbols-rounded">notifications</span>
          </button>

          <div class="user-profile">
            <div class="user-avatar"></div>
            <div class="user-info">
              <span class="user-name"></span>
              <span class="user-role"></span>
            </div>
          </div>
        </div>
      </header>
    `;

    void this.#cargarUsuario();
  }

  async #cargarUsuario() {
    const avatar = this.querySelector(".user-avatar");
    const nombre = this.querySelector(".user-name");
    const rol = this.querySelector(".user-role");
    /** @type {{ nombre: string, apellidos: string, rol: string }} */
    let usuario = { nombre: "", apellidos: "", rol: "" };
    try {
      usuario = await usuarioDePanel();
    } catch (error) {
      if (error instanceof FORBIDDEN) {
        window.location.replace(rutaAccesoDenegado());
        return;
      }
      if (!(error instanceof UNAUTHENTICATED)) throw error;
    }
    const visible = nombreCompleto(usuario);
    const letras = iniciales(usuario.nombre, usuario.apellidos);
    if (avatar) avatar.textContent = letras || SIN_DATO;
    if (nombre) nombre.textContent = visible || SIN_DATO;
    if (rol) rol.textContent = usuario.rol || SIN_DATO;
  }
}

customElements.define("admin-topbar", AdminTopbar);
