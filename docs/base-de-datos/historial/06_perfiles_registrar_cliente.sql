-- ==========================================================
-- Función: public.perfiles_registrar_cliente
-- Proyecto: Prueba BD2 · copiada del Dashboard (Database > Functions) el 2026-11-19
-- Tipo: Function · Lenguaje: plpgsql · Seguridad: SECURITY INVOKER
-- Argumentos: p_nombre text, p_apellidos text, p_correo text, p_hash text,
--             p_telefono text, p_fecha_nacimiento date, p_calle text,
--             p_numero_exterior integer, p_codigo_postal text, p_privacidad boolean
-- Retorna: uuid  (id_usuario creado)
-- Descripción: registra un cliente (usuario, cliente, datos_confidenciales,
--              direcciones_fisicas, perfiles_consentimiento). Valida aviso de
--              privacidad, mayoría de edad (18) y formato bcrypt del hash;
--              si falla lanza ERRCODE 22023.
-- ==========================================================
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
$function$;
