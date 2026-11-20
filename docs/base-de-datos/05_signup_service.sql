-- =====================================================================
-- Rol dedicado SOLO para el registro público de Clientes
-- Ejecutar en el SQL Editor de Supabase (como postgres)
-- =====================================================================

-- 1) Rol de login sin privilegios extra ------------------------------
-- Cambia la contraseña por una larga y generada (gestor de contraseñas).
create role signup_service
  with login
  password 'CAMBIA_ESTA_CONTRASENA'
  nosuperuser nocreatedb nocreaterole noinherit nobypassrls;

-- 2) Acceso mínimo al esquema ----------------------------------------
grant usage on schema public to signup_service;

-- 3) GRANTs por columna: SOLO INSERT, sin SELECT/UPDATE/DELETE -------
-- (id_direccion y fecha_registro quedan fuera: se llenan por DEFAULT)
grant insert (
  id_usuario, rol, nombre, apellidos, contrasena,
  fecha_nacimiento, aviso_privacidad_aceptado
) on public.usuario to signup_service;

grant insert (id_usuario) on public.cliente to signup_service;

grant insert (id_usuario, correo, telefono)
  on public.datos_confidenciales to signup_service;

grant insert (
  id_usuario, id_codigo_postal, calle, numero_exterior, numero_interior
) on public.direcciones_fisicas to signup_service;

-- Nota: no hace falta SELECT sobre catalogo_de_cps ni sobre rol:
-- Postgres ejecuta las comprobaciones de llave foránea con los
-- privilegios del dueño de la tabla referenciada.

-- 4) Policies de RLS (RLS sigue activo; este rol NO tiene BYPASSRLS) --

-- usuario: el rol sólo puede crear usuarios con rol 'Cliente'.
-- Así, aunque alguien manipulara la petición, este canal público
-- jamás podría crear un Administrador o un Root.
create policy "signup_service puede insertar clientes"
  on public.usuario
  for insert
  to signup_service
  with check (rol = 'Cliente');

create policy "signup_service puede insertar cliente"
  on public.cliente
  for insert
  to signup_service
  with check (true);

create policy "signup_service puede insertar datos_confidenciales"
  on public.datos_confidenciales
  for insert
  to signup_service
  with check (true);

create policy "signup_service puede insertar direcciones_fisicas"
  on public.direcciones_fisicas
  for insert
  to signup_service
  with check (true);

-- 5) Verificación (todo debe dar lo indicado) -------------------------
-- select has_table_privilege('signup_service', 'public.usuario', 'select');  -- false
-- select has_table_privilege('signup_service', 'public.usuario', 'delete');  -- false
-- select has_column_privilege('signup_service', 'public.usuario', 'rol', 'insert'); -- true
-- select has_column_privilege('signup_service', 'public.usuario', 'fecha_registro', 'insert'); -- false

-- 6) Recomendado: asegúrate de que la Data API (anon key) no pueda
-- insertar en estas tablas. Revisa con:
--   select grantee, table_name, privilege_type
--   from information_schema.role_table_grants
--   where table_schema = 'public'
--     and table_name in ('usuario','cliente','datos_confidenciales','direcciones_fisicas')
--     and grantee in ('anon','authenticated');
-- y, si no los necesitas, revócalos:
--   revoke insert, update, delete on public.usuario from anon, authenticated;
--   (idem para las otras tres tablas)

-- Para deshacer todo:
-- drop policy "signup_service puede insertar clientes" on public.usuario;
-- drop policy "signup_service puede insertar cliente" on public.cliente;
-- drop policy "signup_service puede insertar datos_confidenciales" on public.datos_confidenciales;
-- drop policy "signup_service puede insertar direcciones_fisicas" on public.direcciones_fisicas;
-- revoke all on all tables in schema public from signup_service;
-- revoke usage on schema public from signup_service;
-- drop role signup_service;
