package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.CodigoError
import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repoInt: PerfilesRepositoryInt,
) {
    suspend operator fun invoke(correo: String, contrasena: String): Sesion {
        require(correo.isNotBlank() && contrasena.isNotBlank()) {
            "Correo y contraseña requeridos"
        }
        val sesion = repoInt.login(correo.trim(), contrasena)
        // Android solo acepta Cliente (los administradores entran por la web).
        // El repositorio ya guardó el token, así que se borra antes de rechazar.
        if (sesion.usuario.rol != ROL_CLIENTE) {
            repoInt.logout()
            throw ErrorApi(CodigoError.FORBIDDEN)
        }
        return sesion
    }

    private companion object {
        const val ROL_CLIENTE = "Cliente"
    }
}