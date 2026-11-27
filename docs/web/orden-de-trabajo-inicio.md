# Orden de trabajo — Dashboard de Inicio

> Cómo construir el panel de Inicio **sin esperar** a estandarizar el resto del panel.
> Fuente de verdad de diseño y arquitectura: [guía maestra](./guia-maestra-web.md) (§0.3 y §5).
> Si este orden y la guía se contradicen en un contrato de datos, gana la guía. La arquitectura compacta de la §5 gana sobre cualquier mención vieja a `inicio.viewmodel.js`, `JS/core` o `JS/ui`.

## Hecho

1. Página vacía en `/admin/inicio`. El login llega ahí.
2. `CSS/foundations/tokens.inicio.css`, cargado solo por esa página.
3. Reglas de prioridad, entidades, mappers, fixtures y `InicioRepositoryMock` con la suite de contrato.
4. Estado y textos en `JS/jsInicio/inicio.js` y `inicio.presentar.js`. Pruebas de carga, vacío, error de un widget, reintento, filtro y 401.

No se crea un archivo ViewModel. `crearInicio` es quien guarda el estado.

## Criterio

El dashboard no hereda estilos ni componentes de los módulos actuales. Consume tokens nuevos (`qa-*`) y el shell que ya existe (`<admin-sidebar>`, `<admin-topbar>`, `layoutAdmin.css`). Los módulos viejos no se migran en este corte.

