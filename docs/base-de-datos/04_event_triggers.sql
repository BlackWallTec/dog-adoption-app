-- ==========================================================
-- 04_event_triggers.sql  ·  Proyecto: Prueba BD2
-- Generado: 2026-11-19
-- ==========================================================
-- Event triggers de la base de datos.
-- pg_dump con --schema=public NO los incluye, por eso van aparte.
--
-- ensure_rls: cada vez que se crea una tabla en public, ejecuta
-- public.rls_auto_enable() para activarle RLS automáticamente.
--
-- Requisito: ejecutar DESPUÉS de 01_schema.sql (la función
-- public.rls_auto_enable() debe existir).
-- ==========================================================

drop event trigger if exists ensure_rls;

create event trigger ensure_rls
  on ddl_command_end
  when tag in ('CREATE TABLE', 'CREATE TABLE AS', 'SELECT INTO')
  execute function public.rls_auto_enable();