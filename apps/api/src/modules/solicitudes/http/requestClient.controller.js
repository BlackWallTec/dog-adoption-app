import adoptionRequestModel from "./modelosSolicitudes/adoptionRequest.model.js";
import profileModel from "./modelosSolicitudes/profile.model.js";

// ===== Funciones de apoyo (no son rutas) =====

// Supabase a veces regresa una relación como objeto y a veces como lista
const first = (value) => (Array.isArray(value) ? (value[0] ?? null) : (value ?? null));

// Convierte el perfil "crudo" (anidado como la BD) en algo plano para Android
const formatProfile = (raw) => {
  const datos = first(first(raw.cliente)?.datos_confidenciales);
  const direccion = first(raw.direcciones_fisicas);
  const cp = first(direccion?.catalogo_de_cps);

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
    ocupacion: "", // la ocupación no está en el perfil: el usuario la escribe
    domicilio: cp?.codigo_postal ? `${domicilio} · C.P. ${cp.codigo_postal}` : domicilio,
    municipio: cp?.municipio ?? "",
    telefono: datos?.telefono ?? "",
    correo: datos?.correo ?? "",
  };
};

// ===== Rutas Cliente (solo LECTURAS; el envío lo agrega Lucca) =====

// GET /api/solicitudes/cliente/activa?idUsuario=...&idPerro=...
export const getActive = async (req, res) => {
  try {
    const { idUsuario, idPerro } = req.query;

    if (!idUsuario || !idPerro) {
      return res.status(400).json({
        success: false,
        message: "Faltan idUsuario o idPerro.",
      });
    }

    const activa = await adoptionRequestModel.getActiveRequest(idUsuario, idPerro);

    return res.status(200).json({
      success: true,
      // null = no tiene solicitud activa (puede empezar)
      data: activa
        ? {
            id_solicitud: activa.id_solicitud,
            estado: first(activa.estado_solicitud)?.descripcion ?? null,
          }
        : null,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al revisar la solicitud activa.",
    });
  }
};

// GET /api/solicitudes/cliente/perfil/:idUsuario
export const getProfile = async (req, res) => {
  try {
    const perfil = await profileModel.getProfile(req.params.idUsuario);

    return res.status(200).json({
      success: true,
      data: formatProfile(perfil),
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar el perfil.",
    });
  }
};
