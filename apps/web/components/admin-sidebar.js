import { PANTALLAS, idMenuActivo } from "../JS/rutas.js";

class AdminSidebar extends HTMLElement {
  connectedCallback() {
    const activo = idMenuActivo(window.location.pathname);
    const enlaces = PANTALLAS.filter((pantalla) => pantalla.etiqueta)
      .map(
        (pantalla) => `
            <a href="${pantalla.ruta}" class="nav-item${activo === pantalla.id ? " active" : ""}" data-menu="${pantalla.id}">
              <span class="material-symbols-rounded">${pantalla.icono}</span>
              <span>${pantalla.etiqueta}</span>
              <span class="nav-count" hidden></span>
            </a>`,
      )
      .join("");

    this.innerHTML = `
        <aside class="sidebar">
          <div class="sidebar-header">
            <div class="logo-box">
              <img src="/public/logo.png" alt="Logo Qariño Animal" class="logo-img" onerror="this.remove()">
              <span class="panel-label">PANEL DE ADMINISTRACIÓN</span>
            </div>
          </div>
  
          <nav class="sidebar-nav">${enlaces}
          </nav>
  
          <div class="sidebar-footer">
            <div class="care-card">
              <span class="material-symbols-rounded care-icon">favorite</span>
              <h4>Cuidamos juntos</h4>
              <p>Cada tarea acerca a un peludo a su nuevo hogar.</p>
            </div>
            <span class="footer-location">Qariño Animal · Querétaro</span>
          </div>
        </aside>
      `;
  }

  /** @param {{ solicitudes?: number, donativos?: number }} conteos */
  ponerConteos(conteos = {}) {
    this.#ponerConteo("solicitudes", conteos.solicitudes);
    this.#ponerConteo("donativos", conteos.donativos);
  }

  /** @param {string} id @param {number | undefined} valor */
  #ponerConteo(id, valor) {
    const cifra = this.querySelector(`[data-menu="${id}"] .nav-count`);
    if (!cifra) return;
    const numero = Number(valor) || 0;
    cifra.hidden = numero <= 0;
    cifra.textContent = numero > 99 ? "99+" : String(numero);
  }
}

customElements.define("admin-sidebar", AdminSidebar);
