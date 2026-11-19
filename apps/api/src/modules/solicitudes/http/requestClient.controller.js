import adoptionRequestModel from "./modelosSolicitudes/adoptionRequest.model.js";
import profileModel from "./modelosSolicitudes/profile.model.js";

// ===== Funciones de apoyo (no son rutas) =====
const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

// Supabase a veces regresa una relación como objeto y a veces como lista
const first = (value) => (Array.isArray(value) ? (value[0] ?? null) : (value ?? null));

const normalizeHouseType = (value) => {
  if (typeof value !== "string") return "";
  const normalized = value.trim().toLowerCase();
  if (normalized === "casa") return "Casa";
  if (normalized === "depto" || normalized === "departamento") return "Depto";
  if (normalized === "otro") return "Otro";
  return value.trim();
};

const validateStringField = (
  fields,
  key,
  value,
  { required = true, maxLength, emptyMsg, maxMsg },
) => {
  if (value === undefined || value === null) {
    if (required) fields[key] = emptyMsg;
    return;
  }
  if (typeof value !== "string") {
    fields[key] = emptyMsg;
    return;
  }
  const trimmed = value.trim();
  if (required && trimmed.length === 0) {
    fields[key] = emptyMsg;
    return;
  }
  if (maxLength && trimmed.length > maxLength) {
    fields[key] = maxMsg;
  }
};

const validateBooleanField = (fields, key, value, { required = true, missingMsg }) => {
  if (value === undefined || value === null) {
    if (required) fields[key] = missingMsg;
    return;
  }
  if (typeof value !== "boolean") {
    fields[key] = missingMsg;
  }
};

const validateDraftBody = (body) => {
  const fields = {};

  if (typeof body.idPerro !== "string" || !UUID_REGEX.test(body.idPerro.trim())) {
    fields.idPerro = "El identificador del perro es obligatorio y debe ser un UUID válido.";
  }

  validateStringField(fields, "ocupacion", body.ocupacion, {
    maxLength: 100,
    emptyMsg: "Faltan los datos generales (ocupación).",
    maxMsg: "La ocupación no puede superar los 100 caracteres.",
  });

  validateBooleanField(fields, "tuvoMascotas", body.tuvoMascotas, {
    missingMsg: "Falta indicar si has tenido mascotas.",
  });

  validateBooleanField(fields, "otrasMascotas", body.otrasMascotas, {
    missingMsg: "Falta indicar si tienes otros animales.",
  });

  validateStringField(fields, "motivoAdopcion", body.motivoAdopcion, {
    maxLength: 100,
    emptyMsg: "Falta responder por qué deseas adoptar.",
    maxMsg: "El motivo de adopción no puede superar los 100 caracteres.",
  });

  validateStringField(fields, "dondeVivira", body.dondeVivira, {
    maxLength: 100,
    emptyMsg: "Falta responder dónde vivirá el perrito.",
    maxMsg: "La respuesta de dónde vivirá no puede superar los 100 caracteres.",
  });

  if (
    typeof body.personasEnCasa !== "number" ||
    !Number.isInteger(body.personasEnCasa) ||
    body.personasEnCasa < 1
  ) {
    fields.personasEnCasa = "El número de personas en casa debe ser al menos 1.";
  }

  validateBooleanField(fields, "todosDeAcuerdo", body.todosDeAcuerdo, {
    missingMsg: "Falta indicar si todos están de acuerdo.",
  });

  const tipoViviendaNormalizado = normalizeHouseType(body.tipoVivienda);
  if (!tipoViviendaNormalizado) {
    fields.tipoVivienda = "Falta indicar el tipo de vivienda.";
  }

  validateStringField(fields, "jardinPatio", body.jardinPatio, {
    maxLength: 50,
    emptyMsg: "Falta indicar si tienes jardín o patio.",
    maxMsg: "La respuesta de jardín o patio no puede superar los 50 caracteres.",
  });

  const esCasa = tipoViviendaNormalizado.toLowerCase() === "casa";
  validateBooleanField(fields, "caseroAcepta", body.caseroAcepta, {
    required: !esCasa,
    missingMsg: "Si rentas o vives en departamento/otro, indica si tu casero acepta mascotas.",
  });

  const hasPets = body.tuvoMascotas === true || body.otrasMascotas === true;
  validateStringField(fields, "sobreMascotas", body.sobreMascotas, {
    required: hasPets,
    maxLength: 250,
    emptyMsg: "Falta contar sobre tus mascotas.",
    maxMsg: "La descripción sobre tus mascotas no puede superar los 250 caracteres.",
  });

  validateStringField(fields, "cuidadosBasicos", body.cuidadosBasicos, {
    maxLength: 250,
    emptyMsg: "Falta indicar los cuidados básicos.",
    maxMsg: "Los cuidados básicos no pueden superar los 250 caracteres.",
  });

  validateStringField(fields, "opinionEsterilizacion", body.opinionEsterilizacion, {
    maxLength: 250,
    emptyMsg: "Falta la opinión sobre esterilización.",
    maxMsg: "La opinión sobre esterilización no puede superar los 250 caracteres.",
  });

  validateBooleanField(fields, "tieneVeterinario", body.tieneVeterinario, {
    missingMsg: "Falta indicar si tienes veterinario de confianza.",
  });

  validateStringField(fields, "detalleVeterinario", body.detalleVeterinario, {
    maxLength: 250,
    emptyMsg: "Falta el detalle del veterinario.",
    maxMsg: "El detalle del veterinario no puede superar los 250 caracteres.",
  });

  validateStringField(fields, "tiempoSolo", body.tiempoSolo, {
    maxLength: 250,
    emptyMsg: "Falta indicar el tiempo que estaría sola la mascota.",
    maxMsg: "El tiempo solo no puede superar los 250 caracteres.",
  });

  validateStringField(fields, "rutinaDiaria", body.rutinaDiaria, {
    maxLength: 250,
    emptyMsg: "Falta describir la rutina diaria.",
    maxMsg: "La rutina diaria no puede superar los 250 caracteres.",
  });

  validateStringField(fields, "manejoComportamiento", body.manejoComportamiento, {
    maxLength: 250,
    emptyMsg: "Falta indicar el manejo de comportamiento.",
    maxMsg: "El manejo de comportamiento no puede superar los 250 caracteres.",
  });

  return fields;
};

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

