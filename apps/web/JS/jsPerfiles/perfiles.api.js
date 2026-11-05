export class PerfilesApiError extends Error {
  constructor(message, status = 0, code = "REQUEST_FAILED") {
    super(message);
    this.name = "PerfilesApiError";
    this.status = status;
    this.code = code;
  }
}

export function createPerfilesApi({ fetchImpl = globalThis.fetch } = {}) {
  async function request(action, body) {
    let response;
    try {
      response = await fetchImpl(`/api/perfiles/${action}`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: body === undefined ? undefined : JSON.stringify(body),
      });
    } catch {
      throw new PerfilesApiError("No pudimos conectar. Revisa tu conexión e inténtalo de nuevo.");
    }

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      const messages = {
        400: "Revisa el correo y la contraseña.",
        401: "Correo o contraseña incorrectos.",
        403: "Tu cuenta no tiene acceso al panel administrativo.",
        429: "Has realizado demasiados intentos. Espera unos minutos y vuelve a intentarlo.",
      };
      throw new PerfilesApiError(
        messages[response.status] || "No pudimos iniciar sesión. Inténtalo más tarde.",
        response.status,
      );
    }
    if (action !== "logout" && (!data?.user?.id || !data.user.email || !data.user.rol)) {
      throw new PerfilesApiError("No pudimos comprobar la sesión. Inténtalo de nuevo.", 502);
    }
    if (action === "logout" && data?.ok !== true) {
      throw new PerfilesApiError("No pudimos confirmar el cierre de sesión.", 502);
    }

    return data;
  }

  return {
    login({ email, passwd }) {
      return request("login", { email: email.trim().toLowerCase(), passwd });
    },
    getSession() {
      return request("me");
    },
    logout() {
      return request("logout");
    },
  };
}

export const perfilesApi = createPerfilesApi();
