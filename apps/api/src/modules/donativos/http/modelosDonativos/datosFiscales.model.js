import { supabaseAdmin } from "./supabaseAdmin.js";

// Bucket privado para las constancias (no deben ser públicas, traen RFC y domicilio)
const BUCKET_CONSTANCIAS = "constancias-fiscales";

const EXTENSIONES = {
  "application/pdf": "pdf",
  "image/jpeg": "jpg",
  "image/png": "png",
};

// HU45: datos fiscales del donante
// Todo vive en datos_confidenciales (una fila por usuario, la crea Perfiles)
class DatosFiscalesModel {
  static async getByUsuario(idUsuario) {
    const { data, error } = await supabaseAdmin
      .from("datos_confidenciales")
      .select(
        "id_usuario, rfc, razon_social, regimen_fiscal, codigo_postal, correo, correo_facturacion, constancia_url",
      )
      .eq("id_usuario", idUsuario)
      .maybeSingle();

    if (error) {
      throw new Error(`Error al consultar datos fiscales: ${error.message}`);
    }

    return data; // null si el usuario no existe
  }

  static async update(idUsuario, datos) {
    const { data, error } = await supabaseAdmin
      .from("datos_confidenciales")
      .update({
        rfc: datos.rfc,
        razon_social: datos.razonSocial,
        regimen_fiscal: datos.regimenFiscal,
        codigo_postal: datos.codigoPostal,
        correo_facturacion: datos.correoFacturacion,
      })
      .eq("id_usuario", idUsuario)
      .select("id_usuario");

    if (error) {
      throw new Error(`Error al guardar datos fiscales: ${error.message}`);
    }

    return data?.length > 0; // false = no había fila de ese usuario
  }

  // Sube el archivo al bucket y guarda la ruta en constancia_url.
  // Si ya tenía una se reemplaza (solo se guarda la última)
  static async guardarConstancia(idUsuario, tipoArchivo, archivo) {
    const ruta = `${idUsuario}/constancia.${EXTENSIONES[tipoArchivo]}`;

    const { error: errorSubida } = await supabaseAdmin.storage
      .from(BUCKET_CONSTANCIAS)
      .upload(ruta, archivo, { contentType: tipoArchivo, upsert: true });

    if (errorSubida) {
      throw new Error(`Error al subir la constancia: ${errorSubida.message}`);
    }

    const { error } = await supabaseAdmin
      .from("datos_confidenciales")
      .update({ constancia_url: ruta })
      .eq("id_usuario", idUsuario);

    if (error) {
      throw new Error(`Error al guardar la constancia: ${error.message}`);
    }

    return ruta;
  }
}

export default DatosFiscalesModel;
