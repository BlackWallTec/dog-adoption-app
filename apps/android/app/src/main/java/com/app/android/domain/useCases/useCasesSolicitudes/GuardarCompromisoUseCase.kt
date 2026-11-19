package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import javax.inject.Inject

// Errores del paso 4: un mensaje por campo (null = ese campo esta bien)
data class CompromisoErrors(
    val petNeeds: String? = null,
    val petSterilization: String? = null,
    val hasVet: String? = null,
    val vetDetail: String? = null,
    val timeAlone: String? = null,
    val dailyRoutine: String? = null,
    val dogManagement: String? = null,
) {
    val hasErrors: Boolean
        get() =
            listOf(
                petNeeds,
                petSterilization,
                hasVet,
                vetDetail,
                timeAlone,
                dailyRoutine,
                dogManagement,
            ).any { it != null }
}

class GuardarCompromisoUseCase
    @Inject
    constructor(
        private val borrador: BorradorRepositoryInt,
    ) {
        // Valida si todo esta bien, guarda. Regresa los errores (vacios si se guardo)
        operator fun invoke(datos: Compromiso): CompromisoErrors {
            val limpios =
                datos.copy(
                    petNeeds = datos.petNeeds.trim(),
                    petSterilization = datos.petSterilization.trim(),
                    vetDetail = datos.vetDetail.trim(),
                    timeAlone = datos.timeAlone.trim(),
                    dailyRoutine = datos.dailyRoutine.trim(),
                    dogManagement = datos.dogManagement.trim(),
                )
            val maxLen = 250
            val errores =
                CompromisoErrors(
                    petNeeds =
                        when {
                            limpios.petNeeds.isBlank() -> "Escribe tu respuesta"
                            limpios.petNeeds.length > maxLen -> "No puede superar los 250 caracteres"
                            else -> null
                        },
                    petSterilization =
                        when {
                            limpios.petSterilization.isBlank() -> "Escribe tu respuesta"
                            limpios.petSterilization.length > maxLen -> "No puede superar los 250 caracteres"
                            else -> null
                        },
                    hasVet = if (limpios.hasVet == null) "Selecciona una opción" else null,
                    vetDetail =
                        when {
                            limpios.vetDetail.isBlank() -> "Escribe tu respuesta"
                            limpios.vetDetail.length > maxLen -> "No puede superar los 250 caracteres"
                            else -> null
                        },
                    timeAlone =
                        when {
                            limpios.timeAlone.isBlank() -> "Escribe tu respuesta"
                            limpios.timeAlone.length > maxLen -> "No puede superar los 250 caracteres"
                            else -> null
                        },
                    dailyRoutine =
                        when {
                            limpios.dailyRoutine.isBlank() -> "Escribe tu respuesta"
                            limpios.dailyRoutine.length > maxLen -> "No puede superar los 250 caracteres"
                            else -> null
                        },
                    dogManagement =
                        when {
                            limpios.dogManagement.isBlank() -> "Escribe tu respuesta"
                            limpios.dogManagement.length > maxLen -> "No puede superar los 250 caracteres"
                            else -> null
                        },
                )
            if (!errores.hasErrors) {
                borrador.actualizar { it.copy(compromiso = limpios) }
            }
            return errores
        }
    }
