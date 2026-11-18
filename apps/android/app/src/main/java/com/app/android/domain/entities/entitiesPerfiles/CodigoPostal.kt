package com.app.android.domain.entities.entitiesPerfiles

// El id identifica la colonia; el número postal puede repetirse entre colonias.
data class CodigoPostal(
    val idCodigoPostal: String,
    val codigoPostal: String,
    val colonia: String,
    val municipio: String,
    val estado: String,
)