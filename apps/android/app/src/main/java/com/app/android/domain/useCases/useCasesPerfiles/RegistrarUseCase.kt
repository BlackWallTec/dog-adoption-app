package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.CodigoError
import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.app.android.domain.entities.entitiesPerfiles.validarRegistro
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class RegistrarUseCase @Inject constructor(
    private val repoInt: PerfilesRepositoryInt,
) {
    suspend operator fun invoke(registro: Registro): Usuario {
        val errores = validarRegistro(registro)
        if (errores.isNotEmpty()) throw ErrorApi(CodigoError.VALIDATION_ERROR, campos = errores)
        return repoInt.register(registro)
    }
}