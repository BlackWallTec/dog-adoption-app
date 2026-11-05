package com.app.android.data.dtos.dtosSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
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

// ===== GET /api/solicitudes/cliente/perfil/:idUsuario =====

// { success, data: { ...perfil plano... } }
data class ApiResponseDatosGeneralesDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: DatosGeneralesDto?,
    @SerializedName("message") val message: String?,
)

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