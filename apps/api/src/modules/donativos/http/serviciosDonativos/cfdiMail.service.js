const URL_RESEND = "https://api.resend.com/emails";

export async function enviarCfdiPorCorreo({ destinatario, nombre, uuid, pdf, xml }) {
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
      subject: "Tu comprobante fiscal digital",
      text: `Hola${nombre ? ` ${nombre}` : ""}.\n\nAdjuntamos el PDF y XML de tu CFDI (${uuid}).`,
      attachments: [
        {
          filename: `CFDI-${uuid}.pdf`,
          content: pdf.toString("base64"),
        },
        {
          filename: `CFDI-${uuid}.xml`,
          content: xml.toString("base64"),
        },
      ],
    }),
  });

  if (!response.ok) {
    throw new Error(`El proveedor de correo respondió HTTP ${response.status}.`);
  }
}
