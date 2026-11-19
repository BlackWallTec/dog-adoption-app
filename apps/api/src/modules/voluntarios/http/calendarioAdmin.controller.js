import {
  getProgramasEnRango,
  getInscripcionesActivas,
} from "./modelosVoluntariado/calendario.model.js";
import { ahoraMexico } from "./modelosVoluntariado/programaEdicion.model.js";

// HU30: calendario de programas para el Admin. Solo lectura

const FECHA_REGEX = /^\d{4}-\d{2}-\d{2}$/;
const MAX_DIAS_RANGO = 62;
const MS_POR_DIA = 24 * 60 * 60 * 1000;

const responderError = (res, status, code, message) =>
  res.status(status).json({ success: false, code, message });

// Recorta textos que vienen de columnas char() con espacios de relleno
const limpiar = (valor) => valor?.trim() || null;

// Fechas "AAAA-MM-DD" como días de calendario. Se usa UTC solo para que la
// suma de días no se mueva por la zona horaria del servidor
const aFecha = (texto) => new Date(`${texto}T00:00:00Z`);
const aTexto = (fecha) => fecha.toISOString().slice(0, 10);

export const sumarDias = (texto, dias) => {
  const fecha = aFecha(texto);
  fecha.setUTCDate(fecha.getUTCDate() + dias);
  return aTexto(fecha);
};

// Lunes de la semana a la que pertenece la fecha (domingo = 0 en getUTCDay)
export const lunesDe = (texto) => sumarDias(texto, -((aFecha(texto).getUTCDay() + 6) % 7));

// Supabase regresa "2026-11-21T09:00:00"; se compara como "2026-11-21 09:00:00"
const aTimestamp = (valor) => String(valor).replace("T", " ").slice(0, 19);

// Primero se descarta Invalid Date (ej. 2026-13-01), porque toISOString() lanza RangeError.
// La comparación con el texto atrapa fechas que JS recorre, como 2026-02-30 -> 2026-03-02
const esFechaValida = (texto) => {
  if (typeof texto !== "string" || !FECHA_REGEX.test(texto)) return false;
  const fecha = aFecha(texto);
  return !Number.isNaN(fecha.getTime()) && aTexto(fecha) === texto;
};

// desde <= hasta y máximo 62 días, para que no se pidan años completos
export const esRangoValido = (desde, hasta) => {
  if (!esFechaValida(desde) || !esFechaValida(hasta)) return false;
  const dias = (aFecha(hasta) - aFecha(desde)) / MS_POR_DIA;
  return dias >= 0 && dias <= MAX_DIAS_RANGO;
};

// Prioridad: Cancelado > Finalizado > Lleno > Disponible
export const estadoDe = (programa, inscritos, ahora) => {
  if (limpiar(programa.estado_del_programa) === "Cancelado") return "Cancelado";
  if (aTimestamp(programa.fecha_fin) <= ahora) return "Finalizado";
  if (programa.cupo_maximo != null && inscritos >= programa.cupo_maximo) return "Lleno";
  return "Disponible";
};

// { id_programa } por inscrito -> Map(idPrograma, total)
export const contarPorPrograma = (inscripciones) => {
  const conteo = new Map();
  for (const { id_programa } of inscripciones) {
    conteo.set(id_programa, (conteo.get(id_programa) ?? 0) + 1);
  }
  return conteo;
};

// Semanas (de lunes a lunes siguiente) sin ningún programa no cancelado.
// Solo la semana actual y las futuras
export const semanasSinCobertura = (programas, inicio, finExclusivo, ahora) => {
  const lunesActual = lunesDe(ahora.slice(0, 10));
  const activos = programas.filter((programa) => programa.estado !== "Cancelado");
  const semanas = [];

  for (let lunes = inicio; lunes < finExclusivo; lunes = sumarDias(lunes, 7)) {
    if (lunes < lunesActual) continue;

    const siguienteLunes = sumarDias(lunes, 7);
    const inicioSemana = `${lunes} 00:00:00`;
    const finSemana = `${siguienteLunes} 00:00:00`;

    const cubierta = activos.some(
      (programa) =>
        aTimestamp(programa.inicio) < finSemana && aTimestamp(programa.fin) > inicioSemana,
    );

    if (!cubierta) semanas.push({ inicio: lunes, fin: siguienteLunes });
  }

  return semanas;
};

// Arma la respuesta de la HU30. No consulta nada, se puede probar sin Supabase
export const armarCalendario = ({ programas, inscripciones, inicio, finExclusivo, ahora }) => {
  const inscritosPorPrograma = contarPorPrograma(inscripciones);

  const lista = programas.map((programa) => {
    const inscritos = inscritosPorPrograma.get(programa.id_programa) ?? 0;
    return {
      idPrograma: programa.id_programa,
      nombre: programa.nombre,
      tipo: programa.tipo,
      inicio: programa.fecha_inicio,
      fin: programa.fecha_fin,
      ubicacion: programa.ubicacion,
      cupoMaximo: programa.cupo_maximo,
      inscritos,
      estado: estadoDe(programa, inscritos, ahora),
    };
  });

  return {
    programas: lista,
    semanasSinCobertura: semanasSinCobertura(lista, inicio, finExclusivo, ahora),
  };
};

// GET /api/voluntarios/admin/programas/calendario?desde=AAAA-MM-DD&hasta=AAAA-MM-DD
export const getCalendario = async (req, res) => {
  const { desde, hasta } = req.query;

  if (!esRangoValido(desde, hasta)) {
    return responderError(res, 400, "RANGO_INVALIDO", "El rango de fechas no es válido.");
  }

  // Se amplía a semanas completas para que la cobertura no dependa de días cortados
  const inicio = lunesDe(desde);
  const finExclusivo = sumarDias(lunesDe(hasta), 7);

  try {
    const programas = await getProgramasEnRango(`${inicio} 00:00:00`, `${finExclusivo} 00:00:00`);
    const inscripciones = await getInscripcionesActivas(
      programas.map((programa) => programa.id_programa),
    );

    const data = armarCalendario({
      programas,
      inscripciones,
      inicio,
      finExclusivo,
      ahora: ahoraMexico(),
    });
    return res.status(200).json({ success: true, data });
  } catch (error) {
    console.error("[voluntarios] getCalendario:", error);
    return responderError(res, 500, "ERROR_INTERNO", "Error interno al consultar el calendario.");
  }
};
