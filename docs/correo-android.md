# Correo Android — extensión de Perfiles

Se conserva bcrypt/JWT, `GET /me`, los roles y el token cifrado existente. No hay cambios en web.
Las cuentas anteriores siguen entrando. La confirmación sólo se exige a Clientes nuevos cuando se habilita esta extensión.

## Flujo

- Registro: conserva la transacción de `signup_service`. Si la API devuelve `requiereConfirmacion`, abre la pantalla de código; de lo contrario vuelve al login como antes.
- Confirmación: código de seis dígitos, válido 72 horas, máximo cinco intentos y un solo uso.
- Recuperación: desde “¿Olvidaste tu contraseña?”, código válido quince minutos; cambia el hash bcrypt e invalida los JWT anteriores del Cliente.
- Root y Administradores conservan su comportamiento. Cancelar vuelve al login sin guardar códigos ni contraseñas.

## Archivos

| Parte                                                                 | Cambios                                                                                               |
| --------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| Kotlin: `CorreoScreen`, `CorreoViewModel`                             | Una pantalla compartida para confirmar y recuperar.                                                   |
| Kotlin: `CorreoUseCase`, `CorreoDTO`, `ResultadoRegistro`             | Validación y datos del flujo nuevo.                                                                   |
| `UsuarioAPIservice`, `UsuarioRepositoryImpl`, `PerfilesRepositoryInt` | Reutilizan Retrofit y el manejo de errores actuales; las llamadas de correo no borran el token local. |
| Login, registro y navegación de Perfiles                              | Botones de recuperación/confirmación y paso opcional después del registro.                            |
| API: `correoMovil.router.js`, `correoMovil.service.js`                | Envío Resend y comprobación de códigos.                                                               |
| API: controladores de registro/login, modelo de usuario y middleware  | Alta pendiente optativa y comprobación de versión después de recuperar contraseña.                    |
| Pruebas                                                               | Contratos anteriores, códigos, vencimiento, intentos, roles y conservación del token Android.         |

## Configuración

En `apps/api/.env`, sólo para la prueba local:

```dotenv
CORREO_MOVIL_MODO=local
RESEND_API_KEY=
MAIL_FROM=onboarding@resend.dev
```

Con `CORREO_MOVIL_MODO=desactivado` (valor predeterminado), el registro y login conservan su comportamiento anterior.
`SIGNUP_DATABASE_URL` es la conexión de Postgres con rol `signup_service` que develop ya requiere para registrar Clientes. Se obtiene del responsable de esa conexión; no se sustituye por una API key de Supabase o Resend.
Ninguna clave de Resend va en Kotlin ni en Git. El remitente de prueba de Resend sólo permite enviar al propietario de su cuenta; otros destinatarios requieren un dominio verificado.

## Límite del MVP

Los hashes HMAC, intentos y versiones se guardan en `apps/api/.local/correo-movil.json`; ejecutar una sola instancia y no borrar el estado durante la prueba.
El modo local se rechaza en producción. Antes de desplegar se necesita persistencia transaccional en Supabase y límites compartidos entre instancias.
El vencimiento del código no elimina la cuenta: la limpieza automática tras 72 horas queda pendiente.
Las pruebas simulan base de datos y Resend; la compilación no equivale a un recorrido real en emulador con correo.
