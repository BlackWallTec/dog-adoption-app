-- ==========================================================
-- Función: public.solicitudes_documento_es_mio
-- Proyecto: Prueba BD2 · copiada del Dashboard (Database > Functions) el 2026-11-19
-- Tipo: Function · Lenguaje: sql · Seguridad: SECURITY DEFINER
-- Argumentos: p_id_documento uuid
-- Retorna: boolean
-- Descripción: indica si el documento pertenece al usuario autenticado (auth.uid()).
--              Usada en políticas RLS.
-- ==========================================================
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
$function$;
