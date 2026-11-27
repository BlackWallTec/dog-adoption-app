begin;

create table public.correo_movil_estado (
  id_usuario uuid primary key references public.usuario(id_usuario) on delete cascade,
  version integer not null default 0 check (version >= 0),
  confirmar_hash text,
  confirmar_enviado_en timestamptz,
  confirmar_vence_en timestamptz,
  confirmar_intentos smallint not null default 0 check (confirmar_intentos between 0 and 5),
  recuperar_hash text,
  recuperar_enviado_en timestamptz,
  recuperar_vence_en timestamptz,
  recuperar_intentos smallint not null default 0 check (recuperar_intentos between 0 and 5)
);

comment on table public.correo_movil_estado is
  'Estado privado de códigos de confirmación y recuperación de correo de clientes.';
alter table public.correo_movil_estado enable row level security;
revoke all on table public.correo_movil_estado from public, anon, authenticated;
grant select, insert, update, delete on table public.correo_movil_estado to service_role;

create or replace function public.estado_correo_movil(p_id_usuario uuid)
returns jsonb
language sql
security invoker
set search_path = ''
as $$
  select jsonb_build_object(
    'confirmado', coalesce(u.correo_pendiente_desde is null, true),
    'version', coalesce(e.version, 0)
  )
  from (select p_id_usuario as id_usuario) solicitud
  left join public.usuario u on u.id_usuario = solicitud.id_usuario
  left join public.correo_movil_estado e on e.id_usuario = solicitud.id_usuario;
$$;

revoke all on function public.estado_correo_movil(uuid)
  from public, anon, authenticated;
grant execute on function public.estado_correo_movil(uuid) to service_role;

create or replace function public.emitir_codigo_correo_movil(
  p_id_usuario uuid,
  p_tipo text,
  p_codigo_hash text
)
returns text
language plpgsql
security invoker
set search_path = ''
as $$
declare
  v_pendiente_desde timestamptz;
  v_estado public.correo_movil_estado%rowtype;
  v_ahora timestamptz := statement_timestamp();
  v_vence timestamptz;
begin
  if p_tipo is null or p_tipo not in ('confirmar', 'recuperar')
    or p_codigo_hash is null or p_codigo_hash !~ '^[0-9a-f]{64}$' then
    return 'invalid_request';
  end if;

  select u.correo_pendiente_desde into v_pendiente_desde
  from public.usuario u
  where u.id_usuario = p_id_usuario and u.rol = 'Cliente'
  for update;

  if not found then
    return 'not_eligible';
  end if;

  if p_tipo = 'confirmar' then
    if v_pendiente_desde is null then
      return 'not_eligible';
    end if;
    v_vence := v_pendiente_desde + interval '72 hours';
    if v_vence <= v_ahora then
      return 'expired';
    end if;
  else
    if v_pendiente_desde is not null then
      return 'not_eligible';
    end if;
    v_vence := v_ahora + interval '15 minutes';
  end if;

  insert into public.correo_movil_estado (id_usuario)
  values (p_id_usuario)
  on conflict (id_usuario) do nothing;

  select e.* into v_estado
  from public.correo_movil_estado e
  where e.id_usuario = p_id_usuario
  for update;

  if p_tipo = 'confirmar' then
    if v_estado.confirmar_enviado_en > v_ahora - interval '1 minute' then
      return 'rate_limited';
    end if;
    update public.correo_movil_estado
    set confirmar_hash = p_codigo_hash,
        confirmar_enviado_en = v_ahora,
        confirmar_vence_en = v_vence,
        confirmar_intentos = 0
    where id_usuario = p_id_usuario;
  else
    if v_estado.recuperar_enviado_en > v_ahora - interval '1 minute' then
      return 'rate_limited';
    end if;
    update public.correo_movil_estado
    set recuperar_hash = p_codigo_hash,
        recuperar_enviado_en = v_ahora,
        recuperar_vence_en = v_vence,
        recuperar_intentos = 0
    where id_usuario = p_id_usuario;
  end if;

  return 'sent';
