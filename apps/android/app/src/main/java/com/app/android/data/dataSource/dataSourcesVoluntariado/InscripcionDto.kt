package com.app.android.data.dataSource.dataSourcesVoluntariado

import com.google.gson.annotations.SerializedName

data class InscripcionDto(
    @SerializedName("id_programa") val idPrograma: String,
    @SerializedName("fecha_inscripcion") val fechaInscripcion: String,
    @SerializedName("estado") val estado: String,
)