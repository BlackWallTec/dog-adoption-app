# Qariño Animal — Documentación Técnica

Bienvenido a la documentación de ingeniería de **Qariño Animal** (`BlackWallTec/dog-adoption-app`, versión `0.40.0`), plataforma multiplataforma para adopción canina y gestión de refugios.

## Componentes del Monorepo

- **Backend API (`apps/api`):** Servidor REST en Node.js + Express 5 conectado a Supabase, organizado en 5 módulos de dominio (`perfiles`, `catalogo`, `solicitudes`, `donativos`, `voluntarios`).
- **Panel Web Administrativo (`apps/web`):** Aplicación multipágina (MPA) construida con Vite+, HTML/CSS/JS, FullCalendar y `@qarino/shared`.
- **Paquete Compartido (`apps/shared` / `@qarino/shared`):** Catálogo centralizado de errores (`errores.js`) y validaciones de perfiles compartidas entre API y Web.
- **App Android (`apps/android`):** Aplicación móvil nativa (`com.app.android`, `minSdk 29`, `targetSdk 37`) en Kotlin, Jetpack Compose, Hilt y Retrofit.
- **Base de Datos (`supabase/`):** Esquema PostgreSQL, migraciones, Storage y políticas de seguridad a nivel de fila (RLS).

## Despliegue

API y Web se despliegan juntos en Vercel (`vercel.json`): las rutas `/api/*` se dirigen al servicio `api` y el resto al servicio `web`; `/` redirige a `/admin/login`.

## Accesos Rápidos

- [Instalación y Entorno Local](inicio-rapido/instalacion-local.md)
- [Flujo de Trabajo en Git y PRs](inicio-rapido/flujo-git.md)
- [Visión General del Monorepo](arquitectura/vision-general.md)
- [Referencia de la API (OpenAPI)](openapi.yaml)
