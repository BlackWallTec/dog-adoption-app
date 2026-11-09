-- Aplicar una vez en Supabase, sobre el esquema del proyecto. No ejecuta el esquema adjunto.
-- Si ya existen correos repetidos al ignorar mayúsculas/espacios, el índice falla sin borrarlos.
BEGIN;

CREATE UNIQUE INDEX IF NOT EXISTS perfiles_correo_normalizado_uniq
  ON public.datos_confidenciales (lower(btrim(correo)));

CREATE TABLE IF NOT EXISTS public.perfiles_consentimiento (
  id_usuario uuid PRIMARY KEY REFERENCES public.usuario(id_usuario) ON DELETE CASCADE,
  aviso_version text NOT NULL,
  aceptado_en timestamptz NOT NULL DEFAULT now()
);
ALTER TABLE public.perfiles_consentimiento ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON public.perfiles_consentimiento FROM anon, authenticated;
GRANT SELECT, INSERT ON public.perfiles_consentimiento TO service_role;

CREATE OR REPLACE FUNCTION public.perfiles_registrar_cliente(
  p_nombre text, p_apellidos text, p_correo text, p_hash text, p_telefono text,
  p_fecha_nacimiento date, p_calle text, p_numero_exterior integer,
  p_codigo_postal text, p_privacidad boolean
) RETURNS uuid
LANGUAGE plpgsql SECURITY INVOKER SET search_path = ''
AS $$
DECLARE
  nuevo_id uuid;
BEGIN
  IF p_privacidad IS DISTINCT FROM true
    OR p_fecha_nacimiento IS NULL
    OR p_fecha_nacimiento > (CURRENT_DATE - interval '18 years')::date
    OR coalesce(p_hash, '') !~ '^[$]2[aby][$][0-9]{2}[$][./A-Za-z0-9]{53}$'
  THEN
    RAISE EXCEPTION 'Datos de registro inválidos' USING ERRCODE = '22023';
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
$$;

-- Solo Express puede invocar el registro; la contraseña llega como hash bcrypt.
REVOKE ALL ON FUNCTION public.perfiles_registrar_cliente(
  text, text, text, text, text, date, text, integer, text, boolean
) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.perfiles_registrar_cliente(
  text, text, text, text, text, date, text, integer, text, boolean
) TO service_role;

NOTIFY pgrst, 'reload schema';
COMMIT;
