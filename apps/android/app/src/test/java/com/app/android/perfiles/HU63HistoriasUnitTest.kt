package com.app.android.perfiles

import com.app.android.data.AuthInterceptor
import com.app.android.data.TokenProvider
import com.app.android.domain.entities.entitiesPerfiles.CodigoPostal
import com.app.android.domain.entities.entitiesPerfiles.Perfil
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.app.android.domain.entities.entitiesPerfiles.ResultadoRegistro
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import com.app.android.domain.useCases.useCasesPerfiles.ObtenerPerfilUseCase
import com.app.android.domain.useCases.useCasesPerfiles.RegistrarUseCase
import com.app.android.domain.useCases.useCasesPerfiles.RestaurarSesionUseCase
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HU63HistoriasUnitTest {
    private class RepositorioPrueba : PerfilesRepositoryInt {
        var registroRecibido: Registro? = null
        var inicioSesionLlamadas = 0
        var cierreSesionLlamadas = 0
        var resultadoRegistro: ResultadoRegistro? = null
        var sesionGuardada: Sesion? = null
        var perfilGuardado: Perfil? = null

        override suspend fun login(correo: String, contrasena: String): Result<Sesion> {
            inicioSesionLlamadas++
            return Result.failure(IllegalStateException("No usado en el registro"))
        }

        override suspend fun register(registro: Registro): Result<ResultadoRegistro> {
            registroRecibido = registro
            return Result.success(checkNotNull(resultadoRegistro))
        }

        override suspend fun profile(): Result<Perfil> = Result.success(checkNotNull(perfilGuardado))
        override suspend fun me(): Result<Usuario> = Result.failure(IllegalStateException("No usado"))
        override suspend fun codigosPostales(cp: String): Result<List<CodigoPostal>> =
            Result.failure(IllegalStateException("No usado"))

        override suspend fun logout(): Result<Unit> {
            cierreSesionLlamadas++
            return Result.success(Unit)
        }

        override suspend fun sesionActual(): Result<Sesion?> = Result.success(sesionGuardada)
        override suspend fun solicitarCodigo(correo: String, recuperar: Boolean): Result<Unit> =
            Result.failure(IllegalStateException("No usado"))

        override suspend fun confirmarCorreo(correo: String, codigo: String): Result<Unit> =
            Result.failure(IllegalStateException("No usado"))

        override suspend fun restablecerContrasena(
            correo: String,
            codigo: String,
            contrasena: String,
            repetida: String,
        ): Result<Unit> = Result.failure(IllegalStateException("No usado"))
    }

    @Test
    fun `HU63-D crea una cuenta Cliente sin iniciar sesion`() = runBlocking {
        val repositorio = RepositorioPrueba().apply {
            resultadoRegistro = ResultadoRegistro(
                usuario = Usuario("cliente-1", "Cliente"),
                requiereConfirmacion = true,
                correoEnviado = true,
            )
        }
        val registro = Registro(
            nombre = "Ana",
            apellidos = "Lopez",
            correo = "ana@example.com",
            privacidad = true,
        )

        val resultado = RegistrarUseCase(repositorio)(registro).getOrThrow()

        assertSame(registro, repositorio.registroRecibido)
        assertEquals("Cliente", resultado.usuario.rol)
        assertTrue(resultado.requiereConfirmacion)
        assertEquals(0, repositorio.inicioSesionLlamadas)
    }

    @Test
    fun `HU63-F restaura la sesion Cliente y consulta su perfil`() = runBlocking {
        val repositorio = RepositorioPrueba().apply {
            sesionGuardada = Sesion(Usuario("cliente-1", "Cliente"), "jwt-prueba")
            perfilGuardado = Perfil(nombre = "Ana", apellidos = "Lopez", codigoPostal = "06700")
        }

        val sesion = RestaurarSesionUseCase(repositorio)().getOrThrow()
        val perfil = ObtenerPerfilUseCase(repositorio)().getOrThrow()

        assertEquals("cliente-1", sesion?.usuario?.id)
        assertEquals("Cliente", sesion?.usuario?.rol)
        assertEquals("Ana", perfil.nombre)
        assertEquals("06700", perfil.codigoPostal)
        assertEquals(0, repositorio.cierreSesionLlamadas)
    }

    @Test
    fun `HU63-H envia el token de sesion como Bearer`() {
        val tokenProvider = object : TokenProvider {
            override fun token(): String = "jwt-prueba"
            override fun save(token: String): Boolean = true
            override fun clear() = Unit
        }
        var peticionRecibida: Request? = null
        val peticionOriginal = Request.Builder().url("https://unit.test/perfil").build()
        val chain = Proxy.newProxyInstance(
            Interceptor.Chain::class.java.classLoader,
            arrayOf(Interceptor.Chain::class.java),
        ) { _, metodo, argumentos ->
            when (metodo.name) {
                "request" -> peticionOriginal
                "proceed" -> {
                    val peticionEnviada = argumentos?.first() as Request
                    peticionRecibida = peticionEnviada
                    Response.Builder()
                        .request(peticionEnviada)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("".toResponseBody())
                        .build()
                }
                else -> error("Metodo no usado por AuthInterceptor: ${metodo.name}")
            }
        } as Interceptor.Chain

        AuthInterceptor(tokenProvider).intercept(chain).use {
            assertTrue(it.isSuccessful)
        }

        assertEquals("mobile", peticionRecibida?.header("x-client"))
        assertEquals("Bearer jwt-prueba", peticionRecibida?.header("Authorization"))
        assertFalse(peticionRecibida?.header("Authorization").isNullOrBlank())
    }
}
