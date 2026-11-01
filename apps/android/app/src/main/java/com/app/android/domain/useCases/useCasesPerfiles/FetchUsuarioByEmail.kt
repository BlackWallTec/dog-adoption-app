package com.app.android.domain.useCases.useCasesPerfiles
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class FetchUsuarioByEmail
@Inject
constructor(
    private val repoInt: PerfilesRepositoryInt,
) {
    // Al usar "operator fun invoke", permites que se llame como fetchUsuarioByEmailUseCase(id)
    suspend operator fun invoke(email: String): Usuario? {
        // Llama a la función fetchByEmail del repositorio
        return repoInt.fetchByEmail(email)
    }
}