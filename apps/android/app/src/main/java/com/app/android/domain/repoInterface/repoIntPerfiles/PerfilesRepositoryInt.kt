package com.app.android.domain.repoInterface.repoIntPerfiles

import com.app.android.domain.entities.entitiesPerfiles.*

/**
 * **/
interface PerfilesRepositoryInt {
    // Autentica y guarda exclusivamente el token cifrado.
    suspend fun login(correo: String, contrasena: String): Result<Sesion>
    // Crear cuenta no inicia sesión: el backend devuelve únicamente el usuario.
    suspend fun register(registro: Registro): Result<Usuario>
    suspend fun profile(): Result<Perfil>
    suspend fun me(): Result<Usuario>
    suspend fun codigosPostales(cp: String): Result<List<CodigoPostal>>
    suspend fun logout(): Result<Unit>
    // null significa que no hay sesión; un fallo de red sigue siendo un error.
    suspend fun sesionActual(): Result<Sesion?>
}
