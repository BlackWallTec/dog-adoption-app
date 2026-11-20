package com.app.android.domain.repoInterface.repoIntPerfiles

import com.app.android.domain.entities.entitiesPerfiles.*

// Aunque esto importa la interfaz con las firmas de las funciones, la implementación se encuentra
// en data

interface PerfilesRepositoryInt {
    // Autentica y guarda exclusivamente el token cifrado.
    suspend fun login(correo: String, contrasena: String): Result<Sesion>
    // Crear cuenta no inicia sesión: el backend devuelve únicamente el usuario.
    suspend fun register(registro: Registro): Result<ResultadoRegistro>
    suspend fun profile(): Result<Perfil>
    suspend fun me(): Result<Usuario>
    suspend fun codigosPostales(cp: String): Result<List<CodigoPostal>>
    suspend fun logout(): Result<Unit>
    // null significa que no hay sesión; un fallo de red sigue siendo un error.
    suspend fun sesionActual(): Result<Sesion?>
    suspend fun solicitarCodigo(correo: String, recuperar: Boolean): Result<Unit>
    suspend fun confirmarCorreo(correo: String, codigo: String): Result<Unit>
    suspend fun restablecerContrasena(correo: String, codigo: String, contrasena: String, repetida: String): Result<Unit>
}
