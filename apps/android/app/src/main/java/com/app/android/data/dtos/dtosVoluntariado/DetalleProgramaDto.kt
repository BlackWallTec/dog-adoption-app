package com.app.android.data.dtos.dtosVoluntariado

import com.google.gson.annotations.SerializedName

// HU36 respuesta del backend { "success": true, "data": { ... } }
data class DetalleProgramaResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: DetalleProgramaDto?,
)

data class DetalleProgramaDto(
    @SerializedName("id_programa") val idPrograma: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("tipo_descripcion") val tipoDescripcion: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
    @SerializedName("ubicacion") val ubicacion: String?,
    @SerializedName("cupo_maximo") val cupoMaximo: Int?,
    @SerializedName("estado_del_programa") val estadoDelPrograma: String?,
    @SerializedName("inscritos") val inscritos: Int?,
    @SerializedName("usuario_inscrito") val usuarioInscrito: Boolean?,
)
