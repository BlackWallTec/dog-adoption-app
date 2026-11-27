# Documentación técnica · Base de datos Supabase "Prueba BD2"

- **Proyecto:** Prueba BD2 (`nwqfhcvlwntkqkraodoq`) · entorno `production`
- **Motor:** PostgreSQL 17.11
- **Fecha de extracción:** 2026-11-19
- **Origen:** la estructura se creó pegando SQL a mano en el Dashboard de Supabase
  (incluidas las migraciones de `supabase/migrations/`). Esta documentación se extrajo
  del estado real de la base con `pg_dump` / `psql` 17.12 y la CLI de Supabase.

## Resumen

| Elemento                       |                                                                          Cantidad |
| ------------------------------ | --------------------------------------------------------------------------------: |
| Tablas en `public`             |                                                                                32 |
| Tablas con RLS activado        |                                                                           32 / 32 |
| Políticas RLS en `public`      |                                                                                51 |
| Funciones en `public`          |                                                                                 9 |
| Triggers de tabla              |                                                                                 0 |
| Event triggers                 |                                                                  1 (`ensure_rls`) |
| Buckets de Storage             |                                                                                 4 |
| Políticas en `storage.objects` |                                                                                 0 |
| Extensiones                    | pg_stat_statements, pgcrypto, supabase_vault, uuid-ossp (por defecto de Supabase) |

### Buckets

| Bucket                 | Público | Límite | Tipos permitidos |
| ---------------------- | :-----: | -----: | ---------------- |
| `cfdi-documentos`      |   No    |  10 MB | PDF, XML         |
| `constancias-fiscales` |   No    |  10 MB | PDF, JPEG, PNG   |
| `documento`            |   Sí    |      — | —                |
| `multimedia_perros`    |   Sí    |      — | —                |

## Archivos

```
docs/base-de-datos/
├── README.md                     ← este archivo
├── 01_schema.sql                 ← esquema completo de public: tablas, llaves, índices, tipos,
│                                    funciones, políticas RLS y grants. Sin datos.
├── 02_funciones_triggers.sql     ← extensiones, resumen y definición de las 9 funciones (RPC)
├── 03_rls_storage.sql            ← estado RLS por tabla, políticas de public, buckets y
│                                    políticas de Storage
├── 04_event_triggers.sql         ← event trigger ensure_rls (no lo incluye pg_dump)
├── consultas/                    ← consultas que generan 02_*.sql y 03_*.sql
└── historial/                    ← SQL de cada función copiado del Dashboard (ver su README)
    └── migraciones-antiguas/     ← migraciones aplicadas a mano antes de la migración base

supabase/migrations/
├── 20261119200351_remote_schema.sql   ← migración base (idéntica a 01_schema.sql)
├── 20261120000000_perfiles_cuentas_pendientes_72h.sql
└── 20261126183000_perfiles_codigos_correo_supabase.sql
```

## Migraciones

### Situación inicial

El proyecto remoto **no tenía historial de migraciones**. Toda la estructura, incluidas las
migraciones del CFDI, se pegó a mano en el SQL Editor del Dashboard.

### Migración base

Se creó `supabase/migrations/20261119200351_remote_schema.sql` con el estado completo de la base
(idéntica a `01_schema.sql`) y se registró como aplicada en el remoto con:

```
npx supabase migration repair --status applied 20261119200351 --remote
```

La migración base refleja el estado remoto extraído el 19 de noviembre. Las migraciones posteriores
en el directorio agregan el plazo de confirmación de cuenta y el almacenamiento persistente de los
códigos de correo de Perfiles; deben aplicarse en orden después de revisarlas.

### Migraciones antiguas

Estas migraciones se movieron a `historial/migraciones-antiguas/` porque su contenido ya está
incluido en la migración base (verificado). Se conservan solo como referencia; **no se deben
volver a ejecutar**:

- `20261115000000_create_solicitud_cfdi.sql`
- `20261119000000_add_cfdi_delivery_to_solicitud.sql`

### Reglas para cambios nuevos

1. **No volver a hacer cambios de estructura a mano en el Dashboard.** Si se hace, el historial
   se desincroniza otra vez.
2. Todo cambio va como migración nueva, con fecha posterior a `20261119200351`:
   ```
   npx supabase migration new nombre_del_cambio
   ```
