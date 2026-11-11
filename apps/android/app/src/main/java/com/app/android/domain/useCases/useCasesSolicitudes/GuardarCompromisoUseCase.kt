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
    val acceptsReturn: String? = null,
    val petSpending: String? = null,
    val petFollowing: String? = null,
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
                acceptsReturn,
                petSpending,
                petFollowing,
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
            val errores =
                CompromisoErrors(
                    petNeeds = if (limpios.petNeeds.isBlank()) "Escribe tu respuesta" else null,
                    petSterilization = if (limpios.petSterilization.isBlank()) "Escribe tu respuesta" else null,
                    hasVet = if (limpios.hasVet == null) "Selecciona una opción" else null,
                    vetDetail = if (limpios.vetDetail.isBlank()) "Escribe tu respuesta" else null,
                    timeAlone = if (limpios.timeAlone.isBlank()) "Escribe tu respuesta" else null,
                    dailyRoutine = if (limpios.dailyRoutine.isBlank()) "Escribe tu respuesta" else null,
                    // Para los que tienen que marcar checkmark para continuar (deben de aceptar estos compromisos)
                    acceptsReturn = if (!limpios.acceptsReturn) "Debes de aceptar este compromiso para continuar" else null,
                    petSpending = if (!limpios.petSpending) "Debes confirmar que entiendes los gastos de tu mascota" else null,
                    petFollowing = if (!limpios.petFollowing) "Debes de aceptar el seguimiento de tu mascota para continuar" else null,
                )
            if (!errores.hasErrors) {
                borrador.actualizar { it.copy(compromiso = limpios) }
            }
            return errores
        }
    }
