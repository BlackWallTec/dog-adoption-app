import { createHash, randomBytes } from "node:crypto";
import { mkdirSync, readFileSync, renameSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import bcrypt from "bcryptjs";
import Usuario from "../http/modelosPerfiles/user.model.js";

const MINUTO = 60_000;
const mensajeGenerico = "Si la cuenta corresponde, recibirás un enlace en tu correo.";
const hash = (token) => createHash("sha256").update(token).digest("hex");

export class ErrorCorreo extends Error {
  constructor(message, status = 400, code = "INVALID_EMAIL_TOKEN") {
    super(message);
    this.status = status;
    this.code = code;
  }
}

// MVP local: los tokens se guardan como hashes, fuera de Git. Nunca se habilita en producción.
// Las cuentas y las contraseñas continúan en las tablas actuales de Supabase.
export function crearServicioCorreo({
  archivo,
  usuarios = Usuario,
  ahora = () => Date.now(),
  baseUrl = "http://localhost:5173",
  enviar,
} = {}) {
  let estado = { cuentas: {}, mensajes: [] };
  try {
    estado = JSON.parse(readFileSync(archivo, "utf8"));
  } catch (error) {
    if (error.code !== "ENOENT") throw error;
  }
  const origen = new URL(baseUrl);
  if (
    origen.protocol !== "https:" &&
    !(origen.protocol === "http:" && ["localhost", "127.0.0.1"].includes(origen.hostname))
  ) {
    throw new Error("WEB_PUBLIC_URL debe usar HTTPS o localhost.");
  }
  const guardar = () => {
    mkdirSync(dirname(archivo), { recursive: true });
    writeFileSync(`${archivo}.tmp`, JSON.stringify(estado), { mode: 0o600 });
    renameSync(`${archivo}.tmp`, archivo);
  };
  const cuenta = (id) => estado.cuentas[id];
  const invalidar = () => {
    throw new ErrorCorreo("El enlace venció, ya se utilizó o no es válido. Solicita otro.");
  };
  let pendiente = Promise.resolve();

  async function emitir(id, correo, tipo) {
    const registro = cuenta(id);
    const previo = registro[tipo];
    // El reenvío no permite bombardear un correo ni invalida su enlace durante la espera.
    if (previo && ahora() - previo.enviado < MINUTO) return;
    const token = randomBytes(32).toString("base64url");
    registro[tipo] = {
      hash: hash(token),
      expira: ahora() + (tipo === "confirmar" ? 72 * 60 : 30) * MINUTO,
      enviado: ahora(),
    };
    guardar();
    // El fragmento evita que el token viaje en logs HTTP o en el encabezado Referer.
    const url = new URL("/HTML/htmlPerfiles/correo.html", origen);
    url.hash = new URLSearchParams({ accion: tipo, token }).toString();
    const mensaje = {
      para: correo,
      asunto:
        tipo === "confirmar" ? "Confirma tu cuenta de Qariño Animal" : "Recupera tu contraseña",
      enlace: url.href,
      fecha: new Date(ahora()).toISOString(),
      tipo,
    };
    if (enviar) {
      await enviar(mensaje);
    } else {
      // La bandeja contiene enlaces utilizables: sólo se expone en el servidor local de desarrollo.
      estado.mensajes.unshift(mensaje);
      estado.mensajes = estado.mensajes.slice(0, 30);
      guardar();
    }
  }

  return {
    obtenerEstado(id) {
      const registro = cuenta(id);
      return { confirmado: registro?.confirmado ?? true, version: registro?.version ?? 0 };
    },
    async registrar(id, correo) {
      estado.cuentas[id] = { confirmado: false, version: 0 };
      guardar();
      try {
        await emitir(id, correo, "confirmar");
      } catch {
        // La cuenta sigue pendiente y el usuario puede reenviar; nunca se activa por un fallo de correo.
        console.error("No se pudo entregar la confirmación; solicita un reenvío.");
      }
    },
    async solicitar(correo, tipo) {
      const usuario = await usuarios.fetchByEmail(correo);
      if (usuario) {
        const id = usuario.id_usuario;
        // Las cuentas anteriores a este MVP conservan su acceso y pueden recuperar contraseña.
        estado.cuentas[id] ??= { confirmado: true, version: 0 };
        if ((tipo === "confirmar") !== cuenta(id).confirmado) {
          try {
            await emitir(id, correo, tipo);
          } catch {
            console.error("No se pudo entregar el correo solicitado.");
          }
        }
      }
      return { message: mensajeGenerico };
    },
    consumir(token, tipo, contrasena, confirmContrasena) {
      // Serializa consumos para que dos solicitudes simultáneas no reutilicen un enlace.
      const operacion = pendiente.then(async () => {
        if (typeof token !== "string" || !/^[\w-]{43}$/.test(token)) invalidar();
        const encontrado = Object.entries(estado.cuentas).find(
          ([, registro]) => registro[tipo]?.hash === hash(token),
        );
        if (!encontrado || encontrado[1][tipo].expira <= ahora()) invalidar();
        const [id, registro] = encontrado;
        if (tipo === "recuperar") {
          if (
            typeof contrasena !== "string" ||
            contrasena.length < 8 ||
            Buffer.byteLength(contrasena, "utf8") > 72 ||
            /[\r\n]/.test(contrasena) ||
            contrasena !== confirmContrasena
          ) {
            throw new ErrorCorreo(
              "Usa entre 8 y 72 bytes y repite la misma contraseña.",
              422,
              "VALIDATION_ERROR",
            );
          }
          await usuarios.updatePassword(id, await bcrypt.hash(contrasena, 10));
          registro.version += 1;
          delete registro.recuperar;
        } else {
          registro.confirmado = true;
          delete registro.confirmar;
        }
        guardar();
        return { ok: true };
      });
      pendiente = operacion.catch(() => {});
      return operacion;
    },
    bandeja() {
      return estado.mensajes.map((mensaje) => ({ ...mensaje }));
    },
    eliminar(id) {
      delete estado.cuentas[id];
      guardar();
    },
  };
}

let servicio;
export const correoHabilitado = () => process.env.PERFILES_CORREO_MODO === "local";

export function servicioCorreo() {
  if (!correoHabilitado())
    throw new ErrorCorreo("El correo todavía no está configurado.", 503, "EMAIL_UNAVAILABLE");
  if (process.env.NODE_ENV === "production") {
    throw new Error("El almacenamiento local de correo es sólo para desarrollo.");
  }
  // Un proveedor desconocido no debe caer silenciosamente en la bandeja de demostración.
  const transporte = process.env.EMAIL_TRANSPORT || "inbox";
  if (!["inbox", "brevo"].includes(transporte)) {
    throw new ErrorCorreo(
      "Configura EMAIL_TRANSPORT como inbox o brevo.",
      503,
      "EMAIL_UNAVAILABLE",
    );
  }
  servicio ??= crearServicioCorreo({
    archivo: resolve(process.env.PERFILES_CORREO_ARCHIVO || ".local/perfiles-correo.json"),
    baseUrl: process.env.WEB_PUBLIC_URL || "http://localhost:5173",
    enviar: transporte === "brevo" ? enviarConBrevo : undefined,
  });
  return servicio;
}

// Brevo recibe la clave sólo desde la API; la interfaz nunca conoce esta credencial.
// MAIL_FROM debe corresponder a un remitente verificado en la cuenta de Brevo.
export async function enviarConBrevo(mensaje) {
  const remitente = process.env.MAIL_FROM?.trim();
  if (!process.env.BREVO_API_KEY || !/^[^\s@<>]+@[^\s@<>]+\.[^\s@<>]+$/.test(remitente || ""))
    throw new Error("Falta configurar BREVO_API_KEY y un MAIL_FROM válido.");
  const respuesta = await fetch("https://api.brevo.com/v3/smtp/email", {
    method: "POST",
    signal: AbortSignal.timeout(8_000),
    headers: {
      "api-key": process.env.BREVO_API_KEY,
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      sender: { email: remitente, name: process.env.MAIL_FROM_NAME || "Qariño Animal" },
      to: [{ email: mensaje.para }],
      subject: mensaje.asunto,
      textContent: `${mensaje.asunto}\n\nAbre este enlace:\n${mensaje.enlace}\n\nSi no lo solicitaste, ignora este mensaje.`,
    }),
  });
  if (!respuesta.ok) throw new Error("El proveedor rechazó el envío.");
}

// Se exporta por el index del módulo: otros módulos no dependen de sus archivos internos.
export function verificarSesionCorreo(usuario) {
  if (!correoHabilitado()) return true;
  const estado = servicioCorreo().obtenerEstado(usuario.uuid);
  return estado.confirmado && estado.version === (usuario.correoVersion ?? 0);
}
