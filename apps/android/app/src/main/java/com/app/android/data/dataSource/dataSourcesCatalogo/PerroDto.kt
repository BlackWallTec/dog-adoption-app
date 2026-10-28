package com.app.android.data.dataSource.dataSourcesCatalogo

import kotlinx.serialization.Serializable

@Serializable
data class PerroDto(
    val nombre= String,
    val edad: Int?=null,
    val foto_url: String? =null
)