package com.app.android.domain.entities.entitiesSolicitudes

data class AdoptionRequest(
    // Aqui ponemos el formulario + como nos vamos a referir a la mascota
    val dogId: String,
    val form: AdoptionForm,
)
