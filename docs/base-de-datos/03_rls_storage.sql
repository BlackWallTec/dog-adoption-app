-- ==========================================================
-- 03_rls_storage.sql  ·  Proyecto: Prueba BD2
-- Generado: 2026-11-19
-- ==========================================================

-- ---------- 1) Estado de RLS en tablas de public ----------
-- admin: RLS activado
-- admin_revisa_solicitud_adopcion: RLS activado
-- caracter: RLS activado
-- catalogo_de_cps: RLS activado
-- cliente: RLS activado
-- cuidado_especial: RLS activado
-- datos_confidenciales: RLS activado
-- direcciones_fisicas: RLS activado
-- documento: RLS activado
-- documento_tiene_solicitud_adopcion: RLS activado
-- donativo: RLS activado
-- estado_solicitud: RLS activado
-- mensaje: RLS activado
-- multimedia: RLS activado
-- notifica: RLS activado
-- perfiles_consentimiento: RLS activado
-- periodo_prueba: RLS activado
-- perro: RLS activado
-- perro_tiene_caracter: RLS activado
-- perro_tiene_cuidado_especial: RLS activado
-- privilegio: RLS activado
-- programa: RLS activado
-- raza: RLS activado
-- rol: RLS activado
-- se_inscribe: RLS activado
-- seguimiento: RLS activado
-- solicitud_adopcion: RLS activado
-- solicitud_cfdi: RLS activado
-- tamano: RLS activado
-- tipo_programa: RLS activado
-- usuario: RLS activado

-- ---------- 2) Políticas RLS de public ----------
-- (Referencia. Ya vienen incluidas en 01_schema.sql)
-- Tabla: admin_revisa_solicitud_adopcion
drop policy if exists registrar_admin_revisa_solicitud_adopcion on public.admin_revisa_solicitud_adopcion;
create policy registrar_admin_revisa_solicitud_adopcion on public.admin_revisa_solicitud_adopcion as PERMISSIVE for INSERT to authenticated
  with check ((( SELECT solicitudes_es_admin() AS solicitudes_es_admin) AND (id_usuario = ( SELECT auth.uid() AS uid))));

-- Tabla: admin_revisa_solicitud_adopcion
drop policy if exists ver_admin_revisa_solicitud_adopcion on public.admin_revisa_solicitud_adopcion;
create policy ver_admin_revisa_solicitud_adopcion on public.admin_revisa_solicitud_adopcion as PERMISSIVE for SELECT to authenticated
  using (( SELECT solicitudes_es_admin() AS solicitudes_es_admin));

-- Tabla: caracter
drop policy if exists eliminar_caracter on public.caracter;
create policy eliminar_caracter on public.caracter as PERMISSIVE for DELETE to authenticated
  using (true);

-- Tabla: caracter
drop policy if exists modificar_caracter on public.caracter;
create policy modificar_caracter on public.caracter as PERMISSIVE for UPDATE to authenticated
  using (true)
  with check (true);

-- Tabla: caracter
drop policy if exists registrar_caracter on public.caracter;
create policy registrar_caracter on public.caracter as PERMISSIVE for INSERT to authenticated
  with check (true);

-- Tabla: caracter
drop policy if exists ver_caracter on public.caracter;
create policy ver_caracter on public.caracter as PERMISSIVE for SELECT to public
  using (true);

-- Tabla: cliente
drop policy if exists "signup_service puede insertar cliente" on public.cliente;
create policy "signup_service puede insertar cliente" on public.cliente as PERMISSIVE for INSERT to signup_service
  with check (true);

-- Tabla: cuidado_especial
drop policy if exists "Enable read access for all users" on public.cuidado_especial;
create policy "Enable read access for all users" on public.cuidado_especial as PERMISSIVE for SELECT to public
  using (true);

-- Tabla: datos_confidenciales
drop policy if exists "signup_service puede insertar datos_confidenciales" on public.datos_confidenciales;
create policy "signup_service puede insertar datos_confidenciales" on public.datos_confidenciales as PERMISSIVE for INSERT to signup_service
  with check (true);

-- Tabla: direcciones_fisicas
drop policy if exists "signup_service puede insertar direcciones_fisicas" on public.direcciones_fisicas;
create policy "signup_service puede insertar direcciones_fisicas" on public.direcciones_fisicas as PERMISSIVE for INSERT to signup_service
  with check (true);

