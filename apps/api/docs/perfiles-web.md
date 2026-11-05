# Registro e inicio de sesión web (HU63)

El frontend Bootstrap utiliza Express y las tablas `public.usuario`, `cliente`,
`datos_confidenciales` y `direcciones_fisicas`. Se conserva bcrypt + JWT del backend.
Este flujo no crea usuarios en Supabase Auth ni cambia sus ajustes de confirmación.
Por decisión de esta entrega, las nuevas cuentas quedan activas sin verificar correo.

## Preparar Supabase

1. Confirmar que existe el rol `Cliente` en `public.rol`.
2. Revisar duplicados ignorando mayúsculas y espacios, sin mostrar correos:

   ```sql
   SELECT count(*) AS grupos_duplicados FROM (
     SELECT lower(btrim(correo)) FROM public.datos_confidenciales
     GROUP BY lower(btrim(correo)) HAVING count(*) > 1
   ) duplicados;
   ```

3. Si devuelve cero, aplicar `apps/api/sql/perfiles/001_registro_cliente.sql`
   desde el SQL Editor. Si hay duplicados, resolverlos con el responsable de datos;
   la migración no borra ni fusiona cuentas existentes.
4. Comprobar que `service_role` tiene `SELECT`/`INSERT` sobre las cuatro tablas anteriores
   y que las tablas personales no están expuestas a `anon`/`authenticated` mediante
   permisos o políticas generales. Esta migración no modifica las políticas de otros módulos.

La migración añade un índice de correo único normalizado, la tabla
`perfiles_consentimiento` y la función `perfiles_registrar_cliente`. La función solo
se puede ejecutar con la clave del servidor. Sus inserciones se confirman juntas;
si una falla, se revierte todo el registro. No ejecutar el esquema de contexto del SRS.

La dirección libre (calle, colonia y localidad) se almacena en `direcciones_fisicas.calle`,
el número en `numero_exterior` y el CP en `datos_confidenciales.codigo_postal`.
No se altera ni se generan entradas en el catálogo de códigos postales.
La aceptación registra fecha y versión `registro-web-v1`. El texto informativo de la
pantalla debe sustituirse por el aviso oficial del refugio antes de publicar en producción.

## Configuración local

Copiar `apps/api/.env.example` a `apps/api/.env` y completar:

| Variable                    | Valor                                                        |
| --------------------------- | ------------------------------------------------------------ |
| `SUPABASE_URL`              | URL del proyecto de Supabase                                 |
| `SUPABASE_ANON_KEY`         | Clave pública del proyecto, usada por los módulos existentes |
| `SUPABASE_SERVICE_ROLE_KEY` | Clave privada del servidor para perfiles                     |
| `JWT_SECRET`                | Secreto aleatorio local para firmar sesiones                 |

Conservar los nombres actuales de variables aunque el panel entregue claves nuevas
publishable/secret. Nunca colocar la clave privada en HTML, JS del navegador o `VITE_*`.
`.env` está ignorado por Git. Para generar el secreto local con Node:

```powershell
node -e "console.log(require('node:crypto').randomBytes(48).toString('hex'))"
```

Desde la raíz, con dependencias instaladas:

```powershell
.\node_modules\.bin\vp.cmd run --filter api --filter web --parallel dev
```

Abrir http://localhost:5173/HTML/htmlPerfiles/login.html.
Vite reenvía `/api` a Express en el puerto 3000. Para otro backend, configurar
`API_PROXY_TARGET` en `apps/web/.env.local` y reiniciar Vite.

## Contratos y comprobación manual

| Ruta                          | Comportamiento                                      |
| ----------------------------- | --------------------------------------------------- |
| `POST /api/perfiles/register` | Datos de registro → 201, usuario y cookie de sesión |
| `POST /api/perfiles/login`    | `{email, passwd}` → 200, usuario y cookie           |
| `POST /api/perfiles/me`       | Restaura la sesión desde la cookie                  |
| `GET /api/perfiles/profile`   | Devuelve exclusivamente datos del ID del JWT        |
| `POST /api/perfiles/logout`   | Borra la cookie del navegador                       |

El registro recibe `nombre`, `apellidos`, `email`, `telefono`, `fechaNacimiento`
(`AAAA-MM-DD`), `calle`, `numeroExterior` (cadena numérica), `codigoPostal`, `passwd`,
`confirmPasswd` y `privacidad` (booleano). Todos son obligatorios. El rol se fija en
`Cliente` en la base; ningún campo del formulario permite elegir privilegios.
Admite los cinco dominios del SRS, exige 18 años, contraseña de 8 caracteres o más
(hasta 72 bytes UTF-8 por bcrypt), confirmación y aceptación del aviso.
Los campos inválidos devuelven 400; correo duplicado, 409; credenciales incorrectas, 401. Después de 25 fallos por cuenta en un minuto, se devuelve 429 durante 10 minutos.
El contador reside en memoria y requiere almacenamiento compartido si se despliegan
varias instancias; reiniciar la API lo reinicia.

Las sesiones administrativas duran 1 día; las de clientes, 30 días. En web se utiliza
cookie HttpOnly, SameSite=Lax y Secure con `NODE_ENV=production`; el despliegue requiere
HTTPS. No se guarda el token en localStorage. Se conserva el contrato móvil con
`x-client: mobile` y token Bearer. El logout web borra la cookie; no revoca JWT móviles.

Recorrido: crear cuenta con un correo disponible → ver Mis datos → recargar → cerrar
sesión → entrar con la misma cuenta. Comprobar también correo duplicado, menor de edad,
campos vacíos, contraseña incorrecta, cancelación y rechazo 403 en rutas administrativas
para `Cliente`. No se requieren bandeja de correo ni enlaces de activación.

## Pruebas automatizadas

```powershell
.\node_modules\.bin\vp.cmd test apps/api/test apps/web/JS/jsPerfiles --maxWorkers=1 --no-file-parallelism
.\node_modules\.bin\vp.cmd build apps/web
```

Incluyen PostgreSQL en memoria (PGlite) y no necesitan credenciales. Son independientes
del proyecto real: la comprobación final contra Supabase requiere completar la
configuración y aplicar la migración. No se incluye recuperación de contraseña.

Referencias: [funciones de Supabase](https://supabase.com/docs/guides/database/functions),
[claves de API](https://supabase.com/docs/guides/getting-started/api-keys).