end;
$$;

revoke all on function public.emitir_codigo_correo_movil(uuid, text, text)
  from public, anon, authenticated;
grant execute on function public.emitir_codigo_correo_movil(uuid, text, text)
  to service_role;

create or replace function public.consumir_codigo_correo_movil(
  p_id_usuario uuid,
  p_tipo text,
  p_codigo_hash text,
  p_contrasena_hash text default null
)
returns text
language plpgsql
security invoker
set search_path = ''
as $$
declare
  v_pendiente_desde timestamptz;
  v_estado public.correo_movil_estado%rowtype;
  v_ahora timestamptz := statement_timestamp();
  v_hash text;
  v_vence timestamptz;
  v_intentos smallint;
begin
  if p_tipo is null or p_tipo not in ('confirmar', 'recuperar')
    or p_codigo_hash is null or p_codigo_hash !~ '^[0-9a-f]{64}$' then
    return 'invalid';
  end if;

  select u.correo_pendiente_desde into v_pendiente_desde
  from public.usuario u
  where u.id_usuario = p_id_usuario and u.rol = 'Cliente'
  for update;

  if not found then
    return 'invalid';
  end if;

  if p_tipo = 'confirmar' and (
    v_pendiente_desde is null
    or v_pendiente_desde <= v_ahora - interval '72 hours'
  ) then
    return 'invalid';
  end if;
  if p_tipo = 'recuperar' and (
    v_pendiente_desde is not null or p_contrasena_hash is null
  ) then
    return 'invalid';
  end if;

  select e.* into v_estado
  from public.correo_movil_estado e
  where e.id_usuario = p_id_usuario
  for update;

  if not found then
    return 'invalid';
  end if;

  if p_tipo = 'confirmar' then
    v_hash := v_estado.confirmar_hash;
    v_vence := v_estado.confirmar_vence_en;
    v_intentos := v_estado.confirmar_intentos;
  else
    v_hash := v_estado.recuperar_hash;
    v_vence := v_estado.recuperar_vence_en;
    v_intentos := v_estado.recuperar_intentos;
  end if;

  if v_hash is null or v_vence is null or v_vence <= v_ahora or v_intentos >= 5 then
    return 'invalid';
  end if;

  if v_hash <> p_codigo_hash then
    if p_tipo = 'confirmar' then
      update public.correo_movil_estado
      set confirmar_intentos = confirmar_intentos + 1
      where id_usuario = p_id_usuario;
    else
      update public.correo_movil_estado
      set recuperar_intentos = recuperar_intentos + 1
      where id_usuario = p_id_usuario;
    end if;
    return 'invalid';
  end if;

  if p_tipo = 'confirmar' then
    update public.usuario
    set correo_pendiente_desde = null
    where id_usuario = p_id_usuario
      and rol = 'Cliente'
      and correo_pendiente_desde > v_ahora - interval '72 hours';

    if not found then
      return 'invalid';
    end if;

    update public.correo_movil_estado
    set confirmar_hash = null,
        confirmar_enviado_en = null,
        confirmar_vence_en = null,
        confirmar_intentos = 0
    where id_usuario = p_id_usuario;
  else
    update public.usuario
    set contrasena = p_contrasena_hash
    where id_usuario = p_id_usuario
      and rol = 'Cliente'
      and correo_pendiente_desde is null;

    if not found then
      return 'invalid';
    end if;

    update public.correo_movil_estado
    set version = version + 1,
        recuperar_hash = null,
        recuperar_enviado_en = null,
        recuperar_vence_en = null,
        recuperar_intentos = 0
    where id_usuario = p_id_usuario;
  end if;

  return 'ok';
end;
$$;

revoke all on function public.consumir_codigo_correo_movil(uuid, text, text, text)
  from public, anon, authenticated;
grant execute on function public.consumir_codigo_correo_movil(uuid, text, text, text)
  to service_role;

notify pgrst, 'reload schema';
commit;
