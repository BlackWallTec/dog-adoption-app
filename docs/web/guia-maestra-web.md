# Guía maestra Web — Panel de administración Qariño Animal

> **Estado:** En curso · **Fecha:** 26 nov 2026
> **Alcance:** `apps/web` (panel administrativo). Proyecto escolar: Inicio se construye compacto, alineado con los módulos que ya existen (Solicitudes, Donativos). No se crea `JS/core`, `JS/ui` ni un ViewModel en archivo propio.
> El diseño de la pantalla (widgets, tokens `qa-*`, contratos con el backend) sigue vigente. La arquitectura en capas de la §5 original no.
> **Marcadores:** `🟡 DECISIÓN` = pendiente de confirmar con el equipo · `[REUTILIZAR]` · `[REFACTORIZAR]` · `[CREAR NUEVO]` · `✅ / ⚠️ / ❌` = disponibilidad en backend.

---

## Índice

0. [Cómo usar esta guía (humanos e IA)](#0-cómo-usar-esta-guía-humanos-e-ia)
1. [Auditoría del repositorio actual](#1-auditoría-del-repositorio-actual)
2. [Diseño y UX del Dashboard de Inicio](#2-diseño-y-ux-del-dashboard-de-inicio)
3. [Sistema visual "a prueba de cambios"](#3-sistema-visual-a-prueba-de-cambios)
4. [Inventario Atomic Design](#4-inventario-atomic-design)
5. [Arquitectura de Inicio](#5-arquitectura-de-inicio)
6. [Matriz de viabilidad contra el backend y contratos](#6-matriz-de-viabilidad-contra-el-backend-y-contratos)
7. [Decisiones abiertas](#7-decisiones-abiertas)
8. [Hoja de ruta por fases](#8-hoja-de-ruta-por-fases)
9. [Checklists de PR](#9-checklists-de-pr)
10. [Anexos](#10-anexos)

---

## 0. Cómo usar esta guía (humanos e IA)

Esta guía es la **fuente de verdad** del diseño y la arquitectura del panel web. Si el código contradice la guía, se corrige el código o se abre una propuesta para cambiar la guía; nunca se "improvisa" un tercer camino.

### 0.1 Reglas de oro (obligatorias)

| #   | Regla                                                                                                                                                  |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| R1  | **Ningún componente usa valores crudos.** Nada de hex, `px` mágicos, sombras o fuentes escritas a mano: solo tokens semánticos o de componente (§3).   |
| R2  | **El color comunica estado; el ícono comunica módulo.** Nunca se usa color para distinguir módulos (Adopciones, Donativos…).                           |
| R3  | **El dashboard prioriza; el módulo resuelve.** Inicio no aprueba, rechaza, edita ni sube nada. Solo resume y lleva al lugar correcto con contexto.     |
| R4  | **La vista no hace `fetch` y no formatea.** `inicio.js` guarda el estado y no pinta. `domain/` no conoce HTTP ni DOM. No hay archivo ViewModel aparte. |
| R5  | **Mock y API implementan el mismo contrato** y pasan la misma suite de pruebas de contrato.                                                            |
| R6  | **Cada widget carga, falla y se reintenta de forma independiente.** Un endpoint caído nunca deja la página en blanco.                                  |
| R7  | **La navegación usa `rutaDe(id)` de `JS/rutas.js`.** Nunca se enlaza a un `.html` físico.                                                              |
| R8  | **Los textos de Inicio viven en `inicio.presentar.js`** (`COPY`), en español de México, con el tono de §2.8.                                           |
| R9  | **Accesible por defecto:** HTML semántico, foco visible, contraste AA, `prefers-reduced-motion` respetado.                                             |
| R10 | **No hay catálogo vivo ni `JS/ui` en este proyecto.** Una clase nueva de Inicio vive en `CSS/cssInicio/` y solo usa tokens `qa-*`.                     |

### 0.2 Instrucciones para agentes de IA

- No recrees `JS/core`, `JS/ui`, `presentation/`, casos de uso de una línea ni `pendientes.store.js`. El estado de Inicio vive en `crearInicio` (`JS/jsInicio/inicio.js`).
- Antes de crear un color, espaciado o radio, busca el token en §3.3. Si no existe, **propón** el token; no lo inventes en el archivo del componente.
- La vista no llama `fetch`. Lee `estado()` y llama las intenciones de `crearInicio`.
- Al conectar backend, consulta §6. Si el dato está marcado ❌, usa el mock y deja el contrato propuesto intacto. El cambio es otra implementación del repositorio, no una capa nueva.
- Cualquier punto marcado `🟡 DECISIÓN` no debe resolverse por cuenta propia: usa la opción "recomendada" y deja un comentario `TODO(decision-Dx)` solo si el código lo requiere.

### 0.3 Decisión de alcance (26 nov 2026)

El árbol MVVM + Clean (casos de uso, presenters por archivo, `JS/core`, `JS/ui`, catálogo vivo) describe una migración de todo el panel. Este repositorio es un proyecto escolar que se está ordenando porque los módulos ya existentes son inconsistentes entre sí. Inicio no abre esa migración.

Se conserva lo que corrige esas inconsistencias: reglas de prioridad con pruebas, mock con la forma real del backend (UUID, folio `QA-` + 8 caracteres), mappers, enlaces con `rutaDe` / `enlaceA`, tokens `qa-*` solo en Inicio, y un fallo por widget. El estado y los textos caben en dos archivos, al modo de `adminDonativos.js` + su vista.

---

## 1. Auditoría del repositorio actual

### 1.1 Stack del apartado web

| Aspecto     | Estado actual                                                                                                                              |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| Tooling     | Vite+ (`vp dev/build/test/check`), monorepo pnpm (`apps/web`, `apps/api`, `apps/shared`, `apps/android`).                                  |
| Tipo de app | **MPA**: un HTML por pantalla. `vite.config.js` reescribe rutas públicas (`/admin/...`) al archivo HTML usando `JS/rutas.js`.              |
| Lenguaje    | JavaScript vanilla ESM. Sin framework de UI. Lint con type-awareness (oxlint vía Vite+).                                                   |
| Componentes | 2 Custom Elements de shell: `<admin-sidebar>` y `<admin-topbar>` (`apps/web/components`).                                                  |
| Estilos     | CSS plano con variables `--md-sys-*` (inspiradas en Material 3), un `layoutAdmin.css` global y una hoja por módulo.                        |
| Íconos      | Material Symbols Rounded **y** Material Icons Round (solo en Catálogo).                                                                    |
| Tipografía  | Nunito (titulares, solo peso 800) + Roboto (cuerpo), cargadas desde Google Fonts en cada HTML con URLs distintas.                          |
| Librerías   | FullCalendar (calendario de voluntariado), `temporal-polyfill`, `@qarino/shared` (clases de error comunes). Bootstrap 5 **solo** en login. |
| Pruebas     | `vite-plus/test` (Vitest), pruebas co-ubicadas `*.test.js`, mayormente sobre funciones puras.                                              |
| Despliegue  | Vercel Services: `web` (estático `dist`) + `api` (Express). `/api/*` va al servicio API.                                                   |

### 1.2 Estructura de carpetas actual (web)

```
apps/web/
├─ components/            admin-sidebar.js, admin-topbar.js  (Custom Elements del shell)
├─ CSS/
│  ├─ layoutAdmin.css     reset + template + modales + formularios + dropzone + badges (≈470 líneas)
│  ├─ atomicDesign/
│  │  ├─ atoms/           variables.css, buttons.css, inputs.css
│  │  ├─ molecules/       search-bar.css
│  │  └─ organisms/       sidebar.css, topbar.css
│  └─ css<Modulo>/        cssCatalogo, cssDonativos, cssPerfiles, cssSolicitudes, cssVoluntariado
├─ HTML/html<Modulo>/     una página por pantalla (+ fragmentos de modales en Catálogo)
├─ JS/
│  ├─ rutas.js            registro central de pantallas (id, etiqueta, ícono, ruta, archivo)
│  ├─ utils/              feedbackHelper.js, modalLoader.js
│  └─ js<Modulo>/         lógica por módulo (vista + fetch + estado mezclados en la mayoría)
├─ public/                dog-sample.png, .well-known/
└─ src/main.js            redirige a login
```

### 1.3 Pantallas existentes y responsabilidades

Esta tabla define **qué NO debe hacer el Dashboard** (para no duplicar).

| Ruta pública                        | Responsabilidad del módulo                                                                       | Lo que Inicio NO hace                      |
| ----------------------------------- | ------------------------------------------------------------------------------------------------ | ------------------------------------------ |
| `/admin/inicio`                     | **No existe aún.** `rutas.js` apunta a `HTML/htmlInicio/adminInicio.html` (archivo inexistente). | —                                          |
| `/admin/perros`                     | CRUD de perros, filtros, búsqueda local, activación/desactivación masiva, registro y edición.    | Listar/editar perros, filtros de catálogo. |
| `/admin/solicitudes`                | Listado con chips de resumen, filtros, paginación, detalle en modal y resolución.                | Tabla de solicitudes, cambiar estado.      |
| `/admin/voluntariado`               | Hub: crear jornada, calendario, consultar inscritos pegando un ID.                               | Crear/editar programas.                    |
| `/admin/voluntariado/nueva`         | Crear/editar programa. Acepta `?id=` (edición) y `?fecha=` (fecha precargada).                   | Formularios.                               |
| `/admin/voluntariado/inscritos`     | Inscritos de un programa. Acepta `?id=<id_programa>`.                                            | Listas de inscritos.                       |
| `/admin/voluntariado/calendario`    | Calendario mensual + semanas sin cobertura.                                                      | Vista de calendario completa.              |
| `/admin/donativos`                  | Donativos pendientes de validación + aprobar.                                                    | Aprobar donativos.                         |
| `/admin/donativos/solicitudes-cfdi` | Solicitudes de factura: revisar, validar/cargar XML+PDF, reintentar correo.                      | Cargar CFDI.                               |
| `/donativos/resumen`                | Página pública con total de donativos pendientes.                                                | —                                          |
| `/admin/configuracion`              | **No existe aún.** Apunta a `HTML/htmlPerfiles/adminPerfiles.html` (inexistente).                | —                                          |
| `/admin/login`                      | Inicio de sesión (Bootstrap). Al entrar redirige a **Perros**.                                   | —                                          |

### 1.4 Hallazgos de la auditoría

**Visuales**

| ID  | Hallazgo                                                                                                                                                                                                                           | Impacto en el Dashboard                           |
| --- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- |
| V1  | Tokens **usados pero no definidos**: `--md-sys-color-on-primary`, `on-primary-container`, `on-secondary`, `on-secondary-container`, `surface`, `secondary-container-low`. Ej. `.btn-primary` pinta texto con un valor inexistente. | Botones y avatar con color de texto impredecible. |
| V2  | Dos espacios de nombres de tokens (`--md-sys-*` y `--qa-*` en `resumenDonativos.css`) más `:root` locales (`--cal-*` en calendario).                                                                                               | No hay una sola fuente de verdad.                 |
| V3  | ≈200 valores hex escritos a mano (63 solo en `adminSolicitudes.css`). Hay **tres verdes** de éxito (`#e6f4ea/#1e4620`, `#e4f3e8/#26734d`) y **tres rojos** (`#ffdad6`, `#fce8e6`, `#f9e3e3/#ad3b3b`).                              | Estados con colores distintos por pantalla.       |
| V4  | Selectores redefinidos por módulo: `.section-header/.section-title` (5 archivos), `.badge-estado` (5), `.table-card` (3), `.table-footer` (3), `.page-arrow` (2), `.btn-primary` (2), `.select-pill` (2).                          | El orden de carga decide cómo se ve la página.    |
| V5  | 8 variantes de botón sin sistema: `btn-primary`, `btn-primary-solid`, `outline-button`, `detail-button`, `btn-secundario`, `btn-text`, `boton`, `button-registrar`.                                                                | Acciones con jerarquía visual inconsistente.      |
| V6  | Dos familias de íconos y 3 formas distintas de cargar fuentes en los HTML.                                                                                                                                                         | Peso extra y glifos distintos.                    |
| V7  | `layoutAdmin.css` mezcla template, modales, formularios, dropzone y badges.                                                                                                                                                        | No es un "template"; difícil de mantener.         |
| V8  | Padding del área de contenido definido por módulo (32/40, 36/40…).                                                                                                                                                                 | Saltos visuales al navegar.                       |
| V9  | Sin escala tipográfica, de espaciado, radios, sombras ni motion.                                                                                                                                                                   | Cada pantalla decide tamaños.                     |
| V10 | Login usa Bootstrap 5 y referencia `CSS/bootstrap-theme.css`, que no existe.                                                                                                                                                       | Experiencia distinta al resto del panel.          |
| V11 | El sidebar busca `/public/logo.png` (no existe; con Vite los archivos de `public/` se sirven desde `/`). Tampoco existe `favicon.svg`.                                                                                             | Logo roto en el shell.                            |

**Frontend (JS / arquitectura)**

| ID  | Hallazgo                                                                                                                                                    |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A1  | Cada módulo tiene su propio wrapper de `fetch` con convenciones de error distintas (`Error("NO_AUTENTICADO")`, `error.status`, clases de `@qarino/shared`). |
| A2  | Rutas de login hardcodeadas y obsoletas (`/HTML/htmlPerfiles/login.html` en Solicitudes) en lugar de `rutaDe("login")`.                                     |
| A3  | Los mocks no reflejan el contrato real: el mock usa folio `QA-0248` e IDs numéricos; la API devuelve `QA-1A2B3C4D` y UUID.                                  |
| A4  | Vista, estado y red mezclados en el mismo archivo (`adminSolicitudes.view.js`, ≈480 líneas).                                                                |
| A5  | Topbar con nombre de administradora hardcodeado ("Dummy Name"); buscador y campana sin funcionalidad.                                                       |
| A6  | El login redirige a `perros`; al existir Inicio debe redirigir a `inicio`.                                                                                  |
| A7  | `inicio` y `configuracion` están en `rutas.js` pero sin HTML ni entrada en `rollupOptions.input` de `vite.config.js`.                                       |
| A8  | Etiqueta inválida `<dic>` en `adminPerros.html`.                                                                                                            |

**Lo que sí es sólido y se conserva:** `rutas.js` como registro central de pantallas; Custom Elements para el shell; utilidades puras con pruebas (`adminPerros.fila.js`, `adminSolicitudes.utils.js`); `@qarino/shared/errores.js` como convención de errores; el uso de `AbortController` + contador de petición para evitar condiciones de carrera.

### 1.5 Auditoría del Atomic Design existente

| Pieza actual                        | Archivo                              | Veredicto        | Motivo                                                                            |
| ----------------------------------- | ------------------------------------ | ---------------- | --------------------------------------------------------------------------------- |
| Variables de color y fuente         | `atoms/variables.css`                | `[REFACTORIZAR]` | Paleta válida como punto de partida, pero incompleta (V1) y sin capas.            |
| `.btn-primary`, `.icon-btn`         | `atoms/buttons.css`                  | `[REFACTORIZAR]` | Base aprovechable; falta sistema de variantes/tamaños y usa token inexistente.    |
| `.input-base`, `.textarea-base`     | `atoms/inputs.css`                   | `[REUTILIZAR]`   | Simples y correctos; solo migrar a tokens.                                        |
| `.global-search`                    | `molecules/search-bar.css`           | `[REFACTORIZAR]` | Duplica estilos de `.input-base`; ancho fijo.                                     |
| Sidebar                             | `organisms/sidebar.css` + componente | `[REFACTORIZAR]` | Estructura buena; agregar contadores, corregir logo, usar tokens.                 |
| Topbar                              | `organisms/topbar.css` + componente  | `[REFACTORIZAR]` | Usuario real, buscador/campana con comportamiento definido.                       |
| Template del shell                  | `layoutAdmin.css`                    | `[REFACTORIZAR]` | Separar: template / modal / formulario / dropzone / badge selector.               |
| Modales globales (feedback/confirm) | `layoutAdmin.css`                    | `[REFACTORIZAR]` | Útiles; mover a organismo `modal`. Usan Material Icons Round.                     |
| `.badge-estado` y variantes         | 5 hojas de módulo                    | `[REFACTORIZAR]` | Consolidar en un átomo `qa-badge` con tonos.                                      |
| `.section-header/.section-title`    | 5 hojas de módulo                    | `[REFACTORIZAR]` | Consolidar en organismo `qa-page-header`.                                         |
| `.table-card`, paginación           | 3 hojas de módulo                    | `[REFACTORIZAR]` | Consolidar en `qa-card` + molécula `qa-pagination` (fuera del alcance de Inicio). |
| `.status-chip`, `.badge-item`       | Solicitudes / layout                 | `[REFACTORIZAR]` | Consolidar en átomo `qa-chip` (seleccionable).                                    |
| `.progress-bar`                     | `layoutAdmin.css`                    | `[REFACTORIZAR]` | Base para `qa-progress` (cupo de jornadas).                                       |
| `.empty-cell`, `.estado-error`      | `layoutAdmin.css`                    | `[REFACTORIZAR]` | Base para `qa-empty-state` y `qa-error-state`.                                    |
| Lógica de foto con respaldo         | Perros y Solicitudes (duplicada)     | `[REFACTORIZAR]` | Un solo átomo `qa-dog-photo` con fallback.                                        |

### 1.6 Lectura del backend

**API:** Express, prefijo `/api/<módulo>`. Autenticación por cookie `token` en web (`verificarToken`) y rol con `requireRole(ROLES.ADMIN, ROLES.ROOT)`. Roles: `Root`, `Administrador`, `Cliente`.

**Tablas relevantes (Supabase/Postgres, `docs/base-de-datos/01_schema.sql`)**

| Dominio        | Tablas                                                                                                                    | Campos útiles para Inicio                                                                                                                                                                                                                                                                                                                                                                                  |
| -------------- | ------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Perros         | `perro`, `multimedia`, `raza`, `tamano`, `caracter`, `cuidado_especial`                                                   | `estado` (texto: Disponible, Adoptado, En tratamiento), `fecha_ingreso`, `activo`, `edad_aprox`, `sexo`.                                                                                                                                                                                                                                                                                                   |
| Solicitudes    | `solicitud_adopcion`, `estado_solicitud`, `documento`, `admin_revisa_solicitud_adopcion`, `periodo_prueba`, `seguimiento` | `fecha_solicitud`, `id_estado` → descripción (Pendiente de Revisión, En revisión, En periodo de prueba, Aprobada, Rechazada, Borrador; valores inferidos de la normalización en `solicitudAdmin.controller.js`, confirmar contra la tabla `estado_solicitud`), `fecha_resolucion`. **No hay columna `folio`**: se deriva como `QA-` + 8 primeros caracteres del UUID. `periodo_prueba.fecha_fin_prevista`. |
| Voluntariado   | `programa`, `tipo_programa`, `se_inscribe`                                                                                | `fecha_inicio/fin`, `cupo_maximo`, `estado_del_programa`, `se_inscribe.fecha_inscripcion`, `asistencia`.                                                                                                                                                                                                                                                                                                   |
| Donativos      | `donativo`, `solicitud_cfdi`, `datos_confidenciales`                                                                      | `donativo.estado` (Pendiente de validación, Validado), `monto`, `fecha`; `solicitud_cfdi.estado`, `correo_estado` (Pendiente de envío, Enviando, Error, Enviado).                                                                                                                                                                                                                                          |
| Notificaciones | `mensaje`, `notifica`                                                                                                     | **Solo para clientes** (`notifica.id_cliente`). No hay notificaciones para administradores.                                                                                                                                                                                                                                                                                                                |
| Usuarios       | `usuario`, `admin`, `cliente`, `rol`                                                                                      | `nombre`, `apellidos`, `rol`.                                                                                                                                                                                                                                                                                                                                                                              |

**Endpoints admin existentes relevantes**

| Endpoint                                                      | Respuesta (forma)                                                                             | Uso en Inicio                      |
| ------------------------------------------------------------- | --------------------------------------------------------------------------------------------- | ---------------------------------- |
| `GET /api/perfiles/me`                                        | `{ user: { uuid, rol } }`                                                                     | Rol (sin nombre).                  |
| `GET /api/perfiles/profile`                                   | `{ profile: { nombre, apellidos, ... } }`                                                     | Nombre para saludo/topbar.         |
| `GET /api/solicitudes/admin/solicitudes?estado&perro&page`    | `{ data[], resumen: { todas, pendientes, aprobadas, rechazadas }, paginacion }`               | KPI y bandeja (parcial).           |
| `GET /api/donativos/admin/pendientes`                         | `[ { id_donativo, monto, fecha, estado, usuario: { nombre, apellidos } } ]`                   | KPI y bandeja.                     |
| `GET /api/donativos/admin/solicitudes-cfdi`                   | `[ { id_solicitud_cfdi, id_donativo, fecha_solicitud, estado, donante, requiere_revision } ]` | KPI y bandeja.                     |
| `GET /api/donativos/pendientes/resumen` (público)             | `{ totalPendientes }`                                                                         | Alternativa ligera para el conteo. |
| `GET /api/catalogo/admin/perros`                              | `{ success, data: [perro + multimedia...] }`                                                  | KPI de perros, perro destacado.    |
| `GET /api/voluntarios/admin/programas/calendario?desde&hasta` | `{ success, data: { programas[], semanasSinCobertura[] } }` (rango ≤ 62 días)                 | Agenda y alerta de cobertura.      |

**Inconsistencias de backend que afectan al front**

- B1: Cuatro formas de respuesta distintas: `{ success, data }` / `{ success, message }` (Catálogo), `{ success, code, message }` (Voluntariado), `{ error, code, fields }` (Perfiles, convención de `@qarino/shared`), arreglos crudos (Donativos admin).
- B2: No hay endpoints agregados para un dashboard, ni notificaciones para administradores, ni búsqueda global.
- B3: `GET /admin/solicitudes` carga todo en memoria y pagina después; su `resumen` no separa "por revisar" de "en revisión".
- B4: `/me` no devuelve nombre; se necesitan dos llamadas para pintar el shell.
- B5: Estados como texto libre (algunos `char(n)` con espacios de relleno) normalizados en cada controlador.

---

## 2. Diseño y UX del Dashboard de Inicio

### 2.1 Propósito

> **Pregunta que responde Inicio:** "¿Qué necesita mi atención hoy y a dónde voy para resolverlo?" — en menos de 5 segundos.

Principios:

1. **Escaneable en forma de F:** saludo y contexto arriba, números accionables en la primera fila, bandeja priorizada a la izquierda (el área más leída), contexto secundario a la derecha.
2. **Todo número es un enlace.** Si un dato no lleva a ninguna acción, no merece estar en la primera fila.
3. **Resumen, no réplica:** máximo 6 elementos por lista; el "ver todo" lleva al módulo.
4. **Calma visual:** el amarillo de marca se usa para identidad (avatar, ítem activo), no para alarmas. La urgencia usa los tonos `warning`/`danger` con moderación.

### 2.2 Crítica constructiva al boceto

**Conservar**

- Saludo personalizado con fecha: humaniza el panel y orienta en el tiempo.
- Fila de KPIs en tarjetas blancas con ícono en la esquina: buena jerarquía.
- Lista de pendientes con etiqueta de módulo y pie "N tareas · Actualizado…".
- Tarjeta emocional con foto de perro: refuerza la misión.
- Contador en el ítem "Solicitudes" del sidebar: excelente patrón de notificación pasiva.
- Tarjeta "Cuidamos juntos" del sidebar.

**Mejorar**

| Elemento del boceto            | Problema                                                      | Propuesta                                                                                                          |
| ------------------------------ | ------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| KPIs                           | No son clicables y no dicen "qué tan urgente".                | Tarjeta completa clicable + línea de contexto ("La más antigua: hace 6 días") con tono `warning` si supera el SLA. |
| "Inscritos esta semana"        | No es accionable ni tiene endpoint (❌).                      | Sustituir por **Facturas (CFDI) por emitir**; el voluntariado pasa a un widget de agenda con alertas reales.       |
| "Pendientes de hoy"            | Sugiere tareas con fecha de hoy, pero son trabajo acumulado.  | Renombrar a **"Requiere tu atención"**, ordenar por antigüedad y añadir chips de filtro por módulo.                |
| Ítems de pendientes            | Todas las etiquetas iguales; sin antigüedad visible.          | Ícono por módulo (R2), meta con antigüedad relativa y punto de urgencia.                                           |
| "Completar datos fiscales de…" | Es una tarea del **cliente**, no del admin.                   | Redactar tareas del admin: "Emitir factura para Luis Torres".                                                      |
| "Confirmar lista de Paseos…"   | No existe endpoint para confirmar asistencia (❌).            | Sale de la bandeja; la agenda muestra la jornada y enlaza a Inscritos (`?id=`).                                    |
| "Una nueva oportunidad"        | Repite una solicitud que ya está en la bandeja (duplicación). | **"Lleva más tiempo esperando":** el perro disponible con más días en el refugio y sin solicitudes activas.        |
| Buscador y campana del topbar  | Sin backend: hoy son UI muerta.                               | Fase 1: buscador = **"Ir a…" (Ctrl/⌘ + K)** sobre pantallas y acciones; campana = resumen de pendientes.           |
| Sin accesos rápidos            | Las acciones más frecuentes requieren 2–3 clics.              | 2 botones en el encabezado: **Registrar perro** y **Crear jornada**.                                               |
| Sin estado del dato            | No se sabe si la información es reciente.                     | "Actualizado hace 3 min" + botón Actualizar; refresco al volver a la pestaña si pasaron > 5 min.                   |

**Quitar**

- El banner inferior "Regístrate para hacer un comentario…" es de la herramienta de diseño, no del producto.
- El contador numérico dentro del sidebar **no** debe repetir el mismo número en tres lugares a la vez (sidebar + KPI + campana) con valores distintos: los tres leen del mismo store (§5.5).

### 2.3 Distribución propuesta (wireframe)

**Escritorio ≥ 1280 px** (rejilla de 12 columnas, `gap` = `--qa-space-6`)

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│ ¡Buenas tardes, Ana!                               [+ Registrar perro] [+ Crear jornada] │
│ Este es el pulso del refugio. · Jueves 26 nov 2026 · Actualizado hace 2 min  ↻        │
├─────────────────────┬─────────────────────┬─────────────────────┬────────────────────┤
│ Solicitudes por     │ Donativos por       │ Facturas por        │ Perros en adopción │
│ atender        4  → │ validar        3  → │ emitir         2  → │              18  → │
│ 2 nuevas · 2 en rev.│ $4,500 MXN          │ 1 requiere revisión │ +3 ingresos en nov │
│ ● Más antigua: 6 d  │ ● Más antiguo: 2 d  │                     │                    │
├─────────────────────┴─────────────────────┴───────┬─────────────┴────────────────────┤
│ Requiere tu atención                 Ver todo →   │ Próximas jornadas  Ver calendario →│
│ [Todo 9] [Adopciones 4] [Donativos 3] [Facturas 2]│ ┌──┐ Paseos con cariño             │
│ ───────────────────────────────────────────────── │ │28│ Sáb 9:00 · Parque Alameda     │
│ (ícono) Revisar solicitud de Sofía García    ›    │ └──┘ ▓▓▓▓▓▓░░ 8/12  Disponible     │
│         Bruno · QA-1A2B3C4D · hace 6 días  ●      │ ┌──┐ Baño y cepillado ...          │
│ (ícono) Validar donativo de Mariana Ruiz     ›    │ ⚠ La semana del 7 dic no tiene    │
│         $1,500 MXN · hace 2 días                  │   jornadas.   [Crear jornada]      │
│ ... (máx. 6)                                      ├────────────────────────────────────┤
│ ───────────────────────────────────────────────── │ Lleva más tiempo esperando         │
│ 9 pendientes · mostrando 6 más antiguos           │ [foto] Canela · 214 días           │
│                                                   │ 4 años · Hembra · Mediana          │
│                                                   │ Sin solicitudes activas [Ver ficha]│
└───────────────────────────────────────────────────┴────────────────────────────────────┘
          8 columnas                                              4 columnas
```

**Adaptación:** cada widget usa _container queries_ (se adapta al ancho de su columna, no de la ventana).

| Ancho del área de contenido | KPIs                  | Cuerpo                                          |
| --------------------------- | --------------------- | ----------------------------------------------- |
| ≥ 1280 px                   | 4 en fila             | 8 / 4 columnas                                  |
| 1024–1279 px                | 4 en fila (compactos) | 7 / 5 columnas                                  |
| 640–1023 px                 | 2 × 2                 | Una columna: Bandeja → Agenda → Perro destacado |
| < 640 px (no prioritario)   | 1 columna             | Una columna; acciones rápidas en menú           |

### 2.4 Widgets y acciones

Cada widget declara: **problema que resuelve · acción que dispara · cómo evita duplicar**.

#### W0 — Encabezado de bienvenida (organismo `qa-page-header`, variante `greeting`)

- **Contenido:** saludo según la hora en `America/Mexico_City` (buenos días < 12 h, buenas tardes < 19 h, buenas noches), nombre de pila, subtítulo de misión, fecha larga en es-MX, sello "Actualizado hace…", botón Actualizar.
- **Acciones rápidas:** `Registrar perro` → `/admin/perros?accion=registrar` · `Crear jornada` → `/admin/voluntariado/nueva`.
- **Problema:** orientar y acortar las dos tareas de alta más frecuentes.
- **Anti-duplicación:** solo enlaza; los formularios siguen en sus módulos.

#### W1 — Indicadores (organismo `qa-kpi-grid` con 4 `qa-kpi-card`)

| KPI                     | Valor                                | Línea de contexto                                    | Tono                               | Destino                                |
| ----------------------- | ------------------------------------ | ---------------------------------------------------- | ---------------------------------- | -------------------------------------- |
| Solicitudes por atender | por revisar + en revisión            | "2 nuevas · 2 en revisión" + "Más antigua: hace 6 d" | `warning` si la más antigua > SLA  | `/admin/solicitudes?estado=pendientes` |
| Donativos por validar   | conteo                               | Monto total por confirmar (MXN) + antigüedad         | `warning` si > SLA                 | `/admin/donativos`                     |
| Facturas por emitir     | solicitudes CFDI pendientes          | "1 requiere revisión" / "1 correo con error"         | `danger` si hay correo con error   | `/admin/donativos/solicitudes-cfdi`    |
| Perros en adopción      | perros activos con estado Disponible | "+3 ingresos este mes"                               | `neutral` (contexto, no pendiente) | `/admin/perros?estado=disponible`      |

- **Problema:** saber el volumen de trabajo por área de un vistazo.
- **Acción:** toda la tarjeta es un enlace (`<a>`), con flecha que aparece al pasar el cursor.
- **Anti-duplicación:** no hay gráficos ni desglose; el detalle vive en el módulo.
- Los umbrales de SLA viven en `jsInicio/domain/reglas/umbrales.js` (D7: solicitudes 3 días, donativos 2, facturas 3).

#### W2 — Bandeja "Requiere tu atención" (organismo `qa-pending-inbox`)

- **Contenido:** lista unificada de pendientes de Adopciones, Donativos y Facturas; máximo 6; chips de filtro con conteo (`Todo`, `Adopciones`, `Donativos`, `Facturas`); pie con total y hora.
- **Cada ítem (`qa-task-item`):** ícono del módulo, título con verbo ("Revisar solicitud de…", "Validar donativo de…", "Emitir factura para…", "Reintentar correo de factura de…"), meta (perro / folio / monto · antigüedad relativa), punto de urgencia, chevron.
- **Acción:** navega al módulo con el elemento abierto (deep link, ver §2.6).
- **Problema:** decidir qué atender primero sin abrir 3 pantallas.
- **Anti-duplicación:** no hay botones de aprobar/rechazar; filtrar es local (no consulta).

**Reglas de prioridad** (dominio puro, `domain/reglas/prioridad.js`, con pruebas):

1. `danger` primero: correo de CFDI con error, CFDI que requiere revisión.
2. Después, por antigüedad descendente (lo más viejo arriba).
3. Empate: Adopciones > Donativos > Facturas (una adopción involucra a un perro esperando).
4. Pesos y umbrales configurables, nunca escritos dentro de la vista.

#### W3 — Próximas jornadas (organismo `qa-volunteer-agenda`)

- **Contenido:** próximas 3 jornadas (hoy a +28 días): bloque de fecha, nombre, tipo, hora, ubicación, barra de cupo `inscritos/cupo`, badge de estado (Disponible, Lleno). Alerta si hay **semanas sin cobertura**.
- **Acciones:** clic en jornada → `/admin/voluntariado/inscritos?id=<idPrograma>` (✅ ya soportado). Alerta → `/admin/voluntariado/nueva?fecha=<lunes>` (✅ ya soportado). "Ver calendario" → `/admin/voluntariado/calendario`.
- **Problema:** anticipar huecos de voluntariado y jornadas llenas o vacías.
- **Anti-duplicación:** no es un calendario; es una lista corta de lo inmediato.

#### W4 — "Lleva más tiempo esperando" (organismo `qa-spotlight-dog`)

- **Contenido:** foto, nombre, días en el refugio, edad · sexo · tamaño, "Sin solicitudes activas".
- **Acción:** `Ver ficha` → `/admin/perros?abrir=<idPerro>`.
- **Problema:** dar visibilidad a los perros que el sistema tiende a olvidar (motivación + acción de difusión).
- **Anti-duplicación:** un solo perro; no es una galería.
- Si no hay candidato: ocultar el widget (no mostrar estado vacío; no aporta).

#### W5 — Shell (topbar y sidebar)

- **Topbar — usuario:** nombre real + rol desde `/me` + `/profile` (o `/me` extendido, §6). Avatar con iniciales.
- **Topbar — "Ir a…" (fase 1):** abre una paleta (Ctrl/⌘ + K o clic en el campo) que busca en `PANTALLAS` de `rutas.js` y en las acciones rápidas. Sin backend. Fase 2: búsqueda global de perros/solicitudes/donantes (❌, contrato en §6).
- **Topbar — campana (fase 1):** popover con los mismos conteos de pendientes por módulo y enlaces. Punto indicador si total > 0. 🟡 D4.
- **Sidebar:** contador junto a Solicitudes y Donativos (suma de donativos + facturas), leído del mismo store de pendientes, con caché corta en `sessionStorage` para no consultar en cada página. 🟡 D5.

### 2.5 Mapa de navegación

| Origen                       | Destino                             | Parámetro                              | ¿El destino lo soporta hoy?                   |
| ---------------------------- | ----------------------------------- | -------------------------------------- | --------------------------------------------- |
| KPI Solicitudes              | `/admin/solicitudes`                | `?estado=pendientes`                   | ❌ Leer el query al iniciar (cambio pequeño). |
| Ítem de solicitud            | `/admin/solicitudes`                | `?abrir=<uuid>`                        | ❌ Abrir `abrirDetalle(id)` al iniciar.       |
| KPI / ítem Donativos         | `/admin/donativos`                  | `?abrir=<uuid>`                        | ❌ Resaltar fila.                             |
| KPI / ítem Facturas          | `/admin/donativos/solicitudes-cfdi` | `?abrir=<uuid>`                        | ❌ Abrir detalle.                             |
| KPI Perros / Perro destacado | `/admin/perros`                     | `?estado=disponible` / `?abrir=<uuid>` | ❌                                            |
| Acción Registrar perro       | `/admin/perros`                     | `?accion=registrar`                    | ❌ Abrir modal de registro.                   |
| Acción Crear jornada         | `/admin/voluntariado/nueva`         | —                                      | ✅                                            |
| Jornada de la agenda         | `/admin/voluntariado/inscritos`     | `?id=<uuid>`                           | ✅                                            |
| Alerta sin cobertura         | `/admin/voluntariado/nueva`         | `?fecha=AAAA-MM-DD`                    | ✅                                            |
| Ver calendario               | `/admin/voluntariado/calendario`    | —                                      | ✅                                            |

Los enlaces se construyen con un helper único `enlaceA(id, params)` en `JS/rutas.js` (🟡 D6). Mientras el destino no soporte el parámetro, el enlace igual funciona (cae en la lista del módulo).

### 2.6 Estados de cada widget

| Widget           | Cargando (skeleton con la forma final)               | Vacío                                                                    | Error (en línea, con Reintentar)                       |
| ---------------- | ---------------------------------------------------- | ------------------------------------------------------------------------ | ------------------------------------------------------ |
| Encabezado       | Saludo sin nombre ("¡Buenas tardes!"), nunca bloquea | —                                                                        | Saludo sin nombre; no se muestra error.                |
| KPI              | Rectángulo para número y línea de meta               | Valor `0` con meta positiva ("Todo al día")                              | Valor "—", meta "No se pudo cargar", ícono reintentar. |
| Bandeja          | 4 filas skeleton                                     | Ilustración (ícono `pets`) + "Todo al día. No hay pendientes por ahora." | Tarjeta `qa-error-state` + Reintentar.                 |
| Bandeja filtrada | —                                                    | "No hay pendientes de Donativos." + chip "Ver todo"                      | —                                                      |
| Agenda           | 3 filas skeleton                                     | "No hay jornadas en las próximas 4 semanas." + `Crear jornada`           | `qa-error-state` compacto + Reintentar.                |
| Perro destacado  | Imagen + 2 líneas skeleton                           | Widget oculto                                                            | Widget oculto (es contexto, no crítico).               |

**Estados globales**

- **401:** redirigir a `rutaDe("login")` (una sola vez, desde el cliente HTTP).
- **403:** página completa "Tu cuenta no tiene acceso al panel" con botón Cerrar sesión.
- **Sin conexión:** banner `qa-inline-alert` tono `warning` sobre el contenido; se conservan los últimos datos.
- **Datos parciales:** cada widget muestra su propio error; el encabezado dice "Algunos datos no se pudieron actualizar".
- **Refresco:** el botón Actualizar no vacía la pantalla; muestra un indicador de progreso en el botón y reemplaza los datos al terminar (sin skeleton).
- **Accesibilidad:** contenedor de cada widget con `aria-busy` durante carga; región `aria-live="polite"` anuncia "Panel actualizado".

### 2.7 Micro-interacciones y motion

- Tarjetas clicables: elevación `1 → 2` y flecha que entra 4 px en `--qa-duration-fast`.
- Conteo animado del valor del KPI en la primera carga (máx. 400 ms). Desactivado con `prefers-reduced-motion`.
- Skeleton con brillo suave; con reduced-motion, sin animación (solo color plano).
- Chips de la bandeja: cambio instantáneo con transición de opacidad de la lista (120 ms).
- Atajos: `Ctrl/⌘ + K` abre "Ir a…", `R` (con foco en el body) actualiza. 🟡 D8.
- Foco visible siempre con `--qa-focus-ring`; nunca `outline: none` sin reemplazo.

### 2.8 Tono y redacción

- Segunda persona, cálido y breve: "Revisa", "Valida", "Emite". Evitar "Usuario", usar el nombre.
- Verbos al inicio de cada tarea; antigüedad relativa ("hace 2 días"), fechas absolutas solo en tooltips (`title`) o en la agenda.
- Moneda: `Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" })`.
- Fechas: `Intl.DateTimeFormat("es-MX", { timeZone: "America/Mexico_City" })`.
- Mensajes de error: qué pasó + qué hacer ("No pudimos cargar los donativos. Reintenta en unos segundos.").

### 2.9 Accesibilidad (mínimos)

- Estructura: `<main>` → `<h1>` saludo → cada widget es `<section aria-labelledby>` con su `<h2>`.
- KPIs: `<a>` con nombre accesible completo ("Solicitudes por atender: 4. La más antigua hace 6 días").
- Bandeja: `<ul>` de `<li><a>`; chips como `role="radiogroup"` o botones con `aria-pressed`.
- Contraste AA (4.5:1 texto normal). Nunca texto amarillo sobre blanco.
- Objetivos táctiles ≥ 40 × 40 px.

---

## 3. Sistema visual "a prueba de cambios"

### 3.1 Arquitectura de tokens en 3 capas

```
Primitivos (--qa-ref-*)        Paleta y escalas crudas. NUNCA se usan en componentes.
        │  #2f6fb5, 16px, 200ms…
        ▼
Semánticos (--qa-color-*, --qa-tone-*, --qa-space-*, --qa-text-*…)
        │  Significado: "texto secundario", "fondo de éxito", "espacio m".
        ▼
De componente (--qa-card-*, --qa-btn-*, --qa-kpi-*…)
           Decisiones por componente, apuntan a semánticos.
```

- **Cambiar la marca** = editar primitivos (1 archivo).
- **Cambiar el significado** (ej. "el acento ahora es verde") = editar semánticos.
- **Cambiar un componente** (ej. radio de tarjetas) = editar su token de componente, sin tocar su CSS.
- Los componentes **solo** leen tokens semánticos o de componente.

### 3.2 Archivos y orden de cascada

Un único punto de entrada de estilos para todo el panel, con `@layer` para que la precedencia no dependa del orden de los `<link>`:

```css
/* CSS/admin.css */
@layer reset, tokens, base, atoms, molecules, organisms, templates, pages, utilities;

@import "./foundations/reset.css" layer(reset);
@import "./foundations/tokens.primitives.css" layer(tokens);
@import "./foundations/tokens.semantic.css" layer(tokens);
@import "./foundations/tokens.components.css" layer(tokens);
@import "./foundations/tokens.legacy-alias.css" layer(tokens);
@import "./foundations/typography.css" layer(base);
/* … átomos, moléculas, organismos, templates … */
@import "./foundations/utilities.css" layer(utilities);
```

- Las hojas de página (`cssInicio/adminInicio.css`) van en `@layer pages` y **solo** posicionan; no redefinen átomos.
- `tokens.legacy-alias.css` mapea los nombres antiguos (`--md-sys-color-*`) a los nuevos para que los módulos existentes no se rompan durante la migración. Se elimina cuando el último módulo migre.

### 3.3 Catálogo de tokens (valores iniciales)

Los valores parten de la paleta actual para no romper la identidad. 🟡 D1 confirma la paleta final.

**Primitivos de color**

| Token                  | Valor     | Origen actual                       |
| ---------------------- | --------- | ----------------------------------- |
| `--qa-ref-yellow-500`  | `#fdd254` | primary                             |
| `--qa-ref-yellow-100`  | `#ffedad` | primary-container                   |
| `--qa-ref-yellow-900`  | `#3d2e00` | nuevo (texto sobre amarillo)        |
| `--qa-ref-blue-700`    | `#245a94` | nuevo (texto info con contraste AA) |
| `--qa-ref-blue-600`    | `#2f6fb5` | secondary                           |
| `--qa-ref-blue-100`    | `#d8e8ff` | secondary-container                 |
| `--qa-ref-neutral-0`   | `#ffffff` | blanco en tarjetas                  |
| `--qa-ref-neutral-50`  | `#f8f2e6` | surface-container-low (fondo crema) |
| `--qa-ref-neutral-100` | `#f1ebdd` | surface-container                   |
| `--qa-ref-neutral-300` | `#d2cabb` | outline-variant                     |
| `--qa-ref-neutral-600` | `#696456` | on-surface-variant                  |
| `--qa-ref-neutral-800` | `#3a3a3a` | on-surface                          |
| `--qa-ref-green-100`   | `#e4f3e8` | unifica los verdes de éxito         |
| `--qa-ref-green-700`   | `#26734d` | unifica los verdes de éxito         |
| `--qa-ref-amber-100`   | `#fff1cc` | nuevo (advertencia)                 |
| `--qa-ref-amber-800`   | `#7a5200` | nuevo (advertencia)                 |
| `--qa-ref-red-100`     | `#fce8e6` | unifica los rojos de error          |
| `--qa-ref-red-700`     | `#ba1a1a` | error                               |

**Semánticos de color**

| Token                      | Apunta a                       | Uso                                         |
| -------------------------- | ------------------------------ | ------------------------------------------- |
| `--qa-color-bg`            | neutral-50                     | Fondo de la app                             |
| `--qa-color-surface`       | neutral-0                      | Tarjetas, sidebar, topbar                   |
| `--qa-color-surface-muted` | neutral-100                    | Encabezados de tabla, chips neutros, hover  |
| `--qa-color-border`        | neutral-300                    | Bordes y divisores                          |
| `--qa-color-text`          | neutral-800                    | Texto principal                             |
| `--qa-color-text-muted`    | neutral-600                    | Texto secundario, metadatos                 |
| `--qa-color-brand`         | yellow-500                     | Identidad (avatar, acentos de marca)        |
| `--qa-color-brand-soft`    | yellow-100                     | Ítem activo del sidebar, tarjeta "Cuidamos" |
| `--qa-color-on-brand`      | yellow-900                     | Texto sobre amarillo                        |
| `--qa-color-accent`        | blue-600                       | Acción primaria, enlaces                    |
| `--qa-color-accent-soft`   | blue-100                       | Fondos de selección                         |
| `--qa-color-on-accent`     | neutral-0                      | Texto sobre azul                            |
| `--qa-focus-ring`          | `0 0 0 3px` + blue-600 al 40 % | Foco visible                                |

**Tonos de estado** (cada uno define `-bg`, `-fg`, `-border`)

| Tono      | bg          | fg          | Uso                                                  |
| --------- | ----------- | ----------- | ---------------------------------------------------- |
| `neutral` | neutral-100 | neutral-600 | Finalizado, inactivo, etiquetas de módulo            |
| `info`    | blue-100    | blue-700    | En revisión, Lleno, información                      |
| `success` | green-100   | green-700   | Aprobada, Validado, Disponible                       |
| `warning` | amber-100   | amber-800   | Pendiente con antigüedad > SLA, semana sin cobertura |
| `danger`  | red-100     | red-700     | Rechazada, Cancelado, correo con error               |

El mapeo **estado de dominio → tono** vive en `inicio.presentar.js`, no en CSS. No hay `JS/ui/tonos.js`:

```js
// Ejemplo de contrato (documentación)
export const TONO_POR_ESTADO = {
  solicitud: {
    por_revisar: "info",
    en_revision: "info",
    periodo_prueba: "info",
    aprobada: "success",
    rechazada: "danger",
  },
  donativo: { "Pendiente de validación": "warning", Validado: "success" },
  programa: { Disponible: "success", Lleno: "info", Finalizado: "neutral", Cancelado: "danger" },
};
```

**Tipografía**

| Token                   | Familia / peso             | Tamaño / interlínea | Uso                       |
| ----------------------- | -------------------------- | ------------------- | ------------------------- |
| `--qa-text-display`     | Nunito 800                 | 28 / 34 px          | Saludo, títulos de página |
| `--qa-text-headline`    | Nunito 800                 | 22 / 28 px          | Título del topbar         |
| `--qa-text-numeral`     | Nunito 800, `tabular-nums` | 32 / 36 px          | Valor de KPI              |
| `--qa-text-title`       | Nunito 700                 | 16 / 22 px          | Título de tarjeta/widget  |
| `--qa-text-body`        | Roboto 400                 | 14 / 20 px          | Texto general             |
| `--qa-text-body-strong` | Roboto 500                 | 14 / 20 px          | Títulos de ítems          |
| `--qa-text-label`       | Roboto 500                 | 12 / 16 px          | Etiquetas, badges, metas  |
| `--qa-text-caption`     | Roboto 400                 | 12 / 16 px          | Pies, sellos de hora      |

Se define cada estilo como shorthand (`font: 800 1.75rem/2.125rem var(--qa-font-display)`) y se expone una clase utilitaria por estilo (`.qa-text-display`, …). Se cargan Nunito 700/800 y Roboto 400/500/700.

**Espaciado (base 4 px)**: `--qa-space-1` 4 · `-2` 8 · `-3` 12 · `-4` 16 · `-5` 20 · `-6` 24 · `-8` 32 · `-10` 40 · `-12` 48.

**Radios**: `--qa-radius-sm` 8 · `-md` 12 · `-lg` 16 · `-xl` 20 · `-pill` 999 px.

**Elevación**

| Token              | Valor                                                        | Uso                        |
| ------------------ | ------------------------------------------------------------ | -------------------------- |
| `--qa-elevation-0` | `none` (la tarjeta se define con borde)                      | Tarjetas en reposo         |
| `--qa-elevation-1` | `0 1px 2px rgb(58 58 58 / 6%), 0 1px 3px rgb(58 58 58 / 4%)` | Tarjeta clicable en reposo |
| `--qa-elevation-2` | `0 4px 12px rgb(58 58 58 / 8%)`                              | Hover, popovers            |
| `--qa-elevation-3` | `0 10px 30px rgb(0 0 0 / 15%)`                               | Modales (valor actual)     |

**Motion**: `--qa-duration-fast` 120 ms · `-base` 200 ms · `-slow` 320 ms · `--qa-ease-standard` `cubic-bezier(.2,0,0,1)`. Con `prefers-reduced-motion: reduce` todas las duraciones pasan a `0ms` desde el archivo de tokens.

**Capas (z-index)**: `--qa-z-sticky` 10 · `--qa-z-dropdown` 100 · `--qa-z-overlay` 1000 · `--qa-z-toast` 1100.

**Layout (componente)**: `--qa-sidebar-width` 240 px · `--qa-topbar-height` 76 px · `--qa-content-padding` `32px 40px` · `--qa-content-max-width` 1440 px · `--qa-grid-gap` `var(--qa-space-6)`.

**Breakpoints** (constantes documentadas; no pueden ser variables CSS en media queries): `sm` 640 · `md` 1024 · `lg` 1280 · `xl` 1536. Para widgets se prefieren `@container` sobre `@media`.

**Tokens de componente (ejemplos)**

```css
/* tokens.components.css — documentación del patrón */
:root {
  --qa-card-bg: var(--qa-color-surface);
  --qa-card-border: 1px solid var(--qa-color-border);
  --qa-card-radius: var(--qa-radius-lg);
  --qa-card-padding: var(--qa-space-6);
  --qa-card-shadow: var(--qa-elevation-0);

  --qa-btn-radius: var(--qa-radius-pill);
  --qa-btn-height-md: 40px;
  --qa-btn-height-sm: 32px;

  --qa-badge-radius: var(--qa-radius-pill);
  --qa-kpi-value-font: var(--qa-text-numeral);
}
```

### 3.4 Convenciones CSS

- Prefijo `qa-` + BEM: `qa-kpi-card`, `qa-kpi-card__value`, `qa-kpi-card--warning`.
- Estados con atributos ARIA o `data-*` cuando existan (`[aria-pressed="true"]`, `[data-estado="cargando"]`), no con clases inventadas.
- Un archivo por componente, nombre igual al componente (`kpi-card.css`).
- Prohibido en componentes: hex, `rgb()` literal, `px` fuera de bordes de 1 px, `!important` (salvo `[hidden]` en reset), selectores por ID.
- Las hojas de página solo usan `grid-area`, `gap` y márgenes entre organismos.

### 3.5 "Cambiar el estándar en minutos" (ejemplos)

| Cambio pedido por el equipo                 | Dónde se edita                                               | Líneas |
| ------------------------------------------- | ------------------------------------------------------------ | ------ |
| Nuevo azul de acento                        | `--qa-ref-blue-600` (y `-700`, `-100`)                       | 1–3    |
| Tarjetas con sombra en lugar de borde       | `--qa-card-border`, `--qa-card-shadow`                       | 2      |
| Esquinas más cuadradas en todo el panel     | `--qa-radius-*`                                              | 5      |
| Panel más denso                             | `--qa-card-padding`, `--qa-content-padding`, `--qa-grid-gap` | 3      |
| "Pendiente" debe verse azul en vez de ámbar | el tono que asigna `inicio.presentar.js`                     | 1      |
| Cambiar la tipografía de títulos            | `--qa-font-display`                                          | 1      |

### 3.6 Guardas automáticas

- **Prueba "sin valores crudos":** un test de Vitest recorre `CSS/atomicDesign/**` y `CSS/css*/` y falla si encuentra hex/`rgb(` fuera de `foundations/`. Arranca en modo "solo componentes nuevos" (lista de exclusión de módulos legados que se va vaciando).
- **Prueba de tokens definidos:** falla si un `var(--qa-…)` usado no está declarado en `foundations/`.
- **Catálogo vivo** (`HTML/htmlDesignSystem/catalogo.html`, solo en dev): muestra cada átomo/molécula en todos sus estados; es el lugar de revisión visual en la sesión de estandarización.

### 3.7 Íconos y fuentes

- Una sola familia: **Material Symbols Rounded** (eje `FILL` 0 por defecto, 1 en ítem activo). Se retira Material Icons Round.
- Átomo `qa-icon` con tamaños `sm` 18 / `md` 20 / `lg` 24.
- Íconos por módulo (constante única, reutilizada por sidebar, bandeja y paleta "Ir a…"): Inicio `grid_view`, Perros `pets`, Solicitudes `assignment`, Voluntariado `volunteer_activism`, Donativos `favorite`, Facturas `receipt_long`, Configuración `settings`. **Fuente:** el campo `icono` de `PANTALLAS` en `rutas.js` (+ `receipt_long` para Facturas).
- Carga de fuentes: un solo `<link>` compartido. 🟡 D2 decide entre Google Fonts o fuentes empaquetadas (`@fontsource`).

---

## 4. Inventario Atomic Design

Lista de referencia por si más adelante se unifica el panel. **En este proyecto no se implementa.** Inicio solo crea, en `CSS/cssInicio/`, las clases que sus widgets necesiten (§0.3, §5.6).

### 4.1 Átomos

| Componente       | Variantes / props                                                                        | Etiqueta         | Origen                                                        |
| ---------------- | ---------------------------------------------------------------------------------------- | ---------------- | ------------------------------------------------------------- |
| Tokens           | 3 capas + alias legado                                                                   | `[REFACTORIZAR]` | `atoms/variables.css`                                         |
| `qa-icon`        | `name`, `size`, `filled`                                                                 | `[CREAR NUEVO]`  | Unifica `.material-symbols-rounded` sueltos                   |
| `qa-button`      | `primary` · `secondary` (outline) · `text` · `danger`; `sm` · `md`; con ícono; `loading` | `[REFACTORIZAR]` | 8 variantes actuales (V5)                                     |
| `qa-icon-button` | `aria-label` obligatorio; `badge` opcional                                               | `[REFACTORIZAR]` | `.icon-btn`                                                   |
| `qa-link`        | `default`, `arrow` ("Ver todo →")                                                        | `[CREAR NUEVO]`  | —                                                             |
| `qa-badge`       | tonos `neutral/info/success/warning/danger`; `dot` opcional                              | `[REFACTORIZAR]` | `.badge-estado` ×5                                            |
| `qa-count-badge` | número en burbuja; `max` (99+)                                                           | `[CREAR NUEVO]`  | Contador del sidebar                                          |
| `qa-chip`        | seleccionable, con conteo                                                                | `[REFACTORIZAR]` | `.status-chip`, `.badge-item`                                 |
| `qa-avatar`      | iniciales / imagen; `sm` · `md`                                                          | `[REFACTORIZAR]` | `.user-avatar`                                                |
| `qa-dog-photo`   | `src`, `alt`, fallback, `size`                                                           | `[REFACTORIZAR]` | Lógica duplicada en Perros/Solicitudes                        |
| `qa-skeleton`    | `text`, `circle`, `rect`; `lines`                                                        | `[CREAR NUEVO]`  | —                                                             |
| `qa-progress`    | `value`, `max`, tono                                                                     | `[REFACTORIZAR]` | `.progress-bar`                                               |
| `qa-urgency-dot` | `warning`, `danger`                                                                      | `[CREAR NUEVO]`  | —                                                             |
| `qa-card`        | superficie base; `interactive`                                                           | `[CREAR NUEVO]`  | Consolida `.table-card`, `.solicitudes-card`, `.gestion-card` |
| `qa-input`       | base de campo                                                                            | `[REUTILIZAR]`   | `.input-base` (migrar a tokens)                               |
| `qa-divider`     | horizontal                                                                               | `[CREAR NUEVO]`  | —                                                             |
| Tipografía       | clases `.qa-text-*`                                                                      | `[CREAR NUEVO]`  | —                                                             |

### 4.2 Moléculas

| Componente          | Compuesto por                                                   | Etiqueta         | Notas                                              |
| ------------------- | --------------------------------------------------------------- | ---------------- | -------------------------------------------------- |
| `qa-kpi-card`       | card(interactive) + icon + texto + numeral + meta + urgency-dot | `[CREAR NUEVO]`  | Toda la tarjeta es `<a>`.                          |
| `qa-task-item`      | icon (módulo) + título + meta + urgency-dot + chevron           | `[CREAR NUEVO]`  | `<li><a>`.                                         |
| `qa-event-item`     | date-block + título + meta + progress + badge                   | `[CREAR NUEVO]`  | Jornada de la agenda.                              |
| `qa-date-block`     | día + mes abreviado                                             | `[CREAR NUEVO]`  | Reutilizable en Calendario.                        |
| `qa-section-header` | título de widget + `qa-link` opcional                           | `[CREAR NUEVO]`  | Encabezado de cada widget.                         |
| `qa-chip-group`     | chips con selección única                                       | `[REFACTORIZAR]` | `.status-chips` de Solicitudes.                    |
| `qa-empty-state`    | icon + título + texto + acción opcional                         | `[REFACTORIZAR]` | `.empty-cell`.                                     |
| `qa-error-state`    | icon + mensaje + botón Reintentar; `compact`                    | `[REFACTORIZAR]` | `.estado-error`.                                   |
| `qa-inline-alert`   | tono + icon + texto + acción                                    | `[REFACTORIZAR]` | `.status-message`, avisos de Inscritos/Calendario. |
| `qa-search-field`   | icon + input + atajo (`Ctrl K`)                                 | `[REFACTORIZAR]` | `.global-search`.                                  |
| `qa-nav-item`       | icon + etiqueta + count-badge                                   | `[REFACTORIZAR]` | `.nav-item`.                                       |
| `qa-user-chip`      | avatar + nombre + rol                                           | `[REFACTORIZAR]` | `.user-profile`.                                   |
| `qa-freshness`      | "Actualizado hace X" + botón Actualizar                         | `[CREAR NUEVO]`  | Encabezado de Inicio; reutilizable en módulos.     |

### 4.3 Organismos

| Componente                 | Etiqueta         | Notas                                                                                |
| -------------------------- | ---------------- | ------------------------------------------------------------------------------------ |
| `<admin-sidebar>`          | `[REFACTORIZAR]` | Contadores desde el store de pendientes; logo desde `/logo.png`.                     |
| `<admin-topbar>`           | `[REFACTORIZAR]` | Usuario real; "Ir a…"; campana con resumen.                                          |
| `qa-page-header`           | `[CREAR NUEVO]`  | Variantes `default` (módulos) y `greeting` (Inicio). Reemplaza `.section-header` ×5. |
| `qa-kpi-grid`              | `[CREAR NUEVO]`  | 4 KPIs, container queries.                                                           |
| `qa-pending-inbox`         | `[CREAR NUEVO]`  | Card + section-header + chip-group + lista + pie + estados.                          |
| `qa-volunteer-agenda`      | `[CREAR NUEVO]`  | Card + lista de event-item + inline-alert + estados.                                 |
| `qa-spotlight-dog`         | `[CREAR NUEVO]`  | Card con foto destacada.                                                             |
| `qa-command-palette`       | `[CREAR NUEVO]`  | Diálogo "Ir a…" (`<dialog>`), navegación por teclado.                                |
| `qa-notifications-popover` | `[CREAR NUEVO]`  | Resumen de pendientes en la campana (🟡 D4).                                         |
| Modales globales           | `[REFACTORIZAR]` | Fuera del alcance de Inicio; se documentan para la migración.                        |

### 4.4 Templates y páginas

| Pieza                     | Etiqueta         | Notas                                                                                      |
| ------------------------- | ---------------- | ------------------------------------------------------------------------------------------ |
| `admin-shell` (template)  | `[REFACTORIZAR]` | Extraído de `layoutAdmin.css`: sidebar + topbar + área de contenido con padding por token. |
| `dashboard` (template)    | `[CREAR NUEVO]`  | `grid-template-areas`: `header` / `kpis` / `inbox agenda` / `inbox spotlight`.             |
| `adminInicio.html` (page) | `[CREAR NUEVO]`  | Ruta ya registrada en `rutas.js`; añadir a `rollupOptions.input`.                          |
| `catalogo.html` (dev)     | `[CREAR NUEVO]`  | Catálogo vivo del sistema de diseño.                                                       |

---

## 5. Arquitectura de Inicio

Inicio separa datos, estado y pintado sin abrir un sistema de diseño para todo el panel. El papel de un ViewModel existe dentro de `crearInicio`; no hay `inicio.viewmodel.js`.

### 5.1 Capas y regla de dependencias

```
inicio.view.js          pinta el estado. No hace fetch ni formatea.
        │ llama intenciones, lee estado()
        ▼
inicio.js               crearInicio: carga, error por widget, filtro, reintento.
        │               elegirFuenteInicio: mock hoy; compuesto o api después.
        ▼
inicio.presentar.js     textos, tono y href (COPY).
        │
domain/reglas           prioridad y antigüedad. No importa data/ ni document.
        ▲
data/                   fixtures → mappers → InicioRepositoryMock
```

Reglas:

- `domain/` no importa `data/`, `fetch` ni `document`.
- `data/` importa `domain/` (entidades y contrato).
- `inicio.presentar.js` no importa `data/`.
- Solo `inicio.js` elige el repositorio concreto.
- La vista, cuando exista, importa `crearInicio` y no el mock.

### 5.2 Árbol que usa Inicio

Misma convención que el resto del web (`HTML/htmlX`, `CSS/…`, `JS/jsX`, pruebas `*.test.js`). Sin `JS/core` ni `JS/ui`.

```
apps/web/
├─ HTML/htmlInicio/adminInicio.html
├─ CSS/
│  ├─ foundations/tokens.inicio.css     solo lo carga adminInicio.html
│  └─ cssInicio/                        clases qa-* de esta página, cuando se pinte
├─ components/                          shell ya existente
└─ JS/
   ├─ rutas.js                          rutaDe + enlaceA
   └─ jsInicio/
      ├─ inicio.js                      estado, intenciones, fuente de datos
      ├─ inicio.presentar.js            textos, tono, href
      ├─ inicio.test.js
      ├─ inicio.view.js                 aún no existe
      ├─ domain/
      │  ├─ entities/                   typedefs
      │  ├─ reglas/                     prioridad.js, antiguedad.js, umbrales.js
      │  └─ repositories/InicioRepository.js
      └─ data/
         ├─ mappers/
         ├─ fixtures/                   ok, vacio, parcial, error, lento, atrasado
         └─ repositories/               InicioRepositoryMock.js + contract.js
```

`InicioRepositoryCompuesto` e `InicioRepositoryApi` se agregan en `data/repositories/` cuando hagan falta. No se anticipan casos de uso de una línea ni un store global de conteos: los conteos del sidebar salen de `estado.conteos`.

### 5.3 Contratos de dominio (JSDoc)

El repo es JavaScript con lint type-aware; los contratos se escriben como `@typedef` y se validan con pruebas.

```js
/** @typedef {"adopcion" | "donativo" | "factura" | "correo_factura"} TipoTarea */
/** @typedef {"normal" | "atrasada" | "critica"} Urgencia */

/**
 * @typedef {Object} TareaPendiente
 * @property {string} id                 UUID del elemento de origen
 * @property {TipoTarea} tipo
 * @property {{ nombre: string }} persona
 * @property {{ id: string, nombre: string } | null} perro
 * @property {string | null} folio       "QA-1A2B3C4D"
 * @property {number | null} monto       MXN
 * @property {Date} creadaEn
 * @property {Urgencia} urgencia         calculada por reglas/prioridad.js
 */

/**
 * @typedef {Object} ResumenInicio
 * @property {{ porRevisar: number, enRevision: number, masAntiguaDesde: Date | null }} solicitudes
 * @property {{ porValidar: number, montoPorValidar: number, masAntiguoDesde: Date | null }} donativos
 * @property {{ pendientes: number, requierenRevision: number, correosConError: number }} facturas
 * @property {{ disponibles: number, ingresosDelMes: number }} perros
 * @property {Date} generadoEn
 */

/**
 * Contrato que cumplen Mock, Compuesto y Api.
 * @typedef {Object} InicioRepository
 * @property {(opts?: { signal?: AbortSignal }) => Promise<ResumenInicio>} obtenerResumen
 * @property {(opts?: { limite?: number, signal?: AbortSignal }) => Promise<TareaPendiente[]>} obtenerPendientes
 * @property {(opts: { desde: string, hasta: string, signal?: AbortSignal }) => Promise<Agenda>} obtenerAgenda
 * @property {(opts?: { signal?: AbortSignal }) => Promise<PerroDestacado | null>} obtenerPerroDestacado
 */
```

### 5.4 Fuentes de datos intercambiables

| Implementación              | Cuándo                                        | Cómo obtiene los datos                                                                                                                               |
| --------------------------- | --------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| `InicioRepositoryMock`      | Desde el día 1, pruebas y demos               | Fixtures con DTOs **idénticos al contrato** de §6, pasan por los mismos mappers. Escenarios: `ok`, `vacio`, `parcial`, `error`, `lento`, `atrasado`. |
| `InicioRepositoryCompuesto` | En cuanto se quiera ver datos reales          | Combina endpoints existentes (§6) con caché por carga para no repetir peticiones.                                                                    |
| `InicioRepositoryApi`       | Cuando backend publique `/api/inicio/admin/*` | Una petición por widget al endpoint agregado.                                                                                                        |

La elige `elegirFuenteInicio` en `inicio.js`:

- Variable de entorno de Vite `VITE_FUENTE_INICIO` = `mock` (D9). `compuesto` y `api` se rechazan hasta que exista ese repositorio.
- En desarrollo, `?fuente=mock&escenario=vacio` sobrescribe. En producción el parámetro se ignora.

**Lo que cambia al conectar el backend real:** la función que construye el repositorio dentro de `inicio.js`. La vista y `inicio.presentar.js` no se tocan. Si el contrato real difiere del propuesto, solo se ajusta el mapper.

### 5.5 Estado (`crearInicio` en `inicio.js`)

Estado (cada cambio crea un objeto nuevo):

```js
{
  usuario:    { estado: "cargando" | "listo" | "error", datos: UsuarioUI | null },
  resumen:    { estado: "cargando" | "listo" | "error", datos: KpiUI[] | null, error: MensajeUI | null },
  pendientes: { estado: "cargando" | "listo" | "vacio" | "error", datos: TareaUI[], filtro: "todos" | "adopcion" | "donativo" | "factura", conteos: Record<string, number> },
  agenda:     { estado: "cargando" | "listo" | "vacio" | "error", datos: JornadaUI[], alertaCobertura: AlertaUI | null },
  destacado:  { estado: "cargando" | "listo" | "oculto", datos: PerroUI | null },
  conteos:    { solicitudes: number, donativos: number },
  actualizadoEn: Date | null,
  refrescando: boolean,
}
```

Intenciones (lo único que la vista puede llamar): `iniciar()`, `actualizar()`, `reintentar(widget)`, `filtrarPendientes(tipo)`, `destruir()`.

Comportamiento:

- `iniciar()` pide saludo, resumen, pendientes, agenda y perro destacado en paralelo con `Promise.allSettled` y actualiza cada widget por separado (R6).
- Cancela peticiones previas con `AbortController` (el mismo patrón que Solicitudes).
- `inicio.presentar.js` convierte entidades en textos, tono y `href` con `enlaceA()`. La vista no formatea.
- `estado.conteos` guarda solicitudes (por revisar + en revisión) y donativos (por validar + facturas pendientes) para el sidebar, cuando se conecte.
- Al volver a la pestaña (`visibilitychange`), refresca si `actualizadoEn` tiene más de 5 minutos.

### 5.6 Vista

- `inicio.view.js` (aún no existe) lee `estado()` y pinta. No importa el mock ni llama `fetch`.
- DOM con `textContent`. Sin `innerHTML` de datos que vengan del servidor.
- Re-render por widget: se reemplaza el nodo de ese widget.
- Las clases nuevas usan prefijo `qa-` y los tokens de `tokens.inicio.css`. Viven en `CSS/cssInicio/`, no en un atomic design compartido.
- Los Custom Elements siguen reservados al shell (`admin-sidebar`, `admin-topbar`). D3.

### 5.7 Manejo de errores

```
repositorio  → mapea DTO → entidad (si el DTO no cumple, lanza INTERNAL con detalle en consola)
reglas       → prioridad y antigüedad
inicio.js    → captura por widget → texto de COPY → estado "error"
             → 401 UNAUTHENTICATED → rutaDe("login") una sola vez
```

No hay `httpClient` ni `sobre.js` compartidos. Esos sobres los normaliza el mapper de Inicio. Un cliente HTTP común solo tendría sentido si otro módulo lo fuera a usar.

### 5.8 Estrategia de pruebas

| Capa                      | Qué se prueba                                                                | Herramienta                          |
| ------------------------- | ---------------------------------------------------------------------------- | ------------------------------------ |
| Dominio                   | Prioridad, antigüedad, urgencia, selección del perro destacado.              | `vite-plus/test` (funciones puras)   |
| Mappers                   | DTO real → entidad (incluye campos `char` con espacios y nulos).             | `vite-plus/test`                     |
| Contrato                  | La misma suite corre contra Mock y Compuesto (con `fetch` simulado).         | `InicioRepository.contract.js`       |
| Estado (`inicio.test.js`) | Carga, vacío, error de un widget, reintento, filtro, 401 una sola vez.       | `vite-plus/test` + repositorio falso |
| Textos                    | Los cubre el mismo test de estado (tono, href, saludo).                      | `vite-plus/test`                     |
| Vista                     | Cuando exista, se prueba en el navegador con `?escenario=`. Sin `happy-dom`. | Manual / el dev server               |

---

## 6. Matriz de viabilidad contra el backend y contratos

### 6.1 Matriz

| Dato / funcionalidad                              | Estado | Observaciones técnicas                                                                                            | Endpoint sugerido                            |
| ------------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------- | -------------------------------------------- |
| Nombre y rol del admin (saludo, topbar)           | ⚠️     | `/me` da rol; `/profile` da nombre. Dos llamadas.                                                                 | Extender `GET /api/perfiles/me` (C1)         |
| Solicitudes por atender (total)                   | ✅     | `resumen.pendientes` de `GET /api/solicitudes/admin/solicitudes?page=1`.                                          | `GET /api/inicio/admin/resumen` (C2)         |
| Desglose por revisar / en revisión                | ⚠️     | Requiere 2 llamadas extra filtrando `estado` y leyendo `paginacion.total`.                                        | C2                                           |
| Antigüedad de la solicitud más vieja              | ❌     | No se expone; el listado no garantiza orden.                                                                      | C2                                           |
| Donativos por validar (conteo, monto, antigüedad) | ✅     | `GET /api/donativos/admin/pendientes` trae `monto` y `fecha`; se calcula en el front.                             | C2                                           |
| Facturas por emitir / que requieren revisión      | ✅     | `GET /api/donativos/admin/solicitudes-cfdi` trae `requiere_revision`.                                             | C2                                           |
| Correos de factura con error                      | ⚠️     | Existe `solicitud_cfdi.correo_estado`, pero el listado no lo devuelve.                                            | C2 + C3                                      |
| Perros disponibles e ingresos del mes             | ✅     | `GET /api/catalogo/admin/perros` trae `estado`, `fecha_ingreso`, `activo`. Respuesta pesada (incluye multimedia). | C2                                           |
| Bandeja unificada de pendientes                   | ⚠️     | Componible en el front con los 3 listados, pero Solicitudes pagina de 10 en 10 sin orden garantizado.             | `GET /api/inicio/admin/pendientes` (C3)      |
| Próximas jornadas con cupo e inscritos            | ✅     | `GET /api/voluntarios/admin/programas/calendario?desde&hasta` (rango ≤ 62 días).                                  | Reutilizar tal cual                          |
| Semanas sin cobertura                             | ✅     | Mismo endpoint (`semanasSinCobertura`).                                                                           | Reutilizar tal cual                          |
| Inscritos nuevos esta semana (boceto)             | ❌     | `se_inscribe.fecha_inscripcion` existe; no hay endpoint. **No se incluye** en la v1.                              | Opcional dentro de C2                        |
| Confirmar asistencia (boceto)                     | ❌     | `se_inscribe.asistencia` existe; no hay endpoint. Pertenece al módulo Voluntariado, no a Inicio.                  | Fuera de alcance                             |
| Perro que más tiempo lleva esperando              | ⚠️     | `fecha_ingreso` disponible; excluir perros con solicitudes activas exige cruzar todas las solicitudes.            | `GET /api/inicio/admin/perro-destacado` (C4) |
| Periodos de prueba por vencer (fase 2)            | ❌     | `periodo_prueba.fecha_fin_prevista` existe; no hay endpoint.                                                      | `GET /api/inicio/admin/periodos-prueba` (C6) |
| Notificaciones para administradores               | ❌     | `notifica` solo admite clientes (`id_cliente`). Fase 1: la campana deriva de C2.                                  | Fase 2 (🟡 D4)                               |
| Búsqueda global                                   | ❌     | No existe. Fase 1: "Ir a…" local sin backend.                                                                     | `GET /api/busqueda/admin` (C5)               |
| Contadores del sidebar                            | ⚠️     | Derivados de C2; mientras tanto, de los endpoints existentes.                                                     | C2                                           |
| Deep links en módulos (`?abrir=`, `?estado=`)     | ⚠️     | Solo Inscritos (`?id=`) y Crear jornada (`?fecha=`) leen parámetros hoy.                                          | Cambio de front en cada módulo (🟡 D6)       |

### 6.2 Convención de respuesta para los endpoints nuevos

Propuesta (🟡 D11): éxito `{ "data": …, "meta": { … } }`; error con la convención ya documentada en `apps/shared/errores.js`: `{ "error": "mensaje", "code": "CODIGO", "fields"?: {…} }`. Fechas ISO 8601 con zona; montos como número en MXN; IDs UUID; claves en `camelCase`.

Todas: `verificarToken` + `requireRole(ROLES.ADMIN, ROLES.ROOT)`; 401 `UNAUTHENTICATED`, 403 `FORBIDDEN`, 500 `INTERNAL_ERROR`.

### 6.3 Contratos sugeridos (especificación para backend)

**C1 — `GET /api/perfiles/me` (extensión, compatible hacia atrás)**

```json
{
  "user": {
    "uuid": "3f0c…",
    "rol": "Administrador",
    "nombre": "Ana",
    "apellidos": "Martínez"
  }
}
```

**C2 — `GET /api/inicio/admin/resumen`**

```json
{
  "data": {
    "solicitudes": {
      "porRevisar": 2,
      "enRevision": 2,
      "masAntiguaDesde": "2026-11-20T15:04:00-06:00"
    },
    "donativos": {
      "porValidar": 3,
      "montoPorValidar": 4500.0,
      "masAntiguoDesde": "2026-11-24T10:00:00-06:00"
    },
    "facturas": { "pendientes": 2, "requierenRevision": 1, "correosConError": 0 },
    "perros": { "disponibles": 18, "ingresosDelMes": 3 },
    "voluntariado": { "inscritosUltimos7Dias": 8 }
  },
  "meta": { "generadoEn": "2026-11-26T14:15:00-06:00", "zonaHoraria": "America/Mexico_City" }
}
```

Reglas: "por revisar" = estado `Pendiente de Revisión`; "en revisión" = `En revisión` (sin `Borrador`). `voluntariado` es opcional (no se pinta en v1).

**C3 — `GET /api/inicio/admin/pendientes?tipo=todos|adopcion|donativo|factura&limite=6`**

```json
{
  "data": [
    {
      "id": "1a2b3c4d-…",
      "tipo": "adopcion",
      "estado": "por_revisar",
      "creadaEn": "2026-11-20T15:04:00-06:00",
      "folio": "QA-1A2B3C4D",
      "persona": { "nombre": "Sofía", "apellidos": "García López" },
      "perro": { "id": "9e8d…", "nombre": "Bruno", "fotoUrl": "https://…" },
      "monto": null
    },
    {
      "id": "7c6b…",
      "tipo": "donativo",
      "estado": "Pendiente de validación",
      "creadaEn": "2026-11-24T10:00:00-06:00",
      "folio": null,
      "persona": { "nombre": "Mariana", "apellidos": "Ruiz" },
      "perro": null,
      "monto": 1500.0
    },
    {
      "id": "5d4e…",
      "tipo": "factura",
      "estado": "Pendiente de atención",
      "creadaEn": "2026-11-25T09:30:00-06:00",
      "folio": null,
      "persona": { "nombre": "Luis", "apellidos": "Torres" },
      "perro": null,
      "monto": 2000.0,
      "requiereRevision": true,
      "correoEstado": "Pendiente de envío"
    }
  ],
  "meta": { "total": 9, "porTipo": { "adopcion": 4, "donativo": 3, "factura": 2 } }
}
```

El backend entrega **datos**, no textos: la redacción ("Revisar solicitud de…") y la prioridad visual las decide el front (R8). Orden por defecto: `creadaEn` ascendente.

**C4 — `GET /api/inicio/admin/perro-destacado`**

```json
{
  "data": {
    "perro": {
      "id": "9e8d…",
      "nombre": "Canela",
      "sexo": "Hembra",
      "edadAprox": 4,
      "tamano": "Mediano",
      "fotoUrl": "https://…",
      "fechaIngreso": "2026-04-26T00:00:00-06:00"
    },
    "diasEnRefugio": 214,
    "solicitudesActivas": 0
  }
}
```

Criterio: `activo = true`, `estado = 'Disponible'`, sin solicitudes en estados activos, mayor antigüedad. Si no hay candidato: `{ "data": null }` (200).

**Agenda — reutilizar `GET /api/voluntarios/admin/programas/calendario?desde=AAAA-MM-DD&hasta=AAAA-MM-DD`** (sin cambios). El front pide de hoy a +28 días y toma las 3 primeras jornadas no finalizadas.

**C5 (fase 2) — `GET /api/busqueda/admin?q=bru&limite=5`**

```json
{
  "data": {
    "perros": [{ "id": "…", "nombre": "Bruno", "estado": "Disponible", "fotoUrl": "…" }],
    "solicitudes": [
      { "id": "…", "folio": "QA-1A2B3C4D", "adoptante": "Sofía García", "perro": "Bruno" }
    ],
    "donativos": [{ "id": "…", "donante": "Bruno Pérez", "monto": 500.0 }],
    "programas": [{ "id": "…", "nombre": "Paseos con cariño", "inicio": "…" }]
  }
}
```

Mínimo 2 caracteres; búsqueda sin acentos (`unaccent`) e insensible a mayúsculas.

**C6 (fase 2) — `GET /api/inicio/admin/periodos-prueba?vencenEnDias=7`**

```json
{
  "data": [
    {
      "idSolicitud": "…",
      "folio": "QA-…",
      "perro": { "id": "…", "nombre": "Luna" },
      "adoptante": "…",
      "fechaFinPrevista": "2026-12-01T00:00:00-06:00"
    }
  ]
}
```

---

## 7. Decisiones abiertas

Para la sesión de estandarización. La opción **recomendada** va primero.

| ID  | Pregunta                                                               | Opciones (recomendada primero)                                                                  |
| --- | ---------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| D1  | ¿Se conserva la paleta actual (amarillo + azul + crema)?               | Conservar y unificar · Rediseñar paleta                                                         |
| D2  | ¿Cómo cargar fuentes e íconos?                                         | Un `<link>` compartido a Google Fonts · Empaquetar con `@fontsource` (sin dependencia externa)  |
| D3  | ¿Componentes como funciones de render o como Custom Elements?          | Funciones de render + Custom Elements solo para el shell · Todo Custom Elements                 |
| D4  | ¿Qué hace la campana en la v1?                                         | Popover con resumen de pendientes · Ocultarla hasta tener notificaciones reales                 |
| D5  | ¿Contadores en el sidebar?                                             | Sí, con caché de 2 min en `sessionStorage` · No                                                 |
| D6  | ¿Los módulos aceptarán deep links (`?abrir=`, `?estado=`, `?accion=`)? | Sí, cada dueño de módulo lo implementa · Solo enlazar a la lista                                |
| D7  | Umbrales de urgencia (SLA)                                             | Solicitudes 3 días, donativos 2 días, facturas 3 días · Otros valores                           |
| D8  | ¿Atajos de teclado?                                                    | Solo `Ctrl/⌘ + K` · Agregar `R` para actualizar · Ninguno                                       |
| D9  | Selección de fuente de datos                                           | Variable `VITE_FUENTE_INICIO` + `?fuente=` en dev · Solo constante en código (como `USAR_MOCK`) |
| D10 | Entorno DOM para pruebas de componentes                                | `happy-dom` · `jsdom` · Sin pruebas DOM                                                         |
| D11 | Sobre de respuesta para endpoints nuevos                               | `{ data, meta }` + errores de `@qarino/shared` · `{ success, data }` de Catálogo                |
| D12 | ¿Login debe dejar Bootstrap y usar el sistema de diseño?               | Sí, en una fase posterior · No                                                                  |
| D13 | ¿KPI "Perros en adopción" o "Inscritos esta semana"?                   | Perros en adopción (✅ hoy) · Inscritos (requiere backend)                                      |
| D14 | ¿Quién es dueño de `JS/ui` y del catálogo vivo?                        | Fuera de este proyecto: no se crean · Una persona responsable · Responsabilidad compartida      |

---

## 8. Hoja de ruta por fases

| Fase | Entregable                                                                                                                   | Definición de terminado                                                              |
| ---- | ---------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------ |
| F0   | Sesión de estandarización: decisiones D1–D14 cerradas y actualizadas en esta guía.                                           | Sin `🟡 DECISIÓN` bloqueantes para F1–F6.                                            |
| F1   | **Tokens de Inicio** en `tokens.inicio.css`, cargados solo por esa página. Sin alias legado ni `admin.css`.                  | Las otras pantallas se ven igual. Los componentes de Inicio no llevan hex.           |
| F2   | **Clases `qa-*` de esta página** en `CSS/cssInicio/`, solo las que un widget use. Sin catálogo vivo.                         | Cada widget pintado tiene carga, vacío y error.                                      |
| F3   | **`enlaceA()` en `rutas.js`.** Sin `httpClient` ni `fuenteDatos` compartidos.                                                | Los enlaces de Inicio no apuntan a un `.html`.                                       |
| F4   | **Dominio + mock:** entidades, reglas, mappers, fixtures con formato real, `InicioRepositoryMock`, suite de contrato. Hecho. | Contrato en verde con Mock; escenarios `ok/vacio/parcial/error/lento/atrasado`.      |
| F5   | **Estado y textos** en `inicio.js` y `inicio.presentar.js`. Hecho. Sin archivo ViewModel.                                    | Pruebas de carga, vacío, error de un widget, reintento, filtro y 401.                |
| F6   | **Vista** `inicio.view.js` montada en `adminInicio.html`. Skeletons, vacíos y errores por widget.                            | La página funciona con `?escenario=ok                                                | vacio | parcial | error | lento | atrasado`. |
| F7   | **Shell:** topbar con usuario real, "Ir a…", campana; sidebar con contadores y logo corregido; login redirige a `inicio`.    | Shell consistente en todas las pantallas existentes.                                 |
| F8   | **Integración:** `InicioRepositoryCompuesto` con endpoints actuales; luego `InicioRepositoryApi` cuando existan C1–C4.       | Contrato en verde con Compuesto; cambio de fuente = 1 línea.                         |
| F9   | **Deep links en módulos** (D6) y migración gradual de módulos al sistema de diseño (encabezados, badges, botones).           | Cada módulo migrado sale de la lista de exclusión de la guarda "sin valores crudos". |

F4 y F5 ya están en el código. F6 es pintar. F1 de esta tabla es solo el archivo de tokens de Inicio, no el sistema de tres capas para todo el panel.

---

## 9. Checklists de PR

**Clase nueva de Inicio (`CSS/cssInicio`)**

- [ ] Solo usa tokens de `tokens.inicio.css`. Sin hex nuevo.
- [ ] Prefijo `qa-`.
- [ ] Foco visible y contraste AA.

**Widget / feature nueva**

- [ ] La vista no hace `fetch`. `inicio.js` no pinta. `domain/` no importa `data/`.
- [ ] El mock pasa la suite de contrato.
- [ ] Carga, vacío y error del widget están probados en `inicio.test.js`.
- [ ] Textos en `COPY` (`inicio.presentar.js`). Enlaces con `enlaceA()` / `rutaDe()`.
- [ ] No duplica funcionalidad de un módulo (revisar §1.3).
- [ ] Contrato de backend documentado en §6 si consume un endpoint nuevo.

---

## 10. Anexos

### 10.1 Glosario

| Término             | Significado                                                                            |
| ------------------- | -------------------------------------------------------------------------------------- |
| Token primitivo     | Valor crudo de la paleta o escala (`--qa-ref-*`). Nunca se usa en componentes.         |
| Token semántico     | Valor con significado de uso (`--qa-color-text-muted`).                                |
| Token de componente | Decisión de un componente que apunta a un semántico (`--qa-card-radius`).              |
| DTO                 | Forma exacta de lo que devuelve el backend.                                            |
| Entidad             | Objeto de dominio limpio que usa el front (fechas como `Date`, nombres normalizados).  |
| Modelo de UI        | Datos listos para pintar (textos, tono, `href`), producidos por `inicio.presentar.js`. |
| Composition root    | Único lugar que elige el repositorio concreto (`elegirFuenteInicio` en `inicio.js`).   |
| Deep link           | URL que abre un módulo con un elemento o filtro ya aplicado.                           |
| SLA                 | Antigüedad máxima aceptable antes de marcar un pendiente como atrasado.                |

### 10.2 Archivos de referencia

- Estado de Inicio: `apps/web/JS/jsInicio/inicio.js`
- Textos de Inicio: `apps/web/JS/jsInicio/inicio.presentar.js`
- Registro de pantallas: `apps/web/JS/rutas.js`
- Reescritura de rutas y entradas de build: `apps/web/vite.config.js`
- Shell: `apps/web/components/admin-sidebar.js`, `admin-topbar.js`
- Tokens actuales: `apps/web/CSS/atomicDesign/atoms/variables.css`
- Template actual: `apps/web/CSS/layoutAdmin.css`
- Convención de errores: `apps/shared/errores.js`
- Esquema de base de datos: `docs/base-de-datos/01_schema.sql`
- Endpoints de donativos: `apps/api/src/modules/donativos/ENDPOINTS.md`
- Calendario y cobertura: `apps/api/src/modules/voluntarios/http/calendarioAdmin.controller.js`
- Listado admin de solicitudes: `apps/api/src/modules/solicitudes/http/solicitudAdmin.controller.js`
