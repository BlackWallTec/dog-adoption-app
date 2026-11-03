package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

// Errores del paso 4: un mensaje por campo (null = ese campo esta bien)
data class CompromisoErrors(
    val petNeeds: String? = null,
    val petSterilization: String? = null,
    val petFollowing: String? = null,
) {
    val hasErrors: Boolean
        get() = listOf(petNeeds, petSterilization, petFollowing).any { it != null }
}

class GuardarCompromisoUseCase
    @Inject
    constructor(
        private val repository: SolicitudRepositoryInt,
    ) {
        // Valida si todo esta bien, guarda. Regresa los errores (vacios si se guardo)
        suspend operator fun invoke(
            idSolicitud: String,
            datos: Compromiso,
        ): CompromisoErrors {
            val limpios =
                datos.copy(
                    petNeeds = datos.petNeeds.trim(),
                    petSterilization = datos.petSterilization.trim(),
                )
            val errores =
                CompromisoErrors(
                    petNeeds = if (limpios.petNeeds.isBlank()) "Escribe tu respuesta" else null,
                    petSterilization = if (limpios.petSterilization.isBlank()) "Escribe tu respuesta" else null,
                    petFollowing = if (limpios.petFollowing.isBlank()) "Selecciona una opción" else null,
                )
            if (!errores.hasErrors) {
                repository.guardarCompromiso(idSolicitud, limpios)
            }
            return errores
        }
    }
