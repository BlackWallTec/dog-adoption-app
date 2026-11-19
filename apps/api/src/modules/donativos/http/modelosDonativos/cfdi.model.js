import { supabaseAdmin } from "../../../../config/supabaseClient.js";
import DatosFiscalesModel from "./datosFiscales.model.js";

const BUCKET_CFDI = "cfdi-documentos";
const ESTADO_PENDIENTE = "Pendiente de atención";
const ESTADO_VALIDADO = "Validado";

export class ErrorFlujoCfdi extends Error {
  constructor(codigo, mensaje, status = 409) {
    super(mensaje);
    this.name = "ErrorFlujoCfdi";
    this.codigo = codigo;
    this.status = status;
  }
}

async function obtenerSolicitud(idSolicitud) {
  const { data, error } = await supabaseAdmin
    .from("solicitud_cfdi")
    .select(
      "id_solicitud_cfdi, id_donativo, estado, cfdi_uuid, pdf_storage_path, xml_storage_path, correo_estado, correo_intentos",
    )
    .eq("id_solicitud_cfdi", idSolicitud)
    .maybeSingle();

  if (error) throw new Error(`Error al consultar la solicitud CFDI: ${error.message}`);
  return data;
}

async function obtenerContexto(idSolicitud) {
  const solicitud = await obtenerSolicitud(idSolicitud);
  if (!solicitud) return null;

  const { data: donativo, error: errorDonativo } = await supabaseAdmin
    .from("donativo")
    .select("id_donativo, id_usuario, monto, estado, usuario ( nombre, apellidos )")
    .eq("id_donativo", solicitud.id_donativo)
    .maybeSingle();

  if (errorDonativo) {
    throw new Error(`Error al consultar el donativo del CFDI: ${errorDonativo.message}`);
  }

  const datosFiscales = donativo?.id_usuario
    ? await DatosFiscalesModel.getByUsuario(donativo.id_usuario)
    : null;

  return { solicitud, donativo, datosFiscales };
}

function validarContextoCarga(contexto) {
  if (!contexto) {
    throw new ErrorFlujoCfdi("SOLICITUD_NO_ENCONTRADA", "No se encontró la solicitud CFDI.", 404);
  }

  const { solicitud, donativo, datosFiscales } = contexto;
  if (solicitud.estado !== ESTADO_PENDIENTE) {
    throw new ErrorFlujoCfdi("SOLICITUD_NO_PENDIENTE", "La solicitud ya no está pendiente.");
  }
  if (solicitud.pdf_storage_path || solicitud.xml_storage_path || solicitud.cfdi_uuid) {
    throw new ErrorFlujoCfdi("CFDI_YA_CARGADO", "Esta solicitud ya tiene un CFDI cargado.");
  }
  if (!donativo || donativo.estado !== ESTADO_VALIDADO) {
    throw new ErrorFlujoCfdi(
      "DONATIVO_NO_VALIDADO",
      "El donativo debe estar validado para cargar el CFDI.",
      422,
    );
  }
  if (!datosFiscales?.rfc || !datosFiscales?.correo_facturacion) {
    throw new ErrorFlujoCfdi(
      "DATOS_FISCALES_INCOMPLETOS",
      "Falta el RFC o el correo de facturación del donante.",
      422,
    );
  }

  return contexto;
}

async function verificarUuidDisponible(uuid, idSolicitud) {
  const { data, error } = await supabaseAdmin
    .from("solicitud_cfdi")
    .select("id_solicitud_cfdi")
    .eq("cfdi_uuid", uuid)
    .maybeSingle();

  if (error) throw new Error(`Error al verificar el UUID del CFDI: ${error.message}`);
  if (data && data.id_solicitud_cfdi !== idSolicitud) {
    throw new ErrorFlujoCfdi(
      "UUID_CFDI_DUPLICADO",
      "Ese UUID de CFDI ya está asociado a otra solicitud.",
      409,
    );
  }
}

async function eliminarSubidas(rutas) {
  if (!rutas.length) return;
  const { error } = await supabaseAdmin.storage.from(BUCKET_CFDI).remove(rutas);
  if (error) throw new Error(`No se pudieron limpiar archivos CFDI parciales: ${error.message}`);
}

async function descargar(ruta) {
  const { data, error } = await supabaseAdmin.storage.from(BUCKET_CFDI).download(ruta);
  if (error) throw new Error(`No se pudo recuperar el archivo CFDI: ${error.message}`);
  return Buffer.from(await data.arrayBuffer());
}

