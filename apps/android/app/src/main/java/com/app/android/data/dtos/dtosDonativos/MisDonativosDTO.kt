package com.app.android.data.dtos.dtosDonativos

import com.app.android.domain.entities.entitiesDonativos.Donativo
import com.google.gson.annotations.SerializedName

// Respuesta de GET /api/donativos/cliente/mis-donativos (HU49)
data class ApiResponseMisDonativosDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: List<DonativoDto>?,
    @SerializedName("message") val message: String?,
)

data class DonativoDto(
    @SerializedName("id_donativo") val idDonativo: String?,
    @SerializedName("monto") val monto: Double?,
    @SerializedName("fecha") val fecha: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("cfdi_solicitado") val cfdiSolicitado: Boolean?,
)

fun DonativoDto.toDomain() =
    Donativo(
        id = idDonativo.orEmpty(),
        monto = monto ?: 0.0,
        fecha = fecha.orEmpty().take(10),
        estado = estado.orEmpty(),
        cfdiSolicitado = cfdiSolicitado == true,
    )
