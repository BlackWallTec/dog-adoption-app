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

## Vista previa sin backend ni dependencias

Desde la raíz del repositorio:

```powershell
node apps/web/scripts/preview.mjs
```

Abrir http://127.0.0.1:5173. Para detener el servidor, pulsar `Ctrl+C`.
La URL directa es http://127.0.0.1:5173/HTML/htmlPerfiles/login.html.
Este servidor sólo permite revisar archivos estáticos y **no reenvía peticiones a la API**.

Con las dependencias del proyecto instaladas, se puede usar el flujo habitual:

```powershell
vp run --filter web dev
vp run --filter web build
```

La configuración de Vite incluye la entrada y el login como páginas de producción.
El modo real debe probarse con Vite, cuyo proxy `/api` apunta al backend local.

## Archivos de perfiles

| Archivo | Responsabilidad |
| --- | --- |
| `HTML/htmlPerfiles/login.html` | HTML del login y recuperación de acceso. |
| `CSS/cssPerfiles/perfiles.css` | Diseño del módulo. |
| `JS/jsPerfiles/login.js` | Formularios, errores, carga y mostrar contraseña. |
| `JS/jsPerfiles/perfiles.api.js` | Datos simulados y adaptación al backend. |
| `public/brand-placeholder.svg` | Marca provisional; reemplazar por el logotipo original. |

El registro público de clientes y su confirmación son pantallas posteriores. No se
incluye registro abierto en este acceso exclusivo del equipo administrativo.

## Respuestas simuladas

El modo predeterminado es `mock`, visible mediante la etiqueta **Demostración**.
No crea cuentas, sesiones ni correos reales; tampoco guarda contraseñas ni tokens.
La contraseña es obligatoria; el login no impone una longitud nueva a cuentas existentes.

| Correo de prueba | Resultado con cualquier contraseña no vacía |
| --- | --- |
| Cualquier correo válido distinto de los siguientes | Login simulado exitoso. |
| `credenciales@example.test` | Credenciales incorrectas, HTTP 401 simulado. |
| `pendiente@example.test` | Cuenta pendiente de confirmación, HTTP 403 simulado. |
| `bloqueado@example.test` | Límite de intentos, HTTP 429 simulado. |

La recuperación permite revisar el formulario y su confirmación. Indica explícitamente
que no se envió un correo. No hay almacenamiento de intentos ni bloqueo real.

## Conexión posterior con la API

Después de corregir y validar el backend, establecer `VITE_PERFILES_API_MODE=real`
en el entorno local de Vite y reiniciar el servidor. La configuración predeterminada
permanece en simulación mientras tanto.

El adaptador de login ya usa el contrato actual:

```text
POST /api/perfiles/login
Content-Type: application/json
Body: { "email": "...", "passwd": "..." }
credentials: include
Respuesta: { "user": { "id": "...", "email": "...", "rol": "..." } }
```

El acceso web usa la cookie HttpOnly del servidor. No enviar `x-client: mobile` ni
guardar JWT en localStorage. El éxito del login muestra una confirmación sin abrir
automáticamente otras pantallas que aún no tienen permisos implementados.

La recuperación real devuelve un aviso de funcionalidad pendiente; no se llama a una
ruta inventada. Acordar esa ruta, los códigos de error y el enlace de restablecimiento
antes de conectarla. Las validaciones del frontend deben repetirse en el backend,
que también debe autorizar roles, acceso a datos y operaciones administrativas.
