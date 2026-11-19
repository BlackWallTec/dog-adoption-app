package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.HouseType
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
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
    val petAccept: String? = null,
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
                petAccept,
                aboutPets,
            ).any { it != null }
}

class GuardarTuHogarUseCase
    @Inject
    constructor(
        private val borrador: BorradorRepositoryInt,
    ) {
        operator fun invoke(datos: TuHogar): TuHogarErrors {
            val limpios =
                datos.copy(
                    whyAdopt = datos.whyAdopt.trim(),
                    whereLive = datos.whereLive.trim(),
                    hasGarden = datos.hasGarden.trim(),
                    aboutPets = datos.aboutPets.trim(),
                )
            val hasPets = limpios.hadPet == true || limpios.otherPets == true
            val needsPetAccept = limpios.houseType != HouseType.CASA

            val errores =
                TuHogarErrors(
                    hadPet = if (limpios.hadPet == null) "Selecciona una opción" else null,
                    otherPets = if (limpios.otherPets == null) "Selecciona una opción" else null,
                    whyAdopt = when {
                        limpios.whyAdopt.isBlank() -> "Cuéntanos por qué quieres adoptar"
                        limpios.whyAdopt.length > 100 -> "El motivo no puede superar los 100 caracteres"
                        else -> null
                    },
                    whereLive = when {
                        limpios.whereLive.isBlank() -> "Cuéntanos dónde vivirá y dormirá"
                        limpios.whereLive.length > 100 -> "La respuesta no puede superar los 100 caracteres"
                        else -> null
                    },
                    peopleInHouse = if (limpios.peopleInHouse < 1) "Debe de haber al menos 1 persona" else null,
                    allAgree = if (limpios.allAgree == null) "Selecciona una opción" else null,
                    houseType = null,
                    hasGarden = when {
                        limpios.hasGarden.isBlank() -> "Cuéntanos si tienes jardín o patio"
                        limpios.hasGarden.length > 50 -> "La respuesta no puede superar los 50 caracteres"
                        else -> null
                    },
                    petAccept = if (needsPetAccept && limpios.petAccept == null) "Indica si tu casero acepta mascotas" else null,
                    aboutPets = when {
                        hasPets && limpios.aboutPets.isBlank() -> "Cuéntanos sobre tus mascotas"
                        hasPets && limpios.aboutPets.length > 250 -> "La descripción no puede superar los 250 caracteres"
                        else -> null
                    },
                )
            if (!errores.hasErrors) {
                borrador.actualizar { it.copy(tuHogar = limpios) }
            }
            return errores
        }
    }
