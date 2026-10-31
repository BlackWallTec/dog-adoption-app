package com.app.android.domain.entities.entitiesSolicitudes

data class AdoptionFormErrors(
    // Algunos de estos datos al igual que en el AdoptionForm, falta confirmar con el modulo de
    // Perfiles si el cliente los rellena al crear la cuenta.
    val name: String? = null,
    val age: String? = null,
    val occupation: String? = null,
    val address: String? = null,
    val municipio: String? = null,
    val phone: String? = null,
    val email: String? = null,
) {
    // Valida si hay errores en el formulario
    fun hasErrors(): Boolean =
        listOf(name, occupation, address, municipio, phone, email)
            .any { it != null }
}
