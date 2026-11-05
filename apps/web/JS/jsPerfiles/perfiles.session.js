export function createSessionView(api, root = document) {
  const byId = (id) => root.getElementById(id);
  function show({ user }) {
    byId("login-panel").hidden = true;
    byId("session-panel").hidden = false;
    byId("session-email").textContent = user.email;
    byId("session-role").textContent = user.rol;
    byId("session-feedback").hidden = true;
    byId("session-title").focus();
  }
  byId("session-logout").addEventListener("click", async () => {
    const button = byId("session-logout");
    if (button.disabled) return;
    button.disabled = true;
    byId("session-feedback").hidden = true;
    try {
      await api.logout();
      byId("login-form").reset();
      byId("login-feedback").hidden = true;
      byId("session-panel").hidden = true;
      byId("session-email").textContent = "";
      byId("session-role").textContent = "";
      byId("login-panel").hidden = false;
      byId("login-email").focus();
    } catch (error) {
      byId("session-feedback").textContent = error.message;
      byId("session-feedback").hidden = false;
    } finally {
      button.disabled = false;
    }
  });
  return { show };
}
