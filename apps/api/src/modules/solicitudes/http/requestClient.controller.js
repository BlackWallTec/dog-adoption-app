import adoptionRequestModel from "./modelosSolicitudes/adoptionRequest.model.js";
import profileModel from "./modelosSolicitudes/profile.model.js";

// Funciones de apoyo

// Supabase a veces regresa una relación como objeto y a veces como lista.
// Esto regresa siempre el primer elemento (o null si no hay)
const first = (value) => (Array.isArray(value) ? (value[0] ?? null) : (value ?? null));

// Convierte el perfil crudo (anidado como la BD) en algo plano para Android
const formatProfile = (raw, ocupacion) => {
  const datos = first(first(raw.cliente)?.datos_confidenciales);
  const direccion = first(raw.direcciones_fisicas);
  const cp = first(direccion?.catalogo_de_cps);

  // Arma la dir. "Calle Roble 24, Col. Álamos, Querétaro, Querétaro · C.P. 76160"
  // Solo agrega las partes que si existen
  const calleYNumero = [
    direccion?.calle,
    direccion?.numero_exterior,
    direccion?.numero_interior ? `Int. ${direccion.numero_interior}` : null,
  ]
    .filter(Boolean)
    .join(" ");

  const domicilio = [
    calleYNumero,
    cp?.colonia ? `Col. ${cp.colonia}` : null,
    cp?.municipio,
    cp?.estado,
  ]
    .filter(Boolean)
    .join(", ");

  return {
    id_usuario: raw.id_usuario,
    nombre_completo: `${raw.nombre} ${raw.apellidos}`.trim(),
    fecha_nacimiento: raw.fecha_nacimiento,
    ocupacion: ocupacion ?? "",
    domicilio: cp?.codigo_postal ? `${domicilio} · C.P. ${cp.codigo_postal}` : domicilio,
    municipio: cp?.municipio ?? "",
    telefono: datos?.telefono ?? "",
    correo: datos?.correo ?? "",
  };
};

// Rutas Cliente

// GET /api/solicitudes/cliente/borrador?idUsuario=...&idPerro=...
export const getDraft = async (req, res) => {
  try {
    const { idUsuario, idPerro } = req.query;

    if (!idUsuario || !idPerro) {
      return res.status(400).json({
        success: false,
        message: "Faltan idUsuario o idPerro.",
      });
    }

    const borrador = await adoptionRequestModel.getDraft(idUsuario, idPerro);

    return res.status(200).json({
      success: true,
      data: borrador, // puede ser null akaa que no hay borrador
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al buscar el borrador.",
    });
  }
};

// POST /api/solicitudes/cliente   body: { id_usuario, id_perro }
export const createRequest = async (req, res) => {
  try {
    const { id_usuario, id_perro } = req.body;

    if (!id_usuario || !id_perro) {
      return res.status(400).json({
        success: false,
        message: "Faltan id_usuario o id_perro.",
      });
    }

    const solicitud = await adoptionRequestModel.createRequest(id_usuario, id_perro);

    return res.status(201).json({
      success: true,
      data: solicitud,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al crear la solicitud.",
    });
  }
};

// GET /api/solicitudes/cliente/perfil/:idUsuario
export const getProfile = async (req, res) => {
  try {
    const perfil = await profileModel.getProfile(req.params.idUsuario);

    return res.status(200).json({
      success: true,
      data: formatProfile(perfil, ""),
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar el perfil.",
    });
  }
};

// GET /api/solicitudes/cliente/:idSolicitud/datos-generales
export const getGeneralData = async (req, res) => {
  try {
    // 1 La solicitud nos dice de quien es y que ocupacion guardo
    const solicitud = await adoptionRequestModel.getRequestById(req.params.idSolicitud);
    // 2 El perfil de ese usuario
    const perfil = await profileModel.getProfile(solicitud.id_usuario);

    return res.status(200).json({
      success: true,
      data: formatProfile(perfil, solicitud.ocupacion),
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar los datos generales.",
    });
  }
};

// PUT /api/solicitudes/cliente/:idSolicitud/datos-generales   body: { ocupacion }
export const updateGeneralData = async (req, res) => {
  try {
    const ocupacion = req.body.ocupacion?.trim();

    if (!ocupacion) {
      return res.status(400).json({
        success: false,
        message: "La ocupación es obligatoria.",
      });
    }

    await adoptionRequestModel.updateOccupation(req.params.idSolicitud, ocupacion);

    return res.status(200).json({
      success: true,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al guardar los datos generales.",
    });
  }
};
