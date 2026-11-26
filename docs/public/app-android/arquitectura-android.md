# App Android (`apps/android`)

Aplicación nativa desarrollada en **Kotlin** y **Jetpack Compose** siguiendo **Clean Architecture + MVVM**, con inyección de dependencias mediante **Hilt** (KSP).

## Configuración del Proyecto (`apps/android/app/build.gradle.kts`)

- **Package / Application ID:** `com.app.android`
- **SDK:** `minSdk = 29` (Android 10) | `targetSdk = 37` | `compileSdk = 37`
- **Java:** `sourceCompatibility` / `targetCompatibility` 11 (CI compila con JDK 21)
- **Versión actual:** `versionCode = 40` / `versionName = "0.40.0"`
- **Variables inyectadas en `BuildConfig`:** `BASE_URL`, `SUPABASE_URL`, `SUPABASE_ANON_KEY`, leídas desde `local.properties`. El `secrets-gradle-plugin` usa `local.defaults.properties` como respaldo (define `BASE_URL` hacia Vercel). Ver [Instalación](../inicio-rapido/instalacion-local.md#app-android-apps-android-local-properties).

## Estructura de Paquetes (`com.app.android`)

```text
apps/android/app/src/main/java/com/app/android/
├── MainApplication.kt          # Inicialización de Hilt (@HiltAndroidApp)
├── di/                         # Módulos de inyección de dependencias
│   ├── NetworkModule.kt        # Retrofit, Gson, OkHttp y AuthInterceptor
│   ├── CatalogFiltersModule.kt
│   ├── DonativosModule.kt
│   └── VoluntariadoModule.kt
├── domain/                     # Capa de dominio (Kotlin puro)
│   ├── entities/               # Entidades por módulo (entitiesCatalogo, entitiesDonativos, ...)
│   ├── repoInterface/          # Contratos de repositorio
│   └── useCases/               # Casos de uso
├── data/                       # Implementaciones de red, almacenamiento y repositorios
│   ├── authInterceptor.kt      # Añade x-client: mobile y Authorization: Bearer a cada petición
│   ├── tokenProvider.kt        # Gestión del JWT en EncryptedSharedPreferences
│   ├── dataSource/             # Interfaces Retrofit y fuentes locales por módulo (dataSources<Modulo>/)
│   ├── dtos/                   # Modelos de transferencia JSON por módulo (dtos<Modulo>/)
│   └── repoImp/                # Implementación de repositorios y mappers DTO → Entity (repoImp<Modulo>/)
└── presentation/               # Capa de UI
    ├── navigation/             # Grafo de navegación de Compose
    ├── view/                   # MainActivity, atomicDesign/, ui/ (tema) y views<Modulo>/
    ├── viewmodel/              # ViewModels (Hilt)
    └── util/                   # Utilidades de UI
```

Cada capa contiene un `README.md` con sus propias convenciones.

## Seguridad y Red

- **Sesión:** El JWT se cifra localmente con `androidx.security:security-crypto` (`EncryptedSharedPreferences` en `tokenProvider.kt`). `data/dataSource/dataSourcesSolicitudes/sessionStorage.kt` usa `SharedPreferences` estándar para datos de sesión no sensibles del flujo de solicitudes.
- **Cliente HTTP:** Retrofit + Gson sobre OkHttp, con `logging-interceptor` y `authInterceptor.kt`. Retrofit usa `BuildConfig.BASE_URL`.
- **Contrato con la API:** El backend solo lee `Authorization: Bearer` cuando la petición incluye `x-client: mobile` (ver [Autenticación](../backend-api/autenticacion.md)).
- **Imágenes:** Carga asíncrona mediante Coil 2 (`io.coil-kt:coil-compose`).
