# Panel Web Administrativo (`apps/web`)

Portal web destinado al personal del refugio para administrar el catálogo de perros, revisar solicitudes de adopción, gestionar voluntarios y consultar donativos.

## Stack Técnico (`apps/web/package.json`)

- **Bundler y Servidor Dev:** Vite+ (`vp dev` en `http://localhost:5173`, `vp build` a `dist/`)
- **Interfaz:** HTML, CSS y JavaScript modular (ES Modules), como aplicación multipágina (`appType: "mpa"`)
- **Librerías Principales:**
  - `fullcalendar` + `temporal-polyfill` — Calendario y programación de eventos.
  - `@qarino/shared` — Catálogo compartido de errores y validaciones de perfiles.

## Estructura de `apps/web/`

```text
apps/web/
├── index.html          # Punto de entrada principal
├── vite.config.js      # Proxy de /api, entradas multipágina y reescritura de rutas
├── HTML/               # Vistas por módulo (htmlPerfiles/, htmlCatalogo/, htmlDonativos/, ...)
├── CSS/                # Hojas de estilo por vista/componente
├── JS/                 # Lógica de cliente por módulo (jsPerfiles/, jsCatalogo/, ...) y rutas.js
├── components/         # Componentes reutilizables de interfaz
├── src/                # Módulos fuente auxiliares
└── public/             # Archivos estáticos públicos
```

## Rutas

`JS/rutas.js` define `PANTALLAS`, que asocia rutas limpias (por ejemplo `/admin/login`) con su archivo HTML. Un plugin en `vite.config.js` reescribe esas rutas en desarrollo y en `vp preview`, y al construir copia cada HTML a `<ruta>/index.html` dentro de `dist/`.

## Comunicación con la API

Las peticiones usan rutas **relativas** (`/api/...`):

- **Desarrollo:** el `server.proxy` de Vite reenvía `/api` a `http://localhost:3000`.
- **Producción:** las reescrituras de `vercel.json` envían `/api/*` al servicio `api` en el mismo dominio.

Como la API y el panel comparten origen, la cookie de sesión `token` se envía con `credentials: "same-origin"` (valor por defecto de `fetch`) o `credentials: "include"`; ambos se usan en el código. No hardcodees `http://localhost:3000` en el cliente.
