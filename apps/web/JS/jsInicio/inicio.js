import { UNAUTHENTICATED } from "@qarino/shared/errores.js";
import { rutaDe } from "../rutas.js";
import { usuarioActual } from "../jsPerfiles/usuarioActual.js";
import { crearInicioRepositoryMock } from "./data/repositories/InicioRepositoryMock.js";
import {
  COPY,
  presentarAlertaCobertura,
  presentarJornada,
  presentarKpis,
  presentarPerro,
  presentarTarea,
  presentarUsuario,
  rangoAgenda,
  seleccionarJornadas,
  textoFiltroVacio,
} from "./inicio.presentar.js";

/** @typedef {"usuario" | "resumen" | "pendientes" | "agenda" | "destacado"} Widget */
/** @typedef {"todos" | "adopcion" | "donativo" | "factura"} FiltroBandeja */
/** @typedef {import("./domain/entities/tareaPendiente.js").TareaPendiente} TareaPendiente */
/** @typedef {import("./domain/repositories/InicioRepository.js").InicioRepository} InicioRepository */

const MINUTOS_REFRESCO = 5;
const CONTEOS_BANDEJA = { todos: 0, adopcion: 0, donativo: 0, factura: 0 };

/**
 * @param {Date | null} actualizadoEn
 * @param {Date} ahora
 * @param {number} [minutos]
 */
export function debeRefrescar(actualizadoEn, ahora, minutos = MINUTOS_REFRESCO) {
  if (!actualizadoEn) return false;
  return ahora.getTime() - actualizadoEn.getTime() >= minutos * 60 * 1000;
}

function estadoInicial() {
  return {
    usuario: { estado: "cargando", datos: null },
    resumen: { estado: "cargando", datos: null, error: null },
    pendientes: {
      estado: "cargando",
      datos: [],
      filtro: "todos",
      conteos: { ...CONTEOS_BANDEJA },
      textoVacio: null,
    },
    agenda: { estado: "cargando", datos: [], alertaCobertura: null, textoVacio: null },
    destacado: { estado: "cargando", datos: null },
    conteos: { solicitudes: 0, donativos: 0 },
    actualizadoEn: null,
    refrescando: false,
  };
}

/** @param {unknown} error */
function esAbort(error) {
  return Boolean(
    error && typeof error === "object" && "name" in error && error.name === "AbortError",
  );
}

/** @param {TareaPendiente} tarea */
function grupoDe(tarea) {
  return tarea.tipo === "correo_factura" ? "factura" : tarea.tipo;
}

/**
 * Arma la página de Inicio. La vista todavía no existe: esto solo produce el estado.
 * @param {{
 *   repositorio: InicioRepository,
 *   obtenerUsuario: (opts: { signal: AbortSignal }) => Promise<{ nombre?: string, apellidos?: string, rol?: string }>,
 *   ahora?: () => Date,
 *   redirigir?: (ruta: string) => void,
 * }} deps
 */
