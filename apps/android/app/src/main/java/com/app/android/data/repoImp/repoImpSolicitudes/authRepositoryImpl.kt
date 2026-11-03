package com.app.android.data.repoImp.repoImpSolicitudes

import android.util.Log
import com.app.android.data.dataSource.dataSourcesSolicitudes.SessionStorage
import com.app.android.data.dataSource.dataSourcesSolicitudes.AuthApi
import com.app.android.data.dataSource.dataSourcesSolicitudes.LoginRequestDto
import com.app.android.domain.repoInterface.repoIntSolicitudes.AuthRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val storage: SessionStorage,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = try {
        val response = api.login(LoginRequestDto(email.trim(), password))
        storage.save(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            expiresAtMillis = System.currentTimeMillis() + response.expiresIn * 1000,
        )
        Result.success(Unit)
    } catch (e: HttpException) {
        val mensaje = when (e.code()) {
            401 -> "Correo o contraseña incorrectos."
            403 -> "Confirma tu correo antes de iniciar sesión."
            429 -> "Demasiados intentos. Espera un momento."
            else -> "No se pudo iniciar sesión (${e.code()})."
        }
        Result.failure(IllegalStateException(mensaje))
    } catch (e: IOException) {
        Log.e("AuthRepo", "Fallo de red", e)
        Result.failure(IllegalStateException("No se pudo conectar con el servidor."))
    }

    override fun hasSession(): Boolean = storage.accessToken() != null

    override fun logout() = storage.clear()
}