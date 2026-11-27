import { enlaceA, rutaDe } from "../rutas.js";

export const ENLACE_CALENDARIO = rutaDe("calendario");
import { superaSla } from "./domain/reglas/antiguedad.js";

const ZONA = "America/Mexico_City";
const MS_POR_MINUTO = 60 * 1000;
const MS_POR_HORA = 60 * MS_POR_MINUTO;
const MS_POR_DIA = 24 * MS_POR_HORA;
const DIAS_AGENDA = 28;
const JORNADAS_VISIBLES = 3;

export const COPY = Object.freeze({
  mision: "Cada tarea acerca a un peludo a su nuevo hogar.",
  todoAlDia: "Todo al día",
  bandejaVacia: "Todo al día. No hay pendientes por ahora.",
  agendaVacia: "No hay jornadas en las próximas 4 semanas.",
  listasParaEmitir: "Listas para emitir",
  sinIngresos: "Sin ingresos este mes",
  registrarPerro: "Registrar perro",
  crearJornada: "Crear jornada",
  errores: Object.freeze({
    resumen: "No pudimos cargar los indicadores. Reintenta en unos segundos.",
    pendientes: "No pudimos cargar los pendientes. Reintenta en unos segundos.",
    agenda: "No pudimos cargar las jornadas. Reintenta en unos segundos.",
  }),
  kpis: Object.freeze({
    solicitudes: "Solicitudes por atender",
    donativos: "Donativos por validar",
    facturas: "Facturas por emitir",
    perros: "Perros en adopción",
  }),
  filtros: Object.freeze({
    todos: "Todo",
    adopcion: "Adopciones",
    donativo: "Donativos",
    factura: "Facturas",
  }),
  reintentar: "Reintentar",
  actualizar: "Actualizar",
  verTodo: "Ver todo",
  verCalendario: "Ver calendario",
  verFicha: "Ver ficha",
  atencion: "Requiere tu atención",
  jornadas: "Próximas jornadas",
  esperando: "Lleva más tiempo esperando",
  sinSolicitudes: "Sin solicitudes activas",
  parcial: "Algunos datos no se pudieron actualizar.",
  panelActualizado: "Panel actualizado",
});

const moneda = new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" });

/** @param {number} cantidad @param {string} uno @param {string} muchos */
function plural(cantidad, uno, muchos) {
  return `${cantidad} ${cantidad === 1 ? uno : muchos}`;
}

/** @param {Date} desde @param {Date} ahora */
function transcurrido(desde, ahora) {
  return Math.max(0, ahora.getTime() - desde.getTime());
}

/** @param {Date} desde @param {Date} ahora */
function tiempoRelativo(desde, ahora) {
  const ms = transcurrido(desde, ahora);
  const dias = Math.floor(ms / MS_POR_DIA);
  if (dias >= 1) return dias === 1 ? "hace 1 día" : `hace ${dias} días`;
  const horas = Math.floor(ms / MS_POR_HORA);
  if (horas >= 1) return horas === 1 ? "hace 1 hora" : `hace ${horas} horas`;
  const minutos = Math.floor(ms / MS_POR_MINUTO);
  if (minutos >= 1) return minutos === 1 ? "hace 1 minuto" : `hace ${minutos} minutos`;
  return "hace un momento";
}

/** @param {Date} desde @param {Date} ahora */
function tiempoCorto(desde, ahora) {
  const ms = transcurrido(desde, ahora);
  const dias = Math.floor(ms / MS_POR_DIA);
  if (dias >= 1) return `hace ${dias} d`;
  const horas = Math.floor(ms / MS_POR_HORA);
  if (horas >= 1) return `hace ${horas} h`;
  return "hoy";
}

