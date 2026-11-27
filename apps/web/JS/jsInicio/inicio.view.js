import { crearPaginaInicio } from "./inicio.js";
import { COPY, ENLACE_CALENDARIO, textoActualizado, textoPieBandeja } from "./inicio.presentar.js";

const raiz = document.querySelector(".qa-page");
const pagina = crearPaginaInicio();

pagina.suscribir(pintar);
void pagina.iniciar();

/**
 * @param {string} tag
 * @param {{ class?: string, text?: string, href?: string, hidden?: boolean }} [attrs]
 * @param {Node[]} [hijos]
 */
function nodo(tag, attrs = {}, hijos = []) {
  const elemento = document.createElement(tag);
  if (attrs.class) elemento.className = attrs.class;
  if (attrs.text != null) elemento.textContent = attrs.text;
  if (tag === "a" && attrs.href) elemento.setAttribute("href", attrs.href);
  if (attrs.hidden) elemento.hidden = true;
  for (const hijo of hijos) elemento.append(hijo);
  return elemento;
}

/** @param {HTMLElement} elemento @param {string} nombre @param {string} valor */
function prop(elemento, nombre, valor) {
  elemento.setAttribute(nombre, valor);
  return elemento;
}

/** @param {string} texto */
function icono(texto) {
  return nodo("span", { class: "material-symbols-rounded qa-icon", text: texto });
}

/** @param {string} texto @param {() => void} alClick */
function boton(texto, alClick) {
  const elemento = nodo("button", { class: "qa-button qa-button--secondary", text: texto });
  elemento.type = "button";
  elemento.addEventListener("click", alClick);
  return elemento;
}

/** @param {ParentNode} contenedor @param {number} cantidad @param {string} clase */
function esqueletos(contenedor, cantidad, clase) {
  for (let i = 0; i < cantidad; i += 1) contenedor.append(nodo("div", { class: clase }));
}

/**
 * @param {{
 *   usuario: { estado: string, datos: { saludo: string, fecha: string, mision: string, acciones: { id: string, texto: string, href: string }[] } | null },
 *   resumen: { estado: string, datos: { titulo: string, valor: string, detalle: string, antiguedad: string | null, tono: string, href: string, nombreAccesible: string }[] | null, error: { texto: string } | null },
 *   pendientes: { estado: string, datos: { titulo: string, meta: string, tono: string | null, icono: string, href: string }[], filtro: string, conteos: Record<string, number>, textoVacio: string | null },
 *   agenda: { estado: string, datos: { nombre: string, tipo: string | null, dia: string, mes: string, horario: string, ubicacion: string | null, cupo: string, tono: string, etiqueta: string, porcentaje: number | null, href: string }[], alertaCobertura: { texto: string, href: string } | null, textoVacio: string | null },
 *   destacado: { estado: string, datos: { nombre: string, espera: string, detalle: string, fotoUrl: string | null, href: string } | null },
 *   conteos: { solicitudes: number, donativos: number },
 *   actualizadoEn: Date | null,
 *   refrescando: boolean,
 * }} estado
 */
function pintar(estado) {
  if (!raiz) return;
  raiz.replaceChildren();

  const falloAlguno = [estado.resumen, estado.pendientes, estado.agenda].some(
    (widget) => widget.estado === "error",
  );
  const jornadaHref = estado.usuario.datos?.acciones.find(
    (accion) => accion.id === "jornada",
  )?.href;

  const menu = document.querySelector("admin-sidebar");
  if (menu && "ponerConteos" in menu) {
    /** @type {{ ponerConteos: (conteos: { solicitudes: number, donativos: number }) => void }} */ (
      menu
    ).ponerConteos(estado.conteos);
  }

  raiz.append(
    nodo("div", { class: "qa-dashboard" }, [
      encabezado(estado, falloAlguno),
      indicadores(estado.resumen),
      cuerpo(estado, jornadaHref),
    ]),
  );
}