export function crearInicio(deps) {
  const ahora = deps.ahora ?? (() => new Date());
  const redirigir = deps.redirigir ?? ((ruta) => window.location.replace(ruta));
  let estado = estadoInicial();
  /** @type {Set<(estado: typeof estado) => void>} */
  const oidores = new Set();
  /** @type {Record<Widget, AbortController | null>} */
  const controles = {
    usuario: null,
    resumen: null,
    pendientes: null,
    agenda: null,
    destacado: null,
  };
  /** @type {TareaPendiente[]} */
  let tareas = [];
  /** @type {FiltroBandeja} */
  let filtro = "todos";
  let redirigido = false;
  let generacion = 0;
  let vivo = true;

  /** @param {Partial<typeof estado>} parcial */
  function fijar(parcial) {
    estado = { ...estado, ...parcial };
    for (const oir of oidores) oir(estado);
  }

  function salir() {
    if (redirigido) return;
    redirigido = true;
    redirigir(rutaDe("login"));
  }

  /** @param {Widget} widget */
  function reemplazar(widget) {
    controles[widget]?.abort();
    const control = new AbortController();
    controles[widget] = control;
    return control;
  }

  function conteosBandeja() {
    const conteos = { ...CONTEOS_BANDEJA, todos: tareas.length };
    for (const tarea of tareas) {
      const grupo = grupoDe(tarea);
      if (grupo === "adopcion" || grupo === "donativo" || grupo === "factura") conteos[grupo] += 1;
    }
    return conteos;
  }

  function publicarPendientes() {
    const visibles =
      filtro === "todos" ? tareas : tareas.filter((tarea) => grupoDe(tarea) === filtro);
    const reloj = ahora();
    const vacia = tareas.length === 0;
    fijar({
      pendientes: {
        estado: vacia ? "vacio" : "listo",
        datos: visibles.map((tarea) => presentarTarea(tarea, reloj)),
        filtro,
        conteos: conteosBandeja(),
        textoVacio: vacia
          ? COPY.bandejaVacia
          : visibles.length === 0 && filtro !== "todos"
            ? textoFiltroVacio(filtro)
            : null,
      },
    });
  }

  /**
   * @param {Widget} widget
   * @param {AbortSignal} signal
   */
  async function leer(widget, signal) {
    const reloj = ahora();
    if (widget === "usuario") return deps.obtenerUsuario({ signal });
    if (widget === "resumen") return deps.repositorio.obtenerResumen({ signal });
    if (widget === "pendientes") return deps.repositorio.obtenerPendientes({ signal });
    if (widget === "agenda") {
      const agenda = await deps.repositorio.obtenerAgenda({ ...rangoAgenda(reloj), signal });
      return {
        jornadas: seleccionarJornadas(agenda.jornadas, reloj),
        semanasSinCobertura: agenda.semanasSinCobertura,
      };
    }
    return deps.repositorio.obtenerPerroDestacado({ signal });
  }

  /**
   * @param {Widget} widget
   * @param {unknown} datos
   */
  function aplicarExito(widget, datos) {
    const reloj = ahora();
    if (widget === "usuario") {
      fijar({
        usuario: {
          estado: "listo",
          datos: presentarUsuario(
            /** @type {{ nombre?: string, apellidos?: string, rol?: string }} */ (datos),
            reloj,
          ),
        },
      });
      return;
    }
    if (widget === "resumen") {
      const resumen = /** @type {import("./domain/entities/resumen.js").ResumenInicio} */ (datos);
      fijar({
        resumen: { estado: "listo", datos: presentarKpis(resumen, reloj), error: null },
        conteos: {
          solicitudes: resumen.solicitudes.porRevisar + resumen.solicitudes.enRevision,
          donativos: resumen.donativos.porValidar + resumen.facturas.pendientes,
        },
      });
      return;
    }
    if (widget === "pendientes") {
      tareas = /** @type {TareaPendiente[]} */ (datos);
      publicarPendientes();
      return;
    }
    if (widget === "agenda") {
      const agenda = /** @type {import("./domain/entities/agenda.js").Agenda} */ (datos);
      const jornadas = agenda.jornadas.map(presentarJornada);
      fijar({
        agenda: {
          estado: jornadas.length === 0 ? "vacio" : "listo",
          datos: jornadas,
          alertaCobertura: presentarAlertaCobertura(agenda.semanasSinCobertura),
          textoVacio: jornadas.length === 0 ? COPY.agendaVacia : null,
        },
      });
      return;
    }
    const perro =
      /** @type {import("./domain/entities/perroDestacado.js").PerroDestacado | null} */ (datos);
    fijar({
      destacado: perro
        ? { estado: "listo", datos: presentarPerro(perro) }
        : { estado: "oculto", datos: null },
    });
  }

  /** @param {Widget} widget */
  function aplicarError(widget) {
    if (widget === "usuario") {
      fijar({ usuario: { estado: "error", datos: presentarUsuario(null, ahora()) } });
      return;
    }
    if (widget === "resumen") {
      fijar({ resumen: { estado: "error", datos: null, error: { texto: COPY.errores.resumen } } });
      return;
    }
    if (widget === "pendientes") {
      tareas = [];
      fijar({
        pendientes: {
          estado: "error",
          datos: [],
          filtro,
          conteos: { ...CONTEOS_BANDEJA },
          textoVacio: COPY.errores.pendientes,
        },
      });
      return;
    }
    if (widget === "agenda") {
      fijar({
        agenda: {
          estado: "error",
          datos: [],
          alertaCobertura: null,
          textoVacio: COPY.errores.agenda,
        },
      });
      return;
    }
    fijar({ destacado: { estado: "oculto", datos: null } });
  }

  /** @param {Widget} widget */
  function ponerCargando(widget) {
    if (widget === "usuario") fijar({ usuario: { estado: "cargando", datos: null } });
    if (widget === "resumen") fijar({ resumen: { estado: "cargando", datos: null, error: null } });
    if (widget === "pendientes") {
      fijar({
        pendientes: {
          estado: "cargando",
          datos: [],
          filtro,
          conteos: { ...CONTEOS_BANDEJA },
          textoVacio: null,
        },
      });
    }
    if (widget === "agenda") {
      fijar({ agenda: { estado: "cargando", datos: [], alertaCobertura: null, textoVacio: null } });
    }
    if (widget === "destacado") fijar({ destacado: { estado: "cargando", datos: null } });
  }

  /** @param {Widget} widget */
  async function pedir(widget) {
    const control = reemplazar(widget);
    try {
      const datos = await leer(widget, control.signal);
      if (!vivo || controles[widget] !== control) return;
      aplicarExito(widget, datos);
    } catch (error) {
      if (!vivo || controles[widget] !== control || esAbort(error)) return;
      if (error instanceof UNAUTHENTICATED) {
        salir();
        return;
      }
      aplicarError(widget);
    }
  }

  const widgets = /** @type {Widget[]} */ ([
    "usuario",
    "resumen",
    "pendientes",
    "agenda",
    "destacado",
  ]);

  async function cargarTodo(conEsqueleto) {
    const actual = ++generacion;
    if (conEsqueleto) {
      filtro = "todos";
      for (const widget of widgets) ponerCargando(widget);
    }
    await Promise.allSettled(widgets.map((widget) => pedir(widget)));
    if (!vivo || actual !== generacion) return;
    fijar({ actualizadoEn: ahora(), refrescando: false });
  }

  function alVolver() {
    if (typeof document === "undefined" || document.visibilityState !== "visible") return;
    if (debeRefrescar(estado.actualizadoEn, ahora())) void actualizar();
  }

  if (typeof document !== "undefined") document.addEventListener("visibilitychange", alVolver);

  return {
    estado: () => estado,
    /** @param {(estado: typeof estado) => void} oir */
    suscribir(oir) {
      oidores.add(oir);
      return () => oidores.delete(oir);
    },
    iniciar() {
      return cargarTodo(true);
    },
    actualizar() {
      fijar({ refrescando: true });
      return cargarTodo(false);
    },
    /** @param {Widget} widget */
    reintentar(widget) {
      if (!widgets.includes(widget)) return Promise.resolve();
      ponerCargando(widget);
      return pedir(widget);
    },
    /** @param {FiltroBandeja} tipo */
    filtrarPendientes(tipo) {
      filtro = tipo;
      if (estado.pendientes.estado === "cargando" || estado.pendientes.estado === "error") {
        fijar({ pendientes: { ...estado.pendientes, filtro } });
        return;
      }
      publicarPendientes();
    },
    destruir() {
      vivo = false;
      for (const widget of widgets) controles[widget]?.abort();
      if (typeof document !== "undefined")
        document.removeEventListener("visibilitychange", alVolver);
      oidores.clear();
    },
  };
}

