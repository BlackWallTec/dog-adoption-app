import { INTERNAL } from "@qarino/shared/errores.js";
import { prepararPendientes } from "../../domain/reglas/prioridad.js";
import { mapearAgenda } from "../mappers/agenda.mapper.js";
import { listaDePendientes } from "../mappers/pendiente.mapper.js";
import { mapearPerroDestacado } from "../mappers/perroDestacado.mapper.js";
import { mapearResumen } from "../mappers/resumen.mapper.js";
import * as atrasado from "../fixtures/atrasado.js";
import * as error from "../fixtures/error.js";
import * as lento from "../fixtures/lento.js";
import * as ok from "../fixtures/ok.js";
import * as parcial from "../fixtures/parcial.js";
import * as vacio from "../fixtures/vacio.js";

const ESCENARIOS = { ok, vacio, parcial, error, lento, atrasado };
const LIMITE_POR_DEFECTO = 6;

/** @param {AbortSignal | undefined} signal */
function errorAbort(signal) {
  if (signal?.reason instanceof Error) return signal.reason;
  return new DOMException("The operation was aborted.", "AbortError");
}

/** @param {number} ms @param {AbortSignal | undefined} signal */
function esperar(ms, signal) {
  if (signal?.aborted) return Promise.reject(errorAbort(signal));
  if (!ms) return Promise.resolve();
  return new Promise((resolve, reject) => {
    const timer = setTimeout(resolve, ms);
    signal?.addEventListener(
      "abort",
      () => {
        clearTimeout(timer);
        reject(errorAbort(signal));
      },
      { once: true },
    );
  });
}

/**
 * @param {unknown} causa
 * @param {string} widget
 */
function conDetalle(causa, widget) {
  console.error(`[inicio] ${widget}`, causa);
  if (causa instanceof INTERNAL) throw causa;
  throw new INTERNAL("No pudimos leer los datos de inicio.");
}

/**
 * @param {{ escenario?: keyof typeof ESCENARIOS, ahora?: Date }} [opciones]
 * @returns {import("../../domain/repositories/InicioRepository.js").InicioRepository}
 */
export function crearInicioRepositoryMock({ escenario = "ok", ahora = new Date() } = {}) {
  const fuente = ESCENARIOS[escenario];
  if (!fuente) throw new INTERNAL(`Escenario desconocido: ${escenario}`);

  /**
   * @template T
   * @param {string} nombre
   * @param {AbortSignal | undefined} signal
   * @param {() => T} leer
   */
  async function ejecutar(nombre, signal, leer) {
    await esperar(/** @type {{ retrasoMs?: number }} */ (fuente).retrasoMs ?? 0, signal);
    if (
      escenario === "error" ||
      /** @type {{ metodoQueFalla?: string }} */ (fuente).metodoQueFalla === nombre
    ) {
      throw new INTERNAL(
        /** @type {{ cuerpo?: { error?: string } }} */ (fuente).cuerpo?.error ??
          "No pudimos cargar el inicio.",
      );
    }
    try {
      return leer();
    } catch (causa) {
      conDetalle(causa, nombre);
    }
  }

  return {
    obtenerResumen(opts = {}) {
      return ejecutar("resumen", opts.signal, () => mapearResumen(fuente.resumen));
    },
    obtenerPendientes(opts = {}) {
      const limite = opts.limite ?? LIMITE_POR_DEFECTO;
      return ejecutar("pendientes", opts.signal, () => {
        const preparados = prepararPendientes(listaDePendientes(fuente.pendientes), ahora);
        return preparados.slice(0, limite).map((tarea) => ({
          id: tarea.id,
          tipo: tarea.tipo,
          persona: tarea.persona,
          perro: tarea.perro,
          folio: tarea.folio,
          monto: tarea.monto,
          creadaEn: tarea.creadaEn,
          urgencia: tarea.urgencia,
        }));
      });
    },
    obtenerAgenda(opts) {
      if (!opts?.desde || !opts?.hasta) throw new INTERNAL("Falta el rango de la agenda.");
      return ejecutar("agenda", opts.signal, () => mapearAgenda(fuente.agenda));
    },
    obtenerPerroDestacado(opts = {}) {
      return ejecutar("perro", opts.signal, () => mapearPerroDestacado(fuente.perro));
    },
  };
}
