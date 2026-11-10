import { INTERNAL, errorFromHTTPStatus } from "../../../shared/errores.js";
import { validateRegistration } from "../../../shared/perfiles/registro.validation.js";

// Mensajes para mostrar al usuario según el status HTTP
const MESSAGES = {
  400: "Revisa el correo y la contraseña.",
  401: "Correo o contraseña incorrectos.",
  403: "Tu cuenta no tiene acceso al panel administrativo.",
  404: "No encontramos tu perfil.",
  409: "Este correo ya está registrado. Inicia sesión con tu cuenta.",
  422: "Revisa los campos indicados.",
  429: "Has realizado demasiados intentos. Espera unos minutos y vuelve a intentarlo.",
};

// Respuesta de error de la API ({ error, code, fields }) -> clase de errores.js
function toError(status, data) {
  const message = MESSAGES[status] || "No pudimos completar la solicitud. Inténtalo más tarde.";
  return errorFromHTTPStatus(status, message, data?.fields, data?.code);
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
      throw new INTERNAL("No pudimos conectar. Revisa tu conexión e inténtalo de nuevo.");
    }

    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw toError(response.status, data);

    if (
      ["login", "me", "register"].includes(action) &&
      (!data?.user?.id || !data.user.correo || !data.user.rol)
    ) {
      throw new INTERNAL("No pudimos comprobar la sesión. Inténtalo de nuevo.");
    }
    if (action === "logout" && data?.ok !== true) {
      throw new INTERNAL("No pudimos confirmar el cierre de sesión.");
    }
    if (action === "profile" && (!data?.profile || typeof data.profile !== "object")) {
      throw new INTERNAL("No pudimos consultar tus datos. Inténtalo de nuevo.");
    }

    return data;
  }

  return {
    login({ correo, contrasena }) {
      return request("login", { correo: correo.trim().toLowerCase(), contrasena });
    },
    register(values) {
      const payload = {
        nombre: values.nombre,
        apellidos: values.apellidos,
        correo:
          typeof values.correo === "string" ? values.correo.trim().toLowerCase() : values.correo,
        telefono: values.telefono,
        fecha_nacimiento: values.fecha_nacimiento,
        calle: values.calle,
        numero_exterior: values.numero_exterior,
        numero_interior: values.numero_interior,
        id_codigo_postal: values.id_codigo_postal,
        contrasena: values.contrasena,
        confirmContrasena: values.confirmContrasena,
        privacidad: values.privacidad,
      };

      return request("register", payload);
    },
    getSession() {
      return request("me");
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

export class PerfilesApiError extends Error {
  
};
