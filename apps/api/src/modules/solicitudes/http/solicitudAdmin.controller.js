import SolicitudModel from "./solicitud.model.js";

const PAGE_SIZE = 10;

function normalizeStatus(status) {
  const value = String(status || "")
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase()
    .replace(/\s+/g, "_");
  return value === "pendiente" ? "por_revisar" : value;
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
