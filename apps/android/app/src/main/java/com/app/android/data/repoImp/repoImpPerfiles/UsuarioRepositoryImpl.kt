package com.app.android.data.repoImp.repoImpPerfiles

import com.app.android.data.TokenProvider
import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.ApiErrorDTO
import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.toDomain
import com.app.android.data.dtos.dtosPerfiles.toRequest
import com.app.android.domain.entities.entitiesPerfiles.CodigoError
import com.app.android.domain.entities.entitiesPerfiles.CodigoPostal
import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Perfil
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import com.google.gson.Gson
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UsuarioRepositoryImpl @Inject constructor(
    private val api: UsuarioAPIservice,
    private val tokens: TokenProvider,
    private val gson: Gson,
) : PerfilesRepositoryInt {

    override suspend fun login(correo: String, contrasena: String): Sesion {
        val dto = try {
            api.login(LoginRequestDTO(correo.trim(), contrasena))
        } catch (e: HttpException) {
            throw traducirError(e)
        } catch (_: IOException) {
            throw ErrorApi(CodigoError.SIN_CONEXION)
        } catch (_: JsonParseException) {
            throw ErrorApi(CodigoError.RESPUESTA_INVALIDA)
        }

        // Hoy el backend responde UNAUTHENTICATED cuando el correo no existe.
        // En login eso significa credenciales inválidas.
        val sesion = try {
            dto.toDomain()
        } catch (e: ErrorApi) {
            if (e.codigo == CodigoError.UNAUTHENTICATED) {
                throw ErrorApi(CodigoError.INVALID_CREDENTIALS, e.mensaje)
            }
            throw e
        }

        if (!tokens.save(sesion.token)) throw ErrorApi(CodigoError.GUARDADO_SESION)
        return sesion
    }

    override suspend fun register(registro: Registro): Usuario {
        return try {
            api.register(registro.toRequest()).user.toDomain()
        } catch (e: HttpException) {
            throw traducirError(e)
        } catch (_: IOException) {
            throw ErrorApi(CodigoError.SIN_CONEXION)
        } catch (_: JsonParseException) {
            throw ErrorApi(CodigoError.RESPUESTA_INVALIDA)
        }
    }

    override suspend fun profile(): Perfil {
        return try {
            api.profile().toDomain()
        } catch (e: HttpException) {
            val error = traducirError(e)
            // Si la sesión ya no sirve, borramos el token guardado.
            if (error.codigo == CodigoError.UNAUTHENTICATED) tokens.clear()
            throw error
        } catch (_: IOException) {
            throw ErrorApi(CodigoError.SIN_CONEXION)
        } catch (_: JsonParseException) {
            throw ErrorApi(CodigoError.RESPUESTA_INVALIDA)
        }
    }

    override suspend fun me(): Usuario {
        return try {
            api.me().user.toDomain()
        } catch (e: HttpException) {
            val error = traducirError(e)
            if (error.codigo == CodigoError.UNAUTHENTICATED) tokens.clear()
            throw error
        } catch (_: IOException) {
            throw ErrorApi(CodigoError.SIN_CONEXION)
        } catch (_: JsonParseException) {
            throw ErrorApi(CodigoError.RESPUESTA_INVALIDA)
        }
    }

    override suspend fun codigosPostales(cp: String): List<CodigoPostal> {
        return try {
            api.codigosPostales(cp).toDomain()
        } catch (e: HttpException) {
            throw traducirError(e)
        } catch (_: IOException) {
            throw ErrorApi(CodigoError.SIN_CONEXION)
        } catch (_: JsonParseException) {
            throw ErrorApi(CodigoError.RESPUESTA_INVALIDA)
        }
    }

    override suspend fun logout() {
        tokens.clear()
    }

    override suspend fun sesionActual(): Sesion? {
        val token = tokens.token()?.takeIf { it.isNotBlank() } ?: return null
        return try {
            Sesion(usuario = me(), token = token)
        } catch (e: ErrorApi) {
            // me() ya borró el token si fue UNAUTHENTICATED
            if (e.codigo == CodigoError.UNAUTHENTICATED) null else throw e
        }
    }

    // Convierte un error HTTP en ErrorApi leyendo el campo "code" del cuerpo JSON.
    private fun traducirError(e: HttpException): ErrorApi {
        val cuerpo = try {
            gson.fromJson(e.response()?.errorBody()?.string().orEmpty(), ApiErrorDTO::class.java)
        } catch (_: Exception) {
            null
        }
        // Sin "code" conocido (p. ej. un 502 del proxy) se usa el status HTTP.
        val codigo = CodigoError.values().firstOrNull { it.name == cuerpo?.code }
            ?: codigoPorStatus(e.code())
        return ErrorApi(codigo, cuerpo?.error, cuerpo?.fields.orEmpty())
    }

    private fun codigoPorStatus(status: Int): CodigoError = when (status) {
        400  -> CodigoError.BAD_REQUEST
        401  -> CodigoError.UNAUTHENTICATED
        403  -> CodigoError.FORBIDDEN
        409  -> CodigoError.CONFLICT
        422  -> CodigoError.VALIDATION_ERROR
        429  -> CodigoError.RATE_LIMITED
        else -> CodigoError.INTERNAL_ERROR
    }
}