package com.app.android.data.dataSource.dataSourcesSolicitudes

import com.google.gson.annotations.SerializedName

data class DocumentoDto(
    @SerializedName("tipo") val tipo: String,
    @SerializedName("nombreArchivo") val nombreArchivo: String,
    @SerializedName("tamanoBytes") val tamanoBytes: Long,
)