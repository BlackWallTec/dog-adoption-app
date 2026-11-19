alter table public.solicitud_cfdi
  add column if not exists cfdi_uuid text,
  add column if not exists rfc_receptor text,
  add column if not exists monto_cfdi numeric(14, 2),
  add column if not exists pdf_storage_path text,
  add column if not exists xml_storage_path text,
  add column if not exists fecha_carga_cfdi timestamptz,
  add column if not exists id_administrador_carga uuid references public.usuario (id_usuario) on delete set null,
  add column if not exists correo_estado text not null default 'Pendiente de envío',
  add column if not exists correo_intentos integer not null default 0,
  add column if not exists correo_enviado_en timestamptz,
  add column if not exists correo_error text;

create unique index if not exists solicitud_cfdi_uuid_unico_idx
  on public.solicitud_cfdi (cfdi_uuid)
  where cfdi_uuid is not null;

create index if not exists solicitud_cfdi_correo_pendiente_idx
  on public.solicitud_cfdi (correo_estado, fecha_carga_cfdi)
  where correo_estado <> 'Enviado';

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'cfdi-documentos',
  'cfdi-documentos',
  false,
  10485760,
  array['application/pdf', 'application/xml']
)
on conflict (id) do update
set public = false,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

comment on column public.solicitud_cfdi.cfdi_uuid is
  'UUID del CFDI validado contra el XML original.';
comment on column public.solicitud_cfdi.pdf_storage_path is
  'Ruta privada del PDF del CFDI en Supabase Storage.';
comment on column public.solicitud_cfdi.xml_storage_path is
  'Ruta privada del XML del CFDI en Supabase Storage.';
comment on column public.solicitud_cfdi.correo_estado is
  'Estado separado de almacenamiento: Pendiente de envío, Enviando, Error o Enviado.';