const ESCENARIOS = new Set(["ok", "vacio", "parcial", "error", "lento", "atrasado"]);

/** @param {URLSearchParams} params */
export function elegirFuenteInicio(params) {
  const env = /** @type {{ DEV?: boolean, VITE_FUENTE_INICIO?: string }} */ (import.meta.env);
  const dev = Boolean(env?.DEV);
  const pedida = dev
    ? params.get("fuente") || env?.VITE_FUENTE_INICIO || "mock"
    : env?.VITE_FUENTE_INICIO || "mock";
  const escenarioPedido = dev ? params.get("escenario") : null;
  const escenario = ESCENARIOS.has(escenarioPedido ?? "") ? escenarioPedido : "ok";
  if (pedida !== "mock")
    throw new Error(`La fuente ${pedida} se conecta cuando exista su repositorio.`);
  return crearInicioRepositoryMock({
    escenario: /** @type {"ok" | "vacio" | "parcial" | "error" | "lento" | "atrasado"} */ (
      escenario
    ),
  });
}

/** Usa /me y /profile. Si no hay sesión, el saludo queda con el nombre de muestra. */
export function crearPaginaInicio() {
  return crearInicio({
    repositorio: elegirFuenteInicio(new URLSearchParams(window.location.search)),
    obtenerUsuario: () => usuarioActual(),
    redirigir: (ruta) => window.location.replace(ruta),
  });
}
