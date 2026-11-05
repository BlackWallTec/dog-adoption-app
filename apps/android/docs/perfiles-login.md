# Cuentas y sesión en Android

Implementación en `feature/perfiles`, con Kotlin, Compose, Material 3, Hilt y
Retrofit. Usa el mismo módulo de Express que la web. Bootstrap corresponde a web.

## Comportamiento

- Login para los roles reconocidos por el backend; el servidor decide sus permisos.
- Registro de una cuenta **Cliente**, sin confirmación por correo ni elección de rol.
- Validación de campos, correo, mayoría de edad, contraseña y privacidad.
- Consulta de datos propios, restauración de sesión y cierre de sesión.
- Errores de credenciales, correo repetido, límites de intentos y problemas de red.
- Cancelación visible durante todo el registro.
- Invitados pueden abrir el detalle existente de catálogo. Adoptar exige login
  y entrega a solicitudes el ID del usuario autenticado.

La recuperación de contraseña requiere un servicio posterior. La lista completa del
catálogo y la finalización de solicitudes pertenecen a sus módulos. El aviso coincide
con el texto de web; debe revisarse con el responsable antes de producción.

## Archivos que se modifican

Rutas relativas a `app/src/main/java/com/app/android/`:

| Archivo                                                             | Responsabilidad                                  |
| ------------------------------------------------------------------- | ------------------------------------------------ |
| `presentation/view/viewsPerfiles/LoginScreen.kt`                    | Diseño y previews del login.                     |
| `presentation/view/viewsPerfiles/RegistroScreen.kt`                 | Formulario, fecha, privacidad y previews.        |
| `presentation/view/viewsPerfiles/CuentaScreen.kt`                   | Datos propios, carga, reintento y logout.        |
| `presentation/view/viewsPerfiles/LoginRoute.kt`, `RegistroRoute.kt` | Conectan pantallas y ViewModels.                 |
| `presentation/viewmodel/viewModelPerfiles/`                         | Estados, peticiones, cancelación y sesión.       |
| `presentation/navigation/AppNavigationManager.kt`                   | Destinos y acceso a cuenta/catálogo/solicitudes. |
| `domain/entities/entitiesPerfiles/Registro.kt`                      | Datos y validación de registro.                  |
| `data/dataSource/dataSourcesPerfiles/UsuarioAPIservice.kt`          | Contrato HTTP con Express.                       |
| `data/dtos/dtosPerfiles/RegistroDTO.kt`, `UsuarioDTO.kt`            | JSON de solicitudes y respuestas.                |
| `data/repoImp/repoImpPerfiles/UsuarioRepositoryImpl.kt`             | Registro, login, perfil, sesión y errores.       |
| `data/session/AlmacenSesionSeguro.kt`                               | Persistencia cifrada con Android Keystore.       |
| `data/session/PerfilesAuthInterceptor.kt`                           | Cabeceras móviles y token Bearer.                |
| `di/NetworkModule.kt`, `PerfilesModule.kt`                          | Configuración e inyección.                       |

El contrato original de login de estos seis archivos procedía de commits de contenido
firmados como `luise`: servicio, UsuarioDTO, UsuarioRepositoryImpl, Usuario,
PerfilesRepositoryInt y LoginUseCase. Las funciones nuevas amplían ese trabajo.
`NetworkModule.kt` es compartido y tenía autores adicionales.

## Contrato con Express

Todas las peticiones llevan `x-client: mobile`. Las autenticadas añaden
`Authorization: Bearer <token>`; Android no usa la cookie del navegador.

| Método y ruta                 | Entrada / respuesta                                    |
| ----------------------------- | ------------------------------------------------------ |
| `POST /api/perfiles/login`    | `{email, passwd}` → `{user: {id, email, rol}, token}`. |
| `POST /api/perfiles/register` | Formulario → la misma sesión de login.                 |
| `POST /api/perfiles/me`       | Sin cuerpo; comprueba el token y devuelve `{user}`.    |
| `GET /api/perfiles/profile`   | Devuelve `{profile}` con datos propios.                |
| `POST /api/perfiles/logout`   | Devuelve `{ok: true}`.                                 |

El registro envía `nombre`, `apellidos`, `email`, `telefono`, `fechaNacimiento`
(`YYYY-MM-DD`), `calle`, `numeroExterior`, `codigoPostal`, `passwd`,
`confirmPasswd` y `privacidad` (booleano). La API vuelve a validar y devuelve
los errores por campo en respuestas 400/409. La dirección libre incluye calle,
colonia, ciudad y estado; número exterior y código postal van separados.

