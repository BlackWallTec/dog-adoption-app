package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import javax.inject.Inject

// Errores de la pantalla: con la opción A solo se edita la ocupación
data class ErroresDatosGenerales(
    val ocupacion: String? = null, // null = está bien
) {
    val hayErrores: Boolean
        get() = ocupacion != null
}

// REGLA: validar antes de guardar. Guarda en el BORRADOR (memoria), no en la BD.
class GuardarDatosGeneralesUseCase
@Inject
constructor(
    private val borrador: BorradorRepositoryInt,
) {
    // Ya no es suspend: escribir en memoria es instantáneo
    operator fun invoke(datos: DatosGenerales): ErroresDatosGenerales {
        val ocupacion = datos.ocupacion.trim()

        val errores =
            ErroresDatosGenerales(
                ocupacion = if (ocupacion.isEmpty()) "Escribe tu ocupación" else null,
            )

        if (!errores.hayErrores) {
            borrador.actualizar { it.copy(datosGenerales = datos.copy(ocupacion = ocupacion)) }
        }
        return errores
    }
}