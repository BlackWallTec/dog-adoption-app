package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

data class TuHogarErrors(
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
) {
    val hasErrors: Boolean
        get() =
            listOf(
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
            ).any { it != null }
}

class GuardarTuHogarUseCase
    @Inject
    constructor(
        private val repository: SolicitudRepositoryInt,
    ) {
        // Valida si todo esta bien, guarda. Regresa los errores (vacios si se guardo)
        suspend operator fun invoke(
            idSolicitud: String,
            datos: TuHogar,
        ): TuHogarErrors {
            val limpios =
                datos.copy(
                    whyAdopt = datos.whyAdopt.trim(),
                    whereLive = datos.whereLive.trim(),
                    hasGarden = datos.hasGarden.trim(),
                    aboutPets = datos.aboutPets.trim(),
                )
            // Cuentanos sobre tus mascotas es obligatorio si tiene mascotas
            val hasPets = limpios.hadPet == true || limpios.otherPets == true

            val errores =
                TuHogarErrors(
                    hadPet = if (hasPets && limpios.hadPet == null) "Selecciona una opción" else null,
                    otherPets = if (hasPets && limpios.otherPets == null) "Selecciona una opción" else null,
                    whyAdopt = if (limpios.whyAdopt.isBlank()) "Cuéntanos por qué quieres adoptar" else null,
                    whereLive = if (limpios.whereLive.isBlank()) "Cuéntanos dónde vivirá y dormirá" else null,
                    peopleInHouse = if (limpios.peopleInHouse < 1) "Debe de haber al menos 1 persona" else null,
                    allAgree = if (limpios.allAgree == null) "Selecciona una opción" else null,
                    hasGarden = if (limpios.hasGarden.isBlank()) "Cuéntanos si tienes jardin o patio" else null,
                    aboutPets = if (hasPets && limpios.aboutPets.isBlank()) "Cuéntanos sobre tus mascotas" else null,
                )
            if (!errores.hasErrors) {
                repository.guardarTuHogar(idSolicitud, limpios)
            }
            return errores
        }
    }
