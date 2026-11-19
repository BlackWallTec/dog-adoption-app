import { supabaseAdmin } from "../../../../config/supabaseClient.js";
import DatosFiscalesModel from "./datosFiscales.model.js";

const ESTADO_PENDIENTE = "Pendiente de atención";
const ESTADO_DONATIVO_VALIDADO = "Validado";

function obtenerUsuario(donativo) {
  const usuario = donativo?.usuario;
  return Array.isArray(usuario) ? usuario[0] : usuario;
}

function obtenerNombreUsuario(usuario) {
  return [usuario?.nombre, usuario?.apellidos].filter(Boolean).join(" ") || "Sin nombre";
}

function tieneContenido(valor) {
  return typeof valor === "string" && valor.trim().length > 0;
}

function obtenerMotivosRevision(donativo, idUsuario, datosFiscales) {
  const motivos = [];

  if (!donativo || donativo.estado !== ESTADO_DONATIVO_VALIDADO) {
    motivos.push("El donativo ya no está validado.");
  }

  if (
    !tieneContenido(datosFiscales?.rfc) ||
    !tieneContenido(datosFiscales?.razon_social) ||
    !tieneContenido(datosFiscales?.regimen_fiscal) ||
    !tieneContenido(datosFiscales?.codigo_postal) ||
    !tieneContenido(datosFiscales?.correo_facturacion)
  ) {
    motivos.push("Faltan datos fiscales obligatorios.");
  }

  if (!DatosFiscalesModel.esRutaConstanciaValida(idUsuario, datosFiscales?.constancia_url)) {
    motivos.push("No hay una constancia fiscal disponible.");
  }

  return motivos;
}

class SolicitudCfdiModel {
  static async getPendientes() {
    const { data: solicitudes, error: errorSolicitudes } = await supabaseAdmin
      .from("solicitud_cfdi")
      .select("id_solicitud_cfdi, id_donativo, estado, fecha_solicitud")
      .eq("estado", ESTADO_PENDIENTE)
      .order("fecha_solicitud", { ascending: true });

    if (errorSolicitudes) {
      throw new Error(`Error al consultar solicitudes de CFDI: ${errorSolicitudes.message}`);
    }

    if (!solicitudes?.length) return [];

    const idsDonativos = [...new Set(solicitudes.map(({ id_donativo }) => id_donativo))];
    const { data: donativos, error: errorDonativos } = await supabaseAdmin
      .from("donativo")
      .select("id_donativo, estado, fecha, usuario ( id_usuario, nombre, apellidos )")
      .in("id_donativo", idsDonativos);

    if (errorDonativos) {
      throw new Error(
        `Error al consultar los donativos de solicitudes CFDI: ${errorDonativos.message}`,
      );
    }

    const donativosPorId = new Map(
      (donativos ?? []).map((donativo) => [String(donativo.id_donativo), donativo]),
    );
    const idsUsuario = [
      ...new Set(
        (donativos ?? []).map((donativo) => obtenerUsuario(donativo)?.id_usuario).filter(Boolean),
      ),
    ];
    let datosFiscalesPorUsuario = new Map();

    if (idsUsuario.length > 0) {
      const { data: perfiles, error: errorPerfiles } = await supabaseAdmin
        .from("datos_confidenciales")
        .select(
          "id_usuario, rfc, razon_social, regimen_fiscal, codigo_postal, correo_facturacion, constancia_url",
        )
        .in("id_usuario", idsUsuario);

      if (errorPerfiles) {
        throw new Error(
          `Error al revisar datos fiscales de solicitudes CFDI: ${errorPerfiles.message}`,
        );
      }

      datosFiscalesPorUsuario = new Map(
        (perfiles ?? []).map((perfil) => [String(perfil.id_usuario), perfil]),
      );
    }

    return solicitudes.map((solicitud) => {
      const donativo = donativosPorId.get(String(solicitud.id_donativo));
      const usuario = obtenerUsuario(donativo);
      const datosFiscales = usuario?.id_usuario
        ? datosFiscalesPorUsuario.get(String(usuario.id_usuario))
        : null;
      const motivosRevision = obtenerMotivosRevision(donativo, usuario?.id_usuario, datosFiscales);

      return {
        id_solicitud_cfdi: solicitud.id_solicitud_cfdi,
        id_donativo: solicitud.id_donativo,
        fecha_solicitud: solicitud.fecha_solicitud,
        estado: solicitud.estado,
        donante: obtenerNombreUsuario(usuario),
        requiere_revision: motivosRevision.length > 0,
      };
    });
  }

