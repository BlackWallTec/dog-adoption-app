package com.app.android.data.dtos

import com.google.gson.annotations.SerializedName

data class documentoDto(
    @SerializedName("tipo") val tipo: String,
    @SerializedName("nombreArchivo") val nombreArchivo: String,
    @SerializedName("tamanoBytes") val tamanoBytes: Long,
)