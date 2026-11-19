# HU63: correo web con Brevo sobre develop

Rama: `feature/perfiles-hu63-correo-brevo`.
Base: `develop`, commit `c495d758bb18ee61b7bc317618c3498b2aeac126`, consultado el 19 de noviembre de 2026.

Se trasladó únicamente el trabajo de correo. Android y los demás módulos conservan las versiones de develop.

## Comportamiento y contratos

- La web conserva el alta de Administradores reservada a Root mediante `POST /api/perfiles/register-admin`.
- El registro público de Clientes conserva su transacción con `signup_service` y `SIGNUP_DATABASE_URL`, incorporadas en develop.
- La sesión se consulta con `GET /api/perfiles/me`. Login, registro y sesión conservan `user: { uuid, rol }`; Android conserva además el token cuando envía `x-client: mobile`.
- Al habilitar el correo, las cuentas nuevas deben confirmar su dirección antes de iniciar sesión. Las cuentas anteriores conservan el acceso.
- Root conserva su sesión al crear un Administrador; el mensaje de confirmación se dirige a la cuenta nueva.
- La confirmación vence a las 72 horas. La recuperación vence a los 30 minutos y permite cambiar la contraseña, consumir el enlace una sola vez e invalidar las sesiones anteriores.
- Brevo sustituye al proveedor anterior mediante su API HTTP, sin agregar dependencias. La bandeja local permite verificar el flujo sin enviar mensajes reales.

## Configuración de desarrollo

Conservar las variables actuales de Supabase, `JWT_SECRET` y, para crear Clientes, `SIGNUP_DATABASE_URL`.
En `apps/api/.env` configurar:

```dotenv
PERFILES_CORREO_MODO=local
WEB_PUBLIC_URL=http://localhost:5173
EMAIL_TRANSPORT=inbox
```

Desde la raíz, ejecutar en terminales separadas:

```powershell
# API
cd apps/api
node src/index.js
```

```powershell
# Web
cd apps/web
..\..\node_modules\.bin\vp.cmd dev --host 127.0.0.1 --port 5173 --strictPort
```

Login: <http://localhost:5173/HTML/htmlPerfiles/login.html>.
Recuperación: <http://localhost:5173/HTML/htmlPerfiles/correo.html?accion=recuperar>.
Registro de Administradores: <http://localhost:5173/HTML/htmlPerfiles/registro.html>, con sesión Root.

En modo inbox, la página de correo muestra la bandeja local. Solo es accesible desde localhost y nunca desde un host remoto o con Brevo.
El estado vive en `apps/api/.local/perfiles-correo.json` si la API se ejecuta desde su carpeta. No borrar este archivo durante las pruebas; ejecutar una sola instancia.

## Activar Brevo

Crear una cuenta en Brevo, verificar un remitente y generar una clave de API (no una clave SMTP).
Guardar exclusivamente en `apps/api/.env`:

```dotenv
EMAIL_TRANSPORT=brevo
BREVO_API_KEY=
MAIL_FROM=cuentas@tu-dominio.com
MAIL_FROM_NAME=Qariño Animal
```

`MAIL_FROM` contiene únicamente la dirección verificada, sin nombre ni signos `< >`. Reiniciar la API después de cambiar el transporte.
El adaptador usa `POST https://api.brevo.com/v3/smtp/email`, cabecera `api-key` y cuerpo con `sender`, `to`, `subject` y `textContent`.
Documentación oficial: <https://developers.brevo.com/docs/send-a-transactional-email>.
La clave nunca se entrega al navegador. Los errores del proveedor no exponen su respuesta ni activan una cuenta pendiente.

## Archivos modificados