-- Tabla: documento_tiene_solicitud_adopcion
drop policy if exists modificar_documento_tiene_solicitud_adopcion on public.documento_tiene_solicitud_adopcion;
create policy modificar_documento_tiene_solicitud_adopcion on public.documento_tiene_solicitud_adopcion as PERMISSIVE for UPDATE to authenticated
  using ((EXISTS ( SELECT 1
   FROM solicitud_adopcion s
  WHERE ((s.id_solicitud = documento_tiene_solicitud_adopcion.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))))
  with check (((EXISTS ( SELECT 1
   FROM solicitud_adopcion s
  WHERE ((s.id_solicitud = documento_tiene_solicitud_adopcion.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))) AND solicitudes_documento_es_mio(id_documento)));

-- Tabla: documento_tiene_solicitud_adopcion
drop policy if exists registrar_documento_tiene_solicitud_adopcion on public.documento_tiene_solicitud_adopcion;
create policy registrar_documento_tiene_solicitud_adopcion on public.documento_tiene_solicitud_adopcion as PERMISSIVE for INSERT to authenticated
  with check (((EXISTS ( SELECT 1
   FROM solicitud_adopcion s
  WHERE ((s.id_solicitud = documento_tiene_solicitud_adopcion.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))) AND solicitudes_documento_es_mio(id_documento)));

-- Tabla: documento_tiene_solicitud_adopcion
drop policy if exists ver_documento_tiene_solicitud_adopcion on public.documento_tiene_solicitud_adopcion;
create policy ver_documento_tiene_solicitud_adopcion on public.documento_tiene_solicitud_adopcion as PERMISSIVE for SELECT to authenticated
  using (((EXISTS ( SELECT 1
   FROM solicitud_adopcion s
  WHERE ((s.id_solicitud = documento_tiene_solicitud_adopcion.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))) OR ( SELECT solicitudes_es_admin() AS solicitudes_es_admin)));

-- Tabla: donativo
drop policy if exists "HU44_lectura_donativos_prueba" on public.donativo;
create policy "HU44_lectura_donativos_prueba" on public.donativo as PERMISSIVE for SELECT to authenticated
  using ((( SELECT auth.uid() AS uid) = '7f630a30-0696-4fb6-9975-f5bca4874abc'::uuid));

-- Tabla: estado_solicitud
drop policy if exists ver_estado_solicitud on public.estado_solicitud;
create policy ver_estado_solicitud on public.estado_solicitud as PERMISSIVE for SELECT to public
  using (true);

-- Tabla: multimedia
drop policy if exists eliminar_multimedia on public.multimedia;
create policy eliminar_multimedia on public.multimedia as PERMISSIVE for DELETE to authenticated
  using (true);

-- Tabla: multimedia
drop policy if exists modificar_multimedia on public.multimedia;
create policy modificar_multimedia on public.multimedia as PERMISSIVE for UPDATE to authenticated
  using (true)
  with check (true);

-- Tabla: multimedia
drop policy if exists registrar_multimedia on public.multimedia;
create policy registrar_multimedia on public.multimedia as PERMISSIVE for INSERT to authenticated
  with check (true);

-- Tabla: multimedia
drop policy if exists ver_multimedia on public.multimedia;
create policy ver_multimedia on public.multimedia as PERMISSIVE for SELECT to public
  using (true);

-- Tabla: periodo_prueba
drop policy if exists modificar_periodo_prueba on public.periodo_prueba;
create policy modificar_periodo_prueba on public.periodo_prueba as PERMISSIVE for UPDATE to authenticated
  using (( SELECT solicitudes_es_admin() AS solicitudes_es_admin))
  with check (( SELECT solicitudes_es_admin() AS solicitudes_es_admin));

-- Tabla: periodo_prueba
drop policy if exists registrar_periodo_prueba on public.periodo_prueba;
create policy registrar_periodo_prueba on public.periodo_prueba as PERMISSIVE for INSERT to authenticated
  with check (( SELECT solicitudes_es_admin() AS solicitudes_es_admin));

-- Tabla: periodo_prueba
drop policy if exists ver_periodo_prueba on public.periodo_prueba;
create policy ver_periodo_prueba on public.periodo_prueba as PERMISSIVE for SELECT to authenticated
  using (((EXISTS ( SELECT 1
   FROM solicitud_adopcion s
  WHERE ((s.id_solicitud = periodo_prueba.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))) OR ( SELECT solicitudes_es_admin() AS solicitudes_es_admin)));

-- Tabla: perro
drop policy if exists eliminar_perro on public.perro;
create policy eliminar_perro on public.perro as PERMISSIVE for DELETE to authenticated
  using (true);

-- Tabla: perro
drop policy if exists modificar_perro on public.perro;
create policy modificar_perro on public.perro as PERMISSIVE for UPDATE to authenticated
  using (true)
  with check (true);

-- Tabla: perro
drop policy if exists registrar_perro on public.perro;
create policy registrar_perro on public.perro as PERMISSIVE for INSERT to authenticated
  with check (true);

-- Tabla: perro
drop policy if exists ver_perro on public.perro;
create policy ver_perro on public.perro as PERMISSIVE for SELECT to public
  using (true);

-- Tabla: perro_tiene_caracter
drop policy if exists eliminar_perro_tiene_caracter on public.perro_tiene_caracter;
create policy eliminar_perro_tiene_caracter on public.perro_tiene_caracter as PERMISSIVE for DELETE to authenticated
  using (true);

-- Tabla: perro_tiene_caracter
drop policy if exists modificar_perro_tiene_caracter on public.perro_tiene_caracter;
create policy modificar_perro_tiene_caracter on public.perro_tiene_caracter as PERMISSIVE for UPDATE to authenticated
  using (true)
  with check (true);

-- Tabla: perro_tiene_caracter
drop policy if exists registrar_perro_tiene_caracter on public.perro_tiene_caracter;
create policy registrar_perro_tiene_caracter on public.perro_tiene_caracter as PERMISSIVE for INSERT to authenticated
  with check (true);

-- Tabla: perro_tiene_caracter
drop policy if exists ver_perro_tiene_caracter on public.perro_tiene_caracter;
create policy ver_perro_tiene_caracter on public.perro_tiene_caracter as PERMISSIVE for SELECT to public
  using (true);

-- Tabla: programa
drop policy if exists programa_select_usuarios on public.programa;
create policy programa_select_usuarios on public.programa as PERMISSIVE for SELECT to authenticated
  using (true);

-- Tabla: raza
drop policy if exists eliminar_raza on public.raza;
create policy eliminar_raza on public.raza as PERMISSIVE for DELETE to authenticated
  using (true);

-- Tabla: raza
drop policy if exists modificar_raza on public.raza;
create policy modificar_raza on public.raza as PERMISSIVE for UPDATE to authenticated
  using (true)
  with check (true);

-- Tabla: raza
drop policy if exists registrar_raza on public.raza;
create policy registrar_raza on public.raza as PERMISSIVE for INSERT to authenticated
  with check (true);

-- Tabla: raza
drop policy if exists ver_raza on public.raza;
create policy ver_raza on public.raza as PERMISSIVE for SELECT to public
  using (true);

-- Tabla: se_inscribe
drop policy if exists se_inscribe_delete_propia on public.se_inscribe;
create policy se_inscribe_delete_propia on public.se_inscribe as PERMISSIVE for DELETE to authenticated
  using (((id_usuario = ( SELECT auth.uid() AS uid)) AND (EXISTS ( SELECT 1
   FROM programa p
  WHERE ((p.id_programa = se_inscribe.id_programa) AND (p.fecha_inicio > (now() AT TIME ZONE 'America/Mexico_City'::text)))))));

-- Tabla: se_inscribe
drop policy if exists se_inscribe_insert_propia on public.se_inscribe;
create policy se_inscribe_insert_propia on public.se_inscribe as PERMISSIVE for INSERT to authenticated
  with check (((id_usuario = ( SELECT auth.uid() AS uid)) AND (asistencia IS NULL) AND (TRIM(BOTH FROM estado) = 'Aceptado'::text) AND (EXISTS ( SELECT 1
   FROM programa p
  WHERE ((p.id_programa = se_inscribe.id_programa) AND (lower(TRIM(BOTH FROM p.estado_del_programa)) = 'disponible'::text) AND (p.fecha_inicio > (now() AT TIME ZONE 'America/Mexico_City'::text)) AND ((p.cupo_maximo IS NULL) OR (contar_inscritos(p.id_programa) < p.cupo_maximo)))))));

-- Tabla: se_inscribe
drop policy if exists se_inscribe_select_propias on public.se_inscribe;
create policy se_inscribe_select_propias on public.se_inscribe as PERMISSIVE for SELECT to authenticated
  using ((id_usuario = ( SELECT auth.uid() AS uid)));

-- Tabla: seguimiento
drop policy if exists modificar_seguimiento on public.seguimiento;
create policy modificar_seguimiento on public.seguimiento as PERMISSIVE for UPDATE to authenticated
  using (( SELECT solicitudes_es_admin() AS solicitudes_es_admin))
  with check (( SELECT solicitudes_es_admin() AS solicitudes_es_admin));

-- Tabla: seguimiento
drop policy if exists registrar_seguimiento on public.seguimiento;
create policy registrar_seguimiento on public.seguimiento as PERMISSIVE for INSERT to authenticated
  with check (( SELECT solicitudes_es_admin() AS solicitudes_es_admin));

-- Tabla: seguimiento
drop policy if exists ver_seguimiento on public.seguimiento;
create policy ver_seguimiento on public.seguimiento as PERMISSIVE for SELECT to authenticated
  using (((EXISTS ( SELECT 1
   FROM solicitud_adopcion s
  WHERE ((s.id_solicitud = seguimiento.id_solicitud) AND (s.id_usuario = ( SELECT auth.uid() AS uid))))) OR ( SELECT solicitudes_es_admin() AS solicitudes_es_admin)));

-- Tabla: solicitud_adopcion
drop policy if exists modificar_solicitud_adopcion on public.solicitud_adopcion;
create policy modificar_solicitud_adopcion on public.solicitud_adopcion as PERMISSIVE for UPDATE to authenticated
  using (((id_usuario = ( SELECT auth.uid() AS uid)) AND (id_estado = ( SELECT estado_solicitud.id_estado
   FROM estado_solicitud
  WHERE ((estado_solicitud.descripcion)::text = 'Pendiente de Revisión'::text)))))
  with check (((id_usuario = ( SELECT auth.uid() AS uid)) AND (id_estado = ( SELECT estado_solicitud.id_estado
   FROM estado_solicitud
  WHERE ((estado_solicitud.descripcion)::text = 'Pendiente de Revisión'::text)))));

-- Tabla: solicitud_adopcion
drop policy if exists modificar_solicitud_adopcion_admin on public.solicitud_adopcion;
create policy modificar_solicitud_adopcion_admin on public.solicitud_adopcion as PERMISSIVE for UPDATE to authenticated
  using (( SELECT solicitudes_es_admin() AS solicitudes_es_admin))
  with check (( SELECT solicitudes_es_admin() AS solicitudes_es_admin));

-- Tabla: solicitud_adopcion
drop policy if exists registrar_solicitud_adopcion on public.solicitud_adopcion;
create policy registrar_solicitud_adopcion on public.solicitud_adopcion as PERMISSIVE for INSERT to authenticated
  with check (((id_usuario = ( SELECT auth.uid() AS uid)) AND (id_estado = ( SELECT estado_solicitud.id_estado
   FROM estado_solicitud
  WHERE ((estado_solicitud.descripcion)::text = 'Pendiente de Revisión'::text)))));

-- Tabla: solicitud_adopcion
drop policy if exists ver_solicitud_adopcion on public.solicitud_adopcion;
create policy ver_solicitud_adopcion on public.solicitud_adopcion as PERMISSIVE for SELECT to authenticated
  using (((id_usuario = ( SELECT auth.uid() AS uid)) OR ( SELECT solicitudes_es_admin() AS solicitudes_es_admin)));

-- Tabla: tamano
drop policy if exists eliminar_tamano on public.tamano;
create policy eliminar_tamano on public.tamano as PERMISSIVE for DELETE to authenticated
  using (true);

-- Tabla: tamano
drop policy if exists modificar_tamano on public.tamano;
create policy modificar_tamano on public.tamano as PERMISSIVE for UPDATE to authenticated
  using (true)
  with check (true);

-- Tabla: tamano
drop policy if exists registrar_tamano on public.tamano;
create policy registrar_tamano on public.tamano as PERMISSIVE for INSERT to authenticated
  with check (true);

-- Tabla: tamano
drop policy if exists ver_tamano on public.tamano;
create policy ver_tamano on public.tamano as PERMISSIVE for SELECT to public
  using (true);

-- Tabla: usuario
drop policy if exists "HU44_lectura_usuarios_prueba" on public.usuario;
create policy "HU44_lectura_usuarios_prueba" on public.usuario as PERMISSIVE for SELECT to authenticated
  using ((auth.uid() = '7f630a30-0696-4fb6-9975-f5bca4874abc'::uuid));

-- Tabla: usuario
drop policy if exists "signup_service puede insertar clientes" on public.usuario;
create policy "signup_service puede insertar clientes" on public.usuario as PERMISSIVE for INSERT to signup_service
  with check (((rol)::text = 'Cliente'::text));

-- ---------- 3) Buckets de Storage ----------
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types) values ('cfdi-documentos', 'cfdi-documentos', f, 10485760, '{application/pdf,application/xml}'::text[]) on conflict (id) do nothing;
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types) values ('constancias-fiscales', 'constancias-fiscales', f, 10485760, '{application/pdf,image/jpeg,image/png}'::text[]) on conflict (id) do nothing;
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types) values ('documento', 'documento', t, null, null) on conflict (id) do nothing;
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types) values ('multimedia_perros', 'multimedia_perros', t, null, null) on conflict (id) do nothing;

-- ---------- 4) Políticas de Storage ----------