Relación con las fases de la [§8](./guia-maestra-web.md#8-hoja-de-ruta-por-fases): F4 y F5 están hechos en forma compacta. Sigue F6 (pintar). Quedan fuera el catálogo vivo, `JS/ui`, las guardas de la [§3.6](./guia-maestra-web.md#36-guardas-automáticas) y F9 (migración de módulos).

## Fuera de este corte

- Unificar botones, badges y encabezados de Perros, Solicitudes, Donativos y Voluntariado ([§1.4](./guia-maestra-web.md#14-hallazgos-de-la-auditoría) V3–V5, [§1.5](./guia-maestra-web.md#15-auditoría-del-atomic-design-existente)).
- Redefinir `--md-sys-*` ni el alias legado de la [§3.2](./guia-maestra-web.md#32-archivos-y-orden-de-cascada).
- Esperar `GET /api/inicio/admin/*`. La primera versión usa `InicioRepositoryMock` ([§5.4](./guia-maestra-web.md#54-fuentes-de-datos-intercambiables), [§6](./guia-maestra-web.md#6-matriz-de-viabilidad-contra-el-backend-y-contratos)).
- Paleta "Ir a…" y campana con comportamiento. El campo y el botón del topbar se quedan como están hasta que el dashboard sea usable ([§2.4](./guia-maestra-web.md#24-widgets-y-acciones) W5).

## Decisiones que se cierran con la opción recomendada

El resto de la [§7](./guia-maestra-web.md#7-decisiones-abiertas) no bloquea este corte.

| ID  | Se adopta                                                                                                    | Dónde vive en la guía                                             |
| --- | ------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------- |
| D3  | La vista pinta con funciones y `textContent`. Custom Elements solo en el shell. No hay `JS/ui`.              | [§5.6](./guia-maestra-web.md#56-vista)                            |
| D6  | Los enlaces salen con `?estado=` y `?abrir=` aunque el módulo destino aún no los lea.                        | [§2.5](./guia-maestra-web.md#25-mapa-de-navegación)               |
| D9  | `VITE_FUENTE_INICIO=mock`. En desarrollo, `?fuente=` y `?escenario=` sobrescriben. En producción se ignoran. | [§5.4](./guia-maestra-web.md#54-fuentes-de-datos-intercambiables) |

Reglas que aplican desde el primer archivo: [§0.1](./guia-maestra-web.md#01-reglas-de-oro-obligatorias) R1–R8.

---

## 1. Página vacía en la ruta que ya existe

**Estado: hecho.**

**Hecho cuando:** `/admin/inicio` renderiza el shell y, tras iniciar sesión, el login deja al usuario ahí.

| Paso                                                                                                                                | Referencia                                                                                                                      |
| ----------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| `HTML/htmlInicio/adminInicio.html` con el mismo shell que Solicitudes o Donativos.                                                  | [§1.3](./guia-maestra-web.md#13-pantallas-existentes-y-responsabilidades), [§4.4](./guia-maestra-web.md#44-templates-y-páginas) |
| Registrar el HTML en `rollupOptions.input` de `apps/web/vite.config.js`. La ruta pública ya está en `JS/rutas.js` (`id: "inicio"`). | [§1.4](./guia-maestra-web.md#14-hallazgos-de-la-auditoría) A7, [§10.2](./guia-maestra-web.md#102-archivos-de-referencia)        |
| En `JS/jsPerfiles/login.js`, cambiar `rutaDe("perros")` por `rutaDe("inicio")` (login y sesión restaurada).                         | [§1.4](./guia-maestra-web.md#14-hallazgos-de-la-auditoría) A6, regla R7                                                         |

No se diseña el contenido en este paso. El área `.content-area` puede quedar vacía.

## 2. Tokens mínimos del dashboard

**Estado: hecho.** `CSS/foundations/tokens.inicio.css`.

**Hecho cuando:** Inicio tiene color, tipo, espacio, radio y elevación sin un solo hex en sus componentes, y las otras pantallas se ven igual que antes.

Un archivo nuevo, por ejemplo `apps/web/CSS/foundations/tokens.inicio.css`, cargado solo por `adminInicio.html`. No sustituye `atomicDesign/atoms/variables.css`.

Incluir únicamente los semánticos que Inicio va a leer: superficie, texto, texto muted, borde, marca, acento, foco, los cinco tonos (`neutral`, `info`, `success`, `warning`, `danger`), escala de espacio, radios, elevación y los estilos de texto del saludo y del numeral del KPI.

| Referencia                                                            | Uso                                                                              |
| --------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| [§3.1](./guia-maestra-web.md#31-arquitectura-de-tokens-en-3-capas)    | Capas; los componentes no leen primitivos.                                       |
| [§3.3](./guia-maestra-web.md#33-catálogo-de-tokens-valores-iniciales) | Nombres y valores iniciales. Copiar esos nombres (`--qa-*`), no inventar otros.  |
| [§3.4](./guia-maestra-web.md#34-convenciones-css)                     | Prefijo `qa-` + BEM.                                                             |
| [§1.4](./guia-maestra-web.md#14-hallazgos-de-la-auditoría) V1–V3      | Por qué no se reutilizan los CSS de módulo: tokens indefinidos y hex duplicados. |

El `admin.css` con `@layer` y `tokens.legacy-alias.css` ([§3.2](./guia-maestra-web.md#32-archivos-y-orden-de-cascada)) no entran en este corte.

## 3. Dominio y mock antes que la vista

**Estado: hecho.** `InicioRepositoryMock` pasa la suite en `ok`, `vacio`, `parcial`, `error`, `lento` y `atrasado`. La prioridad tiene pruebas puras.

Carpeta: `apps/web/JS/jsInicio/`, según el árbol de la [§5.2](./guia-maestra-web.md#52-árbol-que-usa-inicio). No hay `usecases/`.

Orden interno:

1. Entidades y typedefs: `ResumenInicio`, `TareaPendiente`, `Agenda`, `PerroDestacado` ([§5.3](./guia-maestra-web.md#53-contratos-de-dominio-jsdoc)).
2. Reglas puras: `prioridad.js` y antigüedad ([§2.4](./guia-maestra-web.md#24-widgets-y-acciones) W2). Umbrales en un solo sitio; valores de partida de D7 en la [§7](./guia-maestra-web.md#7-decisiones-abiertas) (solicitudes 3 días, donativos 2, facturas 3).
3. Fixtures con forma real: UUID, folio `QA-` + 8 caracteres, estados `por_revisar` / `en_revision` / `Pendiente de validación`. No copiar el mock de Solicitudes ([§1.4](./guia-maestra-web.md#14-hallazgos-de-la-auditoría) A3).
4. Mappers DTO → entidad y `InicioRepositoryMock` que cumple el contrato de la [§5.3](./guia-maestra-web.md#53-contratos-de-dominio-jsdoc).
5. Suite `InicioRepository.contract.js` ([§5.8](./guia-maestra-web.md#58-estrategia-de-pruebas)).

Los JSON de los fixtures deben poder sustituirse por las respuestas de C2, C3 y C4 ([§6.3](./guia-maestra-web.md#63-contratos-sugeridos-especificación-para-backend)) sin cambiar el mapper más que en los campos que el backend ajuste.

`domain/` no importa `fetch`, `document` ni `data/` ([§5.1](./guia-maestra-web.md#51-capas-y-regla-de-dependencias)).

## 4. Estado y textos

**Estado: hecho.** No existe `inicio.viewmodel.js`. La vista todavía no existe.

| Pieza                                                                                                        | Dónde quedó                                                         |
| ------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------- |
| Estado por widget: `cargando \| listo \| vacio \| error` (destacado: `oculto`). Conteos en `estado.conteos`. | `crearInicio` en `JS/jsInicio/inicio.js`                            |
| Intenciones: `iniciar`, `actualizar`, `reintentar(widget)`, `filtrarPendientes`, `destruir`.                 | El mismo archivo                                                    |
| `Promise.allSettled` + `AbortController`. Un fallo no apaga la página.                                       | [§5.5](./guia-maestra-web.md#55-estado-crearinicio-en-iniciojs), R6 |
| Textos, tono y `href`.                                                                                       | `inicio.presentar.js` (`COPY`)                                      |
| Enlaces con `rutaDe` / `enlaceA`.                                                                            | `JS/rutas.js`                                                       |
| 401 → `rutaDe("login")` una sola vez.                                                                        | `inicio.js`                                                         |
| Mock vs API.                                                                                                 | `elegirFuenteInicio` en `inicio.js`                                 |

Pruebas: `JS/jsInicio/inicio.test.js`.

## 5. Pintar la página

**Estado: hecho.** `inicio.view.js` y `CSS/cssInicio/adminInicio.css`. `/admin/inicio` muestra los cinco widgets y `?escenario=` cambia lo que se ve.

No se construye el inventario de la [§4](./guia-maestra-web.md#4-inventario-atomic-design) ni `JS/ui`. Las clases que hagan falta (`qa-card`, `qa-kpi-card`, `qa-task-item`, estados vacío y error) viven en `CSS/cssInicio/` y solo leen tokens del paso 2. No redefinen `.badge-estado`, `.section-title` ni `.btn-primary`.

`inicio.view.js` crea los nodos con `textContent`. Los datos del servidor no entran por `innerHTML`. La página llama `iniciar`, `reintentar` y `filtrarPendientes`; no formatea textos ni arma `href`.

Sin hex nuevo fuera de `tokens.inicio.css`. Accesibilidad mínima al pintar: [§2.9](./guia-maestra-web.md#29-accesibilidad-mínimos).

## 6. Widgets, en orden de valor

**Estado: hecho**, junto con el paso 5. Falta pulir micro-interacciones (elevación y conteo animado).

Plantilla de rejilla: [§2.3](./guia-maestra-web.md#23-distribución-propuesta-wireframe) y template `dashboard` de la [§4.4](./guia-maestra-web.md#44-templates-y-páginas). En viewports chicos, una columna; no hace falta pulir &lt; 640 px en este corte.

| Orden | Widget                                                                                   | Guía                                                        | Nota de este corte                                     |
| ----- | ---------------------------------------------------------------------------------------- | ----------------------------------------------------------- | ------------------------------------------------------ |
| 1     | Encabezado: saludo, fecha, acciones Registrar perro y Crear jornada, "Actualizado hace…" | [§2.4](./guia-maestra-web.md#24-widgets-y-acciones) W0      | El nombre puede ser el del mock hasta el paso 7.       |
| 2     | Cuatro KPIs clicables                                                                    | W1, [§2.5](./guia-maestra-web.md#25-mapa-de-navegación)     | Toda la tarjeta es un `<a>`.                           |
| 3     | Bandeja "Requiere tu atención" (máx. 6, chips, pie)                                      | W2, [§2.6](./guia-maestra-web.md#26-estados-de-cada-widget) | Sin botones de aprobar o rechazar.                     |
| 4     | Próximas jornadas + alerta de semana sin cobertura                                       | W3                                                          | Enlaces `?id=` y `?fecha=` ya existen en Voluntariado. |
| 5     | "Lleva más tiempo esperando"                                                             | W4                                                          | Si no hay candidato, el widget no se renderiza.        |

Micro-interacciones (elevación, conteo animado) después de que los cinco pinten datos: [§2.7](./guia-maestra-web.md#27-micro-interacciones-y-motion). Respetar `prefers-reduced-motion`.

Checklist de la página al cerrar el paso: [§9](./guia-maestra-web.md#9-checklists-de-pr) "Widget / feature nueva".

## 7. Shell, solo lo que Inicio necesita

**Estado: hecho.** El topbar pide `/me` y `/profile`. Si no hay sesión, muestra a Ana Martínez en lugar de "Dummy Name". Solicitudes y Donativos del menú leen `estado.conteos` solo en Inicio. No hay archivo de logo, así que el logo roto se queda.

| Pieza                  | Referencia                                                                                                              | Alcance                                                                                                      |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| Nombre y rol           | [§6.1](./guia-maestra-web.md#61-matriz) fila "Nombre y rol". Hoy: `GET /api/perfiles/me` + `GET /api/perfiles/profile`. | Quitar el "Dummy Name" de `admin-topbar.js` ([§1.4](./guia-maestra-web.md#14-hallazgos-de-la-auditoría) A5). |
| Contadores del sidebar | [§2.4](./guia-maestra-web.md#24-widgets-y-acciones) W5, [§5.5](./guia-maestra-web.md#55-estado-crearinicio-en-iniciojs) | Leen `estado.conteos`. No hay `pendientes.store.js`.                                                         |
| Logo roto              | [§1.4](./guia-maestra-web.md#14-hallazgos-de-la-auditoría) V11                                                          | Solo si ya hay archivo de logo; no bloquea el dashboard.                                                     |

No implementar en este paso la paleta Ctrl/⌘+K ni el popover de la campana.

## Después de que el dashboard se pueda enseñar

1. `InicioRepositoryCompuesto` sobre los endpoints que ya existen ([§6.1](./guia-maestra-web.md#61-matriz) filas ✅ y ⚠️).
2. `InicioRepositoryApi` cuando backend publique C1–C4 ([§6.3](./guia-maestra-web.md#63-contratos-sugeridos-especificación-para-backend)). El cambio es el repositorio que elige `inicio.js`, no la vista ni los textos.
3. Deep links reales en Solicitudes, Donativos, Facturas y Perros (D6, [§2.5](./guia-maestra-web.md#25-mapa-de-navegación)).
4. Migración visual del resto del panel ([§8](./guia-maestra-web.md#8-hoja-de-ruta-por-fases) F9).
