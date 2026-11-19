import { INTERNAL, errorFromHTTPStatus } from "@qarino/shared/errores.js";

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
  // Los errores del flujo de correo distinguen un enlace vencido de una cuenta pendiente.
  const correo = ["EMAIL_NOT_CONFIRMED", "INVALID_EMAIL_TOKEN", "EMAIL_UNAVAILABLE"].includes(
    data?.code,
  );
  const message = correo
    ? data.error
    : MESSAGES[status] || "No pudimos completar la solicitud. Inténtalo más tarde.";
  const error = errorFromHTTPStatus(status, message, data?.fields, data?.code);
  error.apiCode = data?.code;
  return error;
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
      ["login", "me", "register-admin"].includes(action) &&
      (!data?.user?.uuid || !data.user.rol)
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
    getCorreoConfig() {
      return request("correo/config", undefined, "GET");
    },
    getBandejaCorreo() {
      return request("correo/bandeja", undefined, "GET");
    },
    solicitarCorreo(correo, recuperar = false) {
      return request(`correo/${recuperar ? "recuperar" : "reenviar"}`, { correo });
    },
    confirmarCorreo(token) {
      return request("correo/confirmar", { token });
    },
    restablecerContrasena(token, contrasena, confirmContrasena) {
      return request("correo/restablecer", { token, contrasena, confirmContrasena });
    },
    login({ correo, contrasena }) {
      return request("login", { correo: correo.trim().toLowerCase(), contrasena });
    },
    // Sólo Root puede crear cuentas (rol Administrador)
    registerAdmin(values) {
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

      return request("register-admin", payload);
    },
    getSession() {
      return request("me", undefined, "GET");
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
