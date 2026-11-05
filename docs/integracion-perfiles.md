# Integración de develop en feature/perfiles

La integración trae los cambios de `develop` a `feature/perfiles`. No actualiza
`develop` ni abre un pull request. El equipo revisará después el PR hacia esa rama.

## Puntos de partida

- Perfiles antes de integrar: `ba06ae5`.
- Develop incorporado: `4596bac`.
- Respaldo local: `backup/perfiles-antes-develop-20261105`.
- Preparación y pruebas en una copia de trabajo separada, en la rama local
  `integracion/perfiles-develop-20261105`.

Se utiliza un merge para conservar el historial compartido. No se hace rebase ni
push forzado de las ramas publicadas.

## Compatibilidad entre módulos

- Se conservan login, registro sin confirmación por correo, perfil y sesión cifrada.
- NetworkModule reúne perfiles, solicitudes y voluntariado. Conserva la URL configurable,
  el token Bearer móvil y el registro HTTP sin contraseñas ni tokens.
- Solicitudes usa las rutas nuevas sin ID de solicitud para los pasos del borrador.
  El borrador comienza con el ID de la sesión y se limpia al salir, vencer la sesión
  o cambiar de usuario.
- Voluntariado se inscribe con `req.user.uuid`, obtenido del JWT. Ya no depende de
  `DEV_USUARIO_ID`, ni acepta un usuario alternativo enviado en el cuerpo.
- Se conserva la configuración de registro de perfiles y su `apps/api/.env.example`.
  Los archivos privados `.env` y `local.properties` siguen fuera de Git.
- Se habilita desugaring para las fechas de voluntariado sin elevar minSdk.
  Referencia: [soporte de APIs Java en Android](https://developer.android.com/studio/write/java8-support).

Los contratos compartidos de autenticación quedan así:

| Ruta                                                        | Acceso                                    |
| ----------------------------------------------------------- | ----------------------------------------- |
| `/api/catalogo/admin/*`                                     | Sesión de Administrador o Root.           |
| `GET /api/solicitudes/cliente/activa`                       | Sesión e idUsuario propio.                |
| `GET /api/solicitudes/cliente/perfil/:idUsuario`            | Sesión e idUsuario propio.                |
| `POST /api/voluntarios/programas/:idPrograma/inscripciones` | Sesión; el servidor determina el usuario. |
| `GET /api/voluntarios/cliente/programas`                    | Público.                                  |

Web envía su cookie de sesión. Android envía `x-client: mobile` y
`Authorization: Bearer <token>`. Las rutas no requieren conocer las claves de Supabase.

## Validación antes del futuro PR

Resultado de la integración del 5 de noviembre de 2026:

- API: 77 pruebas aprobadas; web: 20 pruebas aprobadas.
- Android: 28 pruebas unitarias aprobadas y APK debug compilado.
- Compilación web correcta.
- `vp check`: sin errores, con 9 advertencias de mantenimiento.
- Android Lint: sin errores, con 16 advertencias de mantenimiento.

Desde la raíz:

```powershell
.\node_modules\.bin\vp.cmd check
.\node_modules\.bin\vp.cmd run --no-cache --filter api --filter web test
.\node_modules\.bin\vp.cmd run --no-cache --filter web build
```

Desde `apps/android`:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --no-daemon --no-parallel --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx768m -XX:MaxMetaspaceSize=512m -XX:ReservedCodeCacheSize=96m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process' --console=plain
```

Las pruebas HTTP de integración usan modelos simulados y la suite de registro usa
PostgreSQL local en memoria. No crean cuentas ni inscripciones en Supabase.
Debe comprobarse también el recorrido manual en Android; compilar y pasar las pruebas
unitarias no sustituye esa comprobación. Las nuevas pantallas de otros módulos
conservan el alcance que tienen en develop.

Antes de abrir el PR, traer de nuevo develop si recibió más cambios y repetir las
pruebas. La revisión debe incluir a los responsables de solicitudes, catálogo y
voluntariado por los contratos de sesión compartidos. El respaldo permite comparar
el estado anterior; una reversión en una rama publicada se hace mediante un commit
de reversión, conservando el historial del equipo.
