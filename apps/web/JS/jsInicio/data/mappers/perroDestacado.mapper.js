import { INTERNAL } from "@qarino/shared/errores.js";
import { entero, fechaObligatoria, texto } from "./texto.js";

/** @param {unknown} sobre @returns {import("../../domain/entities/perroDestacado.js").PerroDestacado | null} */
export function mapearPerroDestacado(sobre) {
  if (!sobre || typeof sobre !== "object" || !("data" in sobre)) {
    throw new INTERNAL("Falta el perro destacado.");
  }
  const data = /** @type {{ data: unknown }} */ (sobre).data;
  if (data == null) return null;
  if (typeof data !== "object") throw new INTERNAL("Falta el perro destacado.");

  const cuerpo =
    /** @type {{ perro?: unknown, diasEnRefugio?: unknown, solicitudesActivas?: unknown }} */ (
      data
    );
  if (!cuerpo.perro || typeof cuerpo.perro !== "object")
    throw new INTERNAL("Falta el perro destacado.");
  const perro = /** @type {Record<string, unknown>} */ (cuerpo.perro);
  const id = texto(perro.id);
  const nombre = texto(perro.nombre);
  if (!id || !nombre) throw new INTERNAL("Falta el perro destacado.");

  return {
    id,
    nombre,
    sexo: texto(perro.sexo),
    edadAprox: perro.edadAprox == null ? null : entero(perro.edadAprox, "edadAprox"),
    tamano: texto(perro.tamano),
    fotoUrl: texto(perro.fotoUrl),
    fechaIngreso: fechaObligatoria(perro.fechaIngreso, "fechaIngreso"),
    diasEnRefugio: entero(cuerpo.diasEnRefugio, "diasEnRefugio"),
    solicitudesActivas: entero(cuerpo.solicitudesActivas, "solicitudesActivas"),
  };
}
