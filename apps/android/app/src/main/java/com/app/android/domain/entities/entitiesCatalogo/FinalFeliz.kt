package com.app.android.domain.entities.entitiesCatalogo

// Un perro ya adoptado, publicado para la sección "Finales Felices"
data class FinalFeliz(
    val idPerro: String,
    val nombre: String,
    val fotoHogarUrl: String,
    val historia: String,
    val fechaAdopcion: String?,
    val estado: String,
    val publicado: Boolean,
)