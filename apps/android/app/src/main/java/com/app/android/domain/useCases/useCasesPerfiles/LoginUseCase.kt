package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class LoginUseCase
// Para inyectar en viewmodel
@Inject constructor(private val repo: PerfilesRepositoryInt) {
    // Permite invocar con el nombre de la clase
    suspend operator fun invoke(correo: String, contrasena: String): Result<Sesion> {

        if (correo.isBlank() || contrasena.isBlank()) {
            return Result.failure(ErrorApi("Correo y contraseña requeridos."))
        }

        val resultado = repo.login(correo.trim(), contrasena)
        val sesion = resultado.getOrNull() ?: return resultado

        if (sesion.usuario.rol == "Cliente") return resultado

        // Administrador o Root sólo acceden desde la web
        val cierre = repo.logout()

        return Result.failure(cierre.exceptionOrNull()
            ?: ErrorApi("Esta aplicación es para cuentas de Cliente. Accede desde la web."))
    }
}