function encabezado(estado, falloAlguno) {
  const datos = estado.usuario.datos;
  const texto = nodo("div", {}, [
    nodo("h1", {
      class: "qa-dashboard__saludo qa-text-display",
      text: datos?.saludo ?? "",
    }),
    nodo("p", {
      class: "qa-dashboard__meta",
      text: datos ? `${datos.mision} · ${datos.fecha}` : "",
    }),
    nodo("p", {
      class: "qa-freshness",
      text: textoActualizado(estado.actualizadoEn, new Date()),
    }),
    nodo("p", {
      class: "qa-dashboard__parcial",
      text: falloAlguno ? COPY.parcial : "",
      hidden: !falloAlguno,
    }),
    prop(
      nodo("p", {
        class: "qa-sr-only",
        text:
          estado.actualizadoEn && !estado.refrescando && !falloAlguno ? COPY.panelActualizado : "",
      }),
      "aria-live",
      "polite",
    ),
  ]);
  if (estado.usuario.estado === "cargando") {
    texto.append(nodo("div", { class: "qa-skeleton qa-skeleton--linea" }));
  }

  const acciones = nodo("div", { class: "qa-dashboard__acciones" });
  for (const accion of datos?.acciones ?? []) {
    acciones.append(
      nodo("a", { class: "qa-button qa-button--secondary", text: accion.texto, href: accion.href }),
    );
  }
  const actualizar = boton(COPY.actualizar, () => void pagina.actualizar());
  actualizar.setAttribute("aria-busy", String(estado.refrescando));
  acciones.append(actualizar);

  return nodo("header", { class: "qa-dashboard__header" }, [texto, acciones]);
}

function indicadores(resumen) {
  const seccion = nodo("section", { class: "qa-dashboard__kpis" });
  prop(seccion, "aria-busy", String(resumen.estado === "cargando"));
  prop(seccion, "aria-labelledby", "inicio-kpis");
  seccion.append(nodo("h2", { class: "qa-sr-only", text: "Indicadores" }));
  seccion.querySelector("h2")?.setAttribute("id", "inicio-kpis");

  if (resumen.estado === "cargando") {
    esqueletos(seccion, 4, "qa-skeleton qa-skeleton--kpi");
    return seccion;
  }
  if (resumen.estado === "error") {
    seccion.append(
      nodo("div", { class: "qa-card qa-error-state" }, [
        nodo("p", { text: resumen.error?.texto ?? "" }),
        boton(COPY.reintentar, () => void pagina.reintentar("resumen")),
      ]),
    );
    return seccion;
  }
  for (const kpi of resumen.datos ?? []) {
    const tarjeta = nodo("a", { class: "qa-kpi-card", href: kpi.href });
    tarjeta.setAttribute("aria-label", kpi.nombreAccesible);
    if (kpi.tono !== "neutral") tarjeta.dataset.tono = kpi.tono;
    const antiguedad = nodo("p", { class: "qa-kpi-card__antiguedad" });
    if (kpi.antiguedad) {
      if (kpi.tono === "warning" || kpi.tono === "danger") {
        antiguedad.append(nodo("span", { class: `qa-dot qa-dot--${kpi.tono}` }));
      }
      antiguedad.append(nodo("span", { text: kpi.antiguedad }));
    }
    tarjeta.append(
      nodo("p", { text: kpi.titulo }),
      nodo("p", { class: "qa-kpi-card__valor qa-text-numeral", text: kpi.valor }),
      nodo("p", { class: "qa-kpi-card__detalle", text: kpi.detalle }),
      antiguedad,
    );
    seccion.append(tarjeta);
  }
  return seccion;
}

function cuerpo(estado, jornadaHref) {
  return nodo("div", { class: "qa-dashboard__cuerpo" }, [
    bandeja(estado.pendientes),
    nodo("div", { class: "qa-pila" }, [
      agenda(estado.agenda, jornadaHref),
      perro(estado.destacado),
    ]),
  ]);
}

