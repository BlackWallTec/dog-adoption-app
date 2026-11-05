package com.app.android.data.repoImp.repoImpPerfiles

import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.ErrorRegistroDTO
import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.toDTO
import com.app.android.data.dtos.dtosPerfiles.toDomain
import com.app.android.domain.entities.entitiesPerfiles.*
import com.app.android.domain.repoInterface.repoIntPerfiles.AlmacenSesion
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import com.google.gson.Gson
import com.google.gson.JsonParseException
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException

class UsuarioRepositoryImpl @Inject constructor(
    private val api: UsuarioAPIservice,
    private val almacen: AlmacenSesion,
) : PerfilesRepositoryInt {
    override suspend fun login(email: String, passwd: String): Sesion = remoto(credenciales = true) {
        api.login(LoginRequestDTO(email.trim().lowercase(Locale.ROOT), passwd)).toDomain().also { guardar(it) }
    }

    override suspend fun registrar(datos: Registro): Sesion {
        val normalizado = datos.normalizado()
        val errores = validarRegistro(normalizado)
        if (errores.isNotEmpty()) throw RegistroInvalidoException(errores)
        return remoto { api.registrar(normalizado.toDTO()).toDomain().also { guardar(it) } }
    }

    override suspend fun restaurarSesion(): Sesion? {
        val guardada = withContext(Dispatchers.IO) { almacen.cargar() } ?: return null
        return try {
            remoto {
                val usuario = api.sesion().user?.toDomain() ?: error("Respuesta sin usuario")
                guardada.copy(usuario = usuario).also { guardar(it) }
            }
        } catch (_: SesionVencidaException) { null }
    }

    override suspend fun consultarPerfil(): Perfil = remoto {
        api.perfil().profile?.toDomain() ?: error("Respuesta sin perfil")
    }

    override suspend fun cerrarSesion(notificarServidor: Boolean) {
        withContext(NonCancellable + Dispatchers.IO) { almacen.limpiar() }
        if (!notificarServidor) return
        try { withTimeoutOrNull(1500) { api.logout() } }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { /* El cierre local también funciona sin conexión. */ }
    }

    private suspend fun guardar(sesion: Sesion) {
        try { withContext(Dispatchers.IO) { almacen.guardar(sesion) } }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { throw FalloAutenticacion(MotivoFalloAutenticacion.GUARDADO_SESION) }
    }

    private suspend fun <T> remoto(credenciales: Boolean = false, block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        when (e.code()) {
            401 -> {
                if (credenciales) throw CredencialesInvalidasException()
                withContext(Dispatchers.IO) { almacen.limpiar() }
                throw SesionVencidaException()
            }
            400, 409 -> {
                val campos = runCatching { Gson().fromJson(e.response()?.errorBody()?.string(), ErrorRegistroDTO::class.java)?.fields }.getOrNull()
                throw RegistroInvalidoException(campos ?: if (e.code() == 409) mapOf("email" to "Este correo ya está registrado. Inicia sesión con tu cuenta.") else emptyMap())
            }
            403 -> throw FalloAutenticacion(MotivoFalloAutenticacion.ACCESO_RESTRINGIDO)
            429 -> throw FalloAutenticacion(MotivoFalloAutenticacion.DEMASIADOS_INTENTOS)
            else -> throw FalloAutenticacion(MotivoFalloAutenticacion.SERVIDOR)
        }
    } catch (_: IOException) {
        throw FalloAutenticacion(MotivoFalloAutenticacion.SIN_CONEXION)
    } catch (_: IllegalStateException) {
        throw FalloAutenticacion(MotivoFalloAutenticacion.RESPUESTA_INVALIDA)
    } catch (_: JsonParseException) {
        throw FalloAutenticacion(MotivoFalloAutenticacion.RESPUESTA_INVALIDA)
    }
}
