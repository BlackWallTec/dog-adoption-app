-- ==========================================================
-- 02_funciones_triggers.sql  ·  Proyecto: Prueba BD2
-- Generado: 2026-11-19
-- ==========================================================

-- ---------- 1) Extensiones instaladas ----------
create extension if not exists pg_stat_statements with schema extensions;
create extension if not exists pgcrypto with schema extensions;
create extension if not exists supabase_vault with schema vault;
create extension if not exists "uuid-ossp" with schema extensions;

-- ---------- 2) Triggers fuera de public ----------

-- ---------- 3) Resumen de funciones ----------
-- contar_inscritos(p_id_programa uuid) -> integer  [SECURITY DEFINER]
-- donativos_registrar_monetario(p_id_usuario uuid, p_monto numeric, p_referencia character varying, p_comprobante bytea) -> TABLE(id_donativo uuid, estado character varying)
-- inscribir_en_programa(p_id_programa uuid, p_id_usuario uuid) -> jsonb
-- insert_full_dog(payload jsonb) -> jsonb
-- limpiar_datos_prueba() -> text  [SECURITY DEFINER]
-- perfiles_registrar_cliente(p_nombre text, p_apellidos text, p_correo text, p_hash text, p_telefono text, p_fecha_nacimiento date, p_calle text, p_numero_exterior integer, p_codigo_postal text, p_privacidad boolean) -> uuid
-- rls_auto_enable() -> event_trigger  [SECURITY DEFINER]
-- solicitudes_documento_es_mio(p_id_documento uuid) -> boolean  [SECURITY DEFINER]
-- solicitudes_es_admin() -> boolean  [SECURITY DEFINER]

-- ---------- 4) Definición de funciones ----------
CREATE OR REPLACE FUNCTION public.contar_inscritos(p_id_programa uuid)
 RETURNS integer
 LANGUAGE sql
 STABLE SECURITY DEFINER
 SET search_path TO ''
AS $function$
  SELECT count(*)::integer
  FROM public.se_inscribe
  WHERE id_programa = p_id_programa
    AND trim(estado) = 'Aceptado';
$function$
;

CREATE OR REPLACE FUNCTION public.donativos_registrar_monetario(p_id_usuario uuid, p_monto numeric, p_referencia character varying, p_comprobante bytea)
 RETURNS TABLE(id_donativo uuid, estado character varying)
 LANGUAGE sql
 SET search_path TO 'public'
AS $function$
  insert into public.donativo (id_usuario, tipo, monto, descripcion, comprobante)
  values (p_id_usuario, 'Monetario', p_monto, p_referencia, p_comprobante)
  returning id_donativo, estado;
$function$
;

CREATE OR REPLACE FUNCTION public.inscribir_en_programa(p_id_programa uuid, p_id_usuario uuid)
 RETURNS jsonb
 LANGUAGE plpgsql
 SET search_path TO 'public'
AS $function$
declare
  v_programa  record;
  v_inscritos integer;
  v_fila      record;
  -- Las fechas de la BD están en hora de México y sin zona
  v_ahora     timestamp := (now() at time zone 'America/Mexico_City');
begin
  -- 1. Bloquear el programa (las demás inscripciones a ESTE programa esperan)
  select id_programa, cupo_maximo, estado_del_programa, fecha_inicio
    into v_programa
    from programa
   where id_programa = p_id_programa
   for update;

  if not found then
    return jsonb_build_object('codigo', 'PROGRAMA_NO_ENCONTRADO');
  end if;

  -- 2. Programa disponible (estado nulo o sin fecha de inicio = no disponible)
  if coalesce(lower(trim(v_programa.estado_del_programa)), '') <> 'disponible'
     or v_programa.fecha_inicio is null then
    return jsonb_build_object('codigo', 'PROGRAMA_NO_DISPONIBLE');
  end if;

  -- 3. Programa no iniciado
  if v_programa.fecha_inicio <= v_ahora then
    return jsonb_build_object('codigo', 'PROGRAMA_INICIADO');
  end if;

  -- 4. Usuario no inscrito (solo cuenta si su inscripción está aceptada)
  if exists (
    select 1
      from se_inscribe
     where id_programa = p_id_programa
       and id_usuario  = p_id_usuario
       and trim(estado) = 'Aceptado'
  ) then
    return jsonb_build_object('codigo', 'YA_INSCRITO');
  end if;

  -- 5. Cupo (null = sin límite)
  if v_programa.cupo_maximo is not null then
    select count(*)
      into v_inscritos
      from se_inscribe
     where id_programa = p_id_programa
       and trim(estado) = 'Aceptado';

    if v_inscritos >= v_programa.cupo_maximo then
      return jsonb_build_object('codigo', 'SIN_CUPO');
    end if;
  end if;

  -- 6. Inscribir (si ya existía una fila cancelada, se reactiva)
  insert into se_inscribe (id_usuario, id_programa, fecha_inscripcion, estado)
  values (p_id_usuario, p_id_programa, v_ahora, 'Aceptado')
  on conflict (id_usuario, id_programa)
  do update set estado            = 'Aceptado',
                fecha_inscripcion = excluded.fecha_inscripcion,
                asistencia        = null
  returning id_programa, fecha_inscripcion, estado into v_fila;

  return jsonb_build_object(
    'codigo', 'OK',
    'id_programa', v_fila.id_programa,
    'fecha_inscripcion', v_fila.fecha_inscripcion,
    'estado', trim(v_fila.estado)
  );
