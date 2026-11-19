--
-- PostgreSQL database dump
--


-- Dumped from database version 17.11
-- Dumped by pg_dump version 17.12

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA IF NOT EXISTS public;


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: contar_inscritos(uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.contar_inscritos(p_id_programa uuid) RETURNS integer
    LANGUAGE sql STABLE SECURITY DEFINER
    SET search_path TO ''
    AS $$
  SELECT count(*)::integer
  FROM public.se_inscribe
  WHERE id_programa = p_id_programa
    AND trim(estado) = 'Aceptado';
$$;


--
-- Name: donativos_registrar_monetario(uuid, numeric, character varying, bytea); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.donativos_registrar_monetario(p_id_usuario uuid, p_monto numeric, p_referencia character varying, p_comprobante bytea) RETURNS TABLE(id_donativo uuid, estado character varying)
    LANGUAGE sql
    SET search_path TO 'public'
    AS $$
  insert into public.donativo (id_usuario, tipo, monto, descripcion, comprobante)
  values (p_id_usuario, 'Monetario', p_monto, p_referencia, p_comprobante)
  returning id_donativo, estado;
$$;


--
-- Name: inscribir_en_programa(uuid, uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.inscribir_en_programa(p_id_programa uuid, p_id_usuario uuid) RETURNS jsonb
    LANGUAGE plpgsql
    SET search_path TO 'public'
    AS $$
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
$$;


--
-- Name: insert_full_dog(jsonb); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.insert_full_dog(payload jsonb) RETURNS jsonb
    LANGUAGE plpgsql
    AS $$
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
$$;


--
-- Name: limpiar_datos_prueba(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.limpiar_datos_prueba() RETURNS text
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path TO ''
    AS $$
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
$$;


--
-- Name: perfiles_registrar_cliente(text, text, text, text, text, date, text, integer, text, boolean); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.perfiles_registrar_cliente(p_nombre text, p_apellidos text, p_correo text, p_hash text, p_telefono text, p_fecha_nacimiento date, p_calle text, p_numero_exterior integer, p_codigo_postal text, p_privacidad boolean) RETURNS uuid
    LANGUAGE plpgsql
    SET search_path TO ''
    AS $_$
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
$_$;


--
-- Name: rls_auto_enable(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.rls_auto_enable() RETURNS event_trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path TO 'pg_catalog'
    AS $$
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
$$;


--
-- Name: solicitudes_documento_es_mio(uuid); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.solicitudes_documento_es_mio(p_id_documento uuid) RETURNS boolean
    LANGUAGE sql STABLE SECURITY DEFINER
    SET search_path TO 'public'
    AS $$
  select exists (
    select 1 from public.documento d
    where d.id_documento = p_id_documento
      and d.id_usuario = (select auth.uid())
  );
$$;


--
-- Name: solicitudes_es_admin(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.solicitudes_es_admin() RETURNS boolean
    LANGUAGE sql STABLE SECURITY DEFINER
    SET search_path TO 'public'
    AS $$
  select exists (
    select 1 from public.usuario u
    where u.id_usuario = (select auth.uid())
      and u.rol in ('Administrador', 'Root')
  );
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: admin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.admin (
    id_usuario uuid NOT NULL
);


--
-- Name: admin_revisa_solicitud_adopcion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.admin_revisa_solicitud_adopcion (
    id_usuario uuid NOT NULL,
    id_solicitud uuid NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: caracter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.caracter (
    id_caracter uuid DEFAULT gen_random_uuid() NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion character varying(255)
);


--
-- Name: catalogo_de_cps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.catalogo_de_cps (
    id_codigo_postal uuid DEFAULT gen_random_uuid() NOT NULL,
    codigo_postal character(5) NOT NULL,
    colonia character varying(100),
    municipio character varying(100),
    estado character varying(50),
    pais character varying(50) DEFAULT 'México'::character varying
);


--
-- Name: cliente; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cliente (
    id_usuario uuid NOT NULL
);


--
-- Name: cuidado_especial; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cuidado_especial (
    id_cuidado uuid DEFAULT gen_random_uuid() NOT NULL,
    descripcion character varying(1000) NOT NULL
);


--
-- Name: datos_confidenciales; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.datos_confidenciales (
    id_usuario uuid NOT NULL,
    rfc character varying(13),
    razon_social character varying(150),
    regimen_fiscal character varying(3),
    codigo_postal character varying(5),
    correo character varying(100) NOT NULL,
    correo_facturacion character varying(100),
    telefono character varying(15),
    constancia_url character varying(255)
);


--
-- Name: direcciones_fisicas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.direcciones_fisicas (
    id_direccion uuid DEFAULT gen_random_uuid() NOT NULL,
    id_usuario uuid NOT NULL,
    id_codigo_postal uuid,
    calle character varying(100),
    numero_exterior integer,
    numero_interior integer
);


--
-- Name: documento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.documento (
    id_documento uuid DEFAULT gen_random_uuid() NOT NULL,
    id_usuario uuid NOT NULL,
    tipo character(10) NOT NULL,
    url_archivo text NOT NULL,
    fecha_subida timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado_revision character(10) DEFAULT 'Pendiente'::bpchar,
    comentario character varying(1000)
);


--
-- Name: documento_tiene_solicitud_adopcion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.documento_tiene_solicitud_adopcion (
    id_documento uuid NOT NULL,
    id_solicitud uuid NOT NULL,
    fecha_agregado timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: donativo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.donativo (
    id_donativo uuid DEFAULT gen_random_uuid() NOT NULL,
    id_usuario uuid NOT NULL,
    id_admin uuid,
    id_documento uuid,
    folio_fiscal character(40),
    tipo character(20) NOT NULL,
    monto numeric(10,2),
    descripcion character varying(1000),
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    comprobante bytea,
    estado character varying(30) DEFAULT 'Pendiente de validación'::character varying,
    cfdi_solicitado boolean DEFAULT false,
    comentario_admin character varying(1000)
);


--
-- Name: estado_solicitud; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.estado_solicitud (
    id_estado uuid DEFAULT gen_random_uuid() NOT NULL,
    descripcion character varying(50) NOT NULL
);


--
-- Name: mensaje; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mensaje (
    id_mensaje uuid DEFAULT gen_random_uuid() NOT NULL,
    titulo character varying(100) NOT NULL,
    texto character varying(500) NOT NULL,
    tipo character(30)
);


--
-- Name: multimedia; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.multimedia (
    id_multimedia uuid DEFAULT gen_random_uuid() NOT NULL,
    id_perro uuid NOT NULL,
    url character varying(1000) NOT NULL,
    tipo character(10),
    fecha_subida timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: notifica; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifica (
    id_cliente uuid NOT NULL,
    id_mensaje uuid NOT NULL,
    fecha_envio timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    leido boolean DEFAULT false
);


--
-- Name: perfiles_consentimiento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.perfiles_consentimiento (
    id_usuario uuid NOT NULL,
    aviso_version text NOT NULL,
    aceptado_en timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: periodo_prueba; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.periodo_prueba (
    id_prueba uuid DEFAULT gen_random_uuid() NOT NULL,
    id_solicitud uuid NOT NULL,
    fecha_inicio timestamp without time zone NOT NULL,
    fecha_fin_prevista timestamp without time zone,
    fecha_fin_real timestamp without time zone,
    estatus_prueba character(20) DEFAULT 'En curso'::bpchar,
    observaciones_seguimiento character varying(1000)
);


--
-- Name: perro; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.perro (
    id_perro uuid DEFAULT gen_random_uuid() NOT NULL,
    id_raza uuid NOT NULL,
    id_tamano uuid NOT NULL,
    nombre character varying(100) NOT NULL,
    sexo character(10),
    edad_aprox integer,
    tamano_medida double precision,
    descripcion character varying(1000),
    esterilizado boolean DEFAULT false,
    vacunado boolean DEFAULT false,
    estado character varying(1000) DEFAULT 'Disponible'::character varying,
    fecha_ingreso timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    activo boolean DEFAULT true NOT NULL
);


--
-- Name: perro_tiene_caracter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.perro_tiene_caracter (
    id_perro uuid NOT NULL,
    id_caracter uuid NOT NULL
);


--
-- Name: perro_tiene_cuidado_especial; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.perro_tiene_cuidado_especial (
    id_perro uuid NOT NULL,
    id_cuidado uuid NOT NULL
);


--
-- Name: privilegio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.privilegio (
    privilegio character varying(50) NOT NULL
);


--
-- Name: programa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.programa (
    id_programa uuid DEFAULT gen_random_uuid() NOT NULL,
    tipo character varying(36) NOT NULL,
    id_admin uuid NOT NULL,
    nombre character varying(50) NOT NULL,
    estado_del_programa character(20) DEFAULT 'Disponible'::bpchar,
    descripcion character varying(1000),
    fecha_inicio timestamp without time zone NOT NULL,
    fecha_fin timestamp without time zone NOT NULL,
    ubicacion character varying(150),
    cupo_maximo integer,
    motivo_cancelacion character varying(200)
);


--
-- Name: COLUMN programa.motivo_cancelacion; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.programa.motivo_cancelacion IS 'Motivo opcional que escribe el Admin al cancelar el programa (HU38).';


--
-- Name: raza; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.raza (
    id_raza uuid DEFAULT gen_random_uuid() NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion character varying(255)
);


--
-- Name: rol; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rol (
    nombre character varying(36) NOT NULL,
    descripcion character varying(200),
    activo boolean DEFAULT true
);


--
-- Name: se_inscribe; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.se_inscribe (
    id_usuario uuid NOT NULL,
    id_programa uuid NOT NULL,
    fecha_inscripcion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado character(20) DEFAULT 'Aceptado'::bpchar,
    asistencia boolean
);


--
-- Name: seguimiento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.seguimiento (
    id_seguimiento uuid DEFAULT gen_random_uuid() NOT NULL,
    id_solicitud uuid NOT NULL,
    fecha_inicio timestamp without time zone NOT NULL,
    fecha_fin timestamp without time zone,
    descripcion character varying(1000)
);


--
-- Name: solicitud_adopcion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.solicitud_adopcion (
    id_solicitud uuid DEFAULT gen_random_uuid() NOT NULL,
    id_usuario uuid NOT NULL,
    id_perro uuid NOT NULL,
    id_estado uuid NOT NULL,
    fecha_solicitud timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    motivo character varying(100),
    tipo_vivienda character varying,
    otras_mascotas boolean,
    integrantes_hogar integer,
    comentario_admin character varying(1000),
    fecha_resolucion timestamp without time zone,
    ocupacion character varying,
    sobre_mascotas character varying,
    ha_tenido_mascotas boolean,
    donde_vivira character varying(100),
    todos_de_acuerdo boolean,
    jardin_o_patio character varying(50),
    casero_acepta boolean,
    cuidados_basicos character varying(250),
    opinion_esterilizacion character varying(250),
    tiene_veterinario boolean,
    detalle_veterinario character varying(250),
    tiempo_solo character varying(250),
    rutina_diaria character varying(250),
    manejo_comportamiento character varying(250),
    puede_editar boolean DEFAULT false NOT NULL
);


--
-- Name: solicitud_cfdi; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.solicitud_cfdi (
    id_solicitud_cfdi uuid DEFAULT gen_random_uuid() NOT NULL,
    id_donativo uuid NOT NULL,
    estado text DEFAULT 'Pendiente de atención'::text NOT NULL,
    fecha_solicitud timestamp with time zone DEFAULT now() NOT NULL,
    cfdi_uuid text,
    rfc_receptor text,
    monto_cfdi numeric(14,2),
    pdf_storage_path text,
    xml_storage_path text,
    fecha_carga_cfdi timestamp with time zone,
    id_administrador_carga uuid,
    correo_estado text DEFAULT 'Pendiente de envío'::text NOT NULL,
    correo_intentos integer DEFAULT 0 NOT NULL,
    correo_enviado_en timestamp with time zone,
    correo_error text
);


--
-- Name: TABLE solicitud_cfdi; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.solicitud_cfdi IS 'Solicitudes de CFDI asociadas a donativos; solo se consultan desde la API administrativa.';


--
-- Name: COLUMN solicitud_cfdi.cfdi_uuid; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.solicitud_cfdi.cfdi_uuid IS 'UUID del CFDI validado contra el XML original.';


--
-- Name: COLUMN solicitud_cfdi.pdf_storage_path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.solicitud_cfdi.pdf_storage_path IS 'Ruta privada del PDF del CFDI en Supabase Storage.';


--
-- Name: COLUMN solicitud_cfdi.xml_storage_path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.solicitud_cfdi.xml_storage_path IS 'Ruta privada del XML del CFDI en Supabase Storage.';


--
-- Name: COLUMN solicitud_cfdi.correo_estado; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.solicitud_cfdi.correo_estado IS 'Estado separado de almacenamiento: Pendiente de envío, Enviando, Error o Enviado.';


--
-- Name: tamano; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tamano (
    id_tamano uuid DEFAULT gen_random_uuid() NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion character varying(255)
);


--
-- Name: tipo_programa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipo_programa (
    nombre character varying(36) NOT NULL,
    descripcion character varying(200)
);


--
-- Name: usuario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuario (
    id_usuario uuid DEFAULT gen_random_uuid() NOT NULL,
    rol character varying(36) NOT NULL,
    nombre character varying(50) NOT NULL,
    apellidos character varying(50) NOT NULL,
    contrasena character varying(64) NOT NULL,
    fecha_nacimiento timestamp without time zone,
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    aviso_privacidad_aceptado boolean DEFAULT false NOT NULL
);


--
-- Name: admin admin_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin
    ADD CONSTRAINT admin_pkey PRIMARY KEY (id_usuario);


--
-- Name: admin_revisa_solicitud_adopcion admin_revisa_solicitud_adopcion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin_revisa_solicitud_adopcion
    ADD CONSTRAINT admin_revisa_solicitud_adopcion_pkey PRIMARY KEY (id_usuario, id_solicitud);


--
-- Name: caracter caracter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.caracter
    ADD CONSTRAINT caracter_pkey PRIMARY KEY (id_caracter);


--
-- Name: catalogo_de_cps catalogo_de_cps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.catalogo_de_cps
    ADD CONSTRAINT catalogo_de_cps_pkey PRIMARY KEY (id_codigo_postal);


--
-- Name: cliente cliente_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_pkey PRIMARY KEY (id_usuario);


--
-- Name: cuidado_especial cuidado_especial_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cuidado_especial
    ADD CONSTRAINT cuidado_especial_pkey PRIMARY KEY (id_cuidado);


--
-- Name: datos_confidenciales datos_confidenciales_correo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.datos_confidenciales
    ADD CONSTRAINT datos_confidenciales_correo_key UNIQUE (correo);


--
-- Name: datos_confidenciales datos_confidenciales_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.datos_confidenciales
    ADD CONSTRAINT datos_confidenciales_pkey PRIMARY KEY (id_usuario);


--
-- Name: direcciones_fisicas direcciones_fisicas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.direcciones_fisicas
    ADD CONSTRAINT direcciones_fisicas_pkey PRIMARY KEY (id_direccion);


--
-- Name: documento documento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.documento
    ADD CONSTRAINT documento_pkey PRIMARY KEY (id_documento);


--
-- Name: documento_tiene_solicitud_adopcion documento_tiene_solicitud_adopcion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.documento_tiene_solicitud_adopcion
    ADD CONSTRAINT documento_tiene_solicitud_adopcion_pkey PRIMARY KEY (id_documento, id_solicitud);


--
-- Name: donativo donativo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.donativo
    ADD CONSTRAINT donativo_pkey PRIMARY KEY (id_donativo);


--
-- Name: estado_solicitud estado_solicitud_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estado_solicitud
    ADD CONSTRAINT estado_solicitud_pkey PRIMARY KEY (id_estado);


--
-- Name: mensaje mensaje_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mensaje
    ADD CONSTRAINT mensaje_pkey PRIMARY KEY (id_mensaje);


--
-- Name: multimedia multimedia_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.multimedia
    ADD CONSTRAINT multimedia_pkey PRIMARY KEY (id_multimedia);


--
-- Name: notifica notifica_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifica
    ADD CONSTRAINT notifica_pkey PRIMARY KEY (id_cliente, id_mensaje);


--
-- Name: perfiles_consentimiento perfiles_consentimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perfiles_consentimiento
    ADD CONSTRAINT perfiles_consentimiento_pkey PRIMARY KEY (id_usuario);


--
-- Name: periodo_prueba periodo_prueba_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.periodo_prueba
    ADD CONSTRAINT periodo_prueba_pkey PRIMARY KEY (id_prueba);


--
-- Name: perro perro_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perro
    ADD CONSTRAINT perro_pkey PRIMARY KEY (id_perro);


--
-- Name: perro_tiene_caracter perro_tiene_caracter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perro_tiene_caracter
    ADD CONSTRAINT perro_tiene_caracter_pkey PRIMARY KEY (id_perro, id_caracter);


--
-- Name: perro_tiene_cuidado_especial perro_tiene_cuidado_especial_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perro_tiene_cuidado_especial
    ADD CONSTRAINT perro_tiene_cuidado_especial_pkey PRIMARY KEY (id_perro, id_cuidado);


--
-- Name: privilegio privilegio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.privilegio
    ADD CONSTRAINT privilegio_pkey PRIMARY KEY (privilegio);


--
-- Name: programa programa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.programa
    ADD CONSTRAINT programa_pkey PRIMARY KEY (id_programa);


--
-- Name: raza raza_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.raza
    ADD CONSTRAINT raza_pkey PRIMARY KEY (id_raza);


--
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (nombre);


--
-- Name: se_inscribe se_inscribe_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.se_inscribe
    ADD CONSTRAINT se_inscribe_pkey PRIMARY KEY (id_usuario, id_programa);


--
-- Name: seguimiento seguimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_pkey PRIMARY KEY (id_seguimiento);


--
-- Name: solicitud_adopcion solicitud_adopcion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitud_adopcion
    ADD CONSTRAINT solicitud_adopcion_pkey PRIMARY KEY (id_solicitud);


--
-- Name: solicitud_cfdi solicitud_cfdi_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitud_cfdi
    ADD CONSTRAINT solicitud_cfdi_pkey PRIMARY KEY (id_solicitud_cfdi);


--
-- Name: solicitud_cfdi solicitud_cfdi_un_donativo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitud_cfdi
    ADD CONSTRAINT solicitud_cfdi_un_donativo UNIQUE (id_donativo);


--
-- Name: tamano tamano_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tamano
    ADD CONSTRAINT tamano_pkey PRIMARY KEY (id_tamano);


--
-- Name: tipo_programa tipo_programa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_programa
    ADD CONSTRAINT tipo_programa_pkey PRIMARY KEY (nombre);


--
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- Name: perfiles_correo_normalizado_uniq; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX perfiles_correo_normalizado_uniq ON public.datos_confidenciales USING btree (lower(btrim((correo)::text)));


--
-- Name: solicitud_cfdi_correo_pendiente_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX solicitud_cfdi_correo_pendiente_idx ON public.solicitud_cfdi USING btree (correo_estado, fecha_carga_cfdi) WHERE (correo_estado <> 'Enviado'::text);


--
-- Name: solicitud_cfdi_estado_fecha_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX solicitud_cfdi_estado_fecha_idx ON public.solicitud_cfdi USING btree (estado, fecha_solicitud);


--
-- Name: solicitud_cfdi_uuid_unico_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX solicitud_cfdi_uuid_unico_idx ON public.solicitud_cfdi USING btree (cfdi_uuid) WHERE (cfdi_uuid IS NOT NULL);


--
-- Name: admin admin_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin
    ADD CONSTRAINT admin_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: admin_revisa_solicitud_adopcion admin_revisa_solicitud_adopcion_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin_revisa_solicitud_adopcion
    ADD CONSTRAINT admin_revisa_solicitud_adopcion_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitud_adopcion(id_solicitud) ON DELETE CASCADE;


--
-- Name: admin_revisa_solicitud_adopcion admin_revisa_solicitud_adopcion_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin_revisa_solicitud_adopcion
    ADD CONSTRAINT admin_revisa_solicitud_adopcion_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.admin(id_usuario) ON DELETE CASCADE;


--
-- Name: cliente cliente_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: datos_confidenciales datos_confidenciales_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.datos_confidenciales
    ADD CONSTRAINT datos_confidenciales_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.cliente(id_usuario) ON DELETE CASCADE;


--
-- Name: datos_confidenciales datos_confidenciales_id_usuario_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.datos_confidenciales
    ADD CONSTRAINT datos_confidenciales_id_usuario_fkey1 FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- Name: direcciones_fisicas direcciones_fisicas_id_codigo_postal_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.direcciones_fisicas
    ADD CONSTRAINT direcciones_fisicas_id_codigo_postal_fkey FOREIGN KEY (id_codigo_postal) REFERENCES public.catalogo_de_cps(id_codigo_postal) ON DELETE SET NULL;


--
-- Name: direcciones_fisicas direcciones_fisicas_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.direcciones_fisicas
    ADD CONSTRAINT direcciones_fisicas_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: documento documento_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.documento
    ADD CONSTRAINT documento_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: documento_tiene_solicitud_adopcion documento_tiene_solicitud_adopcion_id_documento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.documento_tiene_solicitud_adopcion
    ADD CONSTRAINT documento_tiene_solicitud_adopcion_id_documento_fkey FOREIGN KEY (id_documento) REFERENCES public.documento(id_documento) ON DELETE CASCADE;


--
-- Name: documento_tiene_solicitud_adopcion documento_tiene_solicitud_adopcion_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.documento_tiene_solicitud_adopcion
    ADD CONSTRAINT documento_tiene_solicitud_adopcion_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitud_adopcion(id_solicitud) ON DELETE CASCADE;


--
-- Name: donativo donativo_id_admin_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.donativo
    ADD CONSTRAINT donativo_id_admin_fkey FOREIGN KEY (id_admin) REFERENCES public.admin(id_usuario) ON DELETE SET NULL;


--
-- Name: donativo donativo_id_documento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.donativo
    ADD CONSTRAINT donativo_id_documento_fkey FOREIGN KEY (id_documento) REFERENCES public.documento(id_documento) ON DELETE SET NULL;


--
-- Name: donativo donativo_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.donativo
    ADD CONSTRAINT donativo_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: multimedia multimedia_id_perro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.multimedia
    ADD CONSTRAINT multimedia_id_perro_fkey FOREIGN KEY (id_perro) REFERENCES public.perro(id_perro) ON DELETE CASCADE;


--
-- Name: notifica notifica_id_cliente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifica
    ADD CONSTRAINT notifica_id_cliente_fkey FOREIGN KEY (id_cliente) REFERENCES public.cliente(id_usuario) ON DELETE CASCADE;


--
-- Name: notifica notifica_id_mensaje_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifica
    ADD CONSTRAINT notifica_id_mensaje_fkey FOREIGN KEY (id_mensaje) REFERENCES public.mensaje(id_mensaje) ON DELETE CASCADE;


--
-- Name: perfiles_consentimiento perfiles_consentimiento_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perfiles_consentimiento
    ADD CONSTRAINT perfiles_consentimiento_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: periodo_prueba periodo_prueba_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.periodo_prueba
    ADD CONSTRAINT periodo_prueba_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitud_adopcion(id_solicitud) ON DELETE CASCADE;


--
-- Name: perro perro_id_raza_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perro
    ADD CONSTRAINT perro_id_raza_fkey FOREIGN KEY (id_raza) REFERENCES public.raza(id_raza);


--
-- Name: perro perro_id_tamano_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perro
    ADD CONSTRAINT perro_id_tamano_fkey FOREIGN KEY (id_tamano) REFERENCES public.tamano(id_tamano);


--
-- Name: perro_tiene_caracter perro_tiene_caracter_id_caracter_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perro_tiene_caracter
    ADD CONSTRAINT perro_tiene_caracter_id_caracter_fkey FOREIGN KEY (id_caracter) REFERENCES public.caracter(id_caracter) ON DELETE CASCADE;


--
-- Name: perro_tiene_caracter perro_tiene_caracter_id_perro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perro_tiene_caracter
    ADD CONSTRAINT perro_tiene_caracter_id_perro_fkey FOREIGN KEY (id_perro) REFERENCES public.perro(id_perro) ON DELETE CASCADE;


--
-- Name: perro_tiene_cuidado_especial perro_tiene_cuidado_especial_id_cuidado_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perro_tiene_cuidado_especial
    ADD CONSTRAINT perro_tiene_cuidado_especial_id_cuidado_fkey FOREIGN KEY (id_cuidado) REFERENCES public.cuidado_especial(id_cuidado) ON DELETE CASCADE;


--
-- Name: perro_tiene_cuidado_especial perro_tiene_cuidado_especial_id_perro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.perro_tiene_cuidado_especial
    ADD CONSTRAINT perro_tiene_cuidado_especial_id_perro_fkey FOREIGN KEY (id_perro) REFERENCES public.perro(id_perro) ON DELETE CASCADE;


--
-- Name: programa programa_id_admin_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.programa
    ADD CONSTRAINT programa_id_admin_fkey FOREIGN KEY (id_admin) REFERENCES public.admin(id_usuario);


--
-- Name: programa programa_tipo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.programa
    ADD CONSTRAINT programa_tipo_fkey FOREIGN KEY (tipo) REFERENCES public.tipo_programa(nombre);


--
-- Name: se_inscribe se_inscribe_id_programa_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.se_inscribe
    ADD CONSTRAINT se_inscribe_id_programa_fkey FOREIGN KEY (id_programa) REFERENCES public.programa(id_programa) ON DELETE CASCADE;


--
-- Name: se_inscribe se_inscribe_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.se_inscribe
    ADD CONSTRAINT se_inscribe_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: seguimiento seguimiento_id_solicitud_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seguimiento
    ADD CONSTRAINT seguimiento_id_solicitud_fkey FOREIGN KEY (id_solicitud) REFERENCES public.solicitud_adopcion(id_solicitud) ON DELETE CASCADE;


--
-- Name: solicitud_adopcion solicitud_adopcion_id_estado_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitud_adopcion
    ADD CONSTRAINT solicitud_adopcion_id_estado_fkey FOREIGN KEY (id_estado) REFERENCES public.estado_solicitud(id_estado);


--
-- Name: solicitud_adopcion solicitud_adopcion_id_perro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitud_adopcion
    ADD CONSTRAINT solicitud_adopcion_id_perro_fkey FOREIGN KEY (id_perro) REFERENCES public.perro(id_perro) ON DELETE CASCADE;


--
-- Name: solicitud_adopcion solicitud_adopcion_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitud_adopcion
    ADD CONSTRAINT solicitud_adopcion_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: solicitud_cfdi solicitud_cfdi_id_administrador_carga_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitud_cfdi
    ADD CONSTRAINT solicitud_cfdi_id_administrador_carga_fkey FOREIGN KEY (id_administrador_carga) REFERENCES public.usuario(id_usuario) ON DELETE SET NULL;


--
-- Name: solicitud_cfdi solicitud_cfdi_id_donativo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitud_cfdi
    ADD CONSTRAINT solicitud_cfdi_id_donativo_fkey FOREIGN KEY (id_donativo) REFERENCES public.donativo(id_donativo) ON DELETE RESTRICT;


--
-- Name: usuario usuario_rol_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_rol_fkey FOREIGN KEY (rol) REFERENCES public.rol(nombre);


--
-- Name: cuidado_especial Enable read access for all users; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Enable read access for all users" ON public.cuidado_especial FOR SELECT USING (true);


--
-- Name: donativo HU44_lectura_donativos_prueba; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "HU44_lectura_donativos_prueba" ON public.donativo FOR SELECT TO authenticated USING ((( SELECT auth.uid() AS uid) = '7f630a30-0696-4fb6-9975-f5bca4874abc'::uuid));


--
-- Name: usuario HU44_lectura_usuarios_prueba; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "HU44_lectura_usuarios_prueba" ON public.usuario FOR SELECT TO authenticated USING ((auth.uid() = '7f630a30-0696-4fb6-9975-f5bca4874abc'::uuid));


--
-- Name: admin; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.admin ENABLE ROW LEVEL SECURITY;

--
-- Name: admin_revisa_solicitud_adopcion; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.admin_revisa_solicitud_adopcion ENABLE ROW LEVEL SECURITY;

--
-- Name: caracter; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.caracter ENABLE ROW LEVEL SECURITY;

--
-- Name: catalogo_de_cps; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.catalogo_de_cps ENABLE ROW LEVEL SECURITY;

--
-- Name: cliente; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.cliente ENABLE ROW LEVEL SECURITY;

--
-- Name: cuidado_especial; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.cuidado_especial ENABLE ROW LEVEL SECURITY;

--
-- Name: datos_confidenciales; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.datos_confidenciales ENABLE ROW LEVEL SECURITY;

--
-- Name: direcciones_fisicas; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.direcciones_fisicas ENABLE ROW LEVEL SECURITY;

--
-- Name: documento; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.documento ENABLE ROW LEVEL SECURITY;

--
-- Name: documento_tiene_solicitud_adopcion; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.documento_tiene_solicitud_adopcion ENABLE ROW LEVEL SECURITY;

--
-- Name: donativo; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.donativo ENABLE ROW LEVEL SECURITY;

--
-- Name: caracter eliminar_caracter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY eliminar_caracter ON public.caracter FOR DELETE TO authenticated USING (true);


--
-- Name: multimedia eliminar_multimedia; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY eliminar_multimedia ON public.multimedia FOR DELETE TO authenticated USING (true);


--
-- Name: perro eliminar_perro; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY eliminar_perro ON public.perro FOR DELETE TO authenticated USING (true);


--
-- Name: perro_tiene_caracter eliminar_perro_tiene_caracter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY eliminar_perro_tiene_caracter ON public.perro_tiene_caracter FOR DELETE TO authenticated USING (true);


--
-- Name: raza eliminar_raza; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY eliminar_raza ON public.raza FOR DELETE TO authenticated USING (true);


--
-- Name: tamano eliminar_tamano; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY eliminar_tamano ON public.tamano FOR DELETE TO authenticated USING (true);


--
-- Name: estado_solicitud; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.estado_solicitud ENABLE ROW LEVEL SECURITY;

--
-- Name: mensaje; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.mensaje ENABLE ROW LEVEL SECURITY;

--
-- Name: caracter modificar_caracter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_caracter ON public.caracter FOR UPDATE TO authenticated USING (true) WITH CHECK (true);


--
-- Name: documento_tiene_solicitud_adopcion modificar_documento_tiene_solicitud_adopcion; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_documento_tiene_solicitud_adopcion ON public.documento_tiene_solicitud_adopcion FOR UPDATE TO authenticated USING ((EXISTS ( SELECT 1
   FROM public.solicitud_adopcion s
  WHERE ((s.id_solicitud = documento_tiene_solicitud_adopcion.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid)))))) WITH CHECK (((EXISTS ( SELECT 1
   FROM public.solicitud_adopcion s
  WHERE ((s.id_solicitud = documento_tiene_solicitud_adopcion.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))) AND public.solicitudes_documento_es_mio(id_documento)));


--
-- Name: multimedia modificar_multimedia; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_multimedia ON public.multimedia FOR UPDATE TO authenticated USING (true) WITH CHECK (true);


--
-- Name: periodo_prueba modificar_periodo_prueba; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_periodo_prueba ON public.periodo_prueba FOR UPDATE TO authenticated USING (( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin)) WITH CHECK (( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin));


--
-- Name: perro modificar_perro; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_perro ON public.perro FOR UPDATE TO authenticated USING (true) WITH CHECK (true);


--
-- Name: perro_tiene_caracter modificar_perro_tiene_caracter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_perro_tiene_caracter ON public.perro_tiene_caracter FOR UPDATE TO authenticated USING (true) WITH CHECK (true);


--
-- Name: raza modificar_raza; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_raza ON public.raza FOR UPDATE TO authenticated USING (true) WITH CHECK (true);


--
-- Name: seguimiento modificar_seguimiento; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_seguimiento ON public.seguimiento FOR UPDATE TO authenticated USING (( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin)) WITH CHECK (( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin));


--
-- Name: solicitud_adopcion modificar_solicitud_adopcion; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_solicitud_adopcion ON public.solicitud_adopcion FOR UPDATE TO authenticated USING (((id_usuario = ( SELECT auth.uid() AS uid)) AND (id_estado = ( SELECT estado_solicitud.id_estado
   FROM public.estado_solicitud
  WHERE ((estado_solicitud.descripcion)::text = 'Pendiente de Revisión'::text))))) WITH CHECK (((id_usuario = ( SELECT auth.uid() AS uid)) AND (id_estado = ( SELECT estado_solicitud.id_estado
   FROM public.estado_solicitud
  WHERE ((estado_solicitud.descripcion)::text = 'Pendiente de Revisión'::text)))));


--
-- Name: solicitud_adopcion modificar_solicitud_adopcion_admin; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_solicitud_adopcion_admin ON public.solicitud_adopcion FOR UPDATE TO authenticated USING (( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin)) WITH CHECK (( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin));


--
-- Name: tamano modificar_tamano; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY modificar_tamano ON public.tamano FOR UPDATE TO authenticated USING (true) WITH CHECK (true);


--
-- Name: multimedia; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.multimedia ENABLE ROW LEVEL SECURITY;

--
-- Name: notifica; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.notifica ENABLE ROW LEVEL SECURITY;

--
-- Name: perfiles_consentimiento; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.perfiles_consentimiento ENABLE ROW LEVEL SECURITY;

--
-- Name: periodo_prueba; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.periodo_prueba ENABLE ROW LEVEL SECURITY;

--
-- Name: perro; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.perro ENABLE ROW LEVEL SECURITY;

--
-- Name: perro_tiene_caracter; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.perro_tiene_caracter ENABLE ROW LEVEL SECURITY;

--
-- Name: perro_tiene_cuidado_especial; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.perro_tiene_cuidado_especial ENABLE ROW LEVEL SECURITY;

--
-- Name: privilegio; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.privilegio ENABLE ROW LEVEL SECURITY;

--
-- Name: programa; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.programa ENABLE ROW LEVEL SECURITY;

--
-- Name: programa programa_select_usuarios; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY programa_select_usuarios ON public.programa FOR SELECT TO authenticated USING (true);


--
-- Name: raza; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.raza ENABLE ROW LEVEL SECURITY;

--
-- Name: admin_revisa_solicitud_adopcion registrar_admin_revisa_solicitud_adopcion; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_admin_revisa_solicitud_adopcion ON public.admin_revisa_solicitud_adopcion FOR INSERT TO authenticated WITH CHECK ((( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin) AND (id_usuario = ( SELECT auth.uid() AS uid))));


--
-- Name: caracter registrar_caracter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_caracter ON public.caracter FOR INSERT TO authenticated WITH CHECK (true);


--
-- Name: documento_tiene_solicitud_adopcion registrar_documento_tiene_solicitud_adopcion; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_documento_tiene_solicitud_adopcion ON public.documento_tiene_solicitud_adopcion FOR INSERT TO authenticated WITH CHECK (((EXISTS ( SELECT 1
   FROM public.solicitud_adopcion s
  WHERE ((s.id_solicitud = documento_tiene_solicitud_adopcion.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))) AND public.solicitudes_documento_es_mio(id_documento)));


--
-- Name: multimedia registrar_multimedia; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_multimedia ON public.multimedia FOR INSERT TO authenticated WITH CHECK (true);


--
-- Name: periodo_prueba registrar_periodo_prueba; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_periodo_prueba ON public.periodo_prueba FOR INSERT TO authenticated WITH CHECK (( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin));


--
-- Name: perro registrar_perro; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_perro ON public.perro FOR INSERT TO authenticated WITH CHECK (true);


--
-- Name: perro_tiene_caracter registrar_perro_tiene_caracter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_perro_tiene_caracter ON public.perro_tiene_caracter FOR INSERT TO authenticated WITH CHECK (true);


--
-- Name: raza registrar_raza; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_raza ON public.raza FOR INSERT TO authenticated WITH CHECK (true);


--
-- Name: seguimiento registrar_seguimiento; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_seguimiento ON public.seguimiento FOR INSERT TO authenticated WITH CHECK (( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin));


--
-- Name: solicitud_adopcion registrar_solicitud_adopcion; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_solicitud_adopcion ON public.solicitud_adopcion FOR INSERT TO authenticated WITH CHECK (((id_usuario = ( SELECT auth.uid() AS uid)) AND (id_estado = ( SELECT estado_solicitud.id_estado
   FROM public.estado_solicitud
  WHERE ((estado_solicitud.descripcion)::text = 'Pendiente de Revisión'::text)))));


--
-- Name: tamano registrar_tamano; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY registrar_tamano ON public.tamano FOR INSERT TO authenticated WITH CHECK (true);


--
-- Name: rol; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.rol ENABLE ROW LEVEL SECURITY;

--
-- Name: se_inscribe; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.se_inscribe ENABLE ROW LEVEL SECURITY;

--
-- Name: se_inscribe se_inscribe_delete_propia; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY se_inscribe_delete_propia ON public.se_inscribe FOR DELETE TO authenticated USING (((id_usuario = ( SELECT auth.uid() AS uid)) AND (EXISTS ( SELECT 1
   FROM public.programa p
  WHERE ((p.id_programa = se_inscribe.id_programa) AND (p.fecha_inicio > (now() AT TIME ZONE 'America/Mexico_City'::text)))))));


--
-- Name: se_inscribe se_inscribe_insert_propia; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY se_inscribe_insert_propia ON public.se_inscribe FOR INSERT TO authenticated WITH CHECK (((id_usuario = ( SELECT auth.uid() AS uid)) AND (asistencia IS NULL) AND (TRIM(BOTH FROM estado) = 'Aceptado'::text) AND (EXISTS ( SELECT 1
   FROM public.programa p
  WHERE ((p.id_programa = se_inscribe.id_programa) AND (lower(TRIM(BOTH FROM p.estado_del_programa)) = 'disponible'::text) AND (p.fecha_inicio > (now() AT TIME ZONE 'America/Mexico_City'::text)) AND ((p.cupo_maximo IS NULL) OR (public.contar_inscritos(p.id_programa) < p.cupo_maximo)))))));


--
-- Name: se_inscribe se_inscribe_select_propias; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY se_inscribe_select_propias ON public.se_inscribe FOR SELECT TO authenticated USING ((id_usuario = ( SELECT auth.uid() AS uid)));


--
-- Name: seguimiento; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.seguimiento ENABLE ROW LEVEL SECURITY;

--
-- Name: cliente signup_service puede insertar cliente; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "signup_service puede insertar cliente" ON public.cliente FOR INSERT TO signup_service WITH CHECK (true);


--
-- Name: usuario signup_service puede insertar clientes; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "signup_service puede insertar clientes" ON public.usuario FOR INSERT TO signup_service WITH CHECK (((rol)::text = 'Cliente'::text));


--
-- Name: datos_confidenciales signup_service puede insertar datos_confidenciales; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "signup_service puede insertar datos_confidenciales" ON public.datos_confidenciales FOR INSERT TO signup_service WITH CHECK (true);


--
-- Name: direcciones_fisicas signup_service puede insertar direcciones_fisicas; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "signup_service puede insertar direcciones_fisicas" ON public.direcciones_fisicas FOR INSERT TO signup_service WITH CHECK (true);


--
-- Name: solicitud_adopcion; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.solicitud_adopcion ENABLE ROW LEVEL SECURITY;

--
-- Name: solicitud_cfdi; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.solicitud_cfdi ENABLE ROW LEVEL SECURITY;

--
-- Name: tamano; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.tamano ENABLE ROW LEVEL SECURITY;

--
-- Name: tipo_programa; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.tipo_programa ENABLE ROW LEVEL SECURITY;

--
-- Name: usuario; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.usuario ENABLE ROW LEVEL SECURITY;

--
-- Name: admin_revisa_solicitud_adopcion ver_admin_revisa_solicitud_adopcion; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_admin_revisa_solicitud_adopcion ON public.admin_revisa_solicitud_adopcion FOR SELECT TO authenticated USING (( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin));


--
-- Name: caracter ver_caracter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_caracter ON public.caracter FOR SELECT USING (true);


--
-- Name: documento_tiene_solicitud_adopcion ver_documento_tiene_solicitud_adopcion; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_documento_tiene_solicitud_adopcion ON public.documento_tiene_solicitud_adopcion FOR SELECT TO authenticated USING (((EXISTS ( SELECT 1
   FROM public.solicitud_adopcion s
  WHERE ((s.id_solicitud = documento_tiene_solicitud_adopcion.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))) OR ( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin)));


--
-- Name: estado_solicitud ver_estado_solicitud; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_estado_solicitud ON public.estado_solicitud FOR SELECT USING (true);


--
-- Name: multimedia ver_multimedia; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_multimedia ON public.multimedia FOR SELECT USING (true);


--
-- Name: periodo_prueba ver_periodo_prueba; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_periodo_prueba ON public.periodo_prueba FOR SELECT TO authenticated USING (((EXISTS ( SELECT 1
   FROM public.solicitud_adopcion s
  WHERE ((s.id_solicitud = periodo_prueba.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))) OR ( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin)));


--
-- Name: perro ver_perro; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_perro ON public.perro FOR SELECT USING (true);


--
-- Name: perro_tiene_caracter ver_perro_tiene_caracter; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_perro_tiene_caracter ON public.perro_tiene_caracter FOR SELECT USING (true);


--
-- Name: raza ver_raza; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_raza ON public.raza FOR SELECT USING (true);


--
-- Name: seguimiento ver_seguimiento; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_seguimiento ON public.seguimiento FOR SELECT TO authenticated USING (((EXISTS ( SELECT 1
   FROM public.solicitud_adopcion s
  WHERE ((s.id_solicitud = seguimiento.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))) OR ( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin)));


--
-- Name: solicitud_adopcion ver_solicitud_adopcion; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_solicitud_adopcion ON public.solicitud_adopcion FOR SELECT TO authenticated USING (((id_usuario = ( SELECT auth.uid() AS uid)) OR ( SELECT public.solicitudes_es_admin() AS solicitudes_es_admin)));


--
-- Name: tamano ver_tamano; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY ver_tamano ON public.tamano FOR SELECT USING (true);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: -
--

GRANT USAGE ON SCHEMA public TO postgres;
GRANT USAGE ON SCHEMA public TO anon;
GRANT USAGE ON SCHEMA public TO authenticated;
GRANT USAGE ON SCHEMA public TO service_role;
GRANT USAGE ON SCHEMA public TO signup_service;


--
-- Name: FUNCTION contar_inscritos(p_id_programa uuid); Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON FUNCTION public.contar_inscritos(p_id_programa uuid) FROM PUBLIC;
GRANT ALL ON FUNCTION public.contar_inscritos(p_id_programa uuid) TO authenticated;
GRANT ALL ON FUNCTION public.contar_inscritos(p_id_programa uuid) TO service_role;
GRANT ALL ON FUNCTION public.contar_inscritos(p_id_programa uuid) TO anon;


--
-- Name: FUNCTION donativos_registrar_monetario(p_id_usuario uuid, p_monto numeric, p_referencia character varying, p_comprobante bytea); Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON FUNCTION public.donativos_registrar_monetario(p_id_usuario uuid, p_monto numeric, p_referencia character varying, p_comprobante bytea) FROM PUBLIC;
GRANT ALL ON FUNCTION public.donativos_registrar_monetario(p_id_usuario uuid, p_monto numeric, p_referencia character varying, p_comprobante bytea) TO service_role;


--
-- Name: FUNCTION inscribir_en_programa(p_id_programa uuid, p_id_usuario uuid); Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON FUNCTION public.inscribir_en_programa(p_id_programa uuid, p_id_usuario uuid) FROM PUBLIC;
GRANT ALL ON FUNCTION public.inscribir_en_programa(p_id_programa uuid, p_id_usuario uuid) TO service_role;


--
-- Name: FUNCTION insert_full_dog(payload jsonb); Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON FUNCTION public.insert_full_dog(payload jsonb) TO anon;
GRANT ALL ON FUNCTION public.insert_full_dog(payload jsonb) TO authenticated;
GRANT ALL ON FUNCTION public.insert_full_dog(payload jsonb) TO service_role;


--
-- Name: FUNCTION limpiar_datos_prueba(); Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON FUNCTION public.limpiar_datos_prueba() FROM PUBLIC;
GRANT ALL ON FUNCTION public.limpiar_datos_prueba() TO service_role;


--
-- Name: FUNCTION perfiles_registrar_cliente(p_nombre text, p_apellidos text, p_correo text, p_hash text, p_telefono text, p_fecha_nacimiento date, p_calle text, p_numero_exterior integer, p_codigo_postal text, p_privacidad boolean); Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON FUNCTION public.perfiles_registrar_cliente(p_nombre text, p_apellidos text, p_correo text, p_hash text, p_telefono text, p_fecha_nacimiento date, p_calle text, p_numero_exterior integer, p_codigo_postal text, p_privacidad boolean) FROM PUBLIC;
GRANT ALL ON FUNCTION public.perfiles_registrar_cliente(p_nombre text, p_apellidos text, p_correo text, p_hash text, p_telefono text, p_fecha_nacimiento date, p_calle text, p_numero_exterior integer, p_codigo_postal text, p_privacidad boolean) TO service_role;


--
-- Name: FUNCTION rls_auto_enable(); Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON FUNCTION public.rls_auto_enable() TO anon;
GRANT ALL ON FUNCTION public.rls_auto_enable() TO authenticated;
GRANT ALL ON FUNCTION public.rls_auto_enable() TO service_role;


--
-- Name: FUNCTION solicitudes_documento_es_mio(p_id_documento uuid); Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON FUNCTION public.solicitudes_documento_es_mio(p_id_documento uuid) FROM PUBLIC;
GRANT ALL ON FUNCTION public.solicitudes_documento_es_mio(p_id_documento uuid) TO authenticated;
GRANT ALL ON FUNCTION public.solicitudes_documento_es_mio(p_id_documento uuid) TO service_role;


--
-- Name: FUNCTION solicitudes_es_admin(); Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON FUNCTION public.solicitudes_es_admin() FROM PUBLIC;
GRANT ALL ON FUNCTION public.solicitudes_es_admin() TO authenticated;
GRANT ALL ON FUNCTION public.solicitudes_es_admin() TO service_role;


--
-- Name: TABLE admin; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.admin TO anon;
GRANT ALL ON TABLE public.admin TO authenticated;
GRANT ALL ON TABLE public.admin TO service_role;


--
-- Name: TABLE admin_revisa_solicitud_adopcion; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.admin_revisa_solicitud_adopcion TO anon;
GRANT ALL ON TABLE public.admin_revisa_solicitud_adopcion TO authenticated;
GRANT ALL ON TABLE public.admin_revisa_solicitud_adopcion TO service_role;


--
-- Name: TABLE caracter; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.caracter TO anon;
GRANT ALL ON TABLE public.caracter TO authenticated;
GRANT ALL ON TABLE public.caracter TO service_role;


--
-- Name: TABLE catalogo_de_cps; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.catalogo_de_cps TO anon;
GRANT ALL ON TABLE public.catalogo_de_cps TO authenticated;
GRANT ALL ON TABLE public.catalogo_de_cps TO service_role;


--
-- Name: TABLE cliente; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.cliente TO anon;
GRANT ALL ON TABLE public.cliente TO authenticated;
GRANT ALL ON TABLE public.cliente TO service_role;


--
-- Name: COLUMN cliente.id_usuario; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(id_usuario) ON TABLE public.cliente TO signup_service;


--
-- Name: TABLE cuidado_especial; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.cuidado_especial TO anon;
GRANT ALL ON TABLE public.cuidado_especial TO authenticated;
GRANT ALL ON TABLE public.cuidado_especial TO service_role;


--
-- Name: TABLE datos_confidenciales; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.datos_confidenciales TO anon;
GRANT ALL ON TABLE public.datos_confidenciales TO authenticated;
GRANT ALL ON TABLE public.datos_confidenciales TO service_role;


--
-- Name: COLUMN datos_confidenciales.id_usuario; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(id_usuario) ON TABLE public.datos_confidenciales TO signup_service;


--
-- Name: COLUMN datos_confidenciales.correo; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(correo) ON TABLE public.datos_confidenciales TO signup_service;


--
-- Name: COLUMN datos_confidenciales.telefono; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(telefono) ON TABLE public.datos_confidenciales TO signup_service;


--
-- Name: TABLE direcciones_fisicas; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.direcciones_fisicas TO anon;
GRANT ALL ON TABLE public.direcciones_fisicas TO authenticated;
GRANT ALL ON TABLE public.direcciones_fisicas TO service_role;


--
-- Name: COLUMN direcciones_fisicas.id_usuario; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(id_usuario) ON TABLE public.direcciones_fisicas TO signup_service;


--
-- Name: COLUMN direcciones_fisicas.id_codigo_postal; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(id_codigo_postal) ON TABLE public.direcciones_fisicas TO signup_service;


--
-- Name: COLUMN direcciones_fisicas.calle; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(calle) ON TABLE public.direcciones_fisicas TO signup_service;


--
-- Name: COLUMN direcciones_fisicas.numero_exterior; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(numero_exterior) ON TABLE public.direcciones_fisicas TO signup_service;


--
-- Name: COLUMN direcciones_fisicas.numero_interior; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(numero_interior) ON TABLE public.direcciones_fisicas TO signup_service;


--
-- Name: TABLE documento; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.documento TO anon;
GRANT ALL ON TABLE public.documento TO authenticated;
GRANT ALL ON TABLE public.documento TO service_role;


--
-- Name: TABLE documento_tiene_solicitud_adopcion; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.documento_tiene_solicitud_adopcion TO anon;
GRANT ALL ON TABLE public.documento_tiene_solicitud_adopcion TO authenticated;
GRANT ALL ON TABLE public.documento_tiene_solicitud_adopcion TO service_role;


--
-- Name: TABLE donativo; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.donativo TO anon;
GRANT ALL ON TABLE public.donativo TO authenticated;
GRANT ALL ON TABLE public.donativo TO service_role;


--
-- Name: TABLE estado_solicitud; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.estado_solicitud TO anon;
GRANT ALL ON TABLE public.estado_solicitud TO authenticated;
GRANT ALL ON TABLE public.estado_solicitud TO service_role;


--
-- Name: TABLE mensaje; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.mensaje TO anon;
GRANT ALL ON TABLE public.mensaje TO authenticated;
GRANT ALL ON TABLE public.mensaje TO service_role;


--
-- Name: TABLE multimedia; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.multimedia TO anon;
GRANT ALL ON TABLE public.multimedia TO authenticated;
GRANT ALL ON TABLE public.multimedia TO service_role;


--
-- Name: TABLE notifica; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.notifica TO anon;
GRANT ALL ON TABLE public.notifica TO authenticated;
GRANT ALL ON TABLE public.notifica TO service_role;


--
-- Name: TABLE perfiles_consentimiento; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.perfiles_consentimiento TO service_role;


--
-- Name: TABLE periodo_prueba; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.periodo_prueba TO anon;
GRANT ALL ON TABLE public.periodo_prueba TO authenticated;
GRANT ALL ON TABLE public.periodo_prueba TO service_role;


--
-- Name: TABLE perro; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.perro TO anon;
GRANT ALL ON TABLE public.perro TO authenticated;
GRANT ALL ON TABLE public.perro TO service_role;


--
-- Name: TABLE perro_tiene_caracter; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.perro_tiene_caracter TO anon;
GRANT ALL ON TABLE public.perro_tiene_caracter TO authenticated;
GRANT ALL ON TABLE public.perro_tiene_caracter TO service_role;


--
-- Name: TABLE perro_tiene_cuidado_especial; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.perro_tiene_cuidado_especial TO anon;
GRANT ALL ON TABLE public.perro_tiene_cuidado_especial TO authenticated;
GRANT ALL ON TABLE public.perro_tiene_cuidado_especial TO service_role;


--
-- Name: TABLE privilegio; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.privilegio TO anon;
GRANT ALL ON TABLE public.privilegio TO authenticated;
GRANT ALL ON TABLE public.privilegio TO service_role;


--
-- Name: TABLE programa; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.programa TO anon;
GRANT ALL ON TABLE public.programa TO authenticated;
GRANT ALL ON TABLE public.programa TO service_role;


--
-- Name: TABLE raza; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.raza TO anon;
GRANT ALL ON TABLE public.raza TO authenticated;
GRANT ALL ON TABLE public.raza TO service_role;


--
-- Name: TABLE rol; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.rol TO anon;
GRANT ALL ON TABLE public.rol TO authenticated;
GRANT ALL ON TABLE public.rol TO service_role;


--
-- Name: TABLE se_inscribe; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.se_inscribe TO anon;
GRANT ALL ON TABLE public.se_inscribe TO authenticated;
GRANT ALL ON TABLE public.se_inscribe TO service_role;


--
-- Name: TABLE seguimiento; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.seguimiento TO anon;
GRANT ALL ON TABLE public.seguimiento TO authenticated;
GRANT ALL ON TABLE public.seguimiento TO service_role;


--
-- Name: TABLE solicitud_adopcion; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.solicitud_adopcion TO anon;
GRANT ALL ON TABLE public.solicitud_adopcion TO authenticated;
GRANT ALL ON TABLE public.solicitud_adopcion TO service_role;


--
-- Name: TABLE solicitud_cfdi; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.solicitud_cfdi TO anon;
GRANT ALL ON TABLE public.solicitud_cfdi TO authenticated;
GRANT ALL ON TABLE public.solicitud_cfdi TO service_role;


--
-- Name: TABLE tamano; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.tamano TO anon;
GRANT ALL ON TABLE public.tamano TO authenticated;
GRANT ALL ON TABLE public.tamano TO service_role;


--
-- Name: TABLE tipo_programa; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.tipo_programa TO anon;
GRANT ALL ON TABLE public.tipo_programa TO authenticated;
GRANT ALL ON TABLE public.tipo_programa TO service_role;


--
-- Name: TABLE usuario; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.usuario TO anon;
GRANT ALL ON TABLE public.usuario TO authenticated;
GRANT ALL ON TABLE public.usuario TO service_role;


--
-- Name: COLUMN usuario.id_usuario; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(id_usuario) ON TABLE public.usuario TO signup_service;


--
-- Name: COLUMN usuario.rol; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(rol) ON TABLE public.usuario TO signup_service;


--
-- Name: COLUMN usuario.nombre; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(nombre) ON TABLE public.usuario TO signup_service;


--
-- Name: COLUMN usuario.apellidos; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(apellidos) ON TABLE public.usuario TO signup_service;


--
-- Name: COLUMN usuario.contrasena; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(contrasena) ON TABLE public.usuario TO signup_service;


--
-- Name: COLUMN usuario.fecha_nacimiento; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(fecha_nacimiento) ON TABLE public.usuario TO signup_service;


--
-- Name: COLUMN usuario.aviso_privacidad_aceptado; Type: ACL; Schema: public; Owner: -
--

GRANT INSERT(aviso_privacidad_aceptado) ON TABLE public.usuario TO signup_service;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO postgres;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO anon;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO service_role;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON SEQUENCES TO postgres;
ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON SEQUENCES TO anon;
ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON SEQUENCES TO authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON SEQUENCES TO service_role;


--
-- Name: DEFAULT PRIVILEGES FOR FUNCTIONS; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON FUNCTIONS TO postgres;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON FUNCTIONS TO anon;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON FUNCTIONS TO authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON FUNCTIONS TO service_role;


--
-- Name: DEFAULT PRIVILEGES FOR FUNCTIONS; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON FUNCTIONS TO postgres;
ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON FUNCTIONS TO anon;
ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON FUNCTIONS TO authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON FUNCTIONS TO service_role;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO anon;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO service_role;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON TABLES TO anon;
ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON TABLES TO authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE supabase_admin IN SCHEMA public GRANT ALL ON TABLES TO service_role;


--
-- PostgreSQL database dump complete
--


