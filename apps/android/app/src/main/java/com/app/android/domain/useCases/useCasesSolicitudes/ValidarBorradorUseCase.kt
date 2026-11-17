package com.app.android.domain.useCases.useCasesSolicitudes

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
            if (dg == null || dg.ocupacion.isBlank()) {
                return ValidacionBorradorResult.Invalido("Faltan los datos generales (ocupación).")
            }

            val hogar =
                borrador.tuHogar
                    ?: return ValidacionBorradorResult.Invalido("Falta completar la información de tu hogar.")
            if (hogar.hadPet == null) return ValidacionBorradorResult.Invalido("Falta indicar si has tenido mascotas.")
            if (hogar.otherPets == null) return ValidacionBorradorResult.Invalido("Falta indicar si tienes otros animales.")
            if (hogar.whyAdopt.isBlank()) return ValidacionBorradorResult.Invalido("Falta responder por qué deseas adoptar.")
            if (hogar.whereLive.isBlank()) return ValidacionBorradorResult.Invalido("Falta responder dónde vivirá el perrito.")
            if (hogar.peopleInHouse < 1) return ValidacionBorradorResult.Invalido("El número de personas en casa debe ser al menos 1.")
            if (hogar.allAgree == null) return ValidacionBorradorResult.Invalido("Falta indicar si todos están de acuerdo.")
            if (hogar.hasGarden.isBlank()) return ValidacionBorradorResult.Invalido("Falta indicar si tienes jardín o patio.")

            val hasPets = hogar.hadPet || hogar.otherPets
            if (hasPets && hogar.aboutPets.isBlank()) {
                return ValidacionBorradorResult.Invalido("Falta contar sobre tus mascotas.")
            }

            val comp =
                borrador.compromiso
                    ?: return ValidacionBorradorResult.Invalido("Falta completar el compromiso de adopción.")
            val maxLen = 300
            if (comp.petNeeds.isBlank()) return ValidacionBorradorResult.Invalido("Falta responder sobre los cuidados básicos.")
            if (comp.petNeeds.length > maxLen) return ValidacionBorradorResult.Invalido("Los cuidados básicos superan los 300 caracteres.")

            if (comp.petSterilization.isBlank()) return ValidacionBorradorResult.Invalido("Falta tu opinión sobre la esterilización.")
            if (comp.petSterilization.length > maxLen) {
                return ValidacionBorradorResult.Invalido("La opinión sobre esterilización supera los 300 caracteres.")
            }

            if (comp.hasVet == null) return ValidacionBorradorResult.Invalido("Falta indicar si cuentas con veterinario.")

            if (comp.vetDetail.isBlank()) return ValidacionBorradorResult.Invalido("Falta detalle del veterinario.")
            if (comp.vetDetail.length > maxLen) {
                return ValidacionBorradorResult.Invalido("El detalle del veterinario supera los 300 caracteres.")
            }

            if (comp.timeAlone.isBlank()) return ValidacionBorradorResult.Invalido("Falta responder cuánto tiempo estaría solo.")
            if (comp.timeAlone.length > maxLen) return ValidacionBorradorResult.Invalido("El tiempo solo supera los 300 caracteres.")

            if (comp.dailyRoutine.isBlank()) return ValidacionBorradorResult.Invalido("Falta describir la rutina diaria.")
            if (comp.dailyRoutine.length > maxLen) return ValidacionBorradorResult.Invalido("La rutina diaria supera los 300 caracteres.")

            if (comp.dogManagement.isBlank()) return ValidacionBorradorResult.Invalido("Falta responder sobre manejo de comportamiento.")
            if (comp.dogManagement.length > maxLen) {
                return ValidacionBorradorResult.Invalido("El manejo de comportamiento supera los 300 caracteres.")
            }

            val docs = borrador.documentos
            if (docs?.identificacion == null || docs.comprobanteDomicilio == null) {
                return ValidacionBorradorResult.Invalido("Faltan documentos obligatorios (INE y Comprobante de domicilio).")
            }

            return ValidacionBorradorResult.Valido
        }
    }
