package com.app.android.perfiles

import com.app.android.data.TokenProvider
import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.*
import com.app.android.data.repoImp.repoImpPerfiles.UsuarioRepositoryImpl
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.app.android.domain.useCases.useCasesPerfiles.BuscarCodigosPostalesUseCase
import com.app.android.domain.useCases.useCasesPerfiles.RegistrarUseCase
import com.app.android.presentation.viewmodel.viewModelPerfiles.RegistroViewModel
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class CorreoCompatibilidadTest {
    private class Tokens : TokenProvider {
        var valor: String? = "sesion-anterior"; var borrados = 0
        override fun token() = valor
        override fun save(token: String): Boolean { valor = token; return true }
        override fun clear() { valor = null; borrados++ }
    }
    private class Api : UsuarioAPIservice {
        var falloCorreo = false
        var registros = 0
        override suspend fun login(body: LoginRequestDTO) = LoginResponseDTO(UserDTO("cliente", "Cliente"), "jwt-existente")
        override suspend fun register(body: RegisterRequestDTO): RegisterResponseDTO {
            registros++
            return Gson().fromJson("""{"user":{"uuid":"cliente","rol":"Cliente"}}""", RegisterResponseDTO::class.java)
        }
        override suspend fun profile(): ProfileEnvelopeDTO = error("No usado")
        override suspend fun me(): MeResponseDTO = error("No usado")
        override suspend fun codigosPostales(cp: String) = CodigosPostalesResponseDTO(listOf(
            cpsResponseDTO("12345678-1234-1234-1234-123456789abc", cp, "Colonia", "Municipio", "Estado")
        ))
        override suspend fun reenviarCodigo(body: CodigoCorreoRequest) = CodigoCorreoResponse(null, "Si corresponde recibirás un código")
        override suspend fun recuperarCodigo(body: CodigoCorreoRequest) = reenviarCodigo(body)
        override suspend fun confirmarCodigo(body: CodigoCorreoRequest): CodigoCorreoResponse {
            if (falloCorreo) throw HttpException(Response.error<Unit>(401, """{"code":"UNAUTHENTICATED","error":"No autenticado"}""".toResponseBody()))
            return CodigoCorreoResponse(true, null)
        }
        override suspend fun restablecerCodigo(body: CodigoCorreoRequest) = confirmarCodigo(body)
    }
    @Test fun correoNoBorraNiSustituyeLaSesionDelLogin() = runBlocking {
        val api = Api(); val tokens = Tokens(); val repo = UsuarioRepositoryImpl(api, tokens, Gson())
        assertEquals("cliente", repo.login("cliente@example.com", "Prueba1234").getOrThrow().usuario.id)
        assertEquals("jwt-existente", tokens.valor)
        api.falloCorreo = true
        assertTrue(repo.confirmarCorreo("cliente@example.com", "012345").isFailure)
        assertEquals("jwt-existente", tokens.valor); assertEquals(0, tokens.borrados)
        api.falloCorreo = false
        assertTrue(repo.confirmarCorreo("cliente@example.com", "012345").isSuccess)
        assertEquals("jwt-existente", tokens.valor)
    }
    @Test fun respuestaAnteriorDeRegistroConservaElFlujoSinConfirmacion() = runBlocking {
        val tokens = Tokens(); val repo = UsuarioRepositoryImpl(Api(), tokens, Gson())
        val resultado = repo.register(Registro()).getOrThrow()
        assertEquals("cliente", resultado.usuario.id)
        assertFalse(resultado.requiereConfirmacion)
        assertEquals("sesion-anterior", tokens.valor)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun exigeElegirColoniaYVuelveAPedirlaAlCambiarCP() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val api = Api()
            val repo = UsuarioRepositoryImpl(api, Tokens(), Gson())
            val vm = RegistroViewModel(RegistrarUseCase(repo), BuscarCodigosPostalesUseCase(repo))
            vm.actualizar("codigoPostal", "06700")
            vm.registrar()
            assertEquals(0, api.registros)
            runCurrent()
            vm.uiState.first { !it.cargandoColonias }
            vm.registrar()
            assertNotNull(vm.uiState.value.campos["id_codigo_postal"])
            assertEquals(0, api.registros)
            val colonia = vm.uiState.value.colonias.single().idCodigoPostal
            vm.seleccionarColonia(colonia)
            assertNull(vm.uiState.value.campos["id_codigo_postal"])
            vm.actualizar("codigoPostal", "01000")
            runCurrent()
            vm.uiState.first { !it.cargandoColonias }
            vm.registrar()
            assertEquals(0, api.registros)
            vm.seleccionarColonia(vm.uiState.value.colonias.single().idCodigoPostal)
            vm.registrar()
            runCurrent()
            vm.uiState.first { it.registroExitoso || it.error != null }
            assertEquals(1, api.registros)
            assertTrue(vm.uiState.value.registroExitoso)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
