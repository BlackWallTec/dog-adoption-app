# Cuentas pendientes de confirmar

El plazo de 72 horas empieza al crear una cuenta **Cliente** con correo habilitado.
Un código nuevo no reinicia ese plazo. Al confirmar los seis dígitos, la API quita
la marca pendiente en Supabase antes de mostrar éxito.

| Archivo                                                                  | Cambio                                                                                                             |
| ------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------ |
| `supabase/migrations/20261120000000_perfiles_cuentas_pendientes_72h.sql` | Columna nullable `usuario.correo_pendiente_desde`, confirmación atómica, procedimiento y tarea cada cinco minutos. |
| `registroPublico.model.js` y `register.controller.js`                    | Guardan la marca con fecha de Postgres en la misma transacción del registro.                                       |
| `user.model.js` y `auth.controller.js`                                   | Consultan la marca pendiente y bloquean el login de ese Cliente.                                                   |
| `correoMovil.service.js`                                                 | Confirma en Supabase y conserva el límite original al reenviar.                                                    |
| `correoMovil.service.test.js` y `correoMovil.routes.test.js`             | Verifican plazo fijo, errores de base y bloqueo sin archivo local.                                                 |
| `correoMovil.supabase.routes.test.js`                                    | Flujo HTTP completo con `CORREO_MOVIL_MODO=supabase`.                                                              |

## Activación

1. Ejecutar la migración completa en **Supabase → SQL Editor** como `postgres`,
   antes de reiniciar la API con este código. Instala `pg_cron` y programa
   `perfiles-cuentas-pendientes-72h`; no hace falta un temporizador en Android.
2. Elegir el modo de los códigos (ver la sección siguiente). No cambia el proveedor Gmail/Resend.
3. Reiniciar la API. Los registros nuevos quedan incluidos en la limpieza.

La tarea procesa hasta 500 cuentas por ejecución. Las cuentas vencen a las 72 horas
y se borran en la siguiente ejecución disponible (normalmente hasta cinco minutos después).
Borra primero sus contactos y después el usuario; las FK eliminan sus filas dependientes.
Root, Administradores, clientes anteriores y confirmados tienen marca NULL o un rol
excluido, por lo que se conservan. No se marca retroactivamente ningún registro.

## Códigos en Supabase (producción / Vercel)

`CORREO_MOVIL_MODO=local` guarda códigos y versiones de sesión en `.local/correo-movil.json`;
sólo sirve en desarrollo y pruebas, y la API lo rechaza con `NODE_ENV=production`.
En Vercel las funciones son efímeras, por eso producción usa `CORREO_MOVIL_MODO=supabase`:

- `supabase/migrations/20261126210000_perfiles_codigos_correo_supabase.sql` crea
  `correo_movil_estado` (sólo hashes HMAC, RLS sin políticas, acceso exclusivo de `service_role`)
  y las RPC `estado_correo_movil`, `emitir_codigo_correo_movil` y `consumir_codigo_correo_movil`.
- Validar, contar intentos y consumir el código ocurre en una sola transacción con `FOR UPDATE`;
  la recuperación cambia el hash bcrypt y aumenta la versión de sesión en la misma transacción.
- El vencimiento de la confirmación es `correo_pendiente_desde + 72 horas`; reenviar no lo cambia.
- Al borrar una cuenta vencida, su fila de códigos se elimina en cascada.

Orden obligatorio para activarlo, porque con el modo activo cada petición autenticada de un
Cliente consulta `estado_correo_movil` (si la función no existe, esas rutas responden 503):

1. Aplicar ambas migraciones en el proyecto de Supabase (primero la de 72 horas).
2. Configurar en Vercel `CORREO_MOVIL_MODO=supabase`, `SUPABASE_SERVICE_ROLE_KEY`, `JWT_SECRET`
   y el transporte (`MAIL_TRANSPORT` con `RESEND_API_KEY`/`MAIL_FROM` o `SMTP_USER`/`SMTP_PASS`).
3. Volver a desplegar.

Si el correo falla después de guardar el código, la cuenta debe esperar un minuto para pedir otro.
Root y Administradores no consultan esta tabla.

Para revisar la tarea: `select jobid, schedule, active from cron.job where jobname = 'perfiles-cuentas-pendientes-72h';`.
Para pausarla: `select cron.unschedule('perfiles-cuentas-pendientes-72h');`.
