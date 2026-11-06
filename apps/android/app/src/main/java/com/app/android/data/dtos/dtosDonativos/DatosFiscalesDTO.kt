package com.app.android.data.dtos.dtosDonativos

import com.app.android.domain.entities.entitiesDonativos.DatosFiscales
import com.google.gson.annotations.SerializedName

// RESPUESTAS (lo que el backend manda)

// Envoltorio { success, data: { ... }, message }
data class ApiResponseDatosFiscalesDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: DatosFiscalesDto?,
    @SerializedName("message") val message: String?,
)

// La fila de datos_confidenciales (solo lo fiscal)
data class DatosFiscalesDto(
    @SerializedName("id_usuario") val idUsuario: String?,
    @SerializedName("rfc") val rfc: String?,
    @SerializedName("razon_social") val razonSocial: String?,
    @SerializedName("regimen_fiscal") val regimenFiscal: String?,
    @SerializedName("codigo_postal") val codigoPostal: String?,
    @SerializedName("correo") val correo: String?,
    @SerializedName("correo_facturacion") val correoFacturacion: String?,
    @SerializedName("constancia_url") val constanciaUrl: String?,
)

// Respuesta sin datos { success, message }
data class ApiResponseDonativosSimpleDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Map<String, String>?,
)

// PETICIONES (lo que se le manda en el body)

// Body del PUT /api/donativos/cliente/datos-fiscales/:idUsuario
data class GuardarDatosFiscalesRequestDto(
    @SerializedName("rfc") val rfc: String,
    @SerializedName("razon_social") val razonSocial: String,
    @SerializedName("regimen_fiscal") val regimenFiscal: String,
    @SerializedName("codigo_postal") val codigoPostal: String,
    @SerializedName("correo_facturacion") val correoFacturacion: String,
)

// ===== MAPPERS: DTO → Entity =====

fun DatosFiscalesDto.toDomain(): DatosFiscales =
    DatosFiscales(
        rfc = rfc ?: "",
        razonSocial = razonSocial ?: "",
        regimenFiscal = regimenFiscal ?: "",
        codigoPostal = codigoPostal ?: "",
        // si nunca puso uno se usa el de su cuenta
        correoFacturacion = correoFacturacion ?: correo ?: "",
        constanciaUrl = constanciaUrl,
    )

fun DatosFiscales.toRequest(): GuardarDatosFiscalesRequestDto =
    GuardarDatosFiscalesRequestDto(
        rfc = rfc,
        razonSocial = razonSocial,
        regimenFiscal = regimenFiscal,
        codigoPostal = codigoPostal,
        correoFacturacion = correoFacturacion,
    )