| Archivo                                                            | Cambio y motivo                                                                                        |
| ------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------ |
| `.gitignore`                                                       | Excluye el estado y los enlaces privados de la bandeja local.                                          |
| `apps/api/.env.example`                                            | Explica el modo local y las variables de Brevo; conserva `SIGNUP_DATABASE_URL`.                        |
| `apps/api/src/modules/perfiles/services/correo.service.js`         | Tokens, vencimiento, persistencia local, reenvío, recuperación y adaptador HTTP de Brevo.              |
| `apps/api/src/modules/perfiles/http/correo.controller.js`          | Rutas de correo, límite por IP y protección de la bandeja local.                                       |
| `apps/api/src/modules/perfiles/http/auth.router.js`                | Añade rutas de correo conservando GET /me y register-admin protegido.                                  |
| `apps/api/src/modules/perfiles/http/auth.controller.js`            | Rechaza cuentas pendientes y añade la versión de sesión al JWT; conserva uuid y rol.                   |
| `apps/api/src/modules/perfiles/http/register.controller.js`        | Genera confirmación después del registro existente; conserva las dos estrategias de develop.           |
| `apps/api/src/modules/perfiles/http/modelosPerfiles/user.model.js` | Actualiza exclusivamente el hash de contraseña por UUID al recuperar la cuenta.                        |
| `apps/api/src/modules/perfiles/index.js`                           | Expone el comprobador de correo por la entrada pública del módulo.                                     |
| `apps/api/src/middleware/is-auth.js`                               | Invalida sesiones anteriores al cambio de contraseña cuando el modo correo está habilitado.            |
| `apps/web/CSS/bootstrap-theme.css`                                 | Completa el tema Bootstrap que las páginas de Perfiles ya referencian.                                 |
| `apps/web/HTML/htmlPerfiles/correo.html`                           | Presenta confirmación, reenvío, recuperación y bandeja local.                                          |
| `apps/web/HTML/htmlPerfiles/login.html`                            | Enlaza recuperación y reenvío de confirmación.                                                         |
| `apps/web/JS/jsPerfiles/correo.js`                                 | Gestiona tokens temporales, formularios y mensajes sin insertar HTML del servidor.                     |
| `apps/web/JS/jsPerfiles/login.js`                                  | Muestra el estado pendiente; conserva la sesión y el perfil actuales.                                  |
| `apps/web/JS/jsPerfiles/perfiles.api.js`                           | Conecta las nuevas rutas y conserva los códigos de error del correo.                                   |
| `apps/web/JS/jsPerfiles/registro.js`                               | Mantiene el alta por Root e informa que el nuevo Administrador debe confirmar su correo.               |
| `apps/web/vite.config.js`                                          | Compila correo.html conservando el sistema de rutas y las demás páginas actuales.                      |
| `apps/api/tests/perfiles/correo.routes.test.js`                    | Recorrido HTTP, contrato móvil/web, registro transaccional simulado, permisos Root y modo desactivado. |
| `apps/api/tests/perfiles/correo.service.test.js`                   | Tokens, expiración, recuperación, errores de almacenamiento y contrato HTTP de Brevo simulado.         |
| `apps/web/JS/jsPerfiles/perfiles.api.test.js`                      | GET /me, uuid, register-admin, cookies y errores de correo.                                            |
| `docs/perfiles-correo-web.md`                                      | Alcance, configuración, archivos afectados y pendientes.                                               |

## Datos y verificación

No se crean tablas ni migraciones. Se conserva bcrypt/JWT; Supabase Auth no interviene en este flujo.
El registro utiliza las tablas existentes `usuario`, `cliente`, `admin`, `datos_confidenciales` y `direcciones_fisicas` según el rol. Recuperar contraseña actualiza solo `usuario.contrasena`.
Las pruebas de correo simulan la base de datos y Brevo: no requieren claves, no crean cuentas reales y no envían correos externos.

```powershell
.\node_modules\.bin\vp.cmd run --filter api --filter web test
.\node_modules\.bin\vp.cmd run --filter web build
.\node_modules\.bin\vp.cmd check
```

## Pendientes antes de producción

Este MVP sigue limitado a desarrollo: habilitar el modo local con `NODE_ENV=production` se rechaza.
Faltan persistencia transaccional en Supabase para tokens, confirmación y versiones de sesión, y límites compartidos entre instancias.
El cambio de contraseña y el consumo del token deben formar una sola transacción en la versión definitiva.
Falta el procedimiento y la tarea programada para eliminar cuentas sin confirmar después de 72 horas; el vencimiento del enlace no elimina la cuenta.
También falta verificar entrega real con una cuenta y un remitente de Brevo. Tener configurado Brevo no convierte el almacenamiento local en apto para Vercel.
Al habilitar confirmación se aplica también a los Clientes que se registren desde Android; la adaptación de sus mensajes queda para otra tarea.
