class AdminSidebar extends HTMLElement {
  connectedCallback() {
    // Detecta la página actual para pintar la pastilla activa en automático
    const rutaActual = window.location.pathname;

    this.innerHTML = `
        <aside class="sidebar">
          <div class="sidebar-header">
            <div class="logo-box">
              <img src="../../public/logo.png" alt="Logo Qariño Animal" class="logo-img">
              <span class="panel-label">PANEL DE ADMINISTRACIÓN</span>
            </div>
          </div>
  
          <nav class="sidebar-nav">
            <a href="../htmlInicio/adminInicio.html" class="nav-item ${rutaActual.includes("Inicio") ? "active" : ""}">
              <span class="material-symbols-rounded">grid_view</span>
              <span>Inicio</span>
            </a>
            <a href="../htmlCatalogo/adminPerros.html" class="nav-item ${rutaActual.includes("Catalogo") || rutaActual.includes("Perros") ? "active" : ""}">
              <span class="material-symbols-rounded">pets</span>
              <span>Perros</span>
            </a>
            <a href="../htmlSolicitudes/adminSolicitudes.html" class="nav-item ${rutaActual.includes("Solicitudes") ? "active" : ""}">
              <span class="material-symbols-rounded">assignment</span>
              <span>Solicitudes</span>
            </a>
            <a href="../htmlVoluntariado/adminVoluntariado.html" class="nav-item ${rutaActual.includes("Voluntariado") ? "active" : ""}">
              <span class="material-symbols-rounded">volunteer_activism</span>
              <span>Voluntariado</span>
            </a>
            <a href="../htmlDonativos/adminDonativos.html" class="nav-item ${rutaActual.includes("Donativos") ? "active" : ""}">
              <span class="material-symbols-rounded">favorite</span>
              <span>Donativos</span>
            </a>
            <a href="../htmlPerfiles/adminPerfiles.html" class="nav-item ${rutaActual.includes("Perfiles") ? "active" : ""}">
              <span class="material-symbols-rounded">settings</span>
              <span>Configuración</span>
            </a>
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
}

customElements.define("admin-sidebar", AdminSidebar);
