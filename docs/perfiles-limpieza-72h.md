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

## Activación

1. Ejecutar la migración completa en **Supabase → SQL Editor** como `postgres`,
   antes de reiniciar la API con este código. Instala `pg_cron` y programa
   `perfiles-cuentas-pendientes-72h`; no hace falta un temporizador en Android.
2. Mantener `CORREO_MOVIL_MODO=local` para la demo. No cambia el proveedor Gmail/Resend.
3. Reiniciar la API. Los registros nuevos quedan incluidos en la limpieza.

La tarea procesa hasta 500 cuentas por ejecución. Las cuentas vencen a las 72 horas
y se borran en la siguiente ejecución disponible (normalmente hasta cinco minutos después).
Borra primero sus contactos y después el usuario; las FK eliminan sus filas dependientes.
Root, Administradores, clientes anteriores y confirmados tienen marca NULL o un rol
excluido, por lo que se conservan. No se marca retroactivamente ningún registro.

Los códigos y versiones de sesión todavía usan el archivo local de la demo; esto
no habilita su despliegue en varias instancias ni elimina la protección de producción.

Para revisar la tarea: `select jobid, schedule, active from cron.job where jobname = 'perfiles-cuentas-pendientes-72h';`.
Para pausarla: `select cron.unschedule('perfiles-cuentas-pendientes-72h');`.
