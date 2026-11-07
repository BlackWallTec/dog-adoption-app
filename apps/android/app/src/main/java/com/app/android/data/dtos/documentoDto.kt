package com.app.android.data.dtos

import com.google.gson.annotations.SerializedName

data class documentoDto(
    @SerializedName("tipo") val tipo: String,
    @SerializedName("fileName") val nombreArchivo: String,
    @SerializedName("sizeBytes") val tamanoBytes: Long,
)