// POST /api/solicitudes/cliente
export const createDraft = async (req, res) => {
  try {
    const idUsuario = req.user.uuid;
    const rawBody = req.body && typeof req.body === "object" ? req.body : {};

    // Extraer solo campos permitidos (acepta camelCase y snake_case de Android; ignora extra fields y nunca acepta idUsuario)
    const allowedInput = {
      idPerro: rawBody.idPerro ?? rawBody.id_perro,
      ocupacion: rawBody.ocupacion,
      tuvoMascotas: rawBody.tuvoMascotas ?? rawBody.tuvo_mascotas,
      otrasMascotas: rawBody.otrasMascotas ?? rawBody.otras_mascotas,
      motivoAdopcion: rawBody.motivoAdopcion ?? rawBody.motivo_adopcion,
      dondeVivira: rawBody.dondeVivira ?? rawBody.donde_vivira,
      personasEnCasa: rawBody.personasEnCasa ?? rawBody.personas_en_casa,
      todosDeAcuerdo: rawBody.todosDeAcuerdo ?? rawBody.todos_de_acuerdo,
      tipoVivienda: rawBody.tipoVivienda ?? rawBody.tipo_vivienda,
      jardinPatio: rawBody.jardinPatio ?? rawBody.jardin_patio,
      caseroAcepta: rawBody.caseroAcepta ?? rawBody.casero_acepta,
      sobreMascotas: rawBody.sobreMascotas ?? rawBody.sobre_mascotas,
      cuidadosBasicos: rawBody.cuidadosBasicos ?? rawBody.cuidados_basicos,
      opinionEsterilizacion: rawBody.opinionEsterilizacion ?? rawBody.opinion_esterilizacion,
      tieneVeterinario: rawBody.tieneVeterinario ?? rawBody.tiene_veterinario,
      detalleVeterinario: rawBody.detalleVeterinario ?? rawBody.detalle_veterinario,
      tiempoSolo: rawBody.tiempoSolo ?? rawBody.tiempo_solo,
      rutinaDiaria: rawBody.rutinaDiaria ?? rawBody.rutina_diaria,
      manejoComportamiento: rawBody.manejoComportamiento ?? rawBody.manejo_comportamiento,
    };

    const validationErrors = validateDraftBody(allowedInput);
    if (Object.keys(validationErrors).length > 0) {
      return res.status(400).json({
        error: "Datos inválidos.",
        code: "VALIDATION_ERROR",
        fields: validationErrors,
      });
    }

    const idPerro = allowedInput.idPerro.trim();

    const submitted = await adoptionRequestModel.getSubmittedRequest(idUsuario, idPerro);
    if (submitted) {
      return res.status(409).json({
        success: false,
        message: "Ya tienes una solicitud enviada o activa para este perro.",
      });
    }

    const dbPayload = {
      ocupacion: allowedInput.ocupacion.trim(),
      ha_tenido_mascotas: allowedInput.tuvoMascotas,
      otras_mascotas: allowedInput.otrasMascotas,
      motivo: allowedInput.motivoAdopcion.trim(),
      donde_vivira: allowedInput.dondeVivira.trim(),
      integrantes_hogar: allowedInput.personasEnCasa,
      todos_de_acuerdo: allowedInput.todosDeAcuerdo,
      tipo_vivienda: normalizeHouseType(allowedInput.tipoVivienda),
      jardin_o_patio: allowedInput.jardinPatio.trim(),
      casero_acepta:
        typeof allowedInput.caseroAcepta === "boolean" ? allowedInput.caseroAcepta : null,
      sobre_mascotas:
        typeof allowedInput.sobreMascotas === "string" && allowedInput.sobreMascotas.trim()
          ? allowedInput.sobreMascotas.trim()
          : null,
      cuidados_basicos: allowedInput.cuidadosBasicos.trim(),
      opinion_esterilizacion: allowedInput.opinionEsterilizacion.trim(),
      tiene_veterinario: allowedInput.tieneVeterinario,
      detalle_veterinario: allowedInput.detalleVeterinario.trim(),
      tiempo_solo: allowedInput.tiempoSolo.trim(),
      rutina_diaria: allowedInput.rutinaDiaria.trim(),
      manejo_comportamiento: allowedInput.manejoComportamiento.trim(),
      puede_editar: true,
    };

    const existingDraft = await adoptionRequestModel.getDraftRequest(idUsuario, idPerro);

    if (existingDraft) {
      const updated = await adoptionRequestModel.updateDraft(
        existingDraft.id_solicitud,
        idUsuario,
        dbPayload,
      );

      return res.status(200).json({
        success: true,
        data: {
          id_solicitud: updated.id_solicitud,
          estado: "Borrador",
        },
      });
    }

    const idEstadoBorrador = await adoptionRequestModel.getStatusId("Borrador");
    const created = await adoptionRequestModel.createDraft({
      ...dbPayload,
      id_usuario: idUsuario,
      id_perro: idPerro,
      id_estado: idEstadoBorrador,
    });

    return res.status(201).json({
      success: true,
      data: {
        id_solicitud: created.id_solicitud,
        estado: "Borrador",
      },
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al guardar el borrador.",
    });
  }
};

