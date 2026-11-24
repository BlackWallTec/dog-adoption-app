package com.app.android.voluntariado

import com.app.android.data.dataSource.dataSourcesVoluntariado.InscripcionApi
import com.app.android.data.dtos.dtosVoluntariado.InscripcionDto
import com.app.android.data.dtos.dtosVoluntariado.RespuestaApiDto
import com.app.android.data.repoImp.repoImpVoluntariado.InscripcionRepositoryImpl
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/*
 * HU37 STRESS TESTS - DATA LAYER (talking to the server)
 *
 * InscripcionDatosTest already tests safeApiCall (every catch branch, status ranges, empty
 * bodies) and the date/state conversion. What it does NOT test is the CANCEL path, so here we cover:
 *   1. The server code "NO_INSCRITO" (new in HU37) becomes VoluntariadoError.NoInscrito.
 *   2. InscripcionRepositoryImpl.cancelarInscripcion end to end (with a fake API, no network).
 */
class CancelarDatosStressTest {
    private val gson = Gson()

    // The exception Retrofit throws for a non-2xx answer: status + JSON body.
    private fun errorHttp(status: Int, code: String): HttpException =
        HttpException(
            Response.error<Any>(status, """{"sucess":false,"code":"$code","message":"x"}""".toResponseBody("application/json".toMediaType())),
        )

    // A fake API: we decide what `cancelarInscripcion` returns or throws, and remember the id it got.
    private class ApiFalsa(val accion: () -> RespuestaApiDto<InscripcionDto>) : InscripcionApi {
        var idRecibido: String? = null

        override suspend fun inscribirse(idPrograma: String): RespuestaApiDto<InscripcionDto> = error("no se usa")

        override suspend fun cancelarInscripcion(idPrograma: String): RespuestaApiDto<InscripcionDto> {
            idRecibido = idPrograma
            return accion()
        }
    }

    private val dtoPendiente = InscripcionDto("programa-1", "2026-11-05T18:00:00Z", "Pendiente")

    private fun cancelar(api: InscripcionApi, id: String = "abc") = runBlocking { InscripcionRepositoryImpl(api, gson).cancelarInscripcion(id) }

    @Test
    fun `estructural - camino feliz - devuelve la inscripcion en Pendiente y manda el id al API`() {
        val api = ApiFalsa { RespuestaApiDto(true, dtoPendiente) }

        val exito = cancelar(api) as Resultado.Exito

        assertEquals("abc", api.idRecibido)
        assertEquals("Pendiente", exito.data.estado)
        assertEquals("programa-1", exito.data.programaId)
    }

    @Test
    fun `estructural - los codigos del servidor al cancelar llegan como el error correcto`() {
        // NO_INSCRITO is the new one; the others are the errors the cancel endpoint can answer.
        val casos =
            mapOf(
                "NO_INSCRITO" to VoluntariadoError.NoInscrito,
                "PROGRAMA_INICIADO" to VoluntariadoError.ProgramaIniciado,
                "PROGRAMA_NO_ENCONTRADO" to VoluntariadoError.ProgramaNoEncontrado,
                "NO_AUTENTICADO" to VoluntariadoError.NoAutenticado,
                "no_inscrito" to VoluntariadoError.Desconocido, // limite: the match is exact, case matters
                "NO_INSCRITO " to VoluntariadoError.Desconocido, // limite: a trailing space is a different code
            )

        for ((code, esperado) in casos) {
            assertEquals(code, Resultado.Error(esperado), cancelar(ApiFalsa { throw errorHttp(409, code) }))
        }
    }

    @Test
    fun `nulos - respuesta sin data o con fecha invalida es Desconocido, no un crash`() {
        assertEquals(Resultado.Error(VoluntariadoError.Desconocido), cancelar(ApiFalsa { RespuestaApiDto(true, null) }))
        assertEquals(Resultado.Error(VoluntariadoError.Desconocido), cancelar(ApiFalsa { RespuestaApiDto(true, dtoPendiente.copy(fechaInscripcion = "hoy")) }))
    }

    @Test
    fun `nulos - el id vacio llega al API sin validarse (la validacion es del servidor)`() {
        val api = ApiFalsa { RespuestaApiDto(true, dtoPendiente) }

        cancelar(api, id = "")

        assertEquals("", api.idRecibido)
    }

    @Test
    fun `estructural - sin internet al cancelar es SinConexion y el repositorio nunca lanza`() {
        assertEquals(Resultado.Error(VoluntariadoError.SinConexion), cancelar(ApiFalsa { throw IOException("sin red") }))
        assertEquals(Resultado.Error(VoluntariadoError.Desconocido), cancelar(ApiFalsa { throw NullPointerException() }))
    }
}