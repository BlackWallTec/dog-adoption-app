class AdminTopbar extends HTMLElement {
  connectedCallback() {
    this.innerHTML = `
        <header class="topbar">
        <h2 class="topbar-title">Perros</h2>

        <div class="topbar-right">
          <div class="global-search">
            <span class="material-symbols-rounded">search</span>
            <input type="text" placeholder="Buscar en el panel" />
          </div>

          <button type="button" class="icon-btn" aria-label="Notificaciones">
            <span class="material-symbols-rounded">notifications</span>
          </button>

          <div class="user-profile">
            <div class="user-avatar">AM</div>
            <div class="user-info">
              <span class="user-name">Dummy Name</span>
              <span class="user-role">Administradora</span>
            </div>
          </div>
        </div>
      </header>
    `;
  }
}

//  To Do: En la línea 20 hace falta remplazar el nombre del administrador hardcodeado por uno real

customElements.define("admin-topbar", AdminTopbar);
