-- ==========================================================
-- Función: public.contar_inscritos
-- Proyecto: Prueba BD2 · copiada del Dashboard (Database > Functions) el 2026-11-19
-- Tipo: Function · Lenguaje: sql · Seguridad: SECURITY DEFINER
-- Argumentos: p_id_programa uuid
-- Retorna: integer
-- Descripción: cuenta las inscripciones con estado 'Aceptado' de un programa.
-- ==========================================================
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
$function$;
