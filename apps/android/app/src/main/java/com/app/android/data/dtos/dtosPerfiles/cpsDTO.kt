package com.app.android.data.dtos.dtosPerfiles
import com.google.gson.annotations.SerializedName

data class cpsResponseDTO(
    @SerializedName("id_codigo_postal") val id_codigo_postal: String?,
    @SerializedName("codigo_postal") val codigo_postal: String?,
    @SerializedName("colonia") val colonia: String?,
    @SerializedName("municipio") val municipio: String?,
    @SerializedName("estado") val estado: String?,
)