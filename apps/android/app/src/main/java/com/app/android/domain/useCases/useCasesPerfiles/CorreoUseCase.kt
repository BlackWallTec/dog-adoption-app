package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class CorreoUseCase @Inject constructor(private val repo: PerfilesRepositoryInt) {
    // Mismo formato que validateEmail en shared del backend: cualquier dominio válido.
    private val correoPermitido = Regex("^[^\\s@]{1,64}@(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}$", RegexOption.IGNORE_CASE)

    private fun validar(correo: String, codigo: String? = null): ErrorApi? = when {
        correo.trim().length > 254 || !correoPermitido.matches(correo.trim()) -> ErrorApi("Ingresa un correo válido, por ejemplo nombre@correo.com.")
        codigo != null && !Regex("^[0-9]{6}$").matches(codigo) -> ErrorApi("Escribe el código de seis dígitos.")
        else -> null
    }

    suspend fun solicitar(correo: String, recuperar: Boolean): Result<Unit> {
        validar(correo)?.let { return Result.failure(it) }
        return repo.solicitarCodigo(correo.trim().lowercase(), recuperar)
    }
    suspend fun confirmar(correo: String, codigo: String): Result<Unit> {
        validar(correo, codigo)?.let { return Result.failure(it) }
        return repo.confirmarCorreo(correo.trim().lowercase(), codigo)
    }
    suspend fun restablecer(correo: String, codigo: String, contrasena: String, repetida: String): Result<Unit> {
        validar(correo, codigo)?.let { return Result.failure(it) }
        if (contrasena.length < 8 || contrasena.toByteArray(Charsets.UTF_8).size > 72 ||
            contrasena.contains('\n') || contrasena.contains('\r') || contrasena != repetida)
            return Result.failure(ErrorApi("Usa al menos 8 caracteres, máximo 72 bytes, y repite la misma contraseña."))
        return repo.restablecerContrasena(correo.trim().lowercase(), codigo, contrasena, repetida)
    }
}
