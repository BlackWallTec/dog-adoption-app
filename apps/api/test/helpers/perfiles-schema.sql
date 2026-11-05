-- Solo para la base aislada de pruebas; no ejecutar en el proyecto de Supabase.
CREATE ROLE anon;
CREATE ROLE authenticated;
CREATE ROLE service_role BYPASSRLS;
CREATE TABLE public.rol (nombre varchar PRIMARY KEY);
INSERT INTO public.rol VALUES ('Cliente'), ('Administrador'), ('Root');
CREATE TABLE public.usuario (
  id_usuario uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  rol varchar NOT NULL REFERENCES public.rol(nombre), nombre varchar NOT NULL,
  apellidos varchar NOT NULL, contrasena varchar NOT NULL,
  fecha_nacimiento timestamp, fecha_registro timestamp DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE public.cliente (id_usuario uuid PRIMARY KEY REFERENCES public.usuario(id_usuario));
CREATE TABLE public.admin (id_usuario uuid PRIMARY KEY REFERENCES public.usuario(id_usuario));
CREATE TABLE public.datos_confidenciales (
  id_usuario uuid PRIMARY KEY REFERENCES public.usuario(id_usuario),
  correo varchar NOT NULL UNIQUE, telefono varchar, codigo_postal varchar,
  FOREIGN KEY (id_usuario) REFERENCES public.cliente(id_usuario)
);
CREATE TABLE public.direcciones_fisicas (
  id_direccion uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  id_usuario uuid NOT NULL REFERENCES public.usuario(id_usuario),
  calle varchar, numero_exterior integer
);
GRANT USAGE ON SCHEMA public TO anon, authenticated, service_role;
GRANT SELECT, INSERT ON ALL TABLES IN SCHEMA public TO service_role;
