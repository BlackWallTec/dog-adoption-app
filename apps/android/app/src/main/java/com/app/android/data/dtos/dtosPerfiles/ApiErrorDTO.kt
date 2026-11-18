package com.app.android.data.dtos.dtosPerfiles

import com.google.gson.annotations.SerializedName

// Cuerpo de error del backend
data class ApiErrorDTO(
    @SerializedName("code") val code: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("fields") val fields: Map<String, String>?,
)