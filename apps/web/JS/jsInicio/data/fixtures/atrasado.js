const creadaEn = "2026-11-01T10:00:00-06:00";

export const resumen = {
  data: {
    solicitudes: { porRevisar: 1, enRevision: 0, masAntiguaDesde: creadaEn },
    donativos: { porValidar: 1, montoPorValidar: 400, masAntiguoDesde: creadaEn },
    facturas: { pendientes: 1, requierenRevision: 1, correosConError: 0 },
    perros: { disponibles: 18, ingresosDelMes: 0 },
  },
  meta: { generadoEn: "2026-11-26T14:15:00-06:00", zonaHoraria: "America/Mexico_City" },
};

export const pendientes = {
  data: [
    {
      id: "a2222222-2222-4222-8222-222222222222",
      tipo: "adopcion",
      estado: "por_revisar",
      creadaEn,
      folio: "QA-A2222222",
      persona: { nombre: "Sofía", apellidos: "García" },
      perro: { id: "b6666666-6666-4666-8666-666666666666", nombre: "Bruno", fotoUrl: null },
      monto: null,
    },
    {
      id: "d3333333-3333-4333-8333-333333333333",
      tipo: "donativo",
      estado: "Pendiente de validación",
      creadaEn,
      folio: null,
      persona: { nombre: "Mariana", apellidos: "Ruiz" },
      perro: null,
      monto: 400,
    },
    {
      id: "f4444444-4444-4444-8444-444444444444",
      tipo: "factura",
      estado: "Pendiente de atención",
      creadaEn,
      folio: null,
      persona: { nombre: "Ana", apellidos: "López" },
      perro: null,
      monto: 800,
      requiereRevision: true,
      correoEstado: "Pendiente de envío",
    },
  ],
  meta: { total: 3, porTipo: { adopcion: 1, donativo: 1, factura: 1 } },
};

export const agenda = { success: true, data: { programas: [], semanasSinCobertura: [] } };

export const perro = { data: null };
