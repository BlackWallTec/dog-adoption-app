package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

// Errores de la pantalla (solo se edita la ocupacion (lo demas viene directo de cuando se crea el perfil)
// lo demás se puede editar en el modulo de perfil aqui entra esa info para leer y yua
data class ErroresDatosGenerales(
    val ocupacion: String? = null, // null = esta bien
) {
    val hayErrores: Boolean
        get() = ocupacion != null
}

class GuardarDatosGeneralesUseCase
@Inject
constructor(
    private val repository: SolicitudRepositoryInt,
) {
    suspend operator fun invoke(
        idSolicitud: String,
        datos: DatosGenerales,
    ): ErroresDatosGenerales {
        val ocupacion = datos.ocupacion.trim()

        val errores =
            ErroresDatosGenerales(
                ocupacion = if (ocupacion.isEmpty()) "Escribe tu ocupación" else null,
            )

        if (!errores.hayErrores) {
            repository.guardarDatosGenerales(idSolicitud, datos.copy(ocupacion = ocupacion))
        }
        return errores
    }
}