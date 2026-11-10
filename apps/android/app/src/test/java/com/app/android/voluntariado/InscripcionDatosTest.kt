package com.app.android.voluntariado

import com.app.android.data.dataSource.dataSourcesVoluntariado.InscripcionApi
import com.app.android.data.dtos.dtosVoluntariado.InscripcionDto
import com.app.android.data.dtos.dtosVoluntariado.RespuestaApiDto
import com.app.android.data.dataSource.dataSourcesVoluntariado.safeApiCall
import com.app.android.data.repoImp.repoImpVoluntariado.InscripcionRepositoryImpl
import com.app.android.data.repoImp.repoImpVoluntariado.toDomain
import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeParseException
import kotlin.coroutines.cancellation.CancellationException

/*
 * PRUEBAS DE LA CAPA DE DATOS
 *
 * Esta capa traduce lo que responde internet al lenguaje de la app:
 *   - `safeApiCall`: convierte cualquier fallo en un `VoluntariadoError`.
 *   - `InscripcionRepositoryImpl`: llama a la API y revisa que traiga datos.
 *   - `toDomain` (el mapper): convierte el JSON a una `Inscripcion`.
 *
 * No hacemos peticiones reales. Fabricamos las respuestas y los errores, así podemos
 * probar en un instante situaciones difíciles de provocar con un servidor de verdad.
 */
class InscripcionDatosTest {
    private val gson = Gson()

    // Fabrica el error que lanzaría Retrofit si el servidor contestara ese status y cuerpo.
    private fun errorHttp(status: Int, cuerpo: String): HttpException =
        HttpException(Response.error<Any>(status, cuerpo.toResponseBody("application/json".toMediaType())))

    private fun cuerpoConCodigo(codigo: String) = """{"sucess":false,"code":"$codigo","message":"x"}"""

    // Ejecuta safeApiCall con una llamada que siempre falla con la excepción indicada.
    private fun errorDe(excepcion: Exception): Resultado<String> =
        runBlocking { safeApiCall(gson) { throw excepcion } }

    private fun dto(
        fecha: String = "2026-11-05T12:00:00",
        estado: String = "Activa",
    ) = InscripcionDto(idPrograma = "programa-1", fechaInscripcion = fecha, estado = estado)

    // Un fake de la API: responde lo que le indique cada prueba.
    private fun repositorio(accion: suspend (String) -> RespuestaApiDto<InscripcionDto>) =
        InscripcionRepositoryImpl(
            api =
                object : InscripcionApi {
                    override suspend fun inscribirse(idPrograma: String) = accion(idPrograma)
                },
            gson = gson,
        )

    // ------------------------------------------------------------------------------
    // safeApiCall - ESTRUCTURAL: un camino por cada bloque `catch`
    // ------------------------------------------------------------------------------

    @Test
    fun `estructural - llamada correcta regresa Exito`() = runBlocking<Unit> {
        assertEquals(Resultado.Exito("ok"), safeApiCall(gson) { "ok" })
    }

    @Test
    fun `estructural - sin internet IOException regresa SinConexion`() {
        assertEquals(Resultado.Error(VoluntariadoError.SinConexion), errorDe(IOException("sin red")))
    }

    @Test
    fun `estructural - cualquier otra excepcion regresa Desconocido`() {
        assertEquals(Resultado.Error(VoluntariadoError.Desconocido), errorDe(IllegalStateException("x")))
    }

    @Test
    fun `estructural - la cancelacion no se convierte en error`() = runBlocking<Unit> {
        // Regla de oro de las corrutinas: cuando el usuario sale de la pantalla, la
        // cancelación debe seguir su camino. Por eso safeApiCall la vuelve a lanzar.
        try {
            safeApiCall(gson) { throw CancellationException("salió de la pantalla") }
            fail("Debió lanzar CancellationException")
        } catch (e: CancellationException) {
            assertEquals("salió de la pantalla", e.message)
        }
    }

    @Test
    fun `estructural - cada codigo de la API se traduce a su error`() {
        val casos =
            mapOf(
                "SIN_CUPO" to VoluntariadoError.SinCupo,
                "YA_INSCRITO" to VoluntariadoError.YaInscrito,
                "PROGRAMA_INICIADO" to VoluntariadoError.ProgramaIniciado,
                "PROGRAMA_NO_DISPONIBLE" to VoluntariadoError.ProgramaNoDisponible,
                "PROGRAMA_NO_ENCONTRADO" to VoluntariadoError.ProgramaNoEncontrado,
                "NO_AUTENTICADO" to VoluntariadoError.NoAutenticado,
                "ERROR_INTERNO" to VoluntariadoError.Desconocido, // la API lo manda en los 500
                "CODIGO_INVENTADO" to VoluntariadoError.Desconocido,
                "sin_cupo" to VoluntariadoError.Desconocido, // las mayúsculas importan
            )

        for ((codigo, esperado) in casos) {
            assertEquals(
                "codigo=$codigo",
                Resultado.Error(esperado),
                errorDe(errorHttp(409, cuerpoConCodigo(codigo))),
            )
        }
    }

