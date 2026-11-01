package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

// Un error por campo, null significa que esta bien ese campo
data class ErroresDatosGenerales(
    val nombre:String? = null,
    val ocupacion: String? = null,
    val domicilio: String? = null,
    val municipio: String? = null,
    val telefono: String? = null,
    val correo: String? = null,
) {
    // Ture si un campo tiene un error (si uno o mas)
    val hayErrores: Boolean
        get() = listOf(nombre,ocupacion,domicilio,municipio,telefono,correo).any { it != null }
}

class GuardarDatosGeneralesUseCase
    @Inject
    constructor(
        private val repository: SolicitudRepositoryInt,
    ) {
        private val formatoCorreo = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    suspend operator fun invoke(
        idSolicitud: String,
        datos: DatosGenerales,
    ): ErroresDatosGenerales {
        // 1. Limpieza de espacios al principio y al final
        val limpios =
            datos.copy(
                nombreCompleto = datos.nombreCompleto.trim(),
                ocupacion = datos.ocupacion.trim(),
                domicilio = datos.domicilio.trim(),
                municipio = datos.municipio.trim(),
                telefono = datos.telefono.filter { it.isDigit() }, // solo num
                correo = datos.correo.trim(),
            )

        // Validación de los campos
        val errores =
            ErroresDatosGenerales(
                nombre = if (limpios.nombreCompleto.isEmpty()) "Escribe tu nombre" else null,
                ocupacion = if (limpios.ocupacion.isEmpty()) "Escribe tu ocupación" else null,
                domicilio = if (limpios.domicilio.isEmpty()) "Escribe tu domicilio" else null,
                municipio = if (limpios.municipio.isEmpty()) "Escribe tu municipio" else null,
                telefono = if (limpios.telefono.length != 10) "El teléfono debe tener 10 dígitos" else null,
                correo = if (!formatoCorreo.matches(limpios.correo)) "Escribe un correo válido" else null,
            )

        // 3. Solo se guarda si todo esta bien
        if (!errores.hayErrores) {
            repository.guardarDatosGenerales(idSolicitud, limpios)
        }
        return errores
    }
}