El token y los datos mínimos de sesión se cifran con AES-GCM; la clave permanece
en Android Keystore. Las preferencias se excluyen de las copias de seguridad.
Las contraseñas no se persisten y el logger HTTP no imprime cuerpos ni autorización.
Un 401 al restaurar o consultar perfil elimina la sesión local; un fallo temporal
permite reintentar. Logout elimina la copia local incluso sin conexión. El backend
actual no revoca anticipadamente los JWT móviles ya emitidos.

Las claves privadas de Supabase permanecen en `apps/api/.env`, fuera de Git.
Nunca deben añadirse a Kotlin, BuildConfig ni al APK.

## Ejecutar en Android Studio

1. Abrir **`dog-adoption-app/apps/android`** como proyecto, no `blackwall`.
2. Esperar la sincronización de Gradle y seleccionar la configuración **app**.
3. Iniciar la API desde la raíz del repositorio:

   ```powershell
   .\node_modules\.bin\vp.cmd run --filter api dev
   ```

4. Seleccionar un emulador o teléfono en la barra superior y pulsar **Run ▶**.
5. Elegir **Regístrate**, completar los campos y crear la cuenta. Aparecerá
   **Mi cuenta**. Cerrar sesión permite volver a comprobar el login.

Por defecto el emulador usa `http://10.0.2.2:3000/`, que llega al equipo anfitrión.
Para teléfono físico usa la IP local del equipo y permite la conexión al puerto
necesario de la API en tu red de pruebas.

Configurar otra URL mediante estas opciones, en orden de prioridad:

1. Gradle: `-PapiBaseUrl=https://api.ejemplo.com/`.
2. Variable de entorno: `ANDROID_API_BASE_URL`.
3. `api.baseUrl` en `apps/android/local.properties`, excluido de Git.

Ejemplo local para teléfono en `local.properties`:

```properties
api.baseUrl=http://192.168.1.50:3000/
```

HTTP se habilita exclusivamente en debug. Compilar release exige una URL HTTPS.
En Android 17 o superior, debug solicita permiso para conectar a un servidor local
cuando la dirección configurada es privada; el usuario puede reintentarlo.

## Ver pantallas sin ejecutar la API

Abrir `LoginScreen.kt`, `RegistroScreen.kt` o `CuentaScreen.kt` y seleccionar
**Split** o **Design**, después **Build & Refresh** si aparece. Los previews usan
valores de ejemplo y no conectan con la API. Los archivos terminados en `Route.kt`
conectan lógica y navegación: para ver el diseño usa los `Screen.kt`.

## Compilación y pruebas

Desde `apps/android`, con JDK 25 y el SDK en `local.properties`:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --no-daemon --console=plain
```

En equipos con poca memoria se verificó esta configuración:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --offline --no-daemon --no-parallel --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx768m -XX:MaxMetaspaceSize=512m -XX:ReservedCodeCacheSize=96m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process' --console=plain
```

Las pruebas unitarias cubren validación, límite de edad, errores, contrato Retrofit,
cabeceras, registro, perfil y restauración/logout. No usan Supabase.
Informe: `app/build/reports/tests/testDebugUnitTest/index.html`.
APK: `app/build/outputs/apk/debug/app-debug.apk`.

Verificación tras integrar develop el 5 de noviembre de 2026: APK principal compilado;
28 pruebas unitarias aprobadas, sin fallos; Android Lint sin errores, con 16
advertencias de mantenimiento. Las pruebas instrumentadas todavía no se ejecutaron:
el emulador exigía 7.2 GB libres para su partición de datos y el equipo no tenía
ese espacio. Se requiere comprobar el recorrido completo en un teléfono o emulador.

Las pruebas instrumentadas en `app/src/androidTest/.../perfiles/` cubren formulario
y cifrado real con Keystore. El recorrido contra la API está desactivado por defecto:
`PerfilesLiveTest` requiere `perfilesLive=true` y `perfilesTestEmail` con un correo
único temporal. Crea una cuenta real; quien lo ejecute debe eliminar después sus filas
de consentimiento, dirección, datos confidenciales, cliente y usuario. Nunca usar una
cuenta existente ni pasar claves de Supabase al dispositivo.

El wrapper de Gradle 9.6.0 se recuperó de la distribución oficial y su SHA-256 es
`497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`.

Referencias: [Android Keystore](https://developer.android.com/privacy-and-security/keystore),
[red del emulador](https://developer.android.com/studio/run/emulator-networking-address),
[permiso de red local](https://developer.android.com/privacy-and-security/local-network-permission).
