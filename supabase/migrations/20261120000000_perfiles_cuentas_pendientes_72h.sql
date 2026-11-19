begin;

-- NULL conserva las cuentas anteriores y significa que no hay confirmación pendiente.
-- Sólo el registro público con correo habilitado guarda la fecha del servidor.
alter table public.usuario
  add column if not exists correo_pendiente_desde timestamptz;
comment on column public.usuario.correo_pendiente_desde is
  'Inicio del plazo de 72 horas para confirmar el correo. NULL excluye la cuenta de la limpieza.';
grant insert (correo_pendiente_desde) on public.usuario to signup_service;
grant select (correo_pendiente_desde), update (correo_pendiente_desde)
  on public.usuario to service_role;
create index if not exists usuario_correo_pendiente_idx
  on public.usuario (correo_pendiente_desde)
  where rol = 'Cliente' and correo_pendiente_desde is not null;

-- Sólo la API, después de validar los seis dígitos, puede quitar la marca.
-- El UPDATE bloquea la misma fila que el procedimiento: confirmar y borrar no compiten.
create or replace function public.confirmar_correo_cliente(p_id uuid)
returns boolean language plpgsql security invoker set search_path = '' as $$
begin
  update public.usuario set correo_pendiente_desde = null
  where id_usuario = p_id and rol = 'Cliente'
    and (correo_pendiente_desde is null
      or correo_pendiente_desde > statement_timestamp() - interval '72 hours');
  return found;
end;
$$;
revoke all on function public.confirmar_correo_cliente(uuid)
  from public, anon, authenticated, signup_service;
grant execute on function public.confirmar_correo_cliente(uuid) to service_role;

-- Lotes pequeños; una confirmación en curso se omite hasta la siguiente ejecución.
-- Los datos de contacto se borran primero por la FK adicional sin ON DELETE CASCADE.
create or replace procedure public.eliminar_cuentas_pendientes_72h()
language plpgsql security invoker set search_path = '' as $$
declare cuenta record;
begin
  for cuenta in
    select id_usuario from public.usuario
    where rol = 'Cliente'
      and correo_pendiente_desde <= statement_timestamp() - interval '72 hours'
    order by correo_pendiente_desde, id_usuario
    limit 500 for update skip locked
  loop
    delete from public.datos_confidenciales where id_usuario = cuenta.id_usuario;
    delete from public.usuario where id_usuario = cuenta.id_usuario;
  end loop;
end;
$$;
revoke all on procedure public.eliminar_cuentas_pendientes_72h()
  from public, anon, authenticated, signup_service, service_role;
grant execute on procedure public.eliminar_cuentas_pendientes_72h() to postgres;

-- pg_cron ejecuta la limpieza aunque la API o la aplicación estén cerradas.
create extension if not exists pg_cron with schema pg_catalog;
grant usage on schema cron to postgres;
grant all privileges on all tables in schema cron to postgres;
select cron.schedule(
  'perfiles-cuentas-pendientes-72h', '*/5 * * * *',
  'CALL public.eliminar_cuentas_pendientes_72h()'
);
notify pgrst, 'reload schema';
commit;