// GET /api/solicitudes/cliente/activa?idUsuario=...&idPerro=...
export const getActive = async (req, res) => {
  try {
    const { idPerro } = req.query;
    const idUsuario = req.user.uuid;

    if (!idPerro) {
      return res.status(400).json({
        success: false,
        message: "Faltan idPerro.",
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

// POST /api/solicitudes/:id/enviar
export const submitSolicitud = async (req, res) => {
  try {
    const idUsuario = req.user.uuid;
    const { id: idSolicitud } = req.params;

    if (!UUID_REGEX.test(String(idSolicitud || ""))) {
      return res.status(400).json({
        success: false,
        message: "El identificador de la solicitud no es válido.",
      });
    }

    const solicitud = await adoptionRequestModel.getRequestWithDocuments(idSolicitud, idUsuario);
    if (!solicitud) {
      return res.status(404).json({
        success: false,
        message: "No se encontró la solicitud.",
      });
    }

    const estadoActual = first(solicitud.estado)?.descripcion?.trim();
    if (estadoActual !== "Borrador" && solicitud.puede_editar !== true) {
      return res.status(409).json({
        success: false,
        message: "Esta solicitud ya fue enviada o no puede modificarse.",
      });
    }

    const fieldErrors = validateDraftBody({
      idPerro: solicitud.id_perro,
      ocupacion: solicitud.ocupacion,
      tuvoMascotas: solicitud.ha_tenido_mascotas,
      otrasMascotas: solicitud.otras_mascotas,
      motivoAdopcion: solicitud.motivo,
      dondeVivira: solicitud.donde_vivira,
      personasEnCasa: solicitud.integrantes_hogar,
      todosDeAcuerdo: solicitud.todos_de_acuerdo,
      tipoVivienda: solicitud.tipo_vivienda,
      jardinPatio: solicitud.jardin_o_patio,
      caseroAcepta: solicitud.casero_acepta,
      sobreMascotas: solicitud.sobre_mascotas,
      cuidadosBasicos: solicitud.cuidados_basicos,
      opinionEsterilizacion: solicitud.opinion_esterilizacion,
      tieneVeterinario: solicitud.tiene_veterinario,
      detalleVeterinario: solicitud.detalle_veterinario,
      tiempoSolo: solicitud.tiempo_solo,
      rutinaDiaria: solicitud.rutina_diaria,
      manejoComportamiento: solicitud.manejo_comportamiento,
    });

    const tiposAdjuntos = new Set(
      (solicitud.documentos || [])
        .map((rel) => first(rel.documento)?.tipo?.trim().toUpperCase())
        .filter(Boolean),
    );

    if (!tiposAdjuntos.has("INE") || !tiposAdjuntos.has("DOMICILIO")) {
      fieldErrors.documentos = "Faltan documentos obligatorios (INE y Comprobante de domicilio).";
    }

    if (Object.keys(fieldErrors).length > 0) {
      return res.status(400).json({
        error: "Datos inválidos.",
        code: "VALIDATION_ERROR",
        fields: fieldErrors,
      });
    }

    const idEstadoPendiente = await adoptionRequestModel.getStatusId("Pendiente de Revisión");
    await adoptionRequestModel.submitDraft(idSolicitud, idUsuario, idEstadoPendiente);

    return res.status(200).json({
      success: true,
      message: "Solicitud enviada correctamente.",
      data: {
        id_solicitud: idSolicitud,
        estado: "Pendiente de Revisión",
      },
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al enviar la solicitud.",
    });
  }
};
