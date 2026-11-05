package com.app.android.perfiles

import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.LoginResponseDTO
import com.app.android.data.dtos.dtosPerfiles.UserDTO
import com.app.android.data.repoImp.repoImpPerfiles.UsuarioRepositoryImpl
import com.app.android.domain.entities.entitiesPerfiles.CredencialesInvalidasException
import com.app.android.domain.entities.entitiesPerfiles.FalloAutenticacion
import com.app.android.domain.entities.entitiesPerfiles.MotivoFalloAutenticacion
import com.app.android.domain.useCases.useCasesPerfiles.LoginUseCase
import com.app.android.presentation.viewmodel.viewModelPerfiles.LoginUiState
import com.app.android.presentation.viewmodel.viewModelPerfiles.validateLogin
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class PerfilesLoginContractTest {
    @Test
    fun `el flujo envia email sin espacios y conserva exactamente la contrasena`() = runBlocking {
        var request: LoginRequestDTO? = null
        val api = StubApi {
            request = it
            successResponse()
        }
        val session = LoginUseCase(repository(api))(" ana@correo.com ", " clave con espacios ")

        assertEquals("ana@correo.com", request?.email)
        assertEquals(" clave con espacios ", request?.passwd)
        assertEquals("usuario-1", session.usuario.id)
        assertEquals("Cliente", session.usuario.rol)
        assertEquals("token-de-prueba", session.token)
    }

    @Test
    fun `las credenciales vacias se rechazan antes de llamar al servicio`() {
        var calls = 0
        val useCase = LoginUseCase(repository(StubApi { calls++; successResponse() }))

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase("", "clave") }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase("ana@correo.com", "   ") }
        }
        assertEquals(0, calls)
    }

    @Test
    fun `HTTP 401 conserva el error de dominio de credenciales de luise`() {
        val repository = repository(StubApi { throw httpError(401) })
        assertThrows(CredencialesInvalidasException::class.java) {
            runBlocking { repository.login("ana@correo.com", "clave") }
        }
    }

    @Test
    fun `HTTP 403 429 y 500 se traducen sin exponer Retrofit a la vista`() {
        for ((status, expected) in listOf(
            403 to MotivoFalloAutenticacion.ACCESO_RESTRINGIDO,
            429 to MotivoFalloAutenticacion.DEMASIADOS_INTENTOS,
            500 to MotivoFalloAutenticacion.SERVIDOR,
        )) {
            val repository = repository(StubApi { throw httpError(status) })
            val failure = assertThrows(FalloAutenticacion::class.java) {
                runBlocking { repository.login("ana@correo.com", "clave") }
            }
            assertEquals(expected, failure.motivo)
        }
    }

    @Test
    fun `un fallo de red y una respuesta sin token no se presentan como login exitoso`() {
        val offline = repository(StubApi { throw IOException("sin red") })
        val missingToken = repository(StubApi { successResponse().copy(token = null) })

        assertEquals(
            MotivoFalloAutenticacion.SIN_CONEXION,
            assertThrows(FalloAutenticacion::class.java) {
                runBlocking { offline.login("ana@correo.com", "clave") }
            }.motivo,
        )
        assertEquals(
            MotivoFalloAutenticacion.RESPUESTA_INVALIDA,
            assertThrows(FalloAutenticacion::class.java) {
                runBlocking { missingToken.login("ana@correo.com", "clave") }
            }.motivo,
        )
    }

    @Test
    fun `salir de la pantalla propaga la cancelacion y no inventa un error de autenticacion`() {
        val repository = repository(StubApi { throw CancellationException("salir") })
        assertThrows(CancellationException::class.java) {
            runBlocking { repository.login("ana@correo.com", "clave") }
        }
    }

    @Test
    fun `el formulario distingue campos vacios y correo invalido sin imponer longitud al login`() {
        val empty = validateLogin(LoginUiState())
        assertNotNull(empty.emailError)
        assertNotNull(empty.passwordError)

        val invalid = validateLogin(LoginUiState(email = "correo sin arroba", password = "corta"))
        assertNotNull(invalid.emailError)
        assertNull(invalid.passwordError)

        val valid = validateLogin(LoginUiState(email = " ana@correo.com ", password = "corta"))
        assertNull(valid.emailError)
        assertNull(valid.passwordError)
        assertTrue(!valid.isLoading)
    }

    private fun repository(api: UsuarioAPIservice) = UsuarioRepositoryImpl(api, AlmacenSesionMemoria())

    private class StubApi(
        private val response: suspend (LoginRequestDTO) -> LoginResponseDTO,
    ) : ApiPerfilesStub() {
        override suspend fun login(body: LoginRequestDTO): LoginResponseDTO = response(body)
    }

    private fun successResponse() = LoginResponseDTO(
        user = UserDTO(id = "usuario-1", email = "ana@correo.com", rol = "Cliente"),
        token = "token-de-prueba",
    )

    private fun httpError(status: Int): HttpException = HttpException(
        Response.error<Any>(status, "{}".toResponseBody("application/json".toMediaType())),
    )
}
