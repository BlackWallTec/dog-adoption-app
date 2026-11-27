import nodemailer from "nodemailer";

const URL_RESEND = "https://api.resend.com/emails";

export async function enviarCfdiPorCorreo({ destinatario, nombre, uuid, pdf, xml }) {
  const transporte = process.env.MAIL_TRANSPORT || "resend";
  const asunto = "Tu comprobante fiscal digital";
  const texto = `Hola${nombre ? ` ${nombre}` : ""}.\n\nAdjuntamos el PDF y XML de tu CFDI (${uuid}).`;
  const adjuntos = [
    { filename: `CFDI-${uuid}.pdf`, content: pdf },
    { filename: `CFDI-${uuid}.xml`, content: xml },
  ];

  if (transporte === "gmail") {
    const remitente = process.env.SMTP_USER?.trim();
    const pass = process.env.SMTP_PASS?.replace(/\s/g, "");
    if (!remitente || !pass) {
      throw new Error("Falta configurar SMTP_USER y SMTP_PASS para enviar el CFDI con Gmail.");
    }

    return nodemailer
      .createTransport({
        service: "gmail",
        auth: { user: remitente, pass },
        connectionTimeout: 8000,
        greetingTimeout: 8000,
        socketTimeout: 8000,
        dnsTimeout: 8000,
      })
      .sendMail({
        from: `Qariño Animal <${remitente}>`,
        to: [destinatario],
        subject: asunto,
        text: texto,
        attachments: adjuntos,
      });
  }

  if (transporte !== "resend") {
    throw new Error("Proveedor de correo CFDI inválido.");
  }

  const apiKey = process.env.RESEND_API_KEY;
  const remitente = process.env.MAIL_FROM;

  if (!apiKey || !remitente) {
    throw new Error("Falta configurar el servicio de correo CFDI.");
  }

  const response = await fetch(URL_RESEND, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      from: remitente,
      to: [destinatario],
      subject: asunto,
      text: texto,
      attachments: adjuntos.map(({ filename, content }) => ({
        filename,
        content: content.toString("base64"),
      })),
    }),
  });

  if (!response.ok) {
    throw new Error(`El proveedor de correo respondió HTTP ${response.status}.`);
  }
}
