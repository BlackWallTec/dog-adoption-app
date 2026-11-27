# Visión General del Monorepo

`dog-adoption-app` está estructurado como un monorepo gestionado con `pnpm` (`pnpm-workspace.yaml`, con versiones centralizadas en su `catalog`) y `vite-plus`.

```text
dog-adoption-app/
├── apps/
│   ├── api/                  # Backend REST en Node.js + Express 5
│   │   ├── src/              # app.js, config/, middleware/, utils/, modules/
│   │   ├── tests/            # Pruebas Vitest + Supertest por módulo
│   │   └── performance_test/ # Pruebas de carga y estrés
│   ├── web/                  # Panel administrativo web (Vite+ MPA, HTML/CSS/JS)
│   ├── shared/               # Paquete interno @qarino/shared (errores y validaciones de perfiles)
│   └── android/              # Aplicación móvil nativa en Kotlin + Jetpack Compose
├── supabase/
│   └── migrations/           # Migraciones SQL versionadas
├── docs/
│   ├── base-de-datos/        # Scripts SQL de referencia (esquema, funciones, RLS) e historial
│   ├── perfiles-limpieza-72h.md
│   └── public/               # Documentación sincronizada con GitBook
├── .github/                  # CONTRIBUTING, plantillas de PR/issues, workflows de CI
├── .vite-hooks/              # Hooks de Git (commit-msg, pre-commit, pre-push)
├── vite.config.ts            # Configuración raíz de Vite+ (lint, fmt, staged, run)
└── vercel.json               # Despliegue de los servicios api y web
```

## Módulos de Dominio

Tanto el backend (`apps/api/src/modules/<modulo>/`) como la app Android dividen la lógica de negocio en cinco dominios:

1. **`perfiles`:** Registro, inicio de sesión, recuperación de cuenta, verificación de correo en móvil, limpieza de cuentas pendientes (72h) y perfiles de usuario.
2. **`catalogo`:** Listado, filtrado y administración de perros disponibles para adopción.
3. **`solicitudes`:** Proceso de solicitud de adopción por pasos, aceptación/rechazo y carga de documentos.
4. **`donativos`:** Registro y aprobación de donaciones, datos fiscales y emisión de CFDI.
5. **`voluntarios`:** Programas de voluntariado, inscripciones y calendario administrativo.

En la API, cada módulo expone su `router` desde `index.js` y organiza su código en `http/` (rutas, controladores y validaciones) con una carpeta de modelos `modelos<Modulo>/`.

## Despliegue (`vercel.json`)

- Servicio `web`: raíz `apps/web`, salida `dist`.
- Servicio `api`: raíz `apps/api`, framework Express, entrada `src/index.js`.
- Reescrituras: `/api/(.*)` → `api`; todo lo demás → `web`.
- Redirección: `/` → `/admin/login`.
