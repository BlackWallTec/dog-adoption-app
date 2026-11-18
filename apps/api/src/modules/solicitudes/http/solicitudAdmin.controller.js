import SolicitudModel from "./solicitud.model.js";

const PAGE_SIZE = 10;

function normalizeStatus(status) {
  const value = String(status || "")
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase()
    .replace(/\s+/g, "_");

  // Mappear los distinto valores de la BD
  const statusMap = {
    pendiente: "por_revisar",
    por_revisar: "por_revisar",
    en_revision: "en_revision",
    pendiente_de_revision: "por_revisar",
    en_periodo_de_prueba: "en_revision",
    aprobada: "aprobada",
    aprobada_definitiva: "aprobada",
    rechazada: "rechazada",
    borrador: "por_revisar",
  };

  return statusMap[value] || value;
}

function mapSolicitud(solicitud) {
  const image =
    (solicitud.perro?.multimedia || []).find(
      (item) => item.tipo?.trim().toLowerCase() === "foto",
    ) || solicitud.perro?.multimedia?.[0];
  const status = normalizeStatus(solicitud.estado?.descripcion);

  return {
    id: solicitud.id_solicitud,
    folio: `QA-${solicitud.id_solicitud.slice(0, 8).toUpperCase()}`,
    fecha: solicitud.fecha_solicitud,
    estado: status,
    adoptante: {
      nombre: solicitud.usuario?.nombre || "",
      apellidos: solicitud.usuario?.apellidos || "",
      email: solicitud.usuario?.cliente?.datos_confidenciales?.correo || "",
    },
    perro: {
      id: solicitud.perro?.id_perro || solicitud.id_perro,
      nombre: solicitud.perro?.nombre || "",
      foto_url: image?.url || "",
    },
  };
}

function matchesStatus(row, status) {
  return status === "pendientes"
    ? ["por_revisar", "en_revision"].includes(row.estado)
    : row.estado === status;
}

function createSummary(rows) {
  return {
    todas: rows.length,
    pendientes: rows.filter((row) => ["por_revisar", "en_revision"].includes(row.estado)).length,
    aprobadas: rows.filter((row) => row.estado === "aprobada").length,
    rechazadas: rows.filter((row) => row.estado === "rechazada").length,
  };
}

export async function getSolicitudesAdmin(req, res) {
  const { perro = "todos", estado = "todos", page = "1" } = req.query;
  const pageNumber = Number(page);

  if (!Number.isInteger(pageNumber) || pageNumber < 1) {
    return res.status(400).json({ error: "El número de página no es válido." });
  }

  try {
    const rows = (await SolicitudModel.getAll()).map(mapSolicitud);
    const resumen = createSummary(rows);
    const filtered = rows.filter((row) => {
      const dogMatches = perro === "todos" || row.perro.id === perro;
      const statusMatches = estado === "todos" || matchesStatus(row, estado);
      return dogMatches && statusMatches;
    });
    const start = (pageNumber - 1) * PAGE_SIZE;

    return res.json({
      data: filtered.slice(start, start + PAGE_SIZE),
      resumen,
      paginacion: {
        page: pageNumber,
        page_size: PAGE_SIZE,
        total: filtered.length,
      },
    });
  } catch (error) {
    console.error("Error al cargar solicitudes:", error);
    return res.status(500).json({ error: "No se pudieron cargar las solicitudes." });
  }
}

// HU05: detalle

const first = (value) => (Array.isArray(value) ? (value[0] ?? null) : (value ?? null));

