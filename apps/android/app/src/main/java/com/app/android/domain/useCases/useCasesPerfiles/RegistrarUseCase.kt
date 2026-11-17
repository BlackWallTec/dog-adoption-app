package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.*
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

/**
 * Operación de domain para crear una cuenta a partir del modelo Registro.
 * Solicita register al contrato del repositorio y devuelve Result<Usuario> al ViewModel.
 * La creación y validación definitiva de los datos corresponden al backend.
 */
class RegistrarUseCase @Inject constructor(private val repo: PerfilesRepositoryInt) {
    suspend operator fun invoke(registro: Registro): Result<Usuario> = repo.register(registro)
}
