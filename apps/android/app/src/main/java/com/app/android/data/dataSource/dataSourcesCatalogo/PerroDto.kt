package com.app.android.data.dataSource.dataSourcesCatalogo

import com.google.gson.annotations.SerializedName           ///

data class PerroDto(
    val nombre: String,

    @SerializedName("edad_aprox")
    val edad: Int? = null,      //
    val foto_url: String? = null
)