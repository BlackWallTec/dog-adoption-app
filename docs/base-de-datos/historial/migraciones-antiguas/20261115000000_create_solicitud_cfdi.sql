create table public.solicitud_cfdi (
  id_solicitud_cfdi uuid primary key default gen_random_uuid(),
  id_donativo uuid not null references public.donativo (id_donativo) on delete restrict,
  estado text not null default 'Pendiente de atención',
  fecha_solicitud timestamptz not null default now(),
  constraint solicitud_cfdi_un_donativo unique (id_donativo)
);

create index solicitud_cfdi_estado_fecha_idx
  on public.solicitud_cfdi (estado, fecha_solicitud);

alter table public.solicitud_cfdi enable row level security;

comment on table public.solicitud_cfdi is
  'Solicitudes de CFDI asociadas a donativos; solo se consultan desde la API administrativa.';
