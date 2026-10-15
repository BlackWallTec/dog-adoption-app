-- ==========================================
-- QARIÑO ANIMAL v1.1 - DATABASE INITIALIZATION
-- Actualizado según el Diccionario de Datos y MER
-- 15 de OCTUBRE de 2026
-- ==========================================

-- ==========================================
-- 1. SEGURIDAD, ROLES Y PRIVILEGIOS
-- ==========================================
CREATE TABLE Rol (
    Nombre VARCHAR(36) PRIMARY KEY,
    Descripcion VARCHAR(200),
    Activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE Privilegio (
    Privilegio VARCHAR(50) PRIMARY KEY
);

CREATE TABLE Rol_tiene_Privilegio (
    Privilegio VARCHAR(50),
    Nombre_Rol VARCHAR(36),
    PRIMARY KEY (Privilegio, Nombre_Rol),
    FOREIGN KEY (Privilegio) REFERENCES Privilegio(Privilegio) ON DELETE CASCADE,
    FOREIGN KEY (Nombre_Rol) REFERENCES Rol(Nombre) ON DELETE CASCADE
);

-- ==========================================
-- 2. USUARIOS Y DATOS CONFIDENCIALES
-- ==========================================
CREATE TABLE Usuario (
    ID_Usuario UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Rol VARCHAR(36) NOT NULL,
    Nombre VARCHAR(50) NOT NULL,
    Apellidos VARCHAR(50) NOT NULL,
    Contrasena VARCHAR(64) NOT NULL,
    Fecha_nacimiento TIMESTAMP,
    Fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (Rol) REFERENCES Rol(Nombre)
);

CREATE TABLE Cliente (
    ID_Usuario UUID PRIMARY KEY,
    FOREIGN KEY (ID_Usuario) REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE
);

CREATE TABLE Admin (
    ID_Usuario UUID PRIMARY KEY,
    FOREIGN KEY (ID_Usuario) REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE
);

CREATE TABLE Datos_confidenciales (
    ID_Usuario UUID PRIMARY KEY,
    RFC VARCHAR(13),
    Razon_social VARCHAR(150),
    Regimen_fiscal VARCHAR(3),
    Codigo_postal VARCHAR(5),
    Correo VARCHAR(100) UNIQUE NOT NULL,
    Correo_facturacion VARCHAR(100),
    Telefono VARCHAR(15),
    Constancia_URL VARCHAR(255),
    FOREIGN KEY (ID_Usuario) REFERENCES Cliente(ID_Usuario) ON DELETE CASCADE
);

-- ==========================================
-- DIRECCIONES Y CATÁLOGOS DE CÓDIGOS POSTALES
-- ==========================================
CREATE TABLE Catalogo_de_CPs (
    ID_Codigo_postal UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_postal CHAR(5) NOT NULL,
    colonia VARCHAR(100),
    municipio VARCHAR(100),
    estado VARCHAR(50),
    pais VARCHAR(50) DEFAULT 'México'
);

CREATE TABLE Direcciones_Fisicas (
    ID_Direccion UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ID_Usuario UUID NOT NULL,
    ID_Codigo_postal UUID,
    Calle VARCHAR(100),
    Numero_exterior INT,
    Numero_interior INT,
    FOREIGN KEY (ID_Usuario) REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,
    FOREIGN KEY (ID_Codigo_postal) REFERENCES Catalogo_de_CPs(ID_Codigo_postal) ON DELETE SET NULL
);

-- ==========================================
-- 3. CATÁLOGOS Y PERROS
-- ==========================================
CREATE TABLE Raza (
    ID_Raza UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Nombre VARCHAR(50) NOT NULL,
    Descripcion VARCHAR(255)
);

CREATE TABLE Tamano (
    ID_Tamano UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Nombre VARCHAR(50) NOT NULL,
    Descripcion VARCHAR(255)
);

CREATE TABLE Caracter (
    ID_Caracter UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Nombre VARCHAR(50) NOT NULL,
    Descripcion VARCHAR(255)
);

CREATE TABLE Cuidado_Especial (
    ID_Cuidado UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Descripcion VARCHAR(1000) NOT NULL
);

CREATE TABLE Perro (
    ID_Perro UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ID_Raza UUID NOT NULL,
    ID_Tamano UUID NOT NULL,
    Nombre VARCHAR(100) NOT NULL,
    Sexo CHAR(10),
    Edad_aprox INT,
    Tamano_medida FLOAT,
    Descripcion VARCHAR(1000),
    Esterilizado BOOLEAN DEFAULT FALSE,
    Vacunado BOOLEAN DEFAULT FALSE,
    Estado VARCHAR(1000) DEFAULT 'Disponible',
    Fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_Raza) REFERENCES Raza(ID_Raza),
    FOREIGN KEY (ID_Tamano) REFERENCES Tamano(ID_Tamano)
);

