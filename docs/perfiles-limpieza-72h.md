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
| `20261126183000_perfiles_codigos_correo_supabase.sql`                    | Guarda hashes, vencimientos e intentos en Supabase y ofrece RPCs atómicos para producción.                         |

## Activación

1. Aplicar las migraciones pendientes después de revisarlas, con `npx supabase db push`.
   La migración de 72 horas programa `perfiles-cuentas-pendientes-72h`; no hace falta un
   temporizador en Android.
2. Para la demo local, mantener `CORREO_MOVIL_MODO=local`. Para un despliegue, configurar
   `CORREO_MOVIL_MODO=supabase` solo después de aplicar
   `20261126183000_perfiles_codigos_correo_supabase.sql`.
3. Configurar en el entorno del servidor `NODE_ENV=production`, `JWT_SECRET`, las variables de
   Supabase y las credenciales del proveedor de correo (`MAIL_TRANSPORT`, `SMTP_USER`/`SMTP_PASS`
   o `RESEND_API_KEY`/`MAIL_FROM`). No copiar secretos al cliente ni al repositorio.
4. Desplegar la API y probar registro, reenvío, confirmación y recuperación antes de habilitarlo a
   usuarios reales.

La tarea procesa hasta 500 cuentas por ejecución. Las cuentas vencen a las 72 horas
y se borran en la siguiente ejecución disponible (normalmente hasta cinco minutos después).
Borra primero sus contactos y después el usuario; las FK eliminan sus filas dependientes.
Root, Administradores, clientes anteriores y confirmados tienen marca NULL o un rol
excluido, por lo que se conservan. No se marca retroactivamente ningún registro.

En modo `local`, los códigos y versiones de sesión usan un archivo local. En modo `supabase`,
quedan en la base de datos con acceso restringido al backend y operaciones atómicas, para que se
compartan entre instancias. No configures `supabase` antes de aplicar la migración nueva.

Para revisar la tarea: `select jobid, schedule, active from cron.job where jobname = 'perfiles-cuentas-pendientes-72h';`.
Para pausarla: `select cron.unschedule('perfiles-cuentas-pendientes-72h');`.
