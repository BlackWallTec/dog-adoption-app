export function createSessionView(api, root = document) {
  const byId = (id) => root.getElementById(id);
  const profileIds = ["profile-name", "profile-phone", "profile-birth", "profile-address"];
  let profileRequest = 0;

  async function loadProfile() {
    const request = ++profileRequest;
    byId("profile-loading").hidden = false;
    byId("profile-feedback").hidden = true;
    byId("profile-retry").hidden = true;
    for (const id of profileIds) byId(id).textContent = "—";
    try {
      const { profile } = await api.getProfile();
      if (request !== profileRequest) return;
      byId("profile-name").textContent =
        `${profile.nombre || ""} ${profile.apellidos || ""}`.trim() || "Sin datos";
      byId("profile-phone").textContent = profile.telefono || "Sin datos";
      byId("profile-birth").textContent =
        profile.fecha_nacimiento?.split("-").reverse().join("/") || "Sin datos";
      byId("profile-address").textContent =
        [
          profile.calle,
          String(profile.numero_exterior ?? ""),
          profile.codigo_postal ? `C. P. ${profile.codigo_postal}` : "",
        ]
          .filter(Boolean)
          .join(", ") || "Sin datos";
    } catch (error) {
      if (request !== profileRequest) return;
      byId("profile-feedback").textContent = error.message;
      byId("profile-feedback").hidden = false;
      byId("profile-retry").hidden = false;
    } finally {
      if (request === profileRequest) byId("profile-loading").hidden = true;
    }
  }

  byId("profile-retry").addEventListener("click", loadProfile);

  function show({ user }) {
    byId("login-panel").hidden = true;
    byId("session-panel").hidden = false;
    byId("session-email").textContent = user.correo;
    byId("session-role").textContent = user.rol;
    byId("session-feedback").hidden = true;
    byId("session-title").focus();
    return loadProfile();
  }

  byId("session-logout").addEventListener("click", async () => {
    const button = byId("session-logout");
    if (button.disabled) return;
    button.disabled = true;
    byId("session-feedback").hidden = true;
    try {
      await api.logout();
      ++profileRequest;
      byId("login-form").reset();
      byId("login-feedback").hidden = true;
      byId("session-panel").hidden = true;
      byId("session-email").textContent = "";
      byId("session-role").textContent = "";
      for (const id of profileIds) byId(id).textContent = "";
      byId("profile-feedback").hidden = true;
      byId("profile-loading").hidden = true;
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