function bandeja(pendientes) {
  const seccion = nodo("section", { class: "qa-card" });
  prop(seccion, "aria-busy", String(pendientes.estado === "cargando"));
  prop(seccion, "aria-labelledby", "inicio-bandeja");
  seccion.append(
    nodo("div", { class: "qa-section-header" }, [nodo("h2", { text: COPY.atencion })]),
  );
  seccion.querySelector("h2")?.setAttribute("id", "inicio-bandeja");

  if (pendientes.estado === "cargando") {
    const pila = nodo("div", { class: "qa-pila" });
    esqueletos(pila, 4, "qa-skeleton qa-skeleton--fila");
    seccion.append(pila);
    return seccion;
  }
  if (pendientes.estado === "error") {
    seccion.append(
      nodo("div", { class: "qa-error-state" }, [
        nodo("p", { text: pendientes.textoVacio ?? "" }),
        boton(COPY.reintentar, () => void pagina.reintentar("pendientes")),
      ]),
    );
    return seccion;
  }

  const chips = nodo("div", { class: "qa-chip-group" });
  prop(chips, "role", "radiogroup");
  prop(chips, "aria-label", "Filtrar pendientes");
  /** @type {Array<"todos" | "adopcion" | "donativo" | "factura">} */
  const filtros = ["todos", "adopcion", "donativo", "factura"];
  for (const clave of filtros) {
    const chip = nodo("button", {
      class: "qa-chip",
      text: `${COPY.filtros[clave]} ${pendientes.conteos[clave] ?? 0}`,
    });
    chip.type = "button";
    chip.setAttribute("aria-pressed", String(pendientes.filtro === clave));
    chip.addEventListener("click", () => pagina.filtrarPendientes(clave));
    chips.append(chip);
  }
  seccion.append(chips);

  if (pendientes.textoVacio) {
    const vacio = nodo("div", { class: "qa-empty-state" }, [
      icono("pets"),
      nodo("p", { text: pendientes.textoVacio }),
    ]);
    if (pendientes.filtro !== "todos") {
      vacio.append(boton(COPY.verTodo, () => pagina.filtrarPendientes("todos")));
    }
    seccion.append(vacio);
    return seccion;
  }

  const lista = nodo("ul", { class: "qa-task-list" });
  for (const tarea of pendientes.datos) {
    const enlace = nodo("a", { class: "qa-task-item", href: tarea.href }, [
      icono(tarea.icono),
      nodo("span", { class: "qa-task-item__texto" }, [
        nodo("span", { text: tarea.titulo }),
        nodo("span", { class: "qa-task-item__meta", text: tarea.meta }),
      ]),
      tarea.tono ? nodo("span", { class: `qa-dot qa-dot--${tarea.tono}` }) : nodo("span"),
    ]);
    lista.append(nodo("li", {}, [enlace]));
  }
  seccion.append(
    lista,
    nodo("p", {
      class: "qa-freshness",
      text: textoPieBandeja(pendientes.conteos.todos, pendientes.datos.length),
    }),
  );
  return seccion;
}

