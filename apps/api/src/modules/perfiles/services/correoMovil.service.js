import { createHmac, randomInt } from "node:crypto";
import bcrypt from "bcryptjs";
import Usuario from "../http/modelosPerfiles/user.model.js";
import ROLES from "../../../config/roles.js";
import {
  BAD_REQUEST,
  RATE_LIMITED,
  VALIDATION,
  INTERNAL,
  SERVICE_UNAVAILABLE,
  ERROR_CODES,
} from "@qarino/shared/errores.js";

const GENERICO = { message: "Si la cuenta corresponde, recibirás un código en tu correo." };
const MODOS_COMPARTIDOS = new Set(["local", "supabase"]);
const correoValido = (valor) => /^[^\s@<>]+@[^\s@<>]+\.[^\s@<>]+$/.test(valor || "");

const proveedorConfigurado = () => {
  const transporte = process.env.MAIL_TRANSPORT || "resend";
  if (transporte === "gmail")
    return correoValido(process.env.SMTP_USER?.trim()) && Boolean(process.env.SMTP_PASS?.trim());
  if (transporte === "resend")
    return correoValido(process.env.MAIL_FROM?.trim()) && Boolean(process.env.RESEND_API_KEY);
  return false;
};

// Mantiene el contrato de la extensión original, pero delega en Supabase el estado
// que debe compartirse entre todas las instancias de la API.
export function crearCorreoMovil({
  usuarios = Usuario,
  enviar,
  secreto,
  codigo = () => String(randomInt(0, 1_000_000)).padStart(6, "0"),
}) {
  if (!secreto) throw new INTERNAL("Falta el secreto del servidor.");
  const hash = (id, tipo, valor) =>
    createHmac("sha256", secreto).update(`${id}:${tipo}:${valor}`).digest("hex");
  const invalido = () => {
    throw new BAD_REQUEST(
      "El código no es válido o venció. Solicita uno nuevo.",
      ERROR_CODES.INVALID_EMAIL_CODE,
    );
  };
  async function emitir(id, correo, tipo) {
    const valor = codigo();
    const estado = await usuarios.emitirCodigoCorreo(id, tipo, hash(id, tipo, valor));
    if (estado === "espera")
      throw new RATE_LIMITED("Espera un minuto antes de solicitar otro código.");
    if (estado !== "creado") invalido();
    await enviar({ para: correo, codigo: valor, tipo });
  }
  return {
    estado(id) {
      return usuarios.estadoCorreo(id);
    },
    async registrar(id, correo) {
      try {
        await emitir(id, correo, "confirmar");
        return { enviado: true };
      } catch {
        console.error(
          "No se entregó la confirmación móvil; la cuenta puede solicitar otro código.",
        );
        return { enviado: false };
      }
    },
    async solicitar(correo, tipo) {
      const usuario = await usuarios.fetchByEmail(correo);
      const permitido =
        usuario?.rol === ROLES.CLIENT &&
        ((tipo === "confirmar" && usuario.correo_pendiente_desde) ||
          (tipo === "recuperar" && !usuario.correo_pendiente_desde));
      if (permitido) {
        try {
          await emitir(usuario.id_usuario, correo, tipo);
        } catch (error) {
          if (!(error instanceof BAD_REQUEST) && !(error instanceof RATE_LIMITED))
            console.error("No se entregó el código de correo móvil.");
        }
      }
      return GENERICO;
    },
    async consumir(correo, valor, tipo, contrasena, confirmContrasena) {
      if (typeof valor !== "string" || !/^\d{6}$/.test(valor)) invalido();
      const usuario = await usuarios.fetchByEmail(correo);
      if (usuario?.rol !== ROLES.CLIENT) invalido();
      const codigoHash = hash(usuario.id_usuario, tipo, valor);
      let resultado;
      if (tipo === "recuperar") {
        if (
          typeof contrasena !== "string" ||
          contrasena.length < 8 ||
          Buffer.byteLength(contrasena, "utf8") > 72 ||
          /[\r\n]/.test(contrasena) ||
          contrasena !== confirmContrasena
        )
          throw new VALIDATION(
            "Usa al menos 8 caracteres, máximo 72 bytes, y repite la misma contraseña.",
            {
              contrasena:
                "Usa al menos 8 caracteres, máximo 72 bytes, y repite la misma contraseña.",
            },
          );
        resultado = await usuarios.restablecerContrasenaCorreo(
          usuario.id_usuario,
          codigoHash,
          await bcrypt.hash(contrasena, 10),
        );
      } else {
        resultado = await usuarios.confirmarCodigoCorreo(usuario.id_usuario, codigoHash);
      }
      if (resultado !== "ok") invalido();
      return { ok: true };
    },
  };
}

