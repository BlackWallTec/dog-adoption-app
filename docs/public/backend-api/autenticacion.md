# Autenticación, Middlewares y Errores

El servidor Express (`apps/api/src/app.js`) configura CORS (`CORS_ORIGIN`, `credentials: true`), `express.json()` y `cookie-parser`, expone `GET /api/health` y monta los cinco enrutadores principales:

- `/api/perfiles`
- `/api/donativos`
- `/api/solicitudes`
- `/api/catalogo`
- `/api/voluntarios`

`apps/api/src/index.js` carga `.env`, crea la app y, fuera de producción, escucha en `PORT` (por defecto `3000`). En Vercel se exporta la app sin llamar a `listen`.

## Flujo de Autenticación

El origen del token depende del encabezado personalizado `x-client`:

| Cliente | Encabezado `x-client` | Origen del JWT                  |
| ------- | --------------------- | ------------------------------- |
| Android | `mobile`              | `Authorization: Bearer <token>` |
| Web     | (ausente)             | Cookie HTTP `token`             |

{% hint style="warning" %}
Un `Authorization: Bearer` enviado **sin** `x-client: mobile` se ignora y la petición recibe `401`. La app Android añade ambos encabezados en `authInterceptor.kt`.
{% endhint %}

1. **`apps/api/src/utils/getTokenFromReq.js`:** Lee el token de la petición según la tabla anterior (o de `req.token` si ya existe).
2. **`apps/api/src/middleware/is-auth.js`** (`verificarToken`): Verifica el JWT con `JWT_SECRET` (algoritmo `HS256`), guarda los claims en `req.user` y valida la sesión de correo móvil (`verificarCorreoMovil`). Responde `401` con `"No autenticado"` o `"Token inválido o expirado"`; si Supabase no puede validar el estado de correo, responde `503`.
3. **`apps/api/src/middleware/require-role.js`** (`requireRole(...roles)`): Autorización por rol; compara `req.user.rol` con los roles permitidos y responde `403` si no coincide.

Ejemplo:

```js
router.get("/admin/perros", verificarToken, requireRole(ROLES.ADMIN, ROLES.ROOT), getPerrosAdmin);
```

### Roles (`apps/api/src/config/roles.js`)

| Constante      | Valor           |
| -------------- | --------------- |
| `ROLES.ROOT`   | `Root`          |
| `ROLES.ADMIN`  | `Administrador` |
| `ROLES.CLIENT` | `Cliente`       |

Los valores forman parte del contrato de sesión con los clientes.

## Limitador de Peticiones (`apps/api/src/middleware/rate-limiter.js`)

`rateLimiter({ max, ventana, prefijo, forzarEnDev })` crea un middleware por ruta usando Upstash Redis (ventana deslizante; por defecto 60 peticiones por minuto).

- Se desactiva cuando `NODE_ENV=test`, cuando `NODE_ENV=development` (salvo `forzarEnDev: true`) o si faltan `UPSTASH_REDIS_REST_URL` / `UPSTASH_REDIS_REST_TOKEN`.
- Identifica al usuario por su ID de sesión o, si no hay sesión, por IP (`x-forwarded-for`).
- Añade los encabezados `X-RateLimit-Limit` y `X-RateLimit-Remaining` y responde `429` al exceder el límite.
- Si Redis falla, deja pasar la petición (fail-open).

## Otras Dependencias del Backend

- **`multer`** — carga de archivos (documentos de solicitudes y archivos de donativos).
- **Resend / `nodemailer`** — envío de correo (CFDI y Perfiles), seleccionado con `MAIL_TRANSPORT`.
- **Códigos de correo de Perfiles:** `CORREO_MOVIL_MODO=local` usa almacenamiento local solo para desarrollo; `CORREO_MOVIL_MODO=supabase` usa RPCs y almacenamiento persistente en Supabase.
- **`fast-xml-parser`** — procesamiento de XML de CFDI en `donativos`.
- **`bcryptjs`** — hash de contraseñas.

## Manejo Centralizado de Errores (`@qarino/shared`)

Los códigos y mensajes de error estandarizados se definen en `apps/shared/errores.js` y se importan tanto en `apps/api` como en `apps/web` mediante la dependencia de workspace `@qarino/shared`.

_(Para consultar los parámetros, cuerpos de petición y respuestas de cada ruta, revisa la [Referencia de la API](../openapi.yaml))._
