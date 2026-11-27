import nodemailer from "nodemailer";
import { SERVICE_UNAVAILABLE, ERROR_CODES } from "@qarino/shared/errores.js";

// Transporte compartido por Perfiles (códigos) y Donativos (CFDI).
// MAIL_TRANSPORT=gmail usa SMTP_USER/SMTP_PASS; por omisión usa Resend con RESEND_API_KEY/MAIL_FROM.
// Las claves se leen sólo en el servidor y nunca se devuelven al cliente.
const TIMEOUT_MS = 8_000;
const NOMBRE = "Qariño Animal";
const CORREO = /^[^\s@<>]+@[^\s@<>]+\.[^\s@<>]+$/;
const CON_NOMBRE = /^[^<>]*<([^\s@<>]+@[^\s@<>]+\.[^\s@<>]+)>$/;

const noDisponible = (mensaje) => new SERVICE_UNAVAILABLE(mensaje, ERROR_CODES.EMAIL_UNAVAILABLE);

// Acepta "correo@dominio" o "Nombre <correo@dominio>" para no romper un MAIL_FROM existente.
function remitente(valor) {
  const limpio = valor?.trim() ?? "";
  if (CORREO.test(limpio)) return `${NOMBRE} <${limpio}>`;
  if (CON_NOMBRE.test(limpio)) return limpio;
  throw noDisponible("Falta configurar el remitente de correo.");
}

export async function enviarCorreo({ para, asunto, texto, adjuntos = [] }) {
  const transporte = process.env.MAIL_TRANSPORT || "resend";

  if (transporte === "gmail") {
    const from = remitente(process.env.SMTP_USER);
    const user = process.env.SMTP_USER.trim();
    const pass = process.env.SMTP_PASS?.replace(/\s/g, "");
    if (!pass) throw noDisponible("Falta SMTP_PASS para enviar con Gmail.");
    return nodemailer
      .createTransport({
        service: "gmail",
        auth: { user, pass },
        connectionTimeout: TIMEOUT_MS,
        greetingTimeout: TIMEOUT_MS,
        socketTimeout: TIMEOUT_MS,
        dnsTimeout: TIMEOUT_MS,
      })
      .sendMail({ from, to: [para], subject: asunto, text: texto, attachments: adjuntos });
  }

  if (transporte !== "resend") throw noDisponible("Proveedor de correo inválido.");
  const from = remitente(process.env.MAIL_FROM);
  if (!process.env.RESEND_API_KEY) throw noDisponible("Falta configurar la clave de Resend.");

  const respuesta = await fetch("https://api.resend.com/emails", {
    method: "POST",
    signal: AbortSignal.timeout(TIMEOUT_MS),
    headers: {
      Authorization: `Bearer ${process.env.RESEND_API_KEY}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      from,
      to: [para],
      subject: asunto,
      text: texto,
      // Resend recibe los adjuntos en base64; nodemailer acepta el Buffer directamente.
      ...(adjuntos.length
        ? {
            attachments: adjuntos.map(({ filename, content }) => ({
              filename,
              content: Buffer.from(content).toString("base64"),
            })),
          }
        : {}),
    }),
  });
  if (!respuesta.ok) {
    // El estado HTTP queda en el error para los registros del servidor, no en la respuesta.
    const error = noDisponible("No se pudo enviar el correo. Inténtalo más tarde.");
    error.proveedorStatus = respuesta.status;
    throw error;
  }
  return respuesta.json?.();
}
