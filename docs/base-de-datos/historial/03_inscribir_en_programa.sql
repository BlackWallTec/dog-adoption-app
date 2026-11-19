-- ==========================================================
-- Función: public.inscribir_en_programa
-- Proyecto: Prueba BD2 · copiada del Dashboard (Database > Functions) el 2026-11-19
-- Tipo: Function · Lenguaje: plpgsql · Seguridad: SECURITY INVOKER
-- Argumentos: p_id_programa uuid, p_id_usuario uuid
-- Retorna: jsonb  ({ codigo: OK | PROGRAMA_NO_ENCONTRADO | PROGRAMA_NO_DISPONIBLE |
--                   PROGRAMA_INICIADO | YA_INSCRITO | SIN_CUPO, ... })
-- Descripción: inscribe a un usuario en un programa validando disponibilidad,
--              fecha de inicio, inscripción previa y cupo (bloquea el programa con FOR UPDATE).
-- ==========================================================
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
$function$;
