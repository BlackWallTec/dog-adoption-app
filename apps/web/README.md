# Frontend web

## Base visual acordada

Usamos **Bootstrap 5.3.8** como base para las nuevas pantallas de toda la web:
rejilla adaptable, utilidades de espaciado, formularios, botones, tarjetas y alertas.
Se mantiene JavaScript nativo y la estructura HTML/CSS/JS del proyecto.

La primera pantalla es el acceso administrativo de perfiles, basado en la referencia
compartida. El catálogo existente conserva sus archivos y estilos; su migración a
Bootstrap queda fuera de este cambio.

### Orden de estilos para cada nueva pantalla

1. Bootstrap 5.3.8 desde el CDN oficial indicado en `HTML/htmlPerfiles/login.html`.
2. `CSS/bootstrap-theme.css`: colores, tipografía y ajustes compartidos.
3. El CSS específico del módulo, por ejemplo `CSS/cssPerfiles/perfiles.css`.

Mantener la versión y el atributo `integrity` iguales en las nuevas páginas.
La referencia oficial es https://getbootstrap.com/docs/5.3/getting-started/introduction/.
Bootstrap y las fuentes de Google requieren conexión a internet.
Esta pantalla usa sólo componentes CSS; no necesita el bundle JavaScript de Bootstrap.
Si una pantalla incorpora un modal, dropdown u otro componente interactivo de Bootstrap,
añadir una sola vez el bundle de la misma versión siguiendo su documentación.

Usar las clases de Bootstrap para las estructuras habituales. Reservar el CSS propio
para la identidad visual o ajustes que las utilidades no cubran. Mantener los estilos
específicos del módulo bajo una clase contenedora para evitar afectar otras pantallas.
Conservar etiquetas visibles, estados de error con texto, foco de teclado y botones
con `type` explícito. No mezclar otro framework de estilos.

## Desarrollo

El login utiliza siempre la API de Express; no tiene cuentas ni respuestas de demostración.
Con las dependencias instaladas, ejecutar desde la raíz del repositorio:

```powershell
.\node_modules\.bin\vp.cmd run --filter api --filter web --parallel dev
```

Abrir http://localhost:5173/HTML/htmlPerfiles/login.html.
La API escucha en el puerto 3000 y Vite reenvía `/api` hacia ella.

Preparar `apps/api/.env` a partir de `apps/api/.env.example`, con `SUPABASE_URL`,
`SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY` y `JWT_SECRET` válidos.
Las claves permanecen en el backend; no se copian a variables `VITE_*`.
Para una API compartida, copiar `apps/web/.env.example` a `apps/web/.env.local`
y ajustar `API_PROXY_TARGET`. Reiniciar Vite después de cambiarlo.
En producción se debe servir `/api` mediante el servidor o proxy del despliegue, con HTTPS.

## Archivos de perfiles

| Archivo                         | Responsabilidad                                         |
| ------------------------------- | ------------------------------------------------------- |
| `HTML/htmlPerfiles/login.html`  | Formulario de login y estado de la sesión.              |
| `CSS/cssPerfiles/perfiles.css`  | Diseño del módulo.                                      |
| `JS/jsPerfiles/login.js`        | Formularios, errores, carga y mostrar contraseña.       |
| `JS/jsPerfiles/perfiles.api.js` | Peticiones reales a login, me y logout.                 |
| `JS/jsPerfiles/perfiles.session.js` | Presentación y cierre de la sesión.                 |

Esta entrega sólo incluye login web para `Root` y `Administrador`.
Crear cuenta, confirmación de correo y recuperación de contraseña quedan pendientes:
el backend todavía no ofrece esos endpoints. El acceso móvil de clientes se conserva.

## Conexión con la API

El adaptador de login ya usa el contrato actual:

```text
POST /api/perfiles/login
Content-Type: application/json
Body: { "email": "...", "passwd": "..." }
credentials: include
Respuesta: { "user": { "id": "...", "email": "...", "rol": "..." } }
```

Al recargar, `POST /api/perfiles/me` restaura el usuario autenticado. Si devuelve 401,
se muestra el formulario. `POST /api/perfiles/logout` borra la cookie del servidor.
El navegador usa una cookie HttpOnly, sin guardar el JWT en localStorage.
La consulta administrativa de perros también exige sesión y rol administrativo.

## Comprobaciones

```powershell
.\node_modules\.bin\vp.cmd test apps/api/test apps/web/JS/jsPerfiles --maxWorkers=1 --no-file-parallelism
.\node_modules\.bin\vp.cmd build apps/web
```

Las pruebas HTTP ejecutan Express, bcrypt y JWT con una consulta a base de datos aislada.
Para verificar Supabase de extremo a extremo se requiere el `.env` local y una cuenta
administrativa real de prueba. Las políticas RLS deben permitir la consulta del servidor
sin exponer contraseñas al cliente.
