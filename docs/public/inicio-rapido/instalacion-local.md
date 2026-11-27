# Instalación y Entorno Local

Esta guía cubre la configuración local del monorepo (`api`, `web` y `android`).

## Prerrequisitos

- **Node.js `>=22.18.0`** (CI usa Node.js 24)
- **pnpm `12.14.0`** (se descarga automáticamente vía `devEngines` al usar `vp`)
- **Vite+ (`vp`):** Herramienta única para instalar dependencias, formatear, ejecutar pruebas y levantar servidores locales.
- **Android Studio** con JDK 17 o 21 (CI usa Temurin 21) y Android SDK 37 (solo para desarrollo móvil).

---

## 1. Instalar Vite+ y Clonar el Repositorio

**macOS / Linux:**

```bash
curl -fsSL https://vite.plus | bash
```

**Windows (PowerShell):**

```powershell
irm https://vite.plus/ps1 | iex
```

Clona el repositorio e instala las dependencias del workspace desde la raíz:

```bash
git clone https://github.com/BlackWallTec/dog-adoption-app.git
cd dog-adoption-app
vp install
```

`vp install` también ejecuta el script `prepare`, que instala los hooks de Git de `.vite-hooks/`.

---

## 2. Configurar Variables de Entorno

### Backend API (`apps/api/.env`)

Copia la plantilla y completa los valores:

```bash
cp apps/api/.env.example apps/api/.env
```

| Grupo          | Variables                                                                 | Notas                                                                                                                    |
| -------------- | ------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| Servidor       | `NODE_ENV`, `PORT`, `CORS_ORIGIN`                                         | Valores por defecto: `development`, `3000`, `http://localhost:5173`.                                                     |
| Supabase       | `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`          | La API no arranca si falta alguna. La service role key es **solo backend**: nunca en clientes ni en Git.                 |
| Auth           | `JWT_SECRET`                                                              | Genéralo con `openssl rand -base64 48`. Fuera de producción, el servidor no arranca sin él.                              |
| Registro       | `SIGNUP_DATABASE_URL`                                                     | Conexión directa a Postgres con el rol `signup_service` (ver [Base de Datos](../arquitectura/base-de-datos-rls.md)).     |
| Correo         | `MAIL_TRANSPORT`, `RESEND_API_KEY`, `MAIL_FROM`, `SMTP_USER`, `SMTP_PASS` | `MAIL_TRANSPORT=resend` (por defecto) o `gmail` con contraseña de aplicación (`SMTP_*`). Compartido por CFDI y Perfiles. |
| Correo Android | `CORREO_MOVIL_MODO`                                                       | `desactivado` (defecto), `local` sólo en desarrollo o `supabase` en producción (requiere su migración).                  |
| Rate limiting  | `UPSTASH_REDIS_REST_URL`, `UPSTASH_REDIS_REST_TOKEN`                      | Opcionales; sin ellas el limitador se desactiva.                                                                         |

### App Android (`apps/android/local.properties`)

`apps/android/local.defaults.properties` (rastreado en Git) define `BASE_URL=https://dog-adoption-app-mauve.vercel.app/` como valor por defecto mediante el `secrets-gradle-plugin`. Para sobrescribir valores sin modificar archivos rastreados, agrégalos en `apps/android/local.properties` (ignorado por Git):

```properties
BASE_URL=http://localhost:3000/
SUPABASE_URL=https://<tu-proyecto>.supabase.co
SUPABASE_ANON_KEY=<tu-anon-key>
```

{% hint style="warning" %}
`app/build.gradle.kts` también declara `buildConfigField` para estas variables leyendo **solo** `local.properties`. Para evitar ambigüedades, define siempre `BASE_URL` explícitamente en `local.properties`.
{% endhint %}

{% hint style="info" %}
Si pruebas en un dispositivo físico o emulador contra `http://localhost:3000/`, redirige el puerto por ADB:
`adb reverse tcp:3000 tcp:3000`
{% endhint %}

---

## 3. Ejecutar Servicios en Desarrollo

| Comando                   | Descripción                                                                 |
| ------------------------- | --------------------------------------------------------------------------- |
| `vp run dev`              | Levanta `apps/api` (`:3000`) y `apps/web` (`:5173`) en paralelo             |
| `vp run --filter api dev` | Inicia solo el servidor Express con `node --watch`                          |
| `vp run --filter web dev` | Inicia solo el panel administrativo en Vite (proxy de `/api` hacia `:3000`) |
| `vp check`                | Ejecuta formateo, linter y verificación de tipos en todo el repo            |
| `vp check --fix`          | Corrige automáticamente problemas de formato y lint                         |
| `vp run -r test`          | Ejecuta las pruebas (Vitest) de todos los paquetes                          |
| `vp run ready`            | Ejecuta `vp check`, pruebas y build de todos los paquetes                   |