    // ------------------------------------------------------------------------------
    // safeApiCall - LÍMITES y RANGOS del status HTTP
    // Sin un código reconocido, solo el 401 significa "no autenticado".
    // ------------------------------------------------------------------------------

    @Test
    fun `limites - status vecinos de 401 sin codigo reconocido`() {
        val casos =
            listOf(
                400 to VoluntariadoError.Desconocido,
                401 to VoluntariadoError.NoAutenticado,
                402 to VoluntariadoError.Desconocido,
            )

        for ((status, esperado) in casos) {
            assertEquals("status=$status", Resultado.Error(esperado), errorDe(errorHttp(status, "{}")))
        }
    }

    @Test
    fun `rangos - todos los status 4xx y 5xx`() {
        for (status in 400..599) {
            val esperado =
                if (status == 401) VoluntariadoError.NoAutenticado else VoluntariadoError.Desconocido

            assertEquals("status=$status", Resultado.Error(esperado), errorDe(errorHttp(status, "<html>")))
        }
    }

    @Test
    fun `estructural - el codigo del cuerpo tiene prioridad sobre el status`() {
        assertEquals(
            Resultado.Error(VoluntariadoError.SinCupo),
            errorDe(errorHttp(401, cuerpoConCodigo("SIN_CUPO"))),
        )
    }

    // ------------------------------------------------------------------------------
    // safeApiCall - NULOS / VACÍOS: cuerpos de error que no traen código
    // ------------------------------------------------------------------------------

    @Test
    fun `nulos y vacios - cuerpos de error sin un codigo util`() {
        val cuerpos =
            listOf(
                "", // vacío: Gson regresa null
                "   ",
                "null", // JSON válido cuyo valor es null
                "{}", // sin el campo code
                """{"code":null}""",
                """{"code":""}""",
                "esto no es json", // Gson lanza una excepción y runCatching la atrapa
            )

        for (cuerpo in cuerpos) {
            assertEquals(
                "cuerpo='$cuerpo' con 500",
                Resultado.Error(VoluntariadoError.Desconocido),
                errorDe(errorHttp(500, cuerpo)),
            )
            assertEquals(
                "cuerpo='$cuerpo' con 401",
                Resultado.Error(VoluntariadoError.NoAutenticado),
                errorDe(errorHttp(401, cuerpo)),
            )
        }
    }

    @Test
    fun `nulos - respuesta sin cuerpo de error`() {
        // Esta respuesta "exitosa" no tiene errorBody: `errorBody()` da null. Su status es 200.
        val excepcion = HttpException(Response.success("ok"))

        assertEquals(Resultado.Error(VoluntariadoError.Desconocido), errorDe(excepcion))
    }

    @Test
    fun `nulos - excepcion HTTP sin respuesta`() {
        // Retrofit declara `response()` como nullable. Una subclase nos deja forzar ese caso.
        val excepcion =
            object : HttpException(Response.error<Any>(401, "".toResponseBody(null))) {
                override fun response(): Response<*>? = null
            }

        assertEquals(Resultado.Error(VoluntariadoError.NoAutenticado), errorDe(excepcion))
    }

    // ------------------------------------------------------------------------------
    // InscripcionRepositoryImpl
    // ------------------------------------------------------------------------------

    @Test
    fun `estructural - respuesta con data regresa la inscripcion y envia el id correcto`() = runBlocking<Unit> {
        var idRecibido: String? = null
        val repo =
            repositorio { id ->
                idRecibido = id
                RespuestaApiDto(exito = true, data = dto())
            }

        val resultado = repo.inscribirse("programa-1")

        assertEquals("programa-1", idRecibido)
        // 12:00 en Ciudad de México son las 18:00 en UTC.
        assertEquals(
            Resultado.Exito(Inscripcion("programa-1", Instant.parse("2026-11-05T18:00:00Z"), "Activa")),
            resultado,
        )
    }

    @Test
    fun `nulos - respuesta sin data regresa Desconocido`() = runBlocking<Unit> {
        val repo = repositorio { RespuestaApiDto(exito = true, data = null) }

        assertEquals(Resultado.Error(VoluntariadoError.Desconocido), repo.inscribirse("p"))
    }

