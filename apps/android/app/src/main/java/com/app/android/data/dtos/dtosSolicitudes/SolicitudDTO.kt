package com.app.android.data.dtos.dtosSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.google.gson.annotations.SerializedName

// ===== GET /api/solicitudes/cliente/activa =====

// { success, data: { id_solicitud, estado } }  — data = null si no tiene solicitud activa
data class ApiResponseSolicitudActivaDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: SolicitudActivaDto?,
    @SerializedName("message") val message: String?,
)

data class SolicitudActivaDto(
    @SerializedName("id_solicitud") val idSolicitud: String?,
    @SerializedName("estado") val estado: String?, // la descripción: "Pendiente de Revisión", etc.
)

// ===== GET /api/solicitudes/cliente/mis-solicitudes =====

// { success, data: [ ...solicitudes ] }  — data = [] si no tiene ninguna
data class ApiResponseMisSolicitudesDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: List<SolicitudClienteDto>?,
    @SerializedName("message") val message: String?,
)

data class SolicitudClienteDto(
    @SerializedName("id_solicitud") val idSolicitud: String?,
    @SerializedName("folio") val folio: String?,
    @SerializedName("fecha_solicitud") val fechaSolicitud: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("perro") val perro: PerroSolicitudDto?,
    @SerializedName("foto_url") val fotoUrl: String?,
    @SerializedName("documento_rechazado") val documentoRechazado: Boolean?,
    @SerializedName("comentario_admin") val comentarioAdmin: String?,
)

data class PerroSolicitudDto(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("foto_url") val fotoUrl: String?,
)

// ===== GET /api/solicitudes/cliente/perfil/:idUsuario =====

// { success, data: { ...perfil plano... } }
data class ApiResponseDatosGeneralesDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: DatosGeneralesDto?,
    @SerializedName("message") val message: String?,
)

// Los datos generales ya hechos buenos bonitos hermosos por formatProfile del backend
data class DatosGeneralesDto(
    @SerializedName("id_usuario") val idUsuario: String?,
    @SerializedName("nombre_completo") val nombreCompleto: String?,
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String?,
    @SerializedName("ocupacion") val ocupacion: String?,
    @SerializedName("domicilio") val domicilio: String?,
    @SerializedName("municipio") val municipio: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("correo") val correo: String?,
)

// ===== MAPPERS: DTO → Entity =====
// Paso 3 y 4

// Respuesta sin datos { success, message }
data class ApiResponseSimpleDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
)

// PETICIONES (lo que se le manda  en el body)

// Body del POST /api/solicitudes/cliente
data class CrearSolicitudRequestDto(
    @SerializedName("id_usuario") val idUsuario: String,
    @SerializedName("id_perro") val idPerro: String,
)

// ===== MAPPERS: DTO → Entity (Recibir información =====

fun SolicitudActivaDto.toDomain(): SolicitudResumen =
    SolicitudResumen(
        idSolicitud = idSolicitud ?: "",
        estado = estado.toEstadoSolicitud(),
    )

// Traduce el texto de la tabla estado_solicitud al enum de la app
fun String?.toEstadoSolicitud(): EstadoSolicitud =
    when (this) {
        "Pendiente de Revisión" -> EstadoSolicitud.PENDIENTE_REVISION
        "En Periodo de Prueba" -> EstadoSolicitud.EN_PERIODO_PRUEBA
        "Aprobada Definitiva" -> EstadoSolicitud.APROBADA
        "Rechazada" -> EstadoSolicitud.RECHAZADA
        else -> EstadoSolicitud.DESCONOCIDO
    }

fun DatosGeneralesDto.toDomain(): DatosGenerales =
    DatosGenerales(
        nombreCompleto = nombreCompleto ?: "",
        fechaNacimiento = fechaNacimiento,
        ocupacion = ocupacion ?: "",
        domicilio = domicilio ?: "",
        municipio = municipio ?: "",
        telefono = telefono ?: "",
        correo = correo ?: "",
    )

fun SolicitudClienteDto.toDomain(): SolicitudCliente =
    SolicitudCliente(
        idSolicitud = idSolicitud ?: "",
        folio = folio ?: "",
        fechaSolicitud = fechaSolicitud ?: "",
        estado = estado.toEstadoSolicitud(),
        nombrePerro = perro?.nombre?.takeIf { it.isNotBlank() },
        fotoUrl = perro?.fotoUrl?.takeIf { it.isNotBlank() },
        documentoRechazado = documentoRechazado ?: false,
        comentarioAdmin = comentarioAdmin?.takeIf { it.isNotBlank() },
    )
