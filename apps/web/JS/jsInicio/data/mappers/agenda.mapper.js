import { INTERNAL } from "@qarino/shared/errores.js";
import { entero, fechaObligatoria, texto } from "./texto.js";

const ESTADOS = new Set(["Disponible", "Lleno", "Finalizado", "Cancelado"]);

/** @param {unknown} item */
function mapearJornada(item) {
  if (!item || typeof item !== "object") throw new INTERNAL("Una jornada está incompleta.");
  const dto = /** @type {Record<string, unknown>} */ (item);
  const id = texto(dto.idPrograma);
  const nombre = texto(dto.nombre);
  if (!id || !nombre) throw new INTERNAL("Una jornada está incompleta.");
  if (typeof dto.estado !== "string" || !ESTADOS.has(dto.estado)) {
    throw new INTERNAL("Estado de jornada desconocido.");
  }

  return {
    id,
    nombre,
    tipo: texto(dto.tipo),
    inicio: fechaObligatoria(dto.inicio, "inicio"),
    fin: fechaObligatoria(dto.fin, "fin"),
    ubicacion: texto(dto.ubicacion),
    cupoMaximo: dto.cupoMaximo == null ? null : entero(dto.cupoMaximo, "cupoMaximo"),
    inscritos: entero(dto.inscritos, "inscritos"),
    estado: /** @type {import("../../domain/entities/agenda.js").EstadoJornada} */ (dto.estado),
  };
}

/** @param {unknown} item */
function mapearSemana(item) {
  if (!item || typeof item !== "object")
    throw new INTERNAL("Una semana sin cobertura está incompleta.");
  const dto = /** @type {{ inicio?: unknown, fin?: unknown }} */ (item);
  const inicio = texto(dto.inicio);
  const fin = texto(dto.fin);
  if (!inicio || !fin) throw new INTERNAL("Una semana sin cobertura está incompleta.");
  return { inicio, fin };
}

/** @param {unknown} sobre @returns {import("../../domain/entities/agenda.js").Agenda} */
export function mapearAgenda(sobre) {
  if (!sobre || typeof sobre !== "object") throw new INTERNAL("La agenda no trae programas.");
  if (/** @type {{ success?: unknown }} */ (sobre).success === false) {
    throw new INTERNAL("No pudimos leer la agenda.");
  }
  const data = /** @type {{ data?: unknown }} */ (sobre).data ?? sobre;
  if (!data || typeof data !== "object") throw new INTERNAL("La agenda no trae programas.");
  const cuerpo = /** @type {{ programas?: unknown, semanasSinCobertura?: unknown }} */ (data);
  if (!Array.isArray(cuerpo.programas) || !Array.isArray(cuerpo.semanasSinCobertura)) {
    throw new INTERNAL("La agenda no trae programas.");
  }
  return {
    jornadas: cuerpo.programas.map(mapearJornada),
    semanasSinCobertura: cuerpo.semanasSinCobertura.map(mapearSemana),
  };
}
