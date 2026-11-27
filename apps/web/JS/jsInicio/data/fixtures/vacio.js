export const resumen = {
  data: {
    solicitudes: { porRevisar: 0, enRevision: 0, masAntiguaDesde: null },
    donativos: { porValidar: 0, montoPorValidar: 0, masAntiguoDesde: null },
    facturas: { pendientes: 0, requierenRevision: 0, correosConError: 0 },
    perros: { disponibles: 0, ingresosDelMes: 0 },
  },
  meta: { generadoEn: "2026-11-26T14:15:00-06:00", zonaHoraria: "America/Mexico_City" },
};

export const pendientes = {
  data: [],
  meta: { total: 0, porTipo: { adopcion: 0, donativo: 0, factura: 0 } },
};

export const agenda = { success: true, data: { programas: [], semanasSinCobertura: [] } };

export const perro = { data: null };
