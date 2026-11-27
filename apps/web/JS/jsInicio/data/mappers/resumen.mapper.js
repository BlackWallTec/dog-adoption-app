import { entero, fecha, fechaObligatoria } from "./texto.js";
import { INTERNAL } from "@qarino/shared/errores.js";

/** @param {unknown} sobre @returns {import("../../domain/entities/resumen.js").ResumenInicio} */
export function mapearResumen(sobre) {
  if (!sobre || typeof sobre !== "object") throw new INTERNAL("El resumen no trae data y meta.");
  const data = /** @type {{ data?: unknown, meta?: unknown }} */ (sobre).data;
  const meta = /** @type {{ meta?: { generadoEn?: unknown } }} */ (sobre).meta;
  if (!data || typeof data !== "object" || !meta || typeof meta !== "object") {
    throw new INTERNAL("El resumen no trae data y meta.");
  }

  const cuerpo = /** @type {Record<string, Record<string, unknown>>} */ (data);
  const solicitudes = cuerpo.solicitudes ?? {};
  const donativos = cuerpo.donativos ?? {};
  const facturas = cuerpo.facturas ?? {};
  const perros = cuerpo.perros ?? {};

  return {
    solicitudes: {
      porRevisar: entero(solicitudes.porRevisar, "solicitudes.porRevisar"),
      enRevision: entero(solicitudes.enRevision, "solicitudes.enRevision"),
      masAntiguaDesde: fecha(solicitudes.masAntiguaDesde, "solicitudes.masAntiguaDesde"),
    },
    donativos: {
      porValidar: entero(donativos.porValidar, "donativos.porValidar"),
      montoPorValidar: entero(donativos.montoPorValidar, "donativos.montoPorValidar"),
      masAntiguoDesde: fecha(donativos.masAntiguoDesde, "donativos.masAntiguoDesde"),
    },
    facturas: {
      pendientes: entero(facturas.pendientes, "facturas.pendientes"),
      requierenRevision: entero(facturas.requierenRevision, "facturas.requierenRevision"),
      correosConError: entero(facturas.correosConError, "facturas.correosConError"),
    },
    perros: {
      disponibles: entero(perros.disponibles, "perros.disponibles"),
      ingresosDelMes: entero(perros.ingresosDelMes, "perros.ingresosDelMes"),
    },
    generadoEn: fechaObligatoria(
      /** @type {{ generadoEn?: unknown }} */ (meta).generadoEn,
      "generadoEn",
    ),
  };
}