/** @param {Date} fecha */
function fechaISO(fecha) {
  return new Intl.DateTimeFormat("en-CA", {
    timeZone: ZONA,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(fecha);
}

/** @param {string} iso @param {number} dias */
function sumarDiasISO(iso, dias) {
  const [anio, mes, dia] = iso.split("-").map(Number);
  return new Date(Date.UTC(anio, mes - 1, dia + dias)).toISOString().slice(0, 10);
}

/** @param {Date} fecha */
function horaEnMexico(fecha) {
  return Number(
    new Intl.DateTimeFormat("en-US", {
      timeZone: ZONA,
      hour: "numeric",
      hourCycle: "h23",
    }).format(fecha),
  );
}

/** @param {Date} fecha */
function fechaLarga(fecha) {
  return new Intl.DateTimeFormat("es-MX", { dateStyle: "full", timeZone: ZONA }).format(fecha);
}

/** @param {Date} fecha */
function horaCorta(fecha) {
  return new Intl.DateTimeFormat("es-MX", {
    hour: "numeric",
    minute: "2-digit",
    timeZone: ZONA,
  }).format(fecha);
}

/** @param {Date} fecha */
function diaMes(fecha) {
  const partes = new Intl.DateTimeFormat("es-MX", {
    day: "numeric",
    month: "short",
    timeZone: ZONA,
  }).formatToParts(fecha);
  const leer = (tipo) => partes.find((parte) => parte.type === tipo)?.value ?? "";
  return { dia: leer("day"), mes: leer("month").replace(".", "") };
}

/** @param {number} hora */
function fraseSaludo(hora) {
  if (hora < 12) return "Buenos días";
  if (hora < 19) return "Buenas tardes";
  return "Buenas noches";
}

/** @param {"adopcion" | "donativo" | "factura"} filtro */
export function textoFiltroVacio(filtro) {
  return `No hay pendientes de ${COPY.filtros[filtro]}.`;
}

/** @param {Date} ahora */
export function rangoAgenda(ahora) {
  const desde = fechaISO(ahora);
  return { desde, hasta: sumarDiasISO(desde, DIAS_AGENDA) };
}

/**
 * @param {import("./domain/entities/agenda.js").Jornada[]} jornadas
 * @param {Date} ahora
 */
export function seleccionarJornadas(jornadas, ahora) {
  const { desde, hasta } = rangoAgenda(ahora);
  const desdeMs = new Date(`${desde}T00:00:00-06:00`).getTime();
  const hastaMs = new Date(`${hasta}T00:00:00-06:00`).getTime() + MS_POR_DIA;
  return jornadas
    .filter((jornada) => jornada.estado !== "Finalizado" && jornada.estado !== "Cancelado")
    .filter((jornada) => jornada.inicio.getTime() >= desdeMs && jornada.inicio.getTime() < hastaMs)
    .sort((a, b) => a.inicio.getTime() - b.inicio.getTime())
    .slice(0, JORNADAS_VISIBLES);
}

/**
 * @param {string} titulo
 * @param {string} valor
 * @param {string} detalle
 * @param {string | null} antiguedad
 */
function nombreAccesible(titulo, valor, detalle, antiguedad) {
  return [titulo, valor, detalle, antiguedad].filter(Boolean).join(". ");
}

/**
 * @param {string} id
 * @param {string} titulo
 * @param {number} valor
 * @param {string} detalle
 * @param {string | null} antiguedad
 * @param {"neutral" | "warning" | "danger"} tono
 * @param {string} href
 */
function kpi(id, titulo, valor, detalle, antiguedad, tono, href) {
  const texto = String(valor);
  return {
    id,
    titulo,
    valor: texto,
    detalle,
    antiguedad,
    tono,
    href,
    nombreAccesible: nombreAccesible(titulo, texto, detalle, antiguedad),
  };
}

/**
 * @param {import("./domain/entities/resumen.js").ResumenInicio} resumen
 * @param {Date} ahora
 */
export function presentarKpis(resumen, ahora) {
  const solicitudes = resumen.solicitudes.porRevisar + resumen.solicitudes.enRevision;
  const antiguedadSolicitudes = resumen.solicitudes.masAntiguaDesde
    ? `Más antigua: ${tiempoCorto(resumen.solicitudes.masAntiguaDesde, ahora)}`
    : null;

  const partesFactura = [];
  if (resumen.facturas.requierenRevision > 0) {
    partesFactura.push(
      plural(resumen.facturas.requierenRevision, "requiere revisión", "requieren revisión"),
    );
  }
  if (resumen.facturas.correosConError > 0) {
    partesFactura.push(
      plural(resumen.facturas.correosConError, "correo con error", "correos con error"),
    );
  }

  return [
    kpi(
      "solicitudes",
      COPY.kpis.solicitudes,
      solicitudes,
      solicitudes === 0
        ? COPY.todoAlDia
        : `${plural(resumen.solicitudes.porRevisar, "nueva", "nuevas")} · ${plural(resumen.solicitudes.enRevision, "en revisión", "en revisión")}`,
      solicitudes === 0 ? null : antiguedadSolicitudes,
      solicitudes > 0 &&
        resumen.solicitudes.masAntiguaDesde &&
        superaSla("adopcion", resumen.solicitudes.masAntiguaDesde, ahora)
        ? "warning"
        : "neutral",
      enlaceA("solicitudes", { estado: "pendientes" }),
    ),
    kpi(
      "donativos",
      COPY.kpis.donativos,
      resumen.donativos.porValidar,
      resumen.donativos.porValidar === 0
        ? COPY.todoAlDia
        : resumen.donativos.masAntiguoDesde
          ? `${moneda.format(resumen.donativos.montoPorValidar)} · ${tiempoCorto(resumen.donativos.masAntiguoDesde, ahora)}`
          : moneda.format(resumen.donativos.montoPorValidar),
      null,
      resumen.donativos.porValidar > 0 &&
        resumen.donativos.masAntiguoDesde &&
        superaSla("donativo", resumen.donativos.masAntiguoDesde, ahora)
        ? "warning"
        : "neutral",
      enlaceA("donativos"),
    ),
    kpi(
      "facturas",
      COPY.kpis.facturas,
      resumen.facturas.pendientes,
      resumen.facturas.pendientes === 0
        ? COPY.todoAlDia
        : partesFactura.length
          ? partesFactura.join(" · ")
          : COPY.listasParaEmitir,
      null,
      resumen.facturas.correosConError > 0 ? "danger" : "neutral",
      enlaceA("adminSolicitudesCfdi"),
    ),
    kpi(
      "perros",
      COPY.kpis.perros,
      resumen.perros.disponibles,
      resumen.perros.disponibles === 0
        ? COPY.todoAlDia
        : resumen.perros.ingresosDelMes === 0
          ? COPY.sinIngresos
          : `+${plural(resumen.perros.ingresosDelMes, "ingreso este mes", "ingresos este mes")}`,
      null,
      "neutral",
      enlaceA("perros", { estado: "disponible" }),
    ),
  ];
}

/** @param {import("./domain/entities/tareaPendiente.js").TareaPendiente} tarea */
function tituloTarea(tarea) {
  const nombre = tarea.persona.nombre;
  if (tarea.tipo === "adopcion") return `Revisar solicitud de ${nombre}`;
  if (tarea.tipo === "donativo") return `Validar donativo de ${nombre}`;
  if (tarea.tipo === "correo_factura") return `Reintentar correo de factura de ${nombre}`;
  return `Emitir factura para ${nombre}`;
}

/** @param {import("./domain/entities/tareaPendiente.js").TareaPendiente} tarea */
function hrefTarea(tarea) {
  if (tarea.tipo === "adopcion") return enlaceA("solicitudes", { abrir: tarea.id });
  if (tarea.tipo === "donativo") return enlaceA("donativos", { abrir: tarea.id });
  return enlaceA("adminSolicitudesCfdi", { abrir: tarea.id });
}

/**
 * @param {import("./domain/entities/tareaPendiente.js").TareaPendiente} tarea
 * @param {Date} ahora
 */
export function presentarTarea(tarea, ahora) {
  const partes = [];
  if (tarea.perro) partes.push(tarea.perro.nombre);
  if (tarea.folio) partes.push(tarea.folio);
  if (tarea.monto != null) partes.push(moneda.format(tarea.monto));
  partes.push(tiempoRelativo(tarea.creadaEn, ahora));
  return {
    id: tarea.id,
    tipo: tarea.tipo,
    titulo: tituloTarea(tarea),
    meta: partes.join(" · "),
    tono:
      tarea.urgencia === "critica" ? "danger" : tarea.urgencia === "atrasada" ? "warning" : null,
    icono:
      tarea.tipo === "adopcion"
        ? "assignment"
        : tarea.tipo === "donativo"
          ? "favorite"
          : "receipt_long",
    href: hrefTarea(tarea),
  };
}

/** @param {Date | null} desde @param {Date} ahora */
export function textoActualizado(desde, ahora) {
  if (!desde) return "";
  return `Actualizado ${tiempoRelativo(desde, ahora)}`;
}

/** @param {number} total @param {number} mostrando */
export function textoPieBandeja(total, mostrando) {
  if (!total) return "";
  if (mostrando < total) return `${total} pendientes · mostrando ${mostrando}`;
  return total === 1 ? "1 pendiente" : `${total} pendientes`;
}

/** @param {import("./domain/entities/agenda.js").Jornada} jornada */
export function presentarJornada(jornada) {
  const fecha = diaMes(jornada.inicio);
  return {
    id: jornada.id,
    nombre: jornada.nombre,
    tipo: jornada.tipo,
    dia: fecha.dia,
    mes: fecha.mes,
    horario: `${horaCorta(jornada.inicio)} – ${horaCorta(jornada.fin)}`,
    ubicacion: jornada.ubicacion,
    cupo:
      jornada.cupoMaximo == null
        ? String(jornada.inscritos)
        : `${jornada.inscritos}/${jornada.cupoMaximo}`,
    etiqueta: jornada.estado,
    tono:
      jornada.estado === "Disponible" ? "success" : jornada.estado === "Lleno" ? "info" : "neutral",
    porcentaje:
      jornada.cupoMaximo > 0
        ? Math.min(100, Math.round((jornada.inscritos / jornada.cupoMaximo) * 100))
        : null,
    href: enlaceA("inscritos", { id: jornada.id }),
  };
}

/** @param {import("./domain/entities/agenda.js").SemanaSinCobertura[]} semanas */
export function presentarAlertaCobertura(semanas) {
  if (!semanas.length) return null;
  return {
    texto:
      semanas.length === 1
        ? "Hay una semana sin cobertura."
        : `Hay ${semanas.length} semanas sin cobertura.`,
    href: enlaceA("nuevaJornada", { fecha: semanas[0].inicio }),
  };
}

/**
 * @param {{ nombre?: string, apellidos?: string, rol?: string } | null} usuario
 * @param {Date} ahora
 */
export function presentarUsuario(usuario, ahora) {
  const frase = fraseSaludo(horaEnMexico(ahora));
  const limpio = usuario?.nombre?.trim();
  const nombre = limpio ? limpio.split(/\s+/)[0] : null;
  return {
    saludo: nombre ? `¡${frase}, ${nombre}!` : `¡${frase}!`,
    nombre,
    rol: usuario?.rol ?? null,
    fecha: fechaLarga(ahora),
    mision: COPY.mision,
    acciones: [
      {
        id: "registrar",
        texto: COPY.registrarPerro,
        href: enlaceA("perros", { accion: "registrar" }),
      },
      { id: "jornada", texto: COPY.crearJornada, href: rutaDe("nuevaJornada") },
    ],
  };
}

/** @param {import("./domain/entities/perroDestacado.js").PerroDestacado} perro */
export function presentarPerro(perro) {
  const partes = [];
  if (perro.edadAprox != null) partes.push(plural(perro.edadAprox, "año", "años"));
  if (perro.sexo) partes.push(perro.sexo);
  if (perro.tamano) partes.push(perro.tamano);
  return {
    id: perro.id,
    nombre: perro.nombre,
    espera: plural(perro.diasEnRefugio, "día en el refugio", "días en el refugio"),
    detalle: partes.join(" · "),
    fotoUrl: perro.fotoUrl,
    href: enlaceA("perros", { abrir: perro.id }),
  };
}
