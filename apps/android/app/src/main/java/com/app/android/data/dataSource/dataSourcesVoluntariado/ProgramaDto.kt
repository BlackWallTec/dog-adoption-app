package com.app.android.data.dataSource.dataSourcesVoluntariado

import com.google.gson.annotations.SerializedName

// Envoltorio de la respuesta del backend: { "success": true, "data": [...] }
data class ProgramasResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ProgramaDto>
)

data class ProgramaDto(
    @SerializedName("id_programa") val idPrograma: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("estado_del_programa") val estadoDelPrograma: String?,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String
)