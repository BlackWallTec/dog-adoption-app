import { tituloDe } from "../JS/rutas.js";
import { iniciales, nombreCompleto, usuarioActual } from "../JS/jsPerfiles/usuarioActual.js";

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
    const usuario = await usuarioActual();
    const avatar = this.querySelector(".user-avatar");
    const nombre = this.querySelector(".user-name");
    const rol = this.querySelector(".user-role");
    if (avatar) avatar.textContent = iniciales(usuario.nombre, usuario.apellidos);
    if (nombre) nombre.textContent = nombreCompleto(usuario);
    if (rol) rol.textContent = usuario.rol;
  }
}

customElements.define("admin-topbar", AdminTopbar);