class CfdiModel {
  static async obtenerContextoParaCarga(idSolicitud) {
    return validarContextoCarga(await obtenerContexto(idSolicitud));
  }

  static async verificarUuidDisponible(uuid, idSolicitud) {
    return verificarUuidDisponible(uuid, idSolicitud);
  }

  static async guardarArchivos({ idSolicitud, idAdministrador, datos, pdf, xml }) {
    const contexto = validarContextoCarga(await obtenerContexto(idSolicitud));
    await verificarUuidDisponible(datos.uuid, idSolicitud);

    const carpeta = `${idSolicitud}/${datos.uuid}`;
    const rutas = {
      pdf: `${carpeta}.pdf`,
      xml: `${carpeta}.xml`,
    };
    const bucket = supabaseAdmin.storage.from(BUCKET_CFDI);
    const subidas = [];

    const { error: errorPdf } = await bucket.upload(rutas.pdf, pdf.buffer, {
      contentType: "application/pdf",
      upsert: false,
    });
    if (errorPdf) {
      throw new Error(`No se pudo guardar el PDF del CFDI: ${errorPdf.message}`);
    }
    subidas.push(rutas.pdf);

    const { error: errorXml } = await bucket.upload(rutas.xml, xml.buffer, {
      contentType: "application/xml",
      upsert: false,
    });
    if (errorXml) {
      await eliminarSubidas(subidas);
      throw new Error(`No se pudo guardar el XML del CFDI: ${errorXml.message}`);
    }
    subidas.push(rutas.xml);

    const { data, error } = await supabaseAdmin
      .from("solicitud_cfdi")
      .update({
        cfdi_uuid: datos.uuid,
        rfc_receptor: datos.rfcReceptor,
        monto_cfdi: datos.total,
        pdf_storage_path: rutas.pdf,
        xml_storage_path: rutas.xml,
        fecha_carga_cfdi: new Date().toISOString(),
        id_administrador_carga: idAdministrador,
        correo_estado: "Pendiente de envío",
        correo_intentos: 0,
        correo_error: null,
      })
      .eq("id_solicitud_cfdi", idSolicitud)
      .eq("estado", ESTADO_PENDIENTE)
      .is("pdf_storage_path", null)
      .is("xml_storage_path", null)
      .select("id_solicitud_cfdi")
      .maybeSingle();

    if (error || !data) {
      await eliminarSubidas(subidas);
      if (error?.code === "23505") {
        throw new ErrorFlujoCfdi(
          "UUID_CFDI_DUPLICADO",
          "Ese UUID de CFDI ya está asociado a otra solicitud.",
          409,
        );
      }
      if (error) throw new Error(`No se pudo asociar el CFDI a la solicitud: ${error.message}`);
      throw new ErrorFlujoCfdi("SOLICITUD_ACTUALIZADA", "La solicitud cambió durante la carga.");
    }

    return {
      solicitud: contexto.solicitud,
      donativo: contexto.donativo,
      datosFiscales: contexto.datosFiscales,
      datos,
    };
  }

  static async iniciarIntentoCorreo(idSolicitud) {
    const solicitud = await obtenerSolicitud(idSolicitud);
    if (!solicitud) {
      throw new ErrorFlujoCfdi("SOLICITUD_NO_ENCONTRADA", "No se encontró la solicitud CFDI.", 404);
    }
    if (!solicitud.pdf_storage_path || !solicitud.xml_storage_path || !solicitud.cfdi_uuid) {
      throw new ErrorFlujoCfdi("CFDI_NO_CARGADO", "Primero debes cargar el PDF y XML del CFDI.");
    }
    if (solicitud.correo_estado === "Enviado") {
      throw new ErrorFlujoCfdi("CORREO_YA_ENVIADO", "El CFDI ya fue enviado por correo.");
    }

    const { data, error } = await supabaseAdmin
      .from("solicitud_cfdi")
      .update({
        correo_estado: "Enviando",
        correo_intentos: solicitud.correo_intentos + 1,
        correo_error: null,
      })
      .eq("id_solicitud_cfdi", idSolicitud)
      .neq("correo_estado", "Enviando")
      .neq("correo_estado", "Enviado")
      .select("id_solicitud_cfdi, cfdi_uuid, correo_intentos")
      .maybeSingle();

    if (error) throw new Error(`No se pudo iniciar el envío de correo: ${error.message}`);
    if (!data) {
      throw new ErrorFlujoCfdi(
        "CORREO_EN_PROCESO",
        "Ya hay un envío de correo en proceso para esta solicitud.",
      );
    }

    return data;
  }

