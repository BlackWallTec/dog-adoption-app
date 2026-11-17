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


// Implementa el contrato de domain con la API, el almacén de tokens y el lector JSON.
// @Inject permite que Hilt proporcione estas dependencias al construir el repositorio.
class UsuarioRepositoryImpl @Inject constructor(
    private val api: UsuarioAPIservice,
    private val tokens: TokenProvider,
    private val gson: Gson,
) : PerfilesRepositoryInt {
    override suspend fun login(correo: String, contrasena: String): Result<Sesion> {
        var tokenGuardado: String? = null
        try {
            return ejecutar {
                // Envía las credenciales y convierte el JSON recibido al modelo Sesion de domain.
                val sesion = api.login(LoginRequestDTO(correo.trim(), contrasena)).toDomain()
                // Obtiene el contexto de esta corrutina y comprueba que su Job siga activo.
                // Si ya se canceló, ensureActive lanza CancellationException antes de guardar el token.
                // No devuelve true/false ni valida las credenciales: comprueba la cancelación.
                currentCoroutineContext().ensureActive()
                // La sesión sólo se entrega si el almacenamiento confirmó el guardado del token.
                if (!tokens.save(sesion.token)) throw ErrorApi("No pudimos guardar tu sesión. Inténtalo de nuevo.")
                // Recuerda qué token pertenece a este intento por si después debe limpiarse.
                tokenGuardado = sesion.token
                // La última expresión de la lambda es el valor que ejecutar envolverá en Result.success.
                sesion
            }
        } catch (e: CancellationException) {
            // Si se salió durante el guardado, no dejar una sesión que la UI nunca recibió.
            tokenGuardado?.let { token ->
                // Si la cancelación ocurrió durante el guardado, intenta limpiar aunque el Job esté cancelado.
                // NonCancellable permite ese bloque de limpieza y Dispatchers.IO evita hacerlo en la UI.
                withContext(NonCancellable + Dispatchers.IO) {
                    // Sólo intenta borrar el token de este intento, no uno distinto guardado después.
                    // runCatching evita que un fallo de limpieza sustituya la cancelación original.
                    runCatching { if (tokens.token() == token) tokens.clear() }
                }
            }
            // Propaga la cancelación al llamador; no la convierte en un error de credenciales.
            throw e
        }
    }

    // Convierte Registro al envío JSON y devuelve el usuario creado como un modelo de domain.
    override suspend fun register(registro: Registro): Result<Usuario> = ejecutar {
        api.register(registro.toRequest()).user.toDomain()
    }

    // Convierte Perfil en el envio de un JSON y devuelve un modelo protegido
    // en Domain
    override suspend fun profile(): Result<Perfil> = ejecutar(protegida = true) {
        api.profile().toDomain()
    }

    override suspend fun me(): Result<Usuario> = ejecutar(protegida = true) {
        api.me().user.toDomain()
    }

    // Convierte la pila de Codigo postales en el envio de un JSON devolviendo un modelo
    // a Dommain para ser usado en la lista de CP con sus respectivas direcciones
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
    // <T> permite reutilizar este manejo de resultados para Sesion, Usuario, Perfil u otros valores.
    // protegida indica que un rechazo de autenticación debe retirar el token guardado.
    private suspend fun <T> ejecutar(protegida: Boolean = false, bloque: suspend () -> T): Result<T> =
        // Ejecuta el bloque en el dispatcher de entrada/salida para no bloquear la interfaz.
        withContext(Dispatchers.IO) {
            try {
                Result.success(bloque())
            } catch (e: CancellationException) {
                // Se relanza antes del catch general: cancelar el trabajo no es un fallo de la operación.
                throw e
            } catch (e: HttpException) {
                // Interpreta el cuerpo de error; si no tiene JSON válido, se usará un mensaje alternativo.
                val cuerpo = try {
                    gson.fromJson(e.response()?.errorBody()?.string(), ApiErrorDTO::class.java)
                } catch (_: Exception) { null }
                // En rutas protegidas, 401 o UNAUTHENTICATED indican que la sesión dejó de ser válida.
                val expirada = protegida && (e.code() == 401 || cuerpo?.code == "UNAUTHENTICATED")
                if (expirada) {
                    try { tokens.clear() } catch (_: Exception) {
                        return@withContext Result.failure(ErrorApi("No pudimos borrar la sesión guardada. Inténtalo de nuevo."))
                    }
                }
                // Conserva fields para que el ViewModel ubique cada error en su campo del registro.
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
