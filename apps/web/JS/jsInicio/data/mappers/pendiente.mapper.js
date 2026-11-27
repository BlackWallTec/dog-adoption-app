import { INTERNAL } from "@qarino/shared/errores.js";
import { entero, fechaObligatoria, texto } from "./texto.js";

const TIPOS = new Set(["adopcion", "donativo", "factura", "correo_factura"]);

/** @param {unknown} persona */
function nombrePersona(persona) {
  if (!persona || typeof persona !== "object") throw new INTERNAL("Falta la persona.");
  const datos = /** @type {{ nombre?: unknown, apellidos?: unknown }} */ (persona);
  const partes = [texto(datos.nombre), texto(datos.apellidos)].filter(Boolean);
  if (!partes.length) throw new INTERNAL("Falta el nombre.");
  return partes.join(" ");
}

/** @param {unknown} perro */
function perroDe(perro) {
  if (perro == null) return null;
  if (typeof perro !== "object") throw new INTERNAL("El perro del pendiente está incompleto.");
  const datos = /** @type {{ id?: unknown, nombre?: unknown }} */ (perro);
  const id = texto(datos.id);
  const nombre = texto(datos.nombre);
  if (!id || !nombre) throw new INTERNAL("El perro del pendiente está incompleto.");
  return { id, nombre };
}

/**
 * Conserva `requiereRevision` y `correoEstado` para la regla de prioridad.
 * El repositorio los quita al devolver la entidad.
 * @param {unknown} item
 */
export function mapearPendiente(item) {
  if (!item || typeof item !== "object") throw new INTERNAL("Un pendiente está incompleto.");
  const dto = /** @type {Record<string, unknown>} */ (item);
  const id = texto(dto.id);
  if (!id) throw new INTERNAL("Falta el id del pendiente.");
  if (typeof dto.tipo !== "string" || !TIPOS.has(dto.tipo)) {
    throw new INTERNAL("Tipo de pendiente desconocido.");
  }

  return {
    id,
    tipo: /** @type {import("../../domain/entities/tareaPendiente.js").TipoTarea} */ (dto.tipo),
    persona: { nombre: nombrePersona(dto.persona) },
    perro: perroDe(dto.perro),
    folio: texto(dto.folio),
    monto: dto.monto == null ? null : entero(dto.monto, "monto"),
    creadaEn: fechaObligatoria(dto.creadaEn, "creadaEn"),
    requiereRevision: dto.requiereRevision === true,
    correoEstado: texto(dto.correoEstado),
  };
}

/** @param {unknown} sobre */
export function listaDePendientes(sobre) {
  if (!sobre || typeof sobre !== "object" || !("data" in sobre)) {
    throw new INTERNAL("Los pendientes no traen una lista.");
  }
  const data = /** @type {{ data?: unknown }} */ (sobre).data;
  if (!Array.isArray(data)) throw new INTERNAL("Los pendientes no traen una lista.");
  return data.map(mapearPendiente);
}
