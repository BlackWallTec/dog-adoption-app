package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.AdoptionForm
import com.app.android.domain.entities.entitiesSolicitudes.AdoptionFormErrors
import javax.inject.Inject

// Para validar los casos de error dentro del formulario (ValidationError dentro del diagrama
// de despliegue).
class ValidateAdoptionFormUseCase
    @Inject
    constructor() {
        operator fun invoke(form: AdoptionForm): AdoptionFormErrors =
            AdoptionFormErrors(
                // Paso 2: Formulario | "Datos generales" (propensos a cambiar)
                name = if (form.name.isBlank()) "El nombre es requerido" else null,
                age = if (form.age == null) "La edad es requerida" else null,
                occupation = if (form.occupation.isBlank()) "La ocupación es requerida" else null,
                address = if (form.address.isBlank()) "La dirección es requerida" else null,
                municipio = if (form.municipio.isBlank()) "Escribe tu municipio" else null,
                // Casos: 10 dígitos, solo números
                phone =
                    if (form.phone.length != 10 || !form.phone.all { it.isDigit() }) {
                        "El teléfono debe tener 10 dígitos"
                    } else {
                        null
                    },
                // Casos: @, puntos y vacíó
                email =
                    if (!form.email.contains("@") ||
                        !form.email.contains(".")
                    ) {
                        "El email no es válido"
                    } else if (form.email.isBlank()) {
                        "El email es requerido"
                    } else {
                        null
                    },
                // Paso 3: Formulario | "Tu hogar"
                hadPet = if (form.hadPet == null) "Selecciona una opción" else null,
                otherPets = if (form.otherPets == null) "Selecciona una opción" else null,
                whyAdopt = if (form.whyAdopt.isBlank()) "Escribe tu respuesta" else null,
                whereLive = if (form.whereLive.isBlank()) "Escribe tu respuesta" else null,
                peopleInHouse = if (form.peopleInHouse == 0) "Selecciona una opción" else null,
                allAgree = if (form.allAgree == null) "Selecciona una opción" else null,
                houseType = if (form.houseType.isBlank()) "Selecciona una opción" else null,
                hasGarden = if (form.hasGarden.isBlank()) "Selecciona una opción" else null,
                petAcept = if (form.petAcept == null) "Selecciona una opción" else null,
                aboutPets = if (form.aboutPets.isBlank()) "Escribe tu respuesta" else null,
                // Paso 4: Formulario | "Compromiso"
                petNeeds = if (form.petNeeds.isBlank()) "Escribe tu respuesta" else null,
                petSterilization = if (form.petSterilization.isBlank()) "Escribe tu respuesta" else null,
                petFollowing = if (form.petFollowing.isBlank()) "Selecciona una opción" else null,
            )
    }
