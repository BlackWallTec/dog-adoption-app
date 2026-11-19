-- ==========================================================
-- Función: public.donativos_registrar_monetario
-- Proyecto: Prueba BD2 · copiada del Dashboard (Database > Functions) el 2026-11-19
-- Tipo: Function · Lenguaje: sql · Seguridad: SECURITY INVOKER
-- Argumentos: p_id_usuario uuid, p_monto numeric, p_referencia character varying, p_comprobante bytea
-- Retorna: TABLE(id_donativo uuid, estado character varying)
-- Descripción: registra un donativo de tipo 'Monetario' y devuelve su id y estado.
-- ==========================================================
CREATE OR REPLACE FUNCTION public.donativos_registrar_monetario(p_id_usuario uuid, p_monto numeric, p_referencia character varying, p_comprobante bytea)
 RETURNS TABLE(id_donativo uuid, estado character varying)
 LANGUAGE sql
 SET search_path TO 'public'
AS $function$
  insert into public.donativo (id_usuario, tipo, monto, descripcion, comprobante)
  values (p_id_usuario, 'Monetario', p_monto, p_referencia, p_comprobante)
  returning id_donativo, estado;
$function$;