3. Se aplica a producción con `npx supabase db push`, después de revisarlo en un PR.
4. **No modificar `20261119200351_remote_schema.sql`.** Si se regenera `01_schema.sql`, la
   migración base se queda como está.

### Códigos de correo de Perfiles

La migración `20261126183000_perfiles_codigos_correo_supabase.sql` agrega estado privado de
códigos y RPCs transaccionales para emitirlos, consumirlos y consultar la versión de sesión. Para
habilitarlo en el entorno desplegado, primero hay que aplicar la migración y después configurar
`CORREO_MOVIL_MODO=supabase`. El modo `local` continúa usando el archivo local y no se debe usar en
producción.

## Transacciones / lógica de negocio

La extracción base contiene **9 funciones** en `public`; la migración de códigos de correo agrega
3 RPCs. Cada llamada se ejecuta dentro de una transacción. El detalle de las funciones base está
en `historial/README.md` y en la sección 3 de `02_funciones_triggers.sql`.

`limpiar_datos_prueba()` vacía **todas** las tablas. Solo `service_role` puede ejecutarla.

## Cómo levantar la base de datos limpia en un proyecto Supabase nuevo

> Requisitos: `psql` 17.x y la contraseña de la base de datos del proyecto **nuevo**.
> Los comandos se ejecutan desde la raíz del repo, en PowerShell.

1. Crear el proyecto nuevo y copiar su URI de **Session pooler** (botón **Connect**).
2. Guardar la contraseña:
   ```
   $env:PGPASSWORD = "contraseña_del_proyecto_nuevo"
   ```
3. Ejecutar los scripts **en este orden**:
   ```
   psql -h <host-pooler> -p 5432 -U postgres.<ref-nuevo> -d postgres -v ON_ERROR_STOP=1 -f docs\base-de-datos\01_schema.sql
   psql -h <host-pooler> -p 5432 -U postgres.<ref-nuevo> -d postgres -v ON_ERROR_STOP=1 -f docs\base-de-datos\03_rls_storage.sql
   psql -h <host-pooler> -p 5432 -U postgres.<ref-nuevo> -d postgres -v ON_ERROR_STOP=1 -f docs\base-de-datos\04_event_triggers.sql
   ```
4. `02_funciones_triggers.sql` es de consulta. Se puede volver a ejecutar sin problema
   (`create or replace`, `if not exists`), pero las funciones ya vienen en `01_schema.sql`.
5. Borrar la contraseña: `Remove-Item Env:PGPASSWORD`

## Cómo regenerar esta documentación

Desde la raíz del repo, en PowerShell:

```
$env:PGPASSWORD = "..."
$h = "aws-1-us-west-2.pooler.supabase.com"; $u = "postgres.nwqfhcvlwntkqkraodoq"

pg_dump -h $h -p 5432 -U $u -d postgres --schema-only --schema=public --no-owner -f docs\base-de-datos\01_schema.sql
psql    -h $h -p 5432 -U $u -d postgres -X -q -f docs\base-de-datos\consultas\02_extraer_funciones_triggers.sql -o docs\base-de-datos\02_funciones_triggers.sql
psql    -h $h -p 5432 -U $u -d postgres -X -q -f docs\base-de-datos\consultas\03_extraer_rls_storage.sql -o docs\base-de-datos\03_rls_storage.sql
```

Después del `pg_dump`, ajustar `01_schema.sql`:

- Quitar las líneas `\restrict` / `\unrestrict`, que agrega pg_dump 17.6+ y solo entiende `psql`.
- Cambiar `CREATE SCHEMA public;` por `CREATE SCHEMA IF NOT EXISTS public;`.

```
$p = Join-Path (Get-Location) 'docs\base-de-datos\01_schema.sql'
$l = Get-Content -Encoding UTF8 $p | Where-Object { $_ -notmatch '^\\(un)?restrict' }
[IO.File]::WriteAllText($p, (($l -join "`r`n") + "`r`n").Replace('CREATE SCHEMA public;', 'CREATE SCHEMA IF NOT EXISTS public;'))
```

Los event triggers (`04_event_triggers.sql`) se revisan a mano con:

```sql
select evtname, evtevent, evttags, evtfoid::regprocedure, evtenabled from pg_event_trigger;
```
