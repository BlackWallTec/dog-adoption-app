package com.app.android.domain.useCases.useCasesDonativos

import com.app.android.domain.entities.entitiesDonativos.ConstanciaArchivo
import com.app.android.domain.entities.entitiesDonativos.DatosFiscales
import com.app.android.domain.repoInterface.repoIntDonativos.DatosFiscalesRepositoryInt
import javax.inject.Inject

// RFC: 3 letras (empresa) o 4 (persona) + fecha + homoclave. Igual que en el backend
private val RFC_REGEX = Regex("^[A-ZÑ&]{3,4}\\d{6}[A-Z\\d]{3}$")
private val CP_REGEX = Regex("^\\d{5}$")
private val CORREO_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

// CA-02: si falta un dato se le marca cuál. null = está bien
data class ErroresDatosFiscales(
    val rfc: String? = null,
    val razonSocial: String? = null,
    val regimenFiscal: String? = null,
    val codigoPostal: String? = null,
    val correoFacturacion: String? = null,
    val constancia: String? = null,
) {
    val hayErrores: Boolean
        get() =
            listOf(rfc, razonSocial, regimenFiscal, codigoPostal, correoFacturacion, constancia)
                .any { it != null }
}

class GuardarDatosFiscalesUseCase
    @Inject
    constructor(
        private val repository: DatosFiscalesRepositoryInt,
    ) {
        // constanciaNueva = el archivo que acaba de escoger (null si no cambió la que ya tenía)
        suspend operator fun invoke(
            idUsuario: String,
            datos: DatosFiscales,
            constanciaNueva: ConstanciaArchivo?,
        ): ErroresDatosFiscales {
            val limpios =
                datos.copy(
                    rfc = datos.rfc.trim().uppercase(),
                    razonSocial = datos.razonSocial.trim(),
                    codigoPostal = datos.codigoPostal.trim(),
                    correoFacturacion = datos.correoFacturacion.trim(),
                )

            val errores =
                ErroresDatosFiscales(
                    rfc =
                        when {
                            limpios.rfc.isEmpty() -> "Escribe tu RFC"
                            !RFC_REGEX.matches(limpios.rfc) -> "Revisa tu RFC, debe tener 12 o 13 caracteres"
                            else -> null
                        },
                    razonSocial = if (limpios.razonSocial.isEmpty()) "Escribe tu razón social" else null,
                    regimenFiscal = if (limpios.regimenFiscal.isEmpty()) "Selecciona tu régimen fiscal" else null,
                    codigoPostal = if (!CP_REGEX.matches(limpios.codigoPostal)) "El código postal tiene 5 números" else null,
                    correoFacturacion =
                        if (!CORREO_REGEX.matches(limpios.correoFacturacion)) "Escribe un correo válido" else null,
                    constancia =
                        if (constanciaNueva == null && limpios.constanciaUrl == null) {
                            "Sube tu constancia de situación fiscal"
                        } else {
                            null
                        },
                )

            if (!errores.hayErrores) {
                repository.guardarDatosFiscales(idUsuario, limpios)
                // la constancia va aparte porque es un archivo
                if (constanciaNueva != null) {
                    repository.subirConstancia(idUsuario, constanciaNueva)
                }
            }
            return errores
        }
    }
