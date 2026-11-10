package com.app.android.data.dataSource.dataSourcesCatalogo

import com.google.gson.annotations.SerializedName           ///

data class PerroDto(

    @SerializedName("id_perro") //nuevo
    val id: String,
    val nombre: String,
    @SerializedName("edad_aprox")
    val edad: Int? = null,
    @SerializedName("sexo")
    val sexo: String,
    @SerializedName("foto_url")
    val foto_url: String? = null
)