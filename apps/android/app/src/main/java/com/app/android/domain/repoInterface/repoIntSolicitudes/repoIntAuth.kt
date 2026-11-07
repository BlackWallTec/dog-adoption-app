package com.app.android.domain.repoInterface.repoIntSolicitudes

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    fun hasSession(): Boolean
    fun logout()
}