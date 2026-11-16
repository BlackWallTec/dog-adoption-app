package com.app.android.data.repoImp.repoImpPerfiles

import com.app.android.data.TokenProvider
import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.*
import com.app.android.domain.entities.entitiesPerfiles.*
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UsuarioRepositoryImpl @Inject constructor(
    private val api: UsuarioAPIservice,
    private val tokens: TokenProvider,
    private val gson: Gson,
) : PerfilesRepositoryInt {
    override suspend fun login(correo: String, contrasena: String): Result<Sesion> {
        var tokenGuardado: String? = null
        try {
            return ejecutar {
                val sesion = api.login(LoginRequestDTO(correo.trim(), contrasena)).toDomain()
                currentCoroutineContext().ensureActive()
                if (!tokens.save(sesion.token)) throw ErrorApi("No pudimos guardar tu sesión. Inténtalo de nuevo.")
                tokenGuardado = sesion.token
                sesion
            }
        } catch (e: CancellationException) {
            // Si se salió durante el guardado, no dejar una sesión que la UI nunca recibió.
            tokenGuardado?.let { token ->
                withContext(NonCancellable + Dispatchers.IO) {
                    runCatching { if (tokens.token() == token) tokens.clear() }
                }
            }
            throw e
        }
    }

    override suspend fun register(registro: Registro): Result<Usuario> = ejecutar {
        api.register(registro.toRequest()).user.toDomain()
    }

    override suspend fun profile(): Result<Perfil> = ejecutar(protegida = true) {
        api.profile().toDomain()
    }

    override suspend fun me(): Result<Usuario> = ejecutar(protegida = true) {
        api.me().user.toDomain()
    }

    override suspend fun codigosPostales(cp: String): Result<List<CodigoPostal>> = ejecutar {
        api.codigosPostales(cp).toDomain()
    }

    override suspend fun logout(): Result<Unit> = ejecutar { tokens.clear() }

    override suspend fun sesionActual(): Result<Sesion?> {
        val resultado: Result<Sesion?> = ejecutar(protegida = true) {
            val token = tokens.token()?.takeIf { it.isNotBlank() }
            if (token == null) null else Sesion(api.me().user.toDomain(), token)
        }
        return if ((resultado.exceptionOrNull() as? ErrorApi)?.sesionExpirada == true) {
            Result.success(null)
        } else resultado
    }

    // Una sola frontera para red, JSON y almacenamiento. Nunca convierte cancelación en error de UI.
    private suspend fun <T> ejecutar(protegida: Boolean = false, bloque: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(bloque())
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val cuerpo = try {
                    gson.fromJson(e.response()?.errorBody()?.string(), ApiErrorDTO::class.java)
                } catch (_: Exception) { null }
                val expirada = protegida && (e.code() == 401 || cuerpo?.code == "UNAUTHENTICATED")
                if (expirada) {
                    try { tokens.clear() } catch (_: Exception) {
                        return@withContext Result.failure(ErrorApi("No pudimos borrar la sesión guardada. Inténtalo de nuevo."))
                    }
                }
                Result.failure(ErrorApi(
                    mensaje = cuerpo?.error?.takeIf { it.isNotBlank() }
                        ?: "El servidor no pudo completar la solicitud (HTTP ${e.code()}).",
                    campos = cuerpo?.fields.orEmpty(),
                    sesionExpirada = expirada,
                ))
            } catch (e: ErrorApi) {
                Result.failure(e)
            } catch (_: IOException) {
                Result.failure(ErrorApi("No pudimos conectarnos. Revisa tu conexión e inténtalo de nuevo."))
            } catch (_: JsonParseException) {
                Result.failure(ErrorApi("No pudimos interpretar la respuesta del servidor."))
            } catch (_: Exception) {
                Result.failure(ErrorApi("No pudimos completar la operación en el dispositivo. Inténtalo de nuevo."))
            }
        }
}
