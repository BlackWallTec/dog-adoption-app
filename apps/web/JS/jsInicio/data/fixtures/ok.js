export const resumen = {
  data: {
    solicitudes: { porRevisar: 1, enRevision: 1, masAntiguaDesde: "2026-11-16T15:04:00-06:00" },
    donativos: {
      porValidar: 2,
      montoPorValidar: 3500,
      masAntiguoDesde: "2026-11-22T10:00:00-06:00",
    },
    facturas: { pendientes: 2, requierenRevision: 1, correosConError: 1 },
    perros: { disponibles: 18, ingresosDelMes: 3 },
    voluntariado: { inscritosUltimos7Dias: 8 },
  },
  meta: { generadoEn: "2026-11-26T14:15:00-06:00", zonaHoraria: "America/Mexico_City" },
};

export const pendientes = {
  data: [
    {
      id: "c5555555-5555-4555-8555-555555555555",
      tipo: "correo_factura",
      estado: "Pendiente de atención",
      creadaEn: "2026-11-25T09:30:00-06:00",
      folio: null,
      persona: { nombre: "Luis", apellidos: "Torres" },
      perro: null,
      monto: 2000,
      requiereRevision: false,
      correoEstado: "Error",
    },
    {
      id: "f4444444-4444-4444-8444-444444444444",
      tipo: "factura",
      estado: "Pendiente de atención",
      creadaEn: "2026-11-26T10:00:00-06:00",
      folio: null,
      persona: { nombre: "Ana", apellidos: "López" },
      perro: null,
      monto: 800,
      requiereRevision: true,
      correoEstado: "Pendiente de envío",
    },
    {
      id: "a2222222-2222-4222-8222-222222222222",
      tipo: "adopcion",
      estado: "en_revision",
      creadaEn: "2026-11-16T15:04:00-06:00",
      folio: "QA-A2222222 ",
      persona: { nombre: "Sofía", apellidos: "García López" },
      perro: { id: "b6666666-6666-4666-8666-666666666666", nombre: "Bruno ", fotoUrl: null },
      monto: null,
    },
    {
      id: "d3333333-3333-4333-8333-333333333333",
      tipo: "donativo",
      estado: "Pendiente de validación",
      creadaEn: "2026-11-22T10:00:00-06:00",
      folio: null,
      persona: { nombre: "Mariana ", apellidos: "Ruiz " },
      perro: null,
      monto: 1500,
    },
    {
      id: "a1111111-1111-4111-8111-111111111111",
      tipo: "adopcion",
      estado: "por_revisar",
      creadaEn: "2026-11-25T15:04:00-06:00",
      folio: "QA-A1111111",
      persona: { nombre: "Pedro", apellidos: "Nava" },
      perro: { id: "b6666666-6666-4666-8666-666666666666", nombre: "Bruno", fotoUrl: null },
      monto: null,
    },
    {
      id: "f9999999-9999-4999-8999-999999999999",
      tipo: "factura",
      estado: "Pendiente de atención",
      creadaEn: "2026-11-25T15:04:00-06:00",
      folio: null,
      persona: { nombre: "Elena", apellidos: "Cruz" },
      perro: null,
      monto: 500,
      requiereRevision: false,
      correoEstado: "Pendiente de envío",
    },
    {
      id: "d0000000-0000-4000-8000-000000000001",
      tipo: "donativo",
      estado: "Pendiente de validación",
      creadaEn: "2026-11-26T12:00:00-06:00",
      folio: null,
      persona: { nombre: "Iván", apellidos: "Solís" },
      perro: null,
      monto: 2000,
    },
  ],
  meta: { total: 7, porTipo: { adopcion: 2, donativo: 2, factura: 3 } },
};

export const agenda = {
  success: true,
  data: {
    programas: [
      {
        idPrograma: "p8888888-8888-4888-8888-888888888888",
        nombre: "Paseo en el parque ",
        tipo: "Paseo",
        inicio: "2026-11-28 09:00:00",
        fin: "2026-11-28 12:00:00",
        ubicacion: "Parque Querétaro",
        cupoMaximo: 10,
        inscritos: 4,
        estado: "Disponible",
      },
    ],
    semanasSinCobertura: [{ inicio: "2026-11-30", fin: "2026-12-07" }],
  },
};

export const perro = {
  data: {
    perro: {
      id: "e7777777-7777-4777-8777-777777777777",
      nombre: "Canela",
      sexo: "Hembra",
      edadAprox: 4,
      tamano: "Mediano",
      fotoUrl: null,
      fechaIngreso: "2026-04-26T00:00:00-06:00",
    },
    diasEnRefugio: 214,
    solicitudesActivas: 0,
  },
};