end;
$function$
;

CREATE OR REPLACE FUNCTION public.insert_full_dog(payload jsonb)
 RETURNS jsonb
 LANGUAGE plpgsql
AS $function$
DECLARE
  v_id_perro UUID; 
  v_multimedia RECORD;
  v_caracter_id UUID; 
  v_cuidado_id UUID; 
  v_respuesta JSONB;
  
  -- Variables de diagnóstico para la excepción
  v_err_message TEXT;
  v_err_detail TEXT;
  v_err_hint TEXT;
  v_err_table TEXT;
  v_err_column TEXT;
  v_err_constraint TEXT;
BEGIN
  -- 1. Inserción en la tabla principal "perro"
  INSERT INTO perro (
    nombre, sexo, edad_aprox, tamano_medida, descripcion, 
    esterilizado, vacunado, estado, id_raza, id_tamano
  )
  VALUES (
    payload->>'nombre',
    payload->>'sexo',
    (payload->>'edad_aprox')::INT,
    (payload->>'tamano_medida')::NUMERIC,
    payload->>'descripcion',
    (payload->>'esterilizado')::BOOLEAN,
    (payload->>'vacunado')::BOOLEAN,
    payload->>'estado',
    (payload->>'id_raza')::UUID,   
    (payload->>'id_tamano')::UUID  
  )
  RETURNING id_perro INTO v_id_perro;

  -- 2. Inserción en la tabla "multimedia"
  IF payload ? 'multimedia' THEN
    FOR v_multimedia IN 
        SELECT * FROM jsonb_to_recordset(payload->'multimedia') AS x(url TEXT, tipo TEXT, fecha_subida TEXT) 
    LOOP
        INSERT INTO multimedia (id_perro, url, tipo, fecha_subida)
        VALUES (
            v_id_perro, 
            v_multimedia.url,
            v_multimedia.tipo,
            -- CORREGIDO: Si viene la fecha del frontend la casteamos a TIMESTAMP, si no, usamos la hora actual del sistema (NOW())
            CASE 
              WHEN v_multimedia.fecha_subida IS NOT NULL AND v_multimedia.fecha_subida != '' 
              THEN v_multimedia.fecha_subida::TIMESTAMP 
              ELSE NOW()::TIMESTAMP 
            END
        );
    END LOOP;
  END IF;

  -- 3. Inserción en la tabla "perro_tiene_caracter"
  IF payload ? 'caracteres' THEN
    FOR v_caracter_id IN 
        SELECT elemento::UUID FROM jsonb_array_elements_text(payload->'caracteres') AS elemento
    LOOP
        INSERT INTO perro_tiene_caracter (id_perro, id_caracter)
        VALUES (v_id_perro, v_caracter_id);
    END LOOP;
  END IF;

  -- 4. Inserción en la tabla "perro_tiene_cuidado_especial"
  IF payload ? 'cuidados' THEN
    FOR v_cuidado_id IN 
        SELECT elemento::UUID FROM jsonb_array_elements_text(payload->'cuidados') AS elemento
    LOOP
        INSERT INTO perro_tiene_cuidado_especial (id_perro, id_cuidado)
        VALUES (v_id_perro, v_cuidado_id);
    END LOOP;
  END IF;

  -- 5. Respuesta exitosa
  v_respuesta := jsonb_build_object(
    'id_perro', v_id_perro,
    'nombre', payload->>'nombre',
    'status', 'success'
  );
  
  RETURN v_respuesta;

EXCEPTION WHEN OTHERS THEN
  GET STACKED DIAGNOSTICS
    v_err_message := MESSAGE_TEXT,
    v_err_detail := PG_EXCEPTION_DETAIL,
    v_err_hint := PG_EXCEPTION_HINT,
    v_err_table := TABLE_NAME,
    v_err_column := COLUMN_NAME,
    v_err_constraint := CONSTRAINT_NAME;

  RAISE EXCEPTION 'DB_ERROR: % | Detalle: % | Tabla: % | Columna: % | Restriccion: %', 
    v_err_message, 
    COALESCE(v_err_detail, 'N/A'), 
    COALESCE(v_err_table, 'N/A'), 
    COALESCE(v_err_column, 'N/A'), 
    COALESCE(v_err_constraint, 'N/A');
END;
$function$
;

