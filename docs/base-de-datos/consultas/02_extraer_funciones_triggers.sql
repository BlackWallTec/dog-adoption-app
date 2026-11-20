-- consultas/02_extraer_funciones_triggers.sql
-- Genera docs/02_funciones_triggers.sql con: extensiones,
-- triggers fuera de public y definición de funciones de public.
\encoding UTF8
\pset tuples_only on
\pset format unaligned

select '-- ==========================================================';
select '-- 02_funciones_triggers.sql  ·  Proyecto: Prueba BD2';
select '-- Generado: ' || now()::date;
select '-- ==========================================================';
select '';

-- 1) Extensiones
select '-- ---------- 1) Extensiones instaladas ----------';
select format('create extension if not exists %I with schema %I;', e.extname, n.nspname)
from pg_extension e
join pg_namespace n on n.oid = e.extnamespace
where e.extname <> 'plpgsql'
order by e.extname;
select '';

-- 2) Triggers en tablas de Supabase (auth, storage...) que llaman funciones de public
select '-- ---------- 2) Triggers fuera de public ----------';
select format('drop trigger if exists %I on %I.%I;', t.tgname, n.nspname, c.relname)
       || chr(10) || pg_get_triggerdef(t.oid) || ';'
from pg_trigger t
join pg_class c      on c.oid  = t.tgrelid
join pg_namespace n  on n.oid  = c.relnamespace
join pg_proc p       on p.oid  = t.tgfoid
join pg_namespace pn on pn.oid = p.pronamespace
where not t.tgisinternal
  and n.nspname  <> 'public'
  and pn.nspname =  'public';
select '';

-- 3) Resumen de funciones de public (excluye las que vienen de extensiones)
select '-- ---------- 3) Resumen de funciones ----------';
select format('-- %s(%s) -> %s%s',
              p.proname,
              pg_get_function_identity_arguments(p.oid),
              coalesce(pg_get_function_result(p.oid), 'procedure'),
              case when p.prosecdef then '  [SECURITY DEFINER]' else '' end)
from pg_proc p
join pg_namespace n on n.oid = p.pronamespace
where n.nspname = 'public'
  and p.prokind in ('f', 'p')
  and not exists (select 1 from pg_depend d where d.objid = p.oid and d.deptype = 'e')
order by p.proname;
select '';

-- 4) Definición completa de cada función
select '-- ---------- 4) Definición de funciones ----------';
select pg_get_functiondef(p.oid) || ';' || chr(10)
from pg_proc p
join pg_namespace n on n.oid = p.pronamespace
where n.nspname = 'public'
  and p.prokind in ('f', 'p')
  and not exists (select 1 from pg_depend d where d.objid = p.oid and d.deptype = 'e')
order by p.proname;