    @Test
    fun `estructural - los errores de la API pasan por safeApiCall`() = runBlocking<Unit> {
        val sinCupo = repositorio { throw errorHttp(409, cuerpoConCodigo("SIN_CUPO")) }
        val sinRed = repositorio { throw IOException("sin red") }

        assertEquals(Resultado.Error(VoluntariadoError.SinCupo), sinCupo.inscribirse("p"))
        assertEquals(Resultado.Error(VoluntariadoError.SinConexion), sinRed.inscribirse("p"))
    }

    @Test
    fun `nulos - un estado null del servidor no convierte el exito en error`() = runBlocking<Unit> {
        // ¿Qué bug había? `estado` no aceptaba null, pero Gson lo dejaba así de todos modos.
        // Luego `.trim()` tronaba y el usuario veía "Algo salió mal", aunque el servidor YA
        // lo hubiera inscrito.
        // ¿Qué valida ahora? Que el DTO acepte null y `orEmpty()` lo vuelva texto vacío, de
        // modo que la inscripción se reporte como lo que fue: un éxito.
        val dtoConNull =
            gson.fromJson(
                """{"id_programa":"p","fecha_inscripcion":"2026-11-05T12:00:00","estado":null}""",
                InscripcionDto::class.java,
            )
        val repo = repositorio { RespuestaApiDto(exito = true, data = dtoConNull) }

        // 12:00 en Ciudad de México son las 18:00 en UTC.
        assertEquals(
            Resultado.Exito(Inscripcion("p", Instant.parse("2026-11-05T18:00:00Z"), "")),
            repo.inscribirse("p"),
        )
    }

    @Test
    fun `limites - una fecha con zona horaria se respeta y no convierte el exito en error`() = runBlocking<Unit> {
        // ¿Qué bug había? El mapper solo aceptaba fechas sin zona. Con "Z" o "+00:00" lanzaba
        // una excepción y el usuario veía un error, aunque el servidor ya lo hubiera inscrito.
        // ¿Qué valida ahora? Que estas fechas se lean tal cual: ya indican su propia zona,
        // así que NO se les aplica la del refugio. Por eso las 12:00 UTC siguen siendo 12:00 UTC.
        for (fecha in listOf("2026-11-05T12:00:00Z", "2026-11-05T12:00:00+00:00", "2026-11-05T06:00:00-06:00")) {
            val repo = repositorio { RespuestaApiDto(exito = true, data = dto(fecha = fecha)) }

            assertEquals(
                "fecha=$fecha",
                Resultado.Exito(Inscripcion("programa-1", Instant.parse("2026-11-05T12:00:00Z"), "Activa")),
                repo.inscribirse("p"),
            )
        }
    }

    // ------------------------------------------------------------------------------
    // Mapper toDomain
    // ------------------------------------------------------------------------------

    @Test
    fun `limites - fechas validas en las fronteras del calendario`() {
        val casos =
            mapOf(
                // Cambio de año: en UTC ya es el año siguiente.
                "2026-12-31T23:59:59" to "2027-01-01T05:59:59Z",
                "2026-01-01T00:00:00" to "2026-01-01T06:00:00Z",
                // 2028 es bisiesto: el 29 de febrero existe.
                "2028-02-29T12:00:00" to "2028-02-29T18:00:00Z",
                // El nanosegundo más cercano a la medianoche.
                "2026-11-05T23:59:59.999999999" to "2026-11-06T05:59:59.999999999Z",
            )

        for ((fecha, esperado) in casos) {
            assertEquals(Instant.parse(esperado), dto(fecha = fecha).toDomain().fechaInscripcion)
        }
    }

    @Test
    fun `limites y vacios - fechas invalidas lanzan excepcion`() {
        val invalidas =
            listOf(
                "", // vacía
                "   ",
                "2026-11-05", // sin hora
                "2026-02-29T12:00:00", // 2026 no es bisiesto
                "2026-13-01T00:00:00", // no existe el mes 13
                "2026-00-01T00:00:00", // ni el mes 0
                "2026-11-31T00:00:00", // noviembre tiene 30 días
                "2026-11-05T24:00:00", // la hora máxima es 23
                "2026-11-05T23:60:00", // el minuto máximo es 59
            )

        for (fecha in invalidas) {
            // assertThrows: la prueba PASA solo si el código lanza esa excepción.
            assertThrows("fecha='$fecha'", DateTimeParseException::class.java) {
                dto(fecha = fecha).toDomain()
            }
        }
    }

    @Test
    fun `nulos y vacios - el estado se limpia de espacios`() {
        val casos = mapOf("Activa" to "Activa", "  Activa  " to "Activa", "\tActiva\n" to "Activa", "" to "", "   " to "")

        for ((recibido, esperado) in casos) {
            assertEquals("estado='$recibido'", esperado, dto(estado = recibido).toDomain().estado)
        }
    }
}
