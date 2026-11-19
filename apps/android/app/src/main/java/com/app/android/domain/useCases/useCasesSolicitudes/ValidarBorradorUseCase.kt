package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.HouseType
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import javax.inject.Inject

sealed interface ValidacionBorradorResult {
    data object Valido : ValidacionBorradorResult

    data class Invalido(
        val mensaje: String,
    ) : ValidacionBorradorResult
}

class ValidarBorradorUseCase
    @Inject
    constructor(
        private val borradorRepo: BorradorRepositoryInt,
    ) {
        operator fun invoke(): ValidacionBorradorResult {
            val borrador =
                borradorRepo.borrador.value
                    ?: return ValidacionBorradorResult.Invalido("No hay una solicitud en borrador activa.")

            if (!borrador.aceptoRequisitos) {
                return ValidacionBorradorResult.Invalido("Falta aceptar los requisitos de adopción.")
            }

            val dg = borrador.datosGenerales
            if (dg == null || dg.ocupacion.trim().isBlank()) {
                return ValidacionBorradorResult.Invalido("Faltan los datos generales (ocupación).")
            }
            if (dg.ocupacion.trim().length > 100) {
                return ValidacionBorradorResult.Invalido("La ocupación no puede superar los 100 caracteres.")
            }

            val hogar =
                borrador.tuHogar
                    ?: return ValidacionBorradorResult.Invalido("Falta completar la información de tu hogar.")
            if (hogar.hadPet == null) return ValidacionBorradorResult.Invalido("Falta indicar si has tenido mascotas.")
            if (hogar.otherPets == null) return ValidacionBorradorResult.Invalido("Falta indicar si tienes otros animales.")

            val whyAdopt = hogar.whyAdopt.trim()
            if (whyAdopt.isBlank()) return ValidacionBorradorResult.Invalido("Falta responder por qué deseas adoptar.")
            if (whyAdopt.length > 100) return ValidacionBorradorResult.Invalido("El motivo de adopción no puede superar los 100 caracteres.")

            val whereLive = hogar.whereLive.trim()
            if (whereLive.isBlank()) return ValidacionBorradorResult.Invalido("Falta responder dónde vivirá el perrito.")
            if (whereLive.length > 100) return ValidacionBorradorResult.Invalido("La respuesta de dónde vivirá no puede superar los 100 caracteres.")

            if (hogar.peopleInHouse < 1) return ValidacionBorradorResult.Invalido("El número de personas en casa debe ser al menos 1.")
            if (hogar.allAgree == null) return ValidacionBorradorResult.Invalido("Falta indicar si todos están de acuerdo.")

            val hasGarden = hogar.hasGarden.trim()
            if (hasGarden.isBlank()) return ValidacionBorradorResult.Invalido("Falta indicar si tienes jardín o patio.")
            if (hasGarden.length > 50) return ValidacionBorradorResult.Invalido("La respuesta de jardín o patio no puede superar los 50 caracteres.")

            if (hogar.houseType != HouseType.CASA) {
                if (hogar.petAccept == null) {
                    return ValidacionBorradorResult.Invalido("Si rentas o vives en departamento/otro, indica si tu casero acepta mascotas.")
                }
            }

            val hasPets = hogar.hadPet == true || hogar.otherPets == true
            if (hasPets) {
                val aboutPets = hogar.aboutPets.trim()
                if (aboutPets.isBlank()) return ValidacionBorradorResult.Invalido("Falta contar sobre tus mascotas.")
                if (aboutPets.length > 250) return ValidacionBorradorResult.Invalido("La descripción sobre tus mascotas no puede superar los 250 caracteres.")
            }

            val comp = borrador.compromiso ?: return ValidacionBorradorResult.Invalido("Falta completar el compromiso de adopción.")
            val maxLen = 250

            val petNeeds = comp.petNeeds.trim()
            if (petNeeds.isBlank()) return ValidacionBorradorResult.Invalido("Falta indicar los cuidados básicos.")
            if (petNeeds.length > maxLen) return ValidacionBorradorResult.Invalido("Los cuidados básicos no pueden superar los 250 caracteres.")

            val petSterilization = comp.petSterilization.trim()
            if (petSterilization.isBlank()) return ValidacionBorradorResult.Invalido("Falta la opinión sobre esterilización.")
            if (petSterilization.length > maxLen) return ValidacionBorradorResult.Invalido("La opinión sobre esterilización no puede superar los 250 caracteres.")

            if (comp.hasVet == null) return ValidacionBorradorResult.Invalido("Falta indicar si tienes veterinario de confianza.")

            val vetDetail = comp.vetDetail.trim()
            if (vetDetail.isBlank()) return ValidacionBorradorResult.Invalido("Falta el detalle del veterinario.")
            if (vetDetail.length > maxLen) return ValidacionBorradorResult.Invalido("El detalle del veterinario no puede superar los 250 caracteres.")

            val timeAlone = comp.timeAlone.trim()
            if (timeAlone.isBlank()) return ValidacionBorradorResult.Invalido("Falta indicar el tiempo que estaría sola la mascota.")
            if (timeAlone.length > maxLen) return ValidacionBorradorResult.Invalido("El tiempo solo no puede superar los 250 caracteres.")

            val dailyRoutine = comp.dailyRoutine.trim()
            if (dailyRoutine.isBlank()) return ValidacionBorradorResult.Invalido("Falta describir la rutina diaria.")
            if (dailyRoutine.length > maxLen) return ValidacionBorradorResult.Invalido("La rutina diaria no puede superar los 250 caracteres.")

            val dogManagement = comp.dogManagement.trim()
            if (dogManagement.isBlank()) return ValidacionBorradorResult.Invalido("Falta indicar el manejo de comportamiento.")
            if (dogManagement.length > maxLen) return ValidacionBorradorResult.Invalido("El manejo de comportamiento no puede superar los 250 caracteres.")

            val docs = borrador.documentos
            if (docs?.identificacion == null || docs.comprobanteDomicilio == null) {
                return ValidacionBorradorResult.Invalido("Faltan documentos obligatorios (INE y Comprobante de domicilio).")
            }

            return ValidacionBorradorResult.Valido
        }
    }
