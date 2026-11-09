const dogs = [
  { id: 12, nombre: "Bruno", foto_url: "/dog-sample.png" },
  { id: 13, nombre: "Toby", foto_url: "/dog-sample.png" },
  { id: 14, nombre: "Nala", foto_url: "/dog-sample.png" },
  { id: 15, nombre: "Leo", foto_url: "/dog-sample.png" },
  { id: 16, nombre: "Luna", foto_url: "/dog-sample.png" },
  { id: 17, nombre: "Milo", foto_url: "/dog-sample.png" },
];

const rows = [
  [
    "QA-0248",
    "Sofía",
    "García López",
    "sofia.garcia@correo.com",
    12,
    "Bruno",
    "2026-10-12T15:04:00Z",
    "en_revision",
  ],
  [
    "QA-0247",
    "Diego",
    "Ramírez",
    "diego.ramirez@correo.com",
    13,
    "Toby",
    "2026-10-12T12:00:00Z",
    "por_revisar",
  ],
  [
    "QA-0246",
    "Valeria",
    "Mendoza",
    "valeria.m@correo.com",
    14,
    "Nala",
    "2026-10-11T12:00:00Z",
    "por_revisar",
  ],
  [
    "QA-0245",
    "Andrés",
    "Pérez",
    "andres.p@correo.com",
    15,
    "Leo",
    "2026-10-10T12:00:00Z",
    "en_revision",
  ],
  [
    "QA-0231",
    "Sofía",
    "García López",
    "sofia.garcia@correo.com",
    16,
    "Luna",
    "2026-09-28T12:00:00Z",
    "aprobada",
  ],
  [
    "QA-0210",
    "Sofía",
    "García López",
    "sofia.garcia@correo.com",
    17,
    "Milo",
    "2026-09-18T12:00:00Z",
    "rechazada",
  ],
].map(([folio, nombre, apellidos, email, perroId, perroNombre, fecha, estado], index) => ({
  id: 248 - index,
  folio,
  fecha,
  estado,
  adoptante: { nombre, apellidos, email },
  perro: { id: perroId, nombre: perroNombre, foto_url: dogs[index].foto_url },
}));

export function getMockDogs() {
  return dogs;
}

export function getMockResponse(filters, mode = "ok") {
  if (mode === "error") throw new Error("mock error");
  if (mode === "401" || mode === "403") return { status: Number(mode) };
  const filtered = rows.filter((row) => {
    const perroMatch = filters.perro === "todos" || String(row.perro.id) === String(filters.perro);
    const estadoMatch =
      filters.estado === "todos" ||
      (filters.estado === "pendientes" && ["por_revisar", "en_revision"].includes(row.estado)) ||
      row.estado === filters.estado;
    return perroMatch && estadoMatch;
  });
  return {
    data: mode === "vacio" ? [] : filtered,
    resumen: { todas: rows.length, pendientes: 4, aprobadas: 1, rechazadas: 1 },
    paginacion: {
      page: filters.page,
      page_size: 10,
      total: mode === "vacio" ? 0 : filtered.length,
    },
  };
}

export { rows };
