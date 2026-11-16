package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class RestaurarSesionUseCase @Inject constructor(private val repo: PerfilesRepositoryInt) {
    suspend operator fun invoke(): Result<Sesion?> {
        val resultado = repo.sesionActual()
        val sesion = resultado.getOrNull() ?: return resultado
        if (sesion.usuario.rol == "Cliente") return resultado
        val cierre = repo.logout()
        return Result.failure(cierre.exceptionOrNull()
            ?: ErrorApi("Esta aplicación es para cuentas de Cliente. Accede desde la web."))
    }
}
