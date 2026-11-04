package com.app.android.data.dtos.dtosCatalogo

import com.app.android.data.dtos.dtosCatalogo.DogDetailDataDto
import com.google.gson.annotations.SerializedName

data class ApiResponseCatalogDogsDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val dogsList: List<DogDetailDataDto>?, // Mismas estructuras internas
)
