# Base de Datos y Políticas RLS

La persistencia y almacenamiento de archivos residen en **Supabase** (PostgreSQL + Supabase Storage).

## Migraciones (`supabase/migrations/`)

- `20261119200351_remote_schema.sql` — Esquema base de tablas, relaciones, funciones y políticas de Row-Level Security (RLS).
- `20261120000000_perfiles_cuentas_pendientes_72h.sql` — Reglas y limpieza automática de cuentas no verificadas tras 72 horas (ver también `docs/perfiles-limpieza-72h.md`).

## Scripts de Referencia (`docs/base-de-datos/`)

Copias legibles del esquema, separadas por tema:

- `01_schema.sql` — Tablas y relaciones.
- `02_funciones_triggers.sql` — Funciones y triggers.
- `03_rls_storage.sql` — Políticas RLS y de Storage.
- `04_event_triggers.sql` — Event triggers.
- `05_signup_service.sql` — Rol `signup_service` para el registro público.
- `consultas/` e `historial/` — Consultas de apoyo y scripts históricos.

## Conexiones desde el Backend (`apps/api/src/config/`)

- **`supabaseClient.js`:** Exporta dos clientes:
  - `supabase` — creado con `SUPABASE_ANON_KEY`; sus operaciones están sujetas a RLS.
  - `supabaseAdmin` — creado con `SUPABASE_SERVICE_ROLE_KEY` (sin persistencia de sesión); omite RLS y es solo para operaciones administrativas del servidor.
- **`postgresSignup.js`:** Conexión directa a PostgreSQL (`SIGNUP_DATABASE_URL`, librería `postgres`) con el rol `signup_service`, que solo puede hacer `INSERT` en las tablas de registro. Se usa **únicamente** en el registro público de Clientes. Está configurada para Vercel (serverless) con el pooler de Supabase en modo transacción (puerto `6543`): `max: 1`, `prepare: false`, `ssl: "require"`.

> **Nota para colaboradores:** Documentar aquí cada tabla, sus columnas principales, buckets de Storage y qué roles tienen permisos `SELECT`, `INSERT`, `UPDATE` o `DELETE` bajo RLS.
