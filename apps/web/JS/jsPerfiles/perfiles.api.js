export class PerfilesApiError extends Error {
  constructor(message, status = 0, code = "REQUEST_FAILED", fields = {}) {
    super(message);
    this.name = "PerfilesApiError";
    this.status = status;
    this.code = code;
    this.fields = fields;
  }
}

export function createPerfilesApi({ fetchImpl = globalThis.fetch } = {}) {
  async function request(action, body, method = "POST") {
    let response;
    try {
      response = await fetchImpl(`/api/perfiles/${action}`, {
        method,
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
        404: "No encontramos tu perfil.",
        409: "Este correo ya está registrado. Inicia sesión con tu cuenta.",
        429: "Has realizado demasiados intentos. Espera unos minutos y vuelve a intentarlo.",
      };
      throw new PerfilesApiError(
        action === "register" && response.status === 400
          ? "Revisa los campos indicados."
          : messages[response.status] || "No pudimos completar la solicitud. Inténtalo más tarde.",
        response.status,
        response.status === 409 ? "EMAIL_EXISTS" : "REQUEST_FAILED",
        action === "register" && [400, 409].includes(response.status) ? data.fields || {} : {},
      );
    }

    if (
      ["login", "me", "register"].includes(action) &&
      (!data?.user?.id || !data.user.correo || !data.user.rol)
    ) {
      throw new PerfilesApiError("No pudimos comprobar la sesión. Inténtalo de nuevo.", 502);
    }
    if (action === "logout" && data?.ok !== true) {
      throw new PerfilesApiError("No pudimos confirmar el cierre de sesión.", 502);
    }
    if (action === "profile" && (!data?.profile || typeof data.profile !== "object")) {
      throw new PerfilesApiError("No pudimos consultar tus datos. Inténtalo de nuevo.", 502);
    }

    return data;
  }

  return {
    login({ correo, contrasena }) {
      return request("login", { correo: correo.trim().toLowerCase(), contrasena });
    },
    getSession() {
      return request("me");
    },
    register(values) {
      const {
        nombre,
        apellidos,
        telefono,
        fecha_nacimiento,
        calle,
        numero_exterior,
        id_codigo_postal,
        contrasena,
        confirmPasswd,
        privacidad,
      } = values;
      return request("register", {
        nombre,
        apellidos,
        correo: values.correo.trim().toLowerCase(),
        telefono,
        fecha_nacimiento,
        calle,
        numero_exterior,
        id_codigo_postal,
        contrasena,
        privacidad,
      });
    },
    getProfile() {
      return request("profile", undefined, "GET");
    },
    getCodigosPostales(cp) {
      return request(`codigos-postales?cp=${encodeURIComponent(cp)}`, undefined, "GET");
    },
    logout() {
      return request("logout");
    },
  };
}

export const perfilesApi = createPerfilesApi();
