package com.app.android.data.dataSource.dataSourcesVoluntariado

import com.google.gson.annotations.SerializedName

data class RespuestaApiDto<T>(
    @SerializedName("sucess") val exito: Boolean,
    @SerializedName("data") val data: T?,
)

data class ErrorApiDto(
    @SerializedName("sucess") val exito: Boolean,
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?,
)