function agenda(estado, jornadaHref) {
  const seccion = nodo("section", { class: "qa-card" });
  prop(seccion, "aria-busy", String(estado.estado === "cargando"));
  prop(seccion, "aria-labelledby", "inicio-agenda");
  seccion.append(
    nodo("div", { class: "qa-section-header" }, [
      nodo("h2", { text: COPY.jornadas }),
      nodo("a", { href: ENLACE_CALENDARIO, text: COPY.verCalendario }),
    ]),
  );
  seccion.querySelector("h2")?.setAttribute("id", "inicio-agenda");

  if (estado.estado === "cargando") {
    const pila = nodo("div", { class: "qa-pila" });
    esqueletos(pila, 3, "qa-skeleton qa-skeleton--fila");
    seccion.append(pila);
    return seccion;
  }
  if (estado.estado === "error") {
    seccion.append(
      nodo("div", { class: "qa-error-state" }, [
        nodo("p", { text: estado.textoVacio ?? "" }),
        boton(COPY.reintentar, () => void pagina.reintentar("agenda")),
      ]),
    );
    return seccion;
  }
  if (estado.estado === "vacio") {
    const vacio = nodo("div", { class: "qa-empty-state" }, [
      nodo("p", { text: estado.textoVacio ?? "" }),
    ]);
    if (jornadaHref)
      vacio.append(
        nodo("a", {
          class: "qa-button qa-button--secondary",
          text: COPY.crearJornada,
          href: jornadaHref,
        }),
      );
    seccion.append(vacio);
  } else {
    const lista = nodo("ul", { class: "qa-event-list" });
    for (const jornada of estado.datos) {
      const texto = nodo("span", { class: "qa-event-item__texto" }, [
        nodo("span", { text: jornada.nombre }),
        nodo("span", {
          class: "qa-event-item__meta",
          text: [jornada.horario, jornada.ubicacion, jornada.cupo].filter(Boolean).join(" · "),
        }),
        nodo("span", { class: `qa-badge qa-badge--${jornada.tono}`, text: jornada.etiqueta }),
      ]);
      if (jornada.porcentaje != null) {
        const barra = nodo("span", { class: "qa-progress" }, [
          nodo("span", { class: "qa-progress__bar" }),
        ]);
        barra.firstElementChild?.setAttribute("style", `width: ${jornada.porcentaje}%`);
        texto.append(barra);
      }
      lista.append(
        nodo("li", {}, [
          nodo("a", { class: "qa-event-item", href: jornada.href }, [
            nodo("span", { class: "qa-date-block" }, [
              nodo("span", { text: jornada.dia }),
              nodo("span", { text: jornada.mes }),
            ]),
            texto,
          ]),
        ]),
      );
    }
    seccion.append(lista);
  }
  if (estado.alertaCobertura) {
    seccion.append(
      nodo("div", { class: "qa-alert" }, [
        nodo("p", { text: estado.alertaCobertura.texto }),
        nodo("a", { href: estado.alertaCobertura.href, text: COPY.crearJornada }),
      ]),
    );
  }
  return seccion;
}

function perro(destacado) {
  if (destacado.estado === "oculto") return nodo("section", { hidden: true });
  const seccion = nodo("section", { class: "qa-card" });
  prop(seccion, "aria-busy", String(destacado.estado === "cargando"));
  prop(seccion, "aria-labelledby", "inicio-perro");
  seccion.append(nodo("h2", { text: COPY.esperando }));
  seccion.querySelector("h2")?.setAttribute("id", "inicio-perro");
  if (destacado.estado === "cargando" || !destacado.datos) {
    seccion.append(nodo("div", { class: "qa-skeleton qa-skeleton--kpi" }));
    return seccion;
  }
  const foto = destacado.datos.fotoUrl
    ? prop(nodo("img", { class: "qa-dog__foto" }), "alt", destacado.datos.nombre)
    : nodo("span", { class: "qa-dog__foto qa-icon material-symbols-rounded", text: "pets" });
  if (foto.tagName === "IMG") foto.setAttribute("src", destacado.datos.fotoUrl ?? "");
  seccion.append(
    nodo("div", { class: "qa-dog" }, [
      foto,
      nodo("div", {}, [
        nodo("p", { text: destacado.datos.nombre }),
        nodo("p", { text: destacado.datos.espera }),
        nodo("p", { class: "qa-event-item__meta", text: destacado.datos.detalle }),
        nodo("p", { text: COPY.sinSolicitudes }),
        nodo("a", { href: destacado.datos.href, text: COPY.verFicha }),
      ]),
    ]),
  );
  return seccion;
}
