import DatosFiscalesModel from "./modelosDonativos/datosFiscales.model.js";

// Lo mismo que se decidió en el spike de la HU46 para el comprobante
export const TIPOS_CONSTANCIA = ["application/pdf", "image/jpeg", "image/png"];

// RFC: 3 letras (empresa) o 4 (persona) + fecha + homoclave
const RFC_REGEX = /^[A-ZÑ&]{3,4}\d{6}[A-Z\d]{3}$/;
const CP_REGEX = /^\d{5}$/;
const CORREO_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// Regresa los campos que están mal, vacío si todo bien
function validarDatosFiscales({ rfc, razonSocial, regimenFiscal, codigoPostal, correoFacturacion }) {
  const errores = {};
  if (typeof rfc !== "string" || !RFC_REGEX.test(rfc.trim().toUpperCase())) {
    errores.rfc = "RFC inválido";
  }
  if (typeof razonSocial !== "string" || !razonSocial.trim()) {
    errores.razon_social = "La razón social es obligatoria";
  }
  if (typeof regimenFiscal !== "string" || !regimenFiscal.trim()) {
    errores.regimen_fiscal = "Selecciona un régimen fiscal";
  }
  if (typeof codigoPostal !== "string" || !CP_REGEX.test(codigoPostal.trim())) {
    errores.codigo_postal = "El código postal debe tener 5 números";
  }
  if (typeof correoFacturacion !== "string" || !CORREO_REGEX.test(correoFacturacion.trim())) {
    errores.correo_facturacion = "Correo inválido";
  }
  return errores;
}

// GET /api/donativos/cliente/datos-fiscales/:idUsuario
export const getDatosFiscales = async (req, res) => {
  try {
    const datos = await DatosFiscalesModel.getByUsuario(req.params.idUsuario);

    if (!datos) {
      return res.status(404).json({ success: false, message: "No se encontró el usuario." });
    }

    return res.status(200).json({ success: true, data: datos });
  } catch (error) {
    console.error("Error al consultar datos fiscales:", error);
    return res.status(500).json({
      success: false,
      message: "No fue posible consultar los datos fiscales.",
    });
  }
};

// PUT /api/donativos/cliente/datos-fiscales/:idUsuario
export const putDatosFiscales = async (req, res) => {
  try {
    const { idUsuario } = req.params;
    const body = req.body ?? {};
    const datos = {
      rfc: body.rfc,
      razonSocial: body.razon_social,
      regimenFiscal: body.regimen_fiscal,
      codigoPostal: body.codigo_postal,
      correoFacturacion: body.correo_facturacion,
    };

    const errores = validarDatosFiscales(datos);
    if (Object.keys(errores).length > 0) {
      return res.status(400).json({ success: false, message: "Datos incompletos.", errores });
    }

    const guardado = await DatosFiscalesModel.update(idUsuario, {
      rfc: datos.rfc.trim().toUpperCase(),
      razonSocial: datos.razonSocial.trim(),
      regimenFiscal: datos.regimenFiscal.trim(),
      codigoPostal: datos.codigoPostal.trim(),
      correoFacturacion: datos.correoFacturacion.trim(),
    });

    if (!guardado) {
      return res.status(404).json({ success: false, message: "No se encontró el usuario." });
    }

    return res.status(200).json({ success: true, message: "Datos fiscales guardados." });
  } catch (error) {
    console.error("Error al guardar datos fiscales:", error);
    return res.status(500).json({
      success: false,
      message: "No fue posible guardar los datos fiscales.",
    });
  }
};

// PUT /api/donativos/cliente/datos-fiscales/:idUsuario/constancia
// El archivo llega crudo en el body (Content-Type = tipo del archivo)
export const putConstancia = async (req, res) => {
  try {
    const { idUsuario } = req.params;
    const tipo = req.get("content-type")?.split(";")[0].trim();

    if (!TIPOS_CONSTANCIA.includes(tipo) || !Buffer.isBuffer(req.body) || req.body.length === 0) {
      return res.status(400).json({
        success: false,
        message: "Sube tu constancia en PDF, JPG o PNG.",
      });
    }

    const usuario = await DatosFiscalesModel.getByUsuario(idUsuario);
    if (!usuario) {
      return res.status(404).json({ success: false, message: "No se encontró el usuario." });
    }

    const ruta = await DatosFiscalesModel.guardarConstancia(idUsuario, tipo, req.body);

    return res.status(200).json({ success: true, message: "Constancia guardada.", data: { constancia_url: ruta } });
  } catch (error) {
    console.error("Error al guardar la constancia:", error);
    return res.status(500).json({
      success: false,
      message: "No fue posible guardar la constancia.",
    });
  }
};
