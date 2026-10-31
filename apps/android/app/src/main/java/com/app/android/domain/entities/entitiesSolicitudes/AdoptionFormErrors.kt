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
    val hadPet: String? = null,
    val otherPets: String? = null,
    val whyAdopt: String? = null,
    val whereLive: String? = null,
    val peopleInHouse: String? = null,
    val allAgree: String? = null,
    val houseType: String? = null,
    val hasGarden: String? = null,
    val petAcept: String? = null,
    val aboutPets: String? = null,
    // Paso 4: del Formulario | "Compromiso"
    val petNeeds: String? = null,
    val petSterilization: String? = null,
    val petFollowing: String? = null,
    // Paso 5: el de andy, vi su estructura ahorita y creo que ya hizo estos pasos en su branch
) {
    // data | Valida si hay errores en el formulario
    fun hasErrors(): Boolean =
        listOf(
            name,
            age,
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
            aboutPets,
            petNeeds,
            petSterilization,
            petFollowing,
        ).any { it != null }
}