  static async getDetallePendiente(idSolicitud) {
    const { data: solicitud, error: errorSolicitud } = await supabaseAdmin
      .from("solicitud_cfdi")
      .select("id_solicitud_cfdi, id_donativo, estado, fecha_solicitud")
      .eq("id_solicitud_cfdi", idSolicitud)
      .eq("estado", ESTADO_PENDIENTE)
      .maybeSingle();

    if (errorSolicitud) {
      throw new Error(`Error al consultar la solicitud de CFDI: ${errorSolicitud.message}`);
    }
    if (!solicitud) return null;

    const { data: donativo, error: errorDonativo } = await supabaseAdmin
      .from("donativo")
      .select(
        "id_donativo, monto, fecha, estado, id_usuario, usuario ( id_usuario, nombre, apellidos )",
      )
      .eq("id_donativo", solicitud.id_donativo)
      .maybeSingle();

    if (errorDonativo) {
      throw new Error(
        `Error al consultar el donativo de la solicitud CFDI: ${errorDonativo.message}`,
      );
    }

    const usuario = obtenerUsuario(donativo);
    const datosFiscales = donativo?.id_usuario
      ? await DatosFiscalesModel.getByUsuario(donativo.id_usuario)
      : null;
    const motivosRevision = obtenerMotivosRevision(donativo, donativo?.id_usuario, datosFiscales);

    let constanciaUrl = null;
    if (
      DatosFiscalesModel.esRutaConstanciaValida(donativo?.id_usuario, datosFiscales?.constancia_url)
    ) {
      try {
        constanciaUrl = await DatosFiscalesModel.crearUrlFirmadaConstancia(
          donativo.id_usuario,
          datosFiscales.constancia_url,
        );
      } catch {
        motivosRevision.push("No se pudo generar el acceso temporal a la constancia fiscal.");
      }
    }

    return {
      id_solicitud_cfdi: solicitud.id_solicitud_cfdi,
      fecha_solicitud: solicitud.fecha_solicitud,
      estado: solicitud.estado,
      donativo: donativo
        ? {
            id_donativo: donativo.id_donativo,
            monto: donativo.monto,
            fecha: donativo.fecha,
            estado: donativo.estado,
            donante: obtenerNombreUsuario(usuario),
          }
        : null,
      datos_fiscales: datosFiscales
        ? {
            rfc: datosFiscales.rfc,
            razon_social: datosFiscales.razon_social,
            regimen_fiscal: datosFiscales.regimen_fiscal,
            codigo_postal: datosFiscales.codigo_postal,
            correo_facturacion: datosFiscales.correo_facturacion,
            constancia_url: constanciaUrl,
          }
        : null,
      listo_para_emitir: motivosRevision.length === 0,
      motivos_revision: motivosRevision,
    };
  }

  // HU49: el donante pide su CFDI, solo si el donativo es suyo y ya esta validado
  static async crear({ idDonativo, idUsuario }) {
    const { data: donativo, error: errorDonativo } = await supabaseAdmin
      .from("donativo")
      .select("id_donativo, estado")
      .eq("id_donativo", idDonativo)
      .eq("id_usuario", idUsuario)
      .maybeSingle();

    if (errorDonativo) {
      throw new Error(`Error al consultar el donativo: ${errorDonativo.message}`);
    }

    if (!donativo) return "no-existe";
    if (donativo.estado !== "Validado") return "no-validado";

    const { error } = await supabaseAdmin.from("solicitud_cfdi").insert({
      id_donativo: idDonativo,
    });

    if (error) {
      if (error.code === "23505") return "repetida"; // ya existe una solicitud para este donativo
      throw new Error(`Error al crear la solicitud de CFDI: ${error.message}`);
    }

    return "creada";
  }
}

export default SolicitudCfdiModel;
