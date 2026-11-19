package com.app.android.perfiles

import com.app.android.data.TokenProvider
import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.*
import com.app.android.data.repoImp.repoImpPerfiles.UsuarioRepositoryImpl
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
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
        override suspend fun login(body: LoginRequestDTO) = LoginResponseDTO(UserDTO("cliente", "Cliente"), "jwt-existente")
        override suspend fun register(body: RegisterRequestDTO): RegisterResponseDTO = Gson().fromJson("""{"user":{"uuid":"cliente","rol":"Cliente"}}""", RegisterResponseDTO::class.java)
        override suspend fun profile(): ProfileEnvelopeDTO = error("No usado")
        override suspend fun me(): MeResponseDTO = error("No usado")
        override suspend fun codigosPostales(cp: String): CodigosPostalesResponseDTO = error("No usado")
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
}
