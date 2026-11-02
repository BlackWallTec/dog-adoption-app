// La simulación es el valor predeterminado. El backend existente aún necesita correcciones.
export const API_MODE = import.meta.env?.VITE_PERFILES_API_MODE === "real" ? "real" : "mock";

export class PerfilesApiError extends Error {
  constructor(message, status = 0, code = "REQUEST_FAILED") {
    super(message);
    this.name = "PerfilesApiError";
    this.status = status;
    this.code = code;
  }
}

export function createPerfilesApi({
  mode = "mock",
  fetchImpl = globalThis.fetch,
  delayMs = 450,
} = {}) {
  const pause = () => new Promise((resolve) => setTimeout(resolve, delayMs));

  return {
    async login({ email, passwd }) {
      const normalizedEmail = email.trim().toLowerCase();

      if (mode === "mock") {
        await pause();

        if (normalizedEmail === "credenciales@example.test") {
          throw new PerfilesApiError("Correo o contraseña incorrectos.", 401, "INVALID_CREDENTIALS");
        }
        if (normalizedEmail === "pendiente@example.test") {
          throw new PerfilesApiError(
            "Confirma tu cuenta desde el enlace de tu correo antes de iniciar sesión.",
            403,
            "EMAIL_NOT_VERIFIED",
          );
        }
        if (normalizedEmail === "bloqueado@example.test") {
          throw new PerfilesApiError(
            "Has realizado demasiados intentos. Espera unos minutos y vuelve a intentarlo.",
            429,
            "TOO_MANY_REQUESTS",
          );
        }

        return {
          user: { id: "demo-admin", email: normalizedEmail, rol: "Administrador" },
          simulated: true,
        };
      }

      let response;
      try {
        response = await fetchImpl("/api/perfiles/login", {
          method: "POST",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email: normalizedEmail, passwd }),
        });
      } catch {
        throw new PerfilesApiError("No pudimos conectar. Revisa tu conexión e inténtalo de nuevo.");
      }

      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        const messages = {
          400: "Revisa el correo y la contraseña.",
          401: "Correo o contraseña incorrectos.",
          403: "Tu cuenta no tiene acceso o necesita confirmación.",
          429: "Has realizado demasiados intentos. Espera unos minutos y vuelve a intentarlo.",
        };
        throw new PerfilesApiError(
          messages[response.status] || "No pudimos iniciar sesión. Inténtalo más tarde.",
          response.status,
        );
      }
      if (!data?.user?.id) {
        throw new PerfilesApiError("No pudimos comprobar la sesión. Inténtalo de nuevo.", 502);
      }

      return { user: data.user, simulated: false };
    },

    async requestPasswordReset({ email }) {
      if (mode !== "mock") {
        // No se inventa una ruta: este contrato todavía no está implementado en la API.
        throw new PerfilesApiError(
          "La recuperación de contraseña todavía no está disponible. Contacta al refugio.",
          501,
          "NOT_IMPLEMENTED",
        );
      }

      await pause();
      return { simulated: true, email: email.trim().toLowerCase() };
    },
  };
}

export const perfilesApi = createPerfilesApi({ mode: API_MODE });
