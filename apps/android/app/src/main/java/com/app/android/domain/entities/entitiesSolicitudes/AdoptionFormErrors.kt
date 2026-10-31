package com.app.android.domain.entities.entitiesSolicitudes

data class AdoptionFormErrors(
    // Algunos de estos datos al igual que en el AdoptionForm, falta confirmar con el modulo de
    // Perfiles si el cliente los rellena al crear la cuenta.
    // Paso 2 del Formulario | "Datos generales"
    val name: String? = null,
    val age: String? = null,
    val occupation: String? = null,
    val address: String? = null,
    val municipio: String? = null,
    val phone: String? = null,
    val email: String? = null,
    // Paso 3 del Formulario | "Tu Hogar"
    // Aunque hay unas partes del formulario que son incisos de los cuales
    // no estoy seguro si van a empezar sin una opción elegida
    // voy a agarrar todos los datos y ponerlos en la funcion hasErrors()
    val hadPet: Boolean? = null,
    val otherPets: Boolean? = null,
    val whyAdopt: String? = null,
    val whereLive: String? = null,
    val peopleInHouse: String? = null,
    val allAgree: Boolean? = null,
    val houseType: String? = null,
    val hasGarden: String? = null,
    val petAcept: Boolean? = null,
    // Paso 4: del Formulario | "Compromiso"
    val petNeeds: String? = null,
    val petSterilization: String? = null,
    val petFollowing: String? = null,
) {
    // data | Valida si hay errores en el formulario
    fun hasErrors(): Boolean =
        listOf(
            name,
            occupation,
            address,
            municipio,
            phone,
            email,
            hadPet,
            otherPets,
            whyAdopt,
            whereLive,
            peopleInHouse,
            allAgree,
            houseType,
            hasGarden,
            petAcept,
            petNeeds,
            petSterilization,
            petFollowing,
        ).any { it != null }
}