CREATE OR REPLACE FUNCTION public.limpiar_datos_prueba()
 RETURNS text
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
BEGIN

    -- Vaciar las 30 tablas de la base de datos.
    -- El orden permite visualizar las dependencias:
    -- primero las tablas relacionadas y después
    -- las tablas principales y los catálogos.

    TRUNCATE TABLE
        public.notifica,
        public.mensaje,
        public.donativo,
        public.se_inscribe,
        public.programa,
        public.tipo_programa,
        public.periodo_prueba,
        public.seguimiento,
        public.admin_revisa_solicitud_adopcion,
        public.documento_tiene_solicitud_adopcion,
        public.solicitud_adopcion,
        public.documento,
        public.estado_solicitud,
        public.perro_tiene_cuidado_especial,
        public.perro_tiene_caracter,
        public.multimedia,
        public.perro,
        public.cuidado_especial,
        public.caracter,
        public.tamano,
        public.raza,
        public.direcciones_fisicas,
        public.catalogo_de_cps,
        public.datos_confidenciales,
        public.admin,
        public.cliente,
        public.usuario,
        public.rol_tiene_privilegio,
        public.privilegio,
        public.rol
    RESTART IDENTITY;

    RETURN 'OK: todas las tablas fueron limpiadas correctamente.';

END;
$function$
;

CREATE OR REPLACE FUNCTION public.perfiles_registrar_cliente(p_nombre text, p_apellidos text, p_correo text, p_hash text, p_telefono text, p_fecha_nacimiento date, p_calle text, p_numero_exterior integer, p_codigo_postal text, p_privacidad boolean)
 RETURNS uuid
 LANGUAGE plpgsql
 SET search_path TO ''
AS $function$
DECLARE nuevo_id uuid;
BEGIN
  IF p_privacidad IS DISTINCT FROM true
    OR p_fecha_nacimiento IS NULL
    OR p_fecha_nacimiento > (CURRENT_DATE - interval '18 years')::date
    OR coalesce(p_hash, '') !~ '^[$]2[aby][$][0-9]{2}[$][./A-Za-z0-9]{53}$'
  THEN RAISE EXCEPTION 'Datos de registro inválidos' USING ERRCODE = '22023';
  END IF;
  INSERT INTO public.usuario (rol, nombre, apellidos, contrasena, fecha_nacimiento)
  VALUES ('Cliente', p_nombre, p_apellidos, p_hash, p_fecha_nacimiento)
  RETURNING id_usuario INTO nuevo_id;
  INSERT INTO public.cliente (id_usuario) VALUES (nuevo_id);
  INSERT INTO public.datos_confidenciales (id_usuario, correo, telefono, codigo_postal)
  VALUES (nuevo_id, lower(btrim(p_correo)), p_telefono, p_codigo_postal);
  INSERT INTO public.direcciones_fisicas (id_usuario, calle, numero_exterior)
  VALUES (nuevo_id, p_calle, p_numero_exterior);
  INSERT INTO public.perfiles_consentimiento (id_usuario, aviso_version)
  VALUES (nuevo_id, 'registro-web-v1');
  RETURN nuevo_id;
END;
$function$
;

CREATE OR REPLACE FUNCTION public.rls_auto_enable()
 RETURNS event_trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'pg_catalog'
AS $function$
DECLARE
  cmd record;
  tbl name;
BEGIN
  FOR cmd IN
    SELECT *
    FROM pg_event_trigger_ddl_commands()
    WHERE command_tag IN ('CREATE TABLE', 'CREATE TABLE AS', 'SELECT INTO')
      AND object_type IN ('table','partitioned table')
  LOOP
     IF cmd.schema_name IS NOT NULL AND cmd.schema_name IN ('public') AND cmd.schema_name NOT IN ('pg_catalog','information_schema') AND cmd.schema_name NOT LIKE 'pg_toast%' AND cmd.schema_name NOT LIKE 'pg_temp%' THEN
      BEGIN
        SELECT c.relname INTO tbl FROM pg_catalog.pg_class c WHERE c.oid = cmd.objid;
        EXECUTE format('alter table if exists %I.%I enable row level security', cmd.schema_name, tbl);
        RAISE LOG 'rls_auto_enable: enabled RLS on %', cmd.object_identity;
      EXCEPTION
        WHEN OTHERS THEN
          RAISE LOG 'rls_auto_enable: failed to enable RLS on %', cmd.object_identity;
      END;
     ELSE
        RAISE LOG 'rls_auto_enable: skip % (either system schema or not in enforced list: %.)', cmd.object_identity, cmd.schema_name;
     END IF;
  END LOOP;
END;
$function$
;

CREATE OR REPLACE FUNCTION public.solicitudes_documento_es_mio(p_id_documento uuid)
 RETURNS boolean
 LANGUAGE sql
 STABLE SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
  select exists (
    select 1 from public.documento d
    where d.id_documento = p_id_documento
      and d.id_usuario = (select auth.uid())
  );
$function$
;

CREATE OR REPLACE FUNCTION public.solicitudes_es_admin()
 RETURNS boolean
 LANGUAGE sql
 STABLE SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
  select exists (
    select 1 from public.usuario u
    where u.id_usuario = (select auth.uid())
      and u.rol in ('Administrador', 'Root')
  );
$function$
;

