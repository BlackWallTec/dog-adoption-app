export class PerfilesApiError extends Error {
  constructor(message, status = 0, code = "REQUEST_FAILED") {
    super(message);
    this.name = "PerfilesApiError";
    this.status = status;
    this.code = code;
  }
}

export function createPerfilesApi({
  fetchImpl = globalThis.fetch,
} = {}) {
  return {
    async login({ email, passwd }) {
      const normalizedEmail = email.trim().toLowerCase();

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

      return { user: data.user };
    },

    async requestPasswordReset() {
        throw new PerfilesApiError(
          "La recuperación de contraseña todavía no está disponible. Contacta al refugio.",
          501,
          "NOT_IMPLEMENTED",
        );
    },
  };
}

export const perfilesApi = createPerfilesApi();
