package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject


class LoginUseCase @Inject constructor(
    private val repoInt: PerfilesRepositoryInt,
) {
    suspend operator fun invoke(email: String, passwd: String): Sesion {
        require(email.isNotBlank() && passwd.isNotBlank()) {
            "Correo y contraseña requeridos"
        }
        return repoInt.login(email.trim(), passwd)
    }
}