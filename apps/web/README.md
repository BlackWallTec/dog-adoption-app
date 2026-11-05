# Frontend web

## Base visual acordada

Usamos **Bootstrap 5.3.8** como base para las nuevas pantallas de toda la web:
rejilla adaptable, utilidades de espaciado, formularios, botones, tarjetas y alertas.
Se mantiene JavaScript nativo y la estructura HTML/CSS/JS del proyecto.

Las pantallas de perfiles permiten iniciar sesión y crear una cuenta de cliente,
con el fondo amarillo casi blanco y sin logos provisionales. El catálogo conserva sus archivos; su migración a
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
El enlace **Crear cuenta** abre `registro.html`. Al registrarse, la cuenta queda activa
y se inicia sesión inmediatamente, sin enviar ni exigir un correo de confirmación.
La API escucha en el puerto 3000 y Vite reenvía `/api` hacia ella.

Preparar `apps/api/.env` a partir de `apps/api/.env.example`, con `SUPABASE_URL`,
`SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY` y `JWT_SECRET` válidos.
Las claves permanecen en el backend; no se copian a variables `VITE_*`.
Antes del primer registro, aplicar `apps/api/sql/perfiles/001_registro_cliente.sql`
siguiendo [la guía de configuración y prueba](../api/docs/perfiles-web.md).
Para una API compartida, copiar `apps/web/.env.example` a `apps/web/.env.local`
y ajustar `API_PROXY_TARGET`. Reiniciar Vite después de cambiarlo.
En producción se debe servir `/api` mediante el servidor o proxy del despliegue, con HTTPS.

## Archivos de perfiles

| Archivo                                     | Responsabilidad                                               |
| ------------------------------------------- | ------------------------------------------------------------- |
| `HTML/htmlPerfiles/login.html`              | Formulario de login y estado de la sesión.                    |
| `HTML/htmlPerfiles/registro.html`           | Formulario de creación de cuenta.                             |
| `CSS/cssPerfiles/perfiles.css`              | Diseño del módulo.                                            |
| `JS/jsPerfiles/login.js`                    | Formularios, errores, carga y mostrar contraseña.             |
| `JS/jsPerfiles/registro.js`                 | Validación, envío del registro y errores por campo.           |
| `JS/jsPerfiles/perfiles.api.js`             | Peticiones reales a registro, login, sesión, perfil y logout. |
| `JS/jsPerfiles/perfiles.session.js`         | Datos propios y cierre de sesión.                             |
| `../shared/perfiles/registro.validation.js` | Reglas compartidas por navegador y Express.                   |

El registro público siempre crea el rol `Cliente`. Los clientes pueden iniciar sesión
y consultar sus propios datos. `Root` y `Administrador` conservan acceso administrativo;
las rutas administrativas rechazan a los clientes incluso con sesión válida.
La recuperación de contraseña queda fuera de esta entrega. No se requiere verificación de correo.

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
`POST /api/perfiles/register` crea la cuenta y devuelve el mismo contrato de sesión.
`GET /api/perfiles/profile` obtiene los datos del usuario del JWT: no acepta otro ID.

## Comprobaciones

```powershell
.\node_modules\.bin\vp.cmd test apps/api/test apps/web/JS/jsPerfiles --maxWorkers=1 --no-file-parallelism
.\node_modules\.bin\vp.cmd build apps/web
```

Las pruebas HTTP ejecutan Express, bcrypt y JWT. Las pruebas de PostgreSQL usan PGlite
en memoria con las relaciones relevantes del esquema y la migración real: verifican
restricciones, rollback y acceso a la RPC. El recorrido del adaptador web también se
prueba con Express y esa base aislada. No conectan a Supabase ni dejan cuentas de prueba.
Para probar el proyecto real se requieren el `.env`, la migración y los permisos del servidor.