CREATE TABLE Multimedia (
    ID_Multimedia UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ID_Perro UUID NOT NULL,
    URL VARCHAR(1000) NOT NULL,
    Tipo CHAR(10),
    Fecha_subida TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_Perro) REFERENCES Perro(ID_Perro) ON DELETE CASCADE
);

-- Tablas Intermedias Perro
CREATE TABLE Perro_tiene_Caracter (
    ID_Perro UUID,
    ID_Caracter UUID,
    PRIMARY KEY (ID_Perro, ID_Caracter),
    FOREIGN KEY (ID_Perro) REFERENCES Perro(ID_Perro) ON DELETE CASCADE,
    FOREIGN KEY (ID_Caracter) REFERENCES Caracter(ID_Caracter) ON DELETE CASCADE
);

CREATE TABLE Perro_tiene_Cuidado_Especial (
    ID_Perro UUID,
    ID_Cuidado UUID,
    PRIMARY KEY (ID_Perro, ID_Cuidado),
    FOREIGN KEY (ID_Perro) REFERENCES Perro(ID_Perro) ON DELETE CASCADE,
    FOREIGN KEY (ID_Cuidado) REFERENCES Cuidado_Especial(ID_Cuidado) ON DELETE CASCADE
);

-- ==========================================
-- 4. ADOPCIONES Y DOCUMENTOS
-- ==========================================
CREATE TABLE Estado_solicitud (
    ID_Estado UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Descripcion VARCHAR(50) NOT NULL
);

CREATE TABLE Documento (
    ID_Documento UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ID_Usuario UUID NOT NULL,
    Tipo CHAR(10) NOT NULL,
    URL_archivo VARCHAR(100) NOT NULL,
    Fecha_subida TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Estado_revision CHAR(10) DEFAULT 'Pendiente',
    Comentario VARCHAR(1000),
    FOREIGN KEY (ID_Usuario) REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE
);

CREATE TABLE Solicitud_adopcion (
    ID_Solicitud UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ID_Usuario UUID NOT NULL,
    ID_Perro UUID NOT NULL,
    ID_Estado UUID NOT NULL,
    Fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Motivo VARCHAR(50),
    Tipo_vivienda CHAR(10),
    Otras_mascotas CHAR(20),
    Integrantes_hogar INT,
    Comentario_admin VARCHAR(1000),
    Fecha_resolucion TIMESTAMP,
    FOREIGN KEY (ID_Usuario) REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,
    FOREIGN KEY (ID_Perro) REFERENCES Perro(ID_Perro) ON DELETE CASCADE,
    FOREIGN KEY (ID_Estado) REFERENCES Estado_solicitud(ID_Estado)
);

CREATE TABLE Documento_Tiene_Solicitud_adopcion (
    ID_Documento UUID,
    ID_Solicitud UUID,
    Fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ID_Documento, ID_Solicitud),
    FOREIGN KEY (ID_Documento) REFERENCES Documento(ID_Documento) ON DELETE CASCADE,
    FOREIGN KEY (ID_Solicitud) REFERENCES Solicitud_adopcion(ID_Solicitud) ON DELETE CASCADE
);

CREATE TABLE Admin_Revisa_Solicitud_adopcion (
    ID_Usuario UUID, -- Refiere a Admin
    ID_Solicitud UUID,
    Fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ID_Usuario, ID_Solicitud),
    FOREIGN KEY (ID_Usuario) REFERENCES Admin(ID_Usuario) ON DELETE CASCADE,
    FOREIGN KEY (ID_Solicitud) REFERENCES Solicitud_adopcion(ID_Solicitud) ON DELETE CASCADE
);

