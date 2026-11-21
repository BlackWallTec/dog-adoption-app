begin;

-- Los códigos viven en Supabase para que todas las instancias de la API
-- compartan intentos, expiración y tiempo de espera.
alter table public.usuario
  add column if not exists correo_version_movil integer not null default 0
  check (correo_version_movil >= 0);

create table if not exists public.correo_movil_desafio (
  id_usuario uuid not null references public.usuario(id_usuario) on delete cascade,
  tipo text not null check (tipo in ('confirmar', 'recuperar')),
  codigo_hash text not null check (codigo_hash ~ '^[0-9a-f]{64}$'),
  enviado_en timestamptz not null default statement_timestamp(),
  vence_en timestamptz not null,
  intentos smallint not null default 0 check (intentos between 0 and 5),
  primary key (id_usuario, tipo)
);

alter table public.correo_movil_desafio enable row level security;
revoke all on table public.correo_movil_desafio from public, anon, authenticated, signup_service;
grant select, insert, update, delete on table public.correo_movil_desafio to service_role;
grant select (correo_version_movil), update (correo_version_movil)
  on public.usuario to service_role;

-- Aplica el enfriamiento de un minuto y calcula la vigencia en la base.
-- Un reenvío de confirmación nunca amplía las 72 horas originales.
create or replace function public.emitir_codigo_correo(
  p_id uuid,
  p_tipo text,
  p_hash text
) returns text
language plpgsql security invoker set search_path = '' as $$
declare
  v_rol text;
  v_pendiente timestamptz;
  v_ultimo timestamptz;
  v_vence timestamptz;
begin
  if p_tipo not in ('confirmar', 'recuperar') or p_hash !~ '^[0-9a-f]{64}$' then
    return 'invalido';
  end if;

  select rol, correo_pendiente_desde
    into v_rol, v_pendiente
  from public.usuario
  where id_usuario = p_id
  for update;

  if not found or v_rol <> 'Cliente' then return 'invalido'; end if;
  if p_tipo = 'confirmar' then
    if v_pendiente is null
      or v_pendiente <= statement_timestamp() - interval '72 hours' then
      return 'invalido';
    end if;
    v_vence := v_pendiente + interval '72 hours';
  else
    if v_pendiente is not null then return 'invalido'; end if;
    v_vence := statement_timestamp() + interval '15 minutes';
  end if;

  select enviado_en into v_ultimo
  from public.correo_movil_desafio
  where id_usuario = p_id and tipo = p_tipo
  for update;
  if found and v_ultimo > statement_timestamp() - interval '1 minute' then
    return 'espera';
  end if;

  insert into public.correo_movil_desafio
    (id_usuario, tipo, codigo_hash, enviado_en, vence_en, intentos)
  values (p_id, p_tipo, p_hash, statement_timestamp(), v_vence, 0)
  on conflict (id_usuario, tipo) do update set
    codigo_hash = excluded.codigo_hash,
    enviado_en = excluded.enviado_en,
    vence_en = excluded.vence_en,
    intentos = 0;
  return 'creado';
end;
$$;

create or replace function public.confirmar_codigo_correo(
  p_id uuid,
  p_hash text
) returns text
language plpgsql security invoker set search_path = '' as $$
declare
  v_reto public.correo_movil_desafio%rowtype;
  v_rol text;
  v_pendiente timestamptz;
begin
  -- El cron de 72 horas bloquea primero esta misma fila; conservar el orden
  -- evita competir o crear interbloqueos con la limpieza.
  select rol, correo_pendiente_desde into v_rol, v_pendiente
  from public.usuario where id_usuario = p_id for update;
  if not found or v_rol <> 'Cliente' or v_pendiente is null
    or v_pendiente <= statement_timestamp() - interval '72 hours' then
    return 'invalido';
  end if;

  select * into v_reto from public.correo_movil_desafio
  where id_usuario = p_id and tipo = 'confirmar' for update;
  if not found or v_reto.vence_en <= statement_timestamp() or v_reto.intentos >= 5 then
    return 'invalido';
  end if;
  if v_reto.codigo_hash <> p_hash then
    update public.correo_movil_desafio set intentos = intentos + 1
    where id_usuario = p_id and tipo = 'confirmar';
    return 'invalido';
  end if;

  update public.usuario set correo_pendiente_desde = null
  where id_usuario = p_id and rol = 'Cliente'
    and correo_pendiente_desde > statement_timestamp() - interval '72 hours';
  if not found then return 'invalido'; end if;

  delete from public.correo_movil_desafio
  where id_usuario = p_id and tipo = 'confirmar';
  return 'ok';
end;
$$;

create or replace function public.restablecer_contrasena_correo(
  p_id uuid,
  p_hash text,
  p_contrasena text
) returns text
language plpgsql security invoker set search_path = '' as $$
declare
  v_reto public.correo_movil_desafio%rowtype;
  v_rol text;
  v_pendiente timestamptz;
begin
  select rol, correo_pendiente_desde into v_rol, v_pendiente
  from public.usuario where id_usuario = p_id for update;
  if not found or v_rol <> 'Cliente' or v_pendiente is not null then
    return 'invalido';
  end if;

  select * into v_reto from public.correo_movil_desafio
  where id_usuario = p_id and tipo = 'recuperar' for update;
  if not found or v_reto.vence_en <= statement_timestamp() or v_reto.intentos >= 5 then
    return 'invalido';
  end if;
  if v_reto.codigo_hash <> p_hash then
    update public.correo_movil_desafio set intentos = intentos + 1
    where id_usuario = p_id and tipo = 'recuperar';
    return 'invalido';
  end if;

  update public.usuario set
    contrasena = p_contrasena,
    correo_version_movil = correo_version_movil + 1
  where id_usuario = p_id and rol = 'Cliente' and correo_pendiente_desde is null;
  if not found then return 'invalido'; end if;

  delete from public.correo_movil_desafio
  where id_usuario = p_id and tipo = 'recuperar';
  return 'ok';
end;
$$;

revoke all on function public.emitir_codigo_correo(uuid, text, text)
  from public, anon, authenticated, signup_service;
revoke all on function public.confirmar_codigo_correo(uuid, text)
  from public, anon, authenticated, signup_service;
revoke all on function public.restablecer_contrasena_correo(uuid, text, text)
  from public, anon, authenticated, signup_service;
grant execute on function public.emitir_codigo_correo(uuid, text, text) to service_role;
grant execute on function public.confirmar_codigo_correo(uuid, text) to service_role;
grant execute on function public.restablecer_contrasena_correo(uuid, text, text) to service_role;

notify pgrst, 'reload schema';
commit;
