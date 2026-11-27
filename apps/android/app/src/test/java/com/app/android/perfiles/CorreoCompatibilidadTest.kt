package com.app.android.perfiles

import com.app.android.data.TokenProvider
import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.*
import com.app.android.data.repoImp.repoImpPerfiles.UsuarioRepositoryImpl
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.app.android.domain.useCases.useCasesPerfiles.BuscarCodigosPostalesUseCase
import com.app.android.domain.useCases.useCasesPerfiles.RegistrarUseCase
import com.app.android.domain.useCases.useCasesPerfiles.CorreoUseCase
import com.app.android.domain.useCases.useCasesPerfiles.LoginUseCase
import com.app.android.presentation.viewmodel.viewModelPerfiles.RegistroViewModel
import com.app.android.presentation.viewmodel.viewModelPerfiles.LoginViewModel
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
        var codigoLoginError: String? = null
        var confirmacionPendiente = false
        val correos = mutableListOf<String>()
        override suspend fun login(body: LoginRequestDTO): LoginResponseDTO {
            codigoLoginError?.let { codigo ->
                throw HttpException(Response.error<Unit>(if (codigo == "EMAIL_NOT_CONFIRMED") 403 else 401,
                    """{"code":"$codigo","error":"No se pudo iniciar sesión"}""".toResponseBody()))
            }
            return LoginResponseDTO(UserDTO("cliente", "Cliente"), "jwt-existente")
        }
        override suspend fun register(body: RegisterRequestDTO): RegisterResponseDTO {
            registros++
            val respuesta = Gson().fromJson("""{"user":{"uuid":"cliente","rol":"Cliente"}}""", RegisterResponseDTO::class.java)
            return if (confirmacionPendiente) respuesta.copy(requiereConfirmacion = true) else respuesta
        }
        override suspend fun profile(): ProfileEnvelopeDTO = error("No usado")
        override suspend fun me(): MeResponseDTO = error("No usado")
        override suspend fun codigosPostales(cp: String) = CodigosPostalesResponseDTO(listOf(
            cpsResponseDTO("12345678-1234-1234-1234-123456789abc", cp, "Colonia", "Municipio", "Estado")
        ))
        override suspend fun reenviarCodigo(body: CodigoCorreoRequest): CodigoCorreoResponse {
            correos.add(body.correo)
            return CodigoCorreoResponse(null, "Si corresponde recibirás un código")
        }
        override suspend fun recuperarCodigo(body: CodigoCorreoRequest) = reenviarCodigo(body)
        override suspend fun confirmarCodigo(body: CodigoCorreoRequest): CodigoCorreoResponse {
            correos.add(body.correo)
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
    @Test fun registroPendienteSolicitaConfirmacionSinIniciarSesion() = runBlocking {
        val api = Api().apply { confirmacionPendiente = true }
        val tokens = Tokens()
        val resultado = UsuarioRepositoryImpl(api, tokens, Gson()).register(Registro()).getOrThrow()
        assertTrue(resultado.requiereConfirmacion)
        assertEquals("sesion-anterior", tokens.valor)
        assertEquals(0, tokens.borrados)
    }
    @Test fun correoAdmiteCualquierDominioValidoYNormalizaAntesDeLlamarAlRepo() = runBlocking {
        for (dominio in listOf("gmail.com", "outlook.com", "yahoo.com", "hotmail.com", "icloud.com", "tec.mx", "mi-empresa.org")) {
            val api = Api()
            val caso = CorreoUseCase(UsuarioRepositoryImpl(api, Tokens(), Gson()))
            val correo = " Prueba@${dominio.uppercase()} "
            assertTrue(caso.solicitar(correo, false).isSuccess)
            assertTrue(caso.solicitar(correo, true).isSuccess)
            assertTrue(caso.confirmar(correo, "012345").isSuccess)
            assertTrue(caso.restablecer(correo, "012345", "Nueva1234", "Nueva1234").isSuccess)
            assertEquals(List(4) { "prueba@$dominio" }, api.correos)
        }
    }
    @Test fun correoRechazaFormatosInvalidosSinHacerPeticiones() = runBlocking {
        val api = Api()
        val caso = CorreoUseCase(UsuarioRepositoryImpl(api, Tokens(), Gson()))
        for (correo in listOf("", "sin-correo", "prueba@sin-punto", "prueba@.com",
            "prueba@dominio.c", "prueba@@gmail.com", "a b@gmail.com", "a\nb@gmail.com",
            "x".repeat(246) + "@gmail.com")) {
            assertTrue(caso.solicitar(correo, false).isFailure)
            assertTrue(caso.solicitar(correo, true).isFailure)
            assertTrue(caso.confirmar(correo, "012345").isFailure)
            assertTrue(caso.restablecer(correo, "012345", "Nueva1234", "Nueva1234").isFailure)
        }
        assertTrue(caso.confirmar("prueba@gmail.com", "12345").isFailure)
        assertTrue(caso.restablecer("prueba@gmail.com", "012345", "Nueva1234", "Distinta1234").isFailure)
        assertTrue(api.correos.isEmpty())
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun loginSoloOfreceRetomarConfirmacionCuandoLaApiIndicaCuentaPendiente() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val api = Api()
            val tokens = Tokens()
            val vm = LoginViewModel(LoginUseCase(UsuarioRepositoryImpl(api, tokens, Gson())))
            assertFalse(vm.uiState.value.correoPendiente)
            vm.onEmailChange("prueba@gmail.com")
            vm.onPasswordChange("Prueba1234")
            api.codigoLoginError = "INVALID_CREDENTIALS"
            vm.login()
            vm.uiState.first { !it.isLoading }
            assertFalse(vm.uiState.value.correoPendiente)
            api.codigoLoginError = "EMAIL_NOT_CONFIRMED"
            vm.login()
            vm.uiState.first { !it.isLoading }
            assertTrue(vm.uiState.value.correoPendiente)
            assertNull(vm.uiState.value.sesionLista)
            assertEquals("sesion-anterior", tokens.valor)
            vm.onEmailChange("otro@gmail.com")
            assertFalse(vm.uiState.value.correoPendiente)
        } finally {
            Dispatchers.resetMain()
        }
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