CREATE TABLE Seguimiento (
    ID_Seguimiento UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ID_Solicitud UUID NOT NULL,
    Fecha_Inicio TIMESTAMP NOT NULL,
    Fecha_Fin TIMESTAMP,
    Descripcion VARCHAR(1000),
    FOREIGN KEY (ID_Solicitud) REFERENCES Solicitud_adopcion(ID_Solicitud) ON DELETE CASCADE
);

CREATE TABLE Periodo_Prueba (
    ID_Prueba UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ID_Solicitud UUID NOT NULL,
    Fecha_Inicio TIMESTAMP NOT NULL,
    Fecha_Fin_Prevista TIMESTAMP,
    Fecha_Fin_Real TIMESTAMP,
    Estatus_Prueba CHAR(20) DEFAULT 'En curso',
    Observaciones_Seguimiento VARCHAR(1000),
    FOREIGN KEY (ID_Solicitud) REFERENCES Solicitud_adopcion(ID_Solicitud) ON DELETE CASCADE
); 

-- ==========================================
-- 5. PROGRAMAS Y VOLUNTARIADO
-- ==========================================
CREATE TABLE Tipo_programa (
    Nombre VARCHAR(36) PRIMARY KEY,
    Descripcion VARCHAR(200)
);

CREATE TABLE Programa (
    ID_Programa UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Tipo VARCHAR(36) NOT NULL,
    ID_Admin UUID NOT NULL,
    Nombre VARCHAR(50) NOT NULL,
    Estado_del_Programa CHAR(20) DEFAULT 'Disponible',
    Descripcion VARCHAR(1000),
    Fecha_Inicio TIMESTAMP NOT NULL,
    Fecha_Fin TIMESTAMP NOT NULL,
    Ubicacion VARCHAR(150),
    Cupo_maximo INT,
    FOREIGN KEY (Tipo) REFERENCES Tipo_programa(Nombre),
    FOREIGN KEY (ID_Admin) REFERENCES Admin(ID_Usuario)
);

CREATE TABLE Se_inscribe (
    ID_Usuario UUID,
    ID_Programa UUID,
    Fecha_inscripcion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Estado CHAR(20) DEFAULT 'Aceptado',
    Asistencia BOOLEAN,
    PRIMARY KEY (ID_Usuario, ID_Programa),
    FOREIGN KEY (ID_Usuario) REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,
    FOREIGN KEY (ID_Programa) REFERENCES Programa(ID_Programa) ON DELETE CASCADE
);

-- ==========================================
-- 6. DONATIVOS
-- ==========================================
CREATE TABLE Donativo (
    ID_Donativo UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ID_Usuario UUID NOT NULL,
    ID_Admin UUID,
    ID_Documento UUID,
    Folio_fiscal CHAR(40),
    Tipo CHAR(20) NOT NULL,
    Monto DECIMAL(10, 2),
    Descripcion VARCHAR(1000),
    Fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Comprobante BYTEA,
    Estado VARCHAR(30) DEFAULT 'Pendiente de validación',
    CFDI_solicitado BOOLEAN DEFAULT FALSE,
    Comentario_admin VARCHAR(1000),
    FOREIGN KEY (ID_Usuario) REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,
    FOREIGN KEY (ID_Admin) REFERENCES Admin(ID_Usuario) ON DELETE SET NULL,
    FOREIGN KEY (ID_Documento) REFERENCES Documento(ID_Documento) ON DELETE SET NULL
);

-- ==========================================
-- 7. COMUNICACIONES
-- ==========================================
CREATE TABLE Mensaje (
    ID_Mensaje UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Titulo VARCHAR(100) NOT NULL,
    Texto VARCHAR(500) NOT NULL,
    Tipo CHAR(30)
);

CREATE TABLE Notifica (
    ID_Cliente UUID,
    ID_Mensaje UUID,
    Fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Leido BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (ID_Cliente, ID_Mensaje),
    FOREIGN KEY (ID_Cliente) REFERENCES Cliente(ID_Usuario) ON DELETE CASCADE,
    FOREIGN KEY (ID_Mensaje) REFERENCES Mensaje(ID_Mensaje) ON DELETE CASCADE
);
