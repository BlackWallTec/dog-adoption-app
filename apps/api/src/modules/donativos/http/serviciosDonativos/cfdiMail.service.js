import { enviarCorreo } from "../../../../utils/correo.js";

// HU57: el transporte (Resend o Gmail con MAIL_TRANSPORT) se comparte con Perfiles.
export async function enviarCfdiPorCorreo({ destinatario, nombre, uuid, pdf, xml }) {
  await enviarCorreo({
    para: destinatario,
    asunto: "Tu comprobante fiscal digital",
    texto: `Hola${nombre ? ` ${nombre}` : ""}.\n\nAdjuntamos el PDF y XML de tu CFDI (${uuid}).`,
    adjuntos: [
      { filename: `CFDI-${uuid}.pdf`, content: pdf },
      { filename: `CFDI-${uuid}.xml`, content: xml },
    ],
  });
}