  static async completarIntentoCorreo(idSolicitud) {
    const { error } = await supabaseAdmin
      .from("solicitud_cfdi")
      .update({
        estado: "Atendida",
        correo_estado: "Enviado",
        correo_enviado_en: new Date().toISOString(),
        correo_error: null,
      })
      .eq("id_solicitud_cfdi", idSolicitud)
      .eq("correo_estado", "Enviando");

    if (error) throw new Error(`No se pudo registrar el envío del CFDI: ${error.message}`);
  }

  static async fallarIntentoCorreo(idSolicitud) {
    const { error } = await supabaseAdmin
      .from("solicitud_cfdi")
      .update({
        correo_estado: "Error",
        correo_error: "No se pudo enviar el correo. Los archivos siguen disponibles en la app.",
      })
      .eq("id_solicitud_cfdi", idSolicitud)
      .eq("correo_estado", "Enviando");

    if (error) throw new Error(`No se pudo registrar el fallo del correo: ${error.message}`);
  }

  static async obtenerArchivosParaReenvio(idSolicitud) {
    const contexto = await obtenerContexto(idSolicitud);
    if (!contexto) {
      throw new ErrorFlujoCfdi("SOLICITUD_NO_ENCONTRADA", "No se encontró la solicitud CFDI.", 404);
    }
    if (!contexto.solicitud.pdf_storage_path || !contexto.solicitud.xml_storage_path) {
      throw new ErrorFlujoCfdi("CFDI_NO_CARGADO", "La solicitud aún no tiene archivos CFDI.");
    }

    const [pdf, xml] = await Promise.all([
      descargar(contexto.solicitud.pdf_storage_path),
      descargar(contexto.solicitud.xml_storage_path),
    ]);

    return { ...contexto, pdf, xml };
  }

  static async listarDelCliente(idUsuario) {
    const { data: donativos, error: errorDonativos } = await supabaseAdmin
      .from("donativo")
      .select("id_donativo, monto, fecha")
      .eq("id_usuario", idUsuario);

    if (errorDonativos) throw new Error(`Error al consultar donativos: ${errorDonativos.message}`);
    if (!donativos?.length) return [];

    const { data: solicitudes, error: errorSolicitudes } = await supabaseAdmin
      .from("solicitud_cfdi")
      .select("id_solicitud_cfdi, id_donativo, cfdi_uuid, fecha_carga_cfdi, correo_estado")
      .in(
        "id_donativo",
        donativos.map(({ id_donativo }) => id_donativo),
      )
      .not("pdf_storage_path", "is", null)
      .not("xml_storage_path", "is", null)
      .order("fecha_carga_cfdi", { ascending: false });

    if (errorSolicitudes) {
      throw new Error(`Error al consultar CFDI del donante: ${errorSolicitudes.message}`);
    }

    const donativosPorId = new Map(
      donativos.map((donativo) => [String(donativo.id_donativo), donativo]),
    );
    return (solicitudes ?? []).map((solicitud) => ({
      id_solicitud_cfdi: solicitud.id_solicitud_cfdi,
      uuid: solicitud.cfdi_uuid,
      fecha_carga: solicitud.fecha_carga_cfdi,
      donativo: donativosPorId.get(String(solicitud.id_donativo)),
    }));
  }

  static async descargarParaCliente(idSolicitud, idUsuario, tipoArchivo) {
    const solicitud = await obtenerSolicitud(idSolicitud);
    if (!solicitud || !solicitud.pdf_storage_path || !solicitud.xml_storage_path) return null;

    const { data: donativo, error } = await supabaseAdmin
      .from("donativo")
      .select("id_usuario")
      .eq("id_donativo", solicitud.id_donativo)
      .maybeSingle();

    if (error) throw new Error(`Error al validar titularidad del CFDI: ${error.message}`);
    if (!donativo || String(donativo.id_usuario) !== String(idUsuario)) return null;

    const path = tipoArchivo === "pdf" ? solicitud.pdf_storage_path : solicitud.xml_storage_path;
    if (!path) return null;

    return {
      uuid: solicitud.cfdi_uuid,
      buffer: await descargar(path),
      contentType: tipoArchivo === "pdf" ? "application/pdf" : "application/xml",
    };
  }
}

export default CfdiModel;