let servicio;
export const correoMovilHabilitado = () => MODOS_COMPARTIDOS.has(process.env.CORREO_MOVIL_MODO);
export async function correoMovilDisponible() {
  if (
    !correoMovilHabilitado() ||
    !(process.env.EMAIL_CODE_SECRET || process.env.JWT_SECRET) ||
    !proveedorConfigurado()
  )
    return false;
  return Usuario.correoMovilDisponible();
}
export function servicioCorreoMovil() {
  if (!correoMovilHabilitado())
    throw new SERVICE_UNAVAILABLE(
      "El correo móvil todavía no está habilitado.",
      ERROR_CODES.EMAIL_UNAVAILABLE,
    );
  servicio ??= crearCorreoMovil({
    secreto: process.env.EMAIL_CODE_SECRET || process.env.JWT_SECRET,
    enviar: enviarCodigoCorreo,
  });
  return servicio;
}

// La clave se usa sólo en el servidor. El código nunca se devuelve en respuestas HTTP.
export async function enviarCodigoCorreo({ para, codigo, tipo }) {
  const transporte = process.env.MAIL_TRANSPORT || "resend";
  if (!["resend", "gmail"].includes(transporte))
    throw new SERVICE_UNAVAILABLE("Proveedor de correo inválido.", ERROR_CODES.EMAIL_UNAVAILABLE);
  const remitente = (
    transporte === "gmail" ? process.env.SMTP_USER : process.env.MAIL_FROM
  )?.trim();
  if (!correoValido(remitente))
    throw new SERVICE_UNAVAILABLE(
      "Falta configurar el remitente de correo.",
      ERROR_CODES.EMAIL_UNAVAILABLE,
    );
  const mensaje = {
    from: `Qariño Animal <${remitente}>`,
    to: [para],
    subject:
      tipo === "confirmar" ? "Confirma tu cuenta de Qariño Animal" : "Recupera tu contraseña",
    text: `Tu código es: ${codigo}\n\nEscríbelo en la aplicación Qariño Animal. ${tipo === "confirmar" ? "Confirma tu cuenta dentro de las 72 horas posteriores al registro." : "Vence en 15 minutos."}\n\nSi no lo solicitaste, ignora este correo.`,
  };
  // Gmail usa una contraseña de aplicación sólo en el servidor; conserva las rutas y códigos de Android.
  if (transporte === "gmail") {
    const pass = process.env.SMTP_PASS?.replace(/\s/g, "");
    if (!pass)
      throw new SERVICE_UNAVAILABLE(
        "Falta SMTP_PASS para enviar con Gmail.",
        ERROR_CODES.EMAIL_UNAVAILABLE,
      );
    const { default: nodemailer } = await import("nodemailer");
    return nodemailer
      .createTransport({
        service: "gmail",
        auth: { user: remitente, pass },
        connectionTimeout: 8000,
        greetingTimeout: 8000,
        socketTimeout: 8000,
        dnsTimeout: 8000,
      })
      .sendMail(mensaje);
  }
  if (!process.env.RESEND_API_KEY)
    throw new SERVICE_UNAVAILABLE(
      "Falta configurar la clave de Resend.",
      ERROR_CODES.EMAIL_UNAVAILABLE,
    );
  const respuesta = await fetch("https://api.resend.com/emails", {
    method: "POST",
    signal: AbortSignal.timeout(8_000),
    headers: {
      Authorization: `Bearer ${process.env.RESEND_API_KEY}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(mensaje),
  });
  if (!respuesta.ok)
    throw new SERVICE_UNAVAILABLE(
      "No se pudo enviar el correo. Inténtalo más tarde.",
      ERROR_CODES.EMAIL_UNAVAILABLE,
    );
  return respuesta.json();
}

export async function verificarCorreoMovil(usuario) {
  if (!correoMovilHabilitado() || usuario.rol !== ROLES.CLIENT) return true;
  const estado = await servicioCorreoMovil().estado(usuario.uuid);
  return estado.confirmado && estado.version === (usuario.correoVersionMovil ?? 0);
}
