export function formatearMonto(monto) {
  if (monto === null || monto === undefined || monto === "") {
    return "—";
  }

  return new Intl.NumberFormat("es-MX", {
    style: "currency",
    currency: "MXN",
  }).format(monto);
}

export function formatearFecha(fecha) {
  if (!fecha) return "Sin fecha";

  const fechaValida = new Date(fecha);
  if (Number.isNaN(fechaValida.getTime())) {
    return "Sin fecha";
  }

  return new Intl.DateTimeFormat("es-MX", {
    dateStyle: "medium",
  }).format(fechaValida);
}

export function prepararFilaDonativo(donativo) {
  return [
    donativo.id ?? "",
    donativo.donante || "Sin nombre",
    formatearMonto(donativo.monto),
    formatearFecha(donativo.fecha),
    donativo.estado || "Sin estado",
  ];
}

export function obtenerMensajeListaVacia(donativos) {
  return donativos.length === 0 ? "No hay donativos pendientes." : null;
}