// true/false/null → "Sí" / "No" / null (null = "Sin respuesta" en la web)
const siNo = (value) => (value === true ? "Sí" : value === false ? "No" : null);

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function mapDetalle(s) {
  const usuario = s.usuario || {};
  const datos = first(first(usuario.cliente)?.datos_confidenciales);
  const direccion = first(usuario.direcciones_fisicas);
  const cp = first(direccion?.catalogo_de_cps);
  const perro = s.perro || {};
  const fotos = perro.multimedia || [];
  const foto = fotos.find((m) => m.tipo?.trim().toLowerCase() === "foto") || fotos[0];

  const domicilio = [
    [direccion?.calle, direccion?.numero_exterior].filter(Boolean).join(" "),
    direccion?.numero_interior ? `Int. ${direccion.numero_interior}` : null,
    cp?.colonia ? `Col. ${cp.colonia}` : null,
    cp?.municipio,
    cp?.estado,
    cp?.codigo_postal ? `C.P. ${cp.codigo_postal}` : null,
  ]
    .filter(Boolean)
    .join(", ");

  return {
    id: s.id_solicitud,
    folio: `QA-${s.id_solicitud.slice(0, 8).toUpperCase()}`,
    fecha: s.fecha_solicitud,
    estado: normalizeStatus(s.estado?.descripcion),
    adoptante: {
      nombre_completo: `${usuario.nombre || ""} ${usuario.apellidos || ""}`.trim(),
      fecha_nacimiento: usuario.fecha_nacimiento,
      ocupacion: s.ocupacion,
      correo: datos?.correo || "",
      telefono: datos?.telefono || "",
      domicilio,
    },
    perro: {
      nombre: perro.nombre || "",
      sexo: perro.sexo?.trim() || "",
      edad_aprox: perro.edad_aprox,
      raza: first(perro.raza)?.nombre || "",
      tamano: first(perro.tamano)?.nombre || "",
      estado: perro.estado?.trim() || "",
      foto_url: foto?.url || "",
    },

    // Respuestas puestas como en el formulario de la app de android
    secciones: [
      {
        titulo: "Tu hogar",
        respuestas: [
          { pregunta: "¿Has tenido mascotas antes?", respuesta: siNo(s.ha_tenido_mascotas) },
          { pregunta: "¿Tienes otros animales en casa?", respuesta: siNo(s.otras_mascotas) },
          { pregunta: "Sobre sus mascotas", respuesta: s.sobre_mascotas },
          { pregunta: "¿Por qué deseas adoptar?", respuesta: s.motivo },
          { pregunta: "¿Dónde vivirá y dormirá?", respuesta: s.donde_vivira },
          { pregunta: "Personas en el hogar", respuesta: s.integrantes_hogar },
          { pregunta: "¿Todos están de acuerdo?", respuesta: siNo(s.todos_de_acuerdo) },
          { pregunta: "Tipo de vivienda", respuesta: s.tipo_vivienda },
          { pregunta: "¿Jardín o patio?", respuesta: s.jardin_o_patio },
          { pregunta: "Si renta, ¿el casero acepta mascotas?", respuesta: siNo(s.casero_acepta) },
        ],
      },
      {
        titulo: "Compromiso",
        respuestas: [
          { pregunta: "Cuidados básicos", respuesta: s.cuidados_basicos },
          { pregunta: "Opinión sobre esterilización", respuesta: s.opinion_esterilizacion },
          { pregunta: "¿Tiene veterinario?", respuesta: siNo(s.tiene_veterinario) },
          { pregunta: "Veterinario", respuesta: s.detalle_veterinario },
          { pregunta: "Tiempo que pasará solo", respuesta: s.tiempo_solo },
          { pregunta: "Rutina diaria", respuesta: s.rutina_diaria },
          { pregunta: "Manejo de comportamiento", respuesta: s.manejo_comportamiento },
        ],
      },
    ],
    documentos: (s.documentos || [])
      .map((fila) => first(fila.documento))
      .filter(Boolean)
      .map((doc) => ({
        id: doc.id_documento,
        tipo: doc.tipo?.trim() || "",
        url: doc.url_archivo || "",
        estado_revision: doc.estado_revision?.trim() || "",
      })),
  };
}

// GET /api/solicitudes/admin/solicitudes/:id
export async function getSolicitudAdminDetalle(req, res) {
  const { id } = req.params;

  if (!UUID_REGEX.test(id)) {
    return res.status(400).json({ error: "El id de la solicitud no es válido." });
  }

  try {
    const solicitud = await SolicitudModel.getById(id);

    if (!solicitud) {
      return res.status(404).json({ error: "No se encontró la solicitud." });
    }

    return res.json({ data: mapDetalle(solicitud) });
  } catch (error) {
    console.error("Error al cargar el detalle:", error);
    return res.status(500).json({ error: "No se pudo cargar el detalle de la solicitud." });
  }
}
