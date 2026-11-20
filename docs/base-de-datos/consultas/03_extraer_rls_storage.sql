-- consultas/03_extraer_rls_storage.sql
-- Genera docs/03_rls_storage.sql con: estado RLS por tabla,
-- políticas de public, buckets y políticas de Storage.
\encoding UTF8
\pset tuples_only on
\pset format unaligned

select '-- ==========================================================';
select '-- 03_rls_storage.sql  ·  Proyecto: Prueba BD2';
select '-- Generado: ' || now()::date;
select '-- ==========================================================';
select '';

-- 1) Estado de RLS por tabla
select '-- ---------- 1) Estado de RLS en tablas de public ----------';
select format('-- %s: %s%s',
              c.relname,
              case when c.relrowsecurity then 'RLS activado' else 'RLS DESACTIVADO  <-- revisar' end,
              case when c.relforcerowsecurity then ' (FORCE)' else '' end)
from pg_class c
join pg_namespace n on n.oid = c.relnamespace
where n.nspname = 'public' and c.relkind in ('r', 'p')
order by c.relname;
select '';

-- 2) Políticas de public (referencia: ya incluidas en 01_schema.sql)
select '-- ---------- 2) Políticas RLS de public ----------';
select '-- (Referencia. Ya vienen incluidas en 01_schema.sql)';
select format('-- Tabla: %s', tablename) || chr(10) ||
       format('drop policy if exists %I on %I.%I;', policyname, schemaname, tablename) || chr(10) ||
       format('create policy %I on %I.%I as %s for %s to %s',
              policyname, schemaname, tablename, permissive, cmd, array_to_string(roles, ', ')) ||
       coalesce(chr(10) || '  using (' || qual || ')', '') ||
       coalesce(chr(10) || '  with check (' || with_check || ')', '') || ';' || chr(10)
from pg_policies
where schemaname = 'public'
order by tablename, policyname;

-- 3) Buckets de Storage
select '-- ---------- 3) Buckets de Storage ----------';
select format('insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types) values (%L, %L, %s, %s, %s) on conflict (id) do nothing;',
              id, name, public,
              coalesce(file_size_limit::text, 'null'),
              coalesce(quote_literal(allowed_mime_types::text) || '::text[]', 'null'))
from storage.buckets
order by id;
select '';

-- 4) Políticas de Storage (storage.objects / storage.buckets)
select '-- ---------- 4) Políticas de Storage ----------';
select format('drop policy if exists %I on %I.%I;', policyname, schemaname, tablename) || chr(10) ||
       format('create policy %I on %I.%I as %s for %s to %s',
              policyname, schemaname, tablename, permissive, cmd, array_to_string(roles, ', ')) ||
       coalesce(chr(10) || '  using (' || qual || ')', '') ||
       coalesce(chr(10) || '  with check (' || with_check || ')', '') || ';' || chr(10)
from pg_policies
where schemaname = 'storage'
order by tablename, policyname;