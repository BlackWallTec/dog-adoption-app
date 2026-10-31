package com.app.android.domain.entities.entitiesSolicitudes

data class AdoptionRequest(
    // Aqui ponemos los datos para cuando
    // se haga la solicitud de adopción: formulario + como nos vamos a referir a la mascota
    val dogId: String,
    val form: AdoptionForm,
)
