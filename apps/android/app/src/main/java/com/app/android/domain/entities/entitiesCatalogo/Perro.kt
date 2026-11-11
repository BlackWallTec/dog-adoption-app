package com.app.android.domain.entities.entitiesCatalogo
data class Perro(
    val id: String,
    val nombre: String,
    val edad: Int?,
    val sexo: String,
    val fotoUrl: String = ""
)