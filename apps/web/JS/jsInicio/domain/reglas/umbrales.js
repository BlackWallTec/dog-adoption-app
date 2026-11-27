/** D7. Solicitudes 3 días, donativos 2, facturas 3. */
export const UMBRALES_SLA_DIAS = Object.freeze({
  adopcion: 3,
  donativo: 2,
  factura: 3,
  correo_factura: 3,
});

/** Menor número gana el empate de antigüedad. */
export const PESO_TIPO = Object.freeze({
  adopcion: 0,
  donativo: 1,
  factura: 2,
  correo_factura: 2,
});
