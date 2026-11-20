-- ==========================================================
-- Función: public.insert_full_dog
-- Proyecto: Prueba BD2 · copiada del Dashboard (Database > Functions) el 2026-11-19
-- Tipo: Function · Lenguaje: plpgsql · Seguridad: SECURITY INVOKER
-- Argumentos: payload jsonb
-- Retorna: jsonb  ({ id_perro, nombre, status: 'success' })
-- Descripción: inserta un perro y, opcionalmente, su multimedia, caracteres y
--              cuidados especiales en una sola transacción. Ante error lanza
--              'DB_ERROR: ...' con el detalle de diagnóstico.
-- ==========================================================
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
$function$;
