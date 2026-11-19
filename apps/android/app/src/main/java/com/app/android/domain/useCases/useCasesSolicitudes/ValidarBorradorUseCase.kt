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
            if (hogar.whyAdopt.length > 100) return ValidacionBorradorResult.Invalido("El motivo de adopción supera los 100 caracteres.")
            if (hogar.whereLive.isBlank()) return ValidacionBorradorResult.Invalido("Falta responder dónde vivirá el perrito.")
            if (hogar.whereLive.length >
                100
            ) {
                return ValidacionBorradorResult.Invalido("La respuesta de dónde vivirá supera los 100 caracteres.")
            }
            if (hogar.peopleInHouse < 1) return ValidacionBorradorResult.Invalido("El número de personas en casa debe ser al menos 1.")
            if (hogar.allAgree == null) return ValidacionBorradorResult.Invalido("Falta indicar si todos están de acuerdo.")
            if (hogar.hasGarden.isBlank()) return ValidacionBorradorResult.Invalido("Falta indicar si tienes jardín o patio.")
            if (hogar.hasGarden.length >
                50
            ) {
                return ValidacionBorradorResult.Invalido("La respuesta de jardín o patio supera los 50 caracteres.")
            }

            val hasPets = hogar.hadPet || hogar.otherPets
            if (hasPets && hogar.aboutPets.isBlank()) {
                return ValidacionBorradorResult.Invalido("Falta contar sobre tus mascotas.")
            }

            val comp = borrador.compromiso ?: return ValidacionBorradorResult.Invalido("Falta completar el compromiso de adopción.")
            val maxLen = 250
            if (comp.petNeeds.length > maxLen) return ValidacionBorradorResult.Invalido("Los cuidados básicos superan los 250 caracteres.")
            if (comp.petSterilization.length >
                maxLen
            ) {
                return ValidacionBorradorResult.Invalido("La opinión sobre esterilización supera los 250 caracteres.")
            }
            if (comp.vetDetail.length >
                maxLen
            ) {
                return ValidacionBorradorResult.Invalido("El detalle del veterinario supera los 250 caracteres.")
            }
            if (comp.timeAlone.length > maxLen) return ValidacionBorradorResult.Invalido("El tiempo solo supera los 250 caracteres.")
            if (comp.dailyRoutine.length > maxLen) return ValidacionBorradorResult.Invalido("La rutina diaria supera los 250 caracteres.")
            if (comp.dogManagement.length >
                maxLen
            ) {
                return ValidacionBorradorResult.Invalido("El manejo de comportamiento supera los 250 caracteres.")
            }

            val docs = borrador.documentos
            if (docs?.identificacion == null || docs.comprobanteDomicilio == null) {
                return ValidacionBorradorResult.Invalido("Faltan documentos obligatorios (INE y Comprobante de domicilio).")
            }

            return ValidacionBorradorResult.Valido
        }
    }
