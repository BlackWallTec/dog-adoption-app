-- ==========================================================
-- Función: public.solicitudes_es_admin
-- Proyecto: Prueba BD2 · copiada del Dashboard (Database > Functions) el 2026-11-19
-- Tipo: Function · Lenguaje: sql · Seguridad: SECURITY DEFINER
-- Argumentos: (ninguno)
-- Retorna: boolean
-- Descripción: indica si el usuario autenticado tiene rol 'Administrador' o 'Root'.
--              Usada en políticas RLS.
-- ==========================================================
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
$function$;
