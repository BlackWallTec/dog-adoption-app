-- ==========================================================
-- Función: public.limpiar_datos_prueba
-- Proyecto: Prueba BD2 · copiada del Dashboard (Database > Functions) el 2026-11-19
-- Tipo: Function · Lenguaje: plpgsql · Seguridad: SECURITY DEFINER
-- Argumentos: (ninguno)
-- Retorna: text
-- Descripción: vacía (TRUNCATE ... RESTART IDENTITY) las tablas de datos de public.
-- ⚠ DESTRUCTIVA: borra todos los datos. Solo para entornos de prueba.
-- ==========================================================
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
$function$;
