package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.*
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

/**
 * Operación de domain para crear una cuenta a partir del modelo Registro.
 * Devuelve el usuario y el siguiente paso opcional de confirmación; no inicia sesión.
 * La creación y validación definitiva de los datos corresponden al backend.
 */
// @Inject indica a Hilt cómo construir este caso de uso y entregarle el repositorio.
class RegistrarUseCase @Inject constructor(private val repo: PerfilesRepositoryInt) {
    // suspend permite llamar operaciones suspendidas; operator invoke deja usar registrar(datos).
    suspend operator fun invoke(registro: Registro): Result<ResultadoRegistro> = repo.register(registro)
}
