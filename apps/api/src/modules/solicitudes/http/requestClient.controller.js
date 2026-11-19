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

// ===== Consulta de mis solicitudes =====

// formato del figma para la id de solicitud (QA- +  los 8 primeros cáracteres del ID)

const folioDe = (idSolicitud) => `QA-${idSolicitud.slice(0, 8).toUpperCase()}`;

// La foto del animal (agarra la primera)
const fotoDe = (perro) => {
  const multimedia = perro?.multimedia || [];
  const foto =
    multimedia.find((item) => item.tipo?.trim().toLowerCase() === "foto") || multimedia[0];
  return foto?.url || null;
};

// Checa si alguno de los documentos fue rechazado en el proceso de solicitud

// Fila de datos que solo el cliente puede ver
const formatMiSolicitud = (fila) => {
  const perro = first(fila.perro);

  return {
    id_solicitud: fila.id_solicitud,
    folio: folioDe(fila.id_solicitud),
    fecha_solicitud: fila.fecha_solicitud,
    estado: first(fila.estado)?.descripcion?.trim() ?? null,
    // null = no hay perro
    perro: perro ? { nombre: perro.nombre, foto_url: fotoDe(perro) } : null,
    puede_editar: fila.puede_editar === true,
    comentario_admin: fila.comentario_admin?.trim() || null,
  };
};

// ===== Rutas Cliente (solo las lecturas de momento; el envío lo agrega Lucca) =====

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
    const perfil = await profileModel.getProfile(req.user.uuid);
    if (!perfil) {
      return res.status(404).json({
        success: false,
        message: "No se encontró el perfil.",
      });
    }

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

// GET /api/solicitudes/cliente/mis-solicitudes

export const getMisSolicitudes = async (req, res) => {
  try {
    const filas = await adoptionRequestModel.getByUser(req.user.uuid);

    // El Borrador vive en la app
    const enviadas = filas.filter((fila) => first(fila.estado)?.descripcion?.trim() !== "Borrador");

    return res.status(200).json({
      success: true,
      data: enviadas.map(formatMiSolicitud), // [] si no ha enviado
    });
  } catch (error) {
    console.error("getMisSolicitudes", error.message);
    return res.status(500).json({
      success: false,
      message: "No se pudieron consultar tus solicitudes.",
    });
  }
};
