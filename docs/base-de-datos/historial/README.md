# Historial · Funciones de la base de datos "Prueba BD2"

Copia del SQL de las **9 funciones** del esquema `public`, tomada del Dashboard de
Supabase (**Database → Functions → Edit function**, solo lectura) el **2026-11-19**.
No se ejecutó ni se guardó nada en el proyecto.

| #   | Archivo                                                                      | Función                                                                                                                                                          | Lenguaje | Seguridad | Retorna                                 |
| --- | ---------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- | --------- | --------------------------------------- |
| 1   | [01_contar_inscritos.sql](01_contar_inscritos.sql)                           | `contar_inscritos(p_id_programa uuid)`                                                                                                                           | sql      | Definer   | integer                                 |
| 2   | [02_donativos_registrar_monetario.sql](02_donativos_registrar_monetario.sql) | `donativos_registrar_monetario(p_id_usuario uuid, p_monto numeric, p_referencia varchar, p_comprobante bytea)`                                                   | sql      | Invoker   | TABLE(id_donativo uuid, estado varchar) |
| 3   | [03_inscribir_en_programa.sql](03_inscribir_en_programa.sql)                 | `inscribir_en_programa(p_id_programa uuid, p_id_usuario uuid)`                                                                                                   | plpgsql  | Invoker   | jsonb                                   |
| 4   | [04_insert_full_dog.sql](04_insert_full_dog.sql)                             | `insert_full_dog(payload jsonb)`                                                                                                                                 | plpgsql  | Invoker   | jsonb                                   |
| 5   | [05_limpiar_datos_prueba.sql](05_limpiar_datos_prueba.sql)                   | `limpiar_datos_prueba()` ⚠ destructiva                                                                                                                           | plpgsql  | Definer   | text                                    |
| 6   | [06_perfiles_registrar_cliente.sql](06_perfiles_registrar_cliente.sql)       | `perfiles_registrar_cliente(p_nombre, p_apellidos, p_correo, p_hash, p_telefono, p_fecha_nacimiento, p_calle, p_numero_exterior, p_codigo_postal, p_privacidad)` | plpgsql  | Invoker   | uuid                                    |
| 7   | [07_rls_auto_enable.sql](07_rls_auto_enable.sql)                             | `rls_auto_enable()`                                                                                                                                              | plpgsql  | Definer   | event_trigger                           |
| 8   | [08_solicitudes_documento_es_mio.sql](08_solicitudes_documento_es_mio.sql)   | `solicitudes_documento_es_mio(p_id_documento uuid)`                                                                                                              | sql      | Definer   | boolean                                 |
| 9   | [09_solicitudes_es_admin.sql](09_solicitudes_es_admin.sql)                   | `solicitudes_es_admin()`                                                                                                                                         | sql      | Definer   | boolean                                 |

## Notas

- El cuerpo de cada función (lo que va entre `$function$`) está copiado tal cual del
  editor del Dashboard.
- El editor del Dashboard no muestra la volatilidad (`STABLE`) ni `SET search_path`;
  esos atributos del encabezado se tomaron de `../02_funciones_triggers.sql`
  (extraído del catálogo con `pg_get_functiondef` el mismo día).
- Cada archivo es un `CREATE OR REPLACE`, así que se puede volver a aplicar sin
  borrar la función. **No ejecutar `05_limpiar_datos_prueba.sql` y luego llamar a
  la función en producción**: vacía todas las tablas.
