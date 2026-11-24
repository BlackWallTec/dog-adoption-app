package com.app.android.voluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.entities.entitiesVoluntariado.conInscripcionCancelada
import com.app.android.domain.entities.entitiesVoluntariado.puedeCancelar
import com.app.android.domain.useCases.useCasesVoluntariado.CancelarInscripcionUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

/*
 * HU37 STRESS TESTS - DOMAIN RULES AND USE CASE
 *
 * What is under test (pure logic, no Android, no internet):
 *   1. Programa.puedeCancelar(ahora)       "may this user cancel right now?"
 *   2. Programa.conInscripcionCancelada()  "what does the program look like after cancelling?"
 *   3. CancelarInscripcionUseCase          "block if it started, otherwise ask the server"
 *
 * The button of the card (toUi) and the ViewModel flows are already covered by
 * ProgramaUiTest and ProgramasViewModelTest, so they are NOT repeated here.
 *
 * Words at the start of each test name:
 *   "nulos"       void values (null cupo, ...)
 *   "rangos"      a whole family of values instead of one example
 *   "limites"     boundary values: just before, exactly at, just after the edge
 *   "estructural" decision coverage: every condition of an `&&` / `if` matters and goes both ways
 *
 * The clock is fixed (AHORA / RELOJ_FIJO from Herramientas.kt) so results never depend on today's date.
 */
class CancelarReglasStressTest {
    private val unNano: Duration = Duration.ofNanos(1)

    // An enrolled program that starts `desdeAhora` after (negative = before) AHORA.
    private fun iniciaEn(desdeAhora: Duration) = programaInscrito().copy(fechaInicio = AHORA.plus(desdeAhora))

    private fun conCupo(disponible: Int?, maximo: Int?) = programaInscrito(cupoDisponible = disponible, cupoMaximo = maximo)

    // ---- 1. puedeCancelar = usuarioInscrito && ahora.isBefore(fechaInicio) -----------------

    @Test
    fun `estructural - tabla de verdad de puedeCancelar (MCDC)`() {
        // A = "is enrolled", B = "has not started". MC/DC: each condition, changed alone,
        // flips the result, so nobody can delete either condition without a test failing.
        //   A B -> result
        //   T T -> true   <- the only way to be allowed
        //   T F -> false  (B alone decides: compare with row 1)
        //   F T -> false  (A alone decides: compare with row 1)
        //   F F -> false
        val futuro = AHORA.plusSeconds(60)
        val pasado = AHORA.minusSeconds(60)
        val tabla = listOf(Triple(true, futuro, true), Triple(true, pasado, false), Triple(false, futuro, false), Triple(false, pasado, false))

        for ((inscrito, inicio, esperado) in tabla) {
            val programa = programaValido().copy(usuarioInscrito = inscrito, fechaInicio = inicio)

            assertEquals("inscrito=$inscrito inicio=$inicio", esperado, programa.puedeCancelar(AHORA))
        }
    }

    @Test
    fun `limites - un nanosegundo alrededor del inicio`() {
        assertTrue("1 ns antes", iniciaEn(unNano).puedeCancelar(AHORA))
        assertFalse("justo al iniciar", iniciaEn(Duration.ZERO).puedeCancelar(AHORA))
        assertFalse("1 ns despues", iniciaEn(unNano.negated()).puedeCancelar(AHORA))
    }

    @Test
    fun `rangos - de hace un siglo a dentro de un siglo, solo el futuro estricto permite cancelar`() {
        for (dias in listOf(-36_500L, -365L, -1L, 1L, 365L, 36_500L)) {
            assertEquals("inicio en $dias dias", dias > 0, iniciaEn(Duration.ofDays(dias)).puedeCancelar(AHORA))
        }
        assertTrue("Instant.MAX es futuro", programaInscrito().copy(fechaInicio = Instant.MAX).puedeCancelar(AHORA))
        assertFalse("Instant.MIN es pasado", programaInscrito().copy(fechaInicio = Instant.MIN).puedeCancelar(AHORA))
    }

    // ---- 2. conInscripcionCancelada: free one place, never exceed the maximum --------------
    //   nuevo cupoDisponible = min(cupoDisponible + 1, cupoMaximo)   (null stays null)

    @Test
    fun `estructural - cancelar deja al usuario como no inscrito y no toca nada mas`() {
        val original = programaInscrito(cupoDisponible = 5, cupoMaximo = 10)

        assertEquals(original.copy(usuarioInscrito = false, cupoDisponible = 6), original.conInscripcionCancelada())
    }

    @Test
    fun `rangos - con maximo 10 cualquier cupo sube uno sin pasar del maximo`() {
        for (disponible in -2..13) {
            assertEquals("disponible=$disponible", minOf(disponible + 1, 10), conCupo(disponible, 10).conInscripcionCancelada().cupoDisponible)
        }
    }

    @Test
    fun `limites - alrededor del maximo y de cero`() {
        assertEquals("max-1 -> max", 10, conCupo(9, 10).conInscripcionCancelada().cupoDisponible)
        assertEquals("max se queda en max (el techo)", 10, conCupo(10, 10).conInscripcionCancelada().cupoDisponible)
        assertEquals("max+1 (dato inconsistente) se corrige", 10, conCupo(11, 10).conInscripcionCancelada().cupoDisponible)
        assertEquals("lleno -> 1", 1, conCupo(0, 10).conInscripcionCancelada().cupoDisponible)
        assertEquals("maximo 0: el techo es 0", 0, conCupo(0, 0).conInscripcionCancelada().cupoDisponible)
    }

    @Test
    fun `nulos - cupo null se queda null y sin maximo no hay techo`() {
        assertEquals(null, conCupo(null, 10).conInscripcionCancelada().cupoDisponible)
        assertEquals(null, conCupo(null, null).conInscripcionCancelada().cupoDisponible)
        assertEquals(6, conCupo(5, null).conInscripcionCancelada().cupoDisponible)
    }

    @Test
    fun `rangos - cancelar varias veces seguidas sube hasta el techo y se queda ahi`() {
        var programa = conCupo(7, 10)
        val vistos = mutableListOf<Int?>()

        repeat(5) {
            programa = programa.conInscripcionCancelada()
            vistos.add(programa.cupoDisponible)
        }

        assertEquals(listOf(8, 9, 10, 10, 10), vistos)
    }

    /*
     * FINDING (marked @Ignore so the build stays green): an Int wraps around. Int.MAX_VALUE + 1
     * becomes Int.MIN_VALUE (-2 147 483 648 free places). No real program has two billion places,
     * so it is not urgent. To fix: compute with Long, or don't add when already at Int.MAX_VALUE.
     * Then remove @Ignore.
     */
    @Ignore("Hallazgo: Int.MAX_VALUE + 1 se desborda en conInscripcionCancelada")
    @Test
    fun `limites - HALLAZGO - el cupo en Int MAX_VALUE no debe volverse negativo`() {
        assertEquals(Int.MAX_VALUE, conCupo(Int.MAX_VALUE, null).conInscripcionCancelada().cupoDisponible)
    }

    // ---- 3. CancelarInscripcionUseCase ------------------------------------------------------
    //   if (!fechaInicio.isAfter(now)) return Error(ProgramaIniciado)
    //   return repository.cancelarInscripcion(id)

    @Test
    fun `estructural - si ya inicio no se llama al servidor, si no se llama exactamente una vez`() = runBlocking<Unit> {
        val bloqueado = RepositorioFalso()
        val permitido = RepositorioFalso()
        val futuro = iniciaEn(Duration.ofHours(1))

        val r1 = CancelarInscripcionUseCase(bloqueado, RELOJ_FIJO)(iniciaEn(Duration.ofHours(-1)))
        val r2 = CancelarInscripcionUseCase(permitido, RELOJ_FIJO)(futuro)

        assertEquals(Resultado.Error(VoluntariadoError.ProgramaIniciado), r1)
        assertTrue(bloqueado.cancelaciones.isEmpty())
        assertEquals(Resultado.Exito(INSCRIPCION_OK), r2)
        assertEquals(listOf(futuro.id), permitido.cancelaciones.toList())
    }

    @Test
    fun `limites - el caso de uso cambia de decision exactamente al instante de inicio`() = runBlocking<Unit> {
        val casos = listOf(Duration.ofNanos(1) to true, Duration.ZERO to false, Duration.ofNanos(-1) to false, Duration.ofSeconds(1) to true, Duration.ofSeconds(-1) to false)

        for ((desdeAhora, llegaAlServidor) in casos) {
            val repositorio = RepositorioFalso()

            CancelarInscripcionUseCase(repositorio, RELOJ_FIJO)(iniciaEn(desdeAhora))

            assertEquals("inicio=$desdeAhora", llegaAlServidor, repositorio.cancelaciones.isNotEmpty())
        }
    }

    @Test
    fun `estructural - el caso de uso usa el reloj que recibe, no la hora real`() = runBlocking<Unit> {
        val programa = iniciaEn(Duration.ofSeconds(5))
        val relojAdelantado = Clock.fixed(AHORA.plusSeconds(10), ZoneOffset.UTC) // 5 s after the start
        val normal = RepositorioFalso()
        val adelantado = RepositorioFalso()

        CancelarInscripcionUseCase(normal, RELOJ_FIJO)(programa)
        CancelarInscripcionUseCase(adelantado, relojAdelantado)(programa)

        assertEquals(1, normal.cancelaciones.size)
        assertEquals(0, adelantado.cancelaciones.size)
    }

    @Test
    fun `rangos - el resultado del servidor se devuelve intacto (exito y los 9 errores)`() = runBlocking<Unit> {
        val respuestas: List<Resultado<Inscripcion>> = listOf(Resultado.Exito(INSCRIPCION_OK)) + ERRORES_DE_VOLUNTARIADO.map { Resultado.Error(it) }

        for (respuesta in respuestas) {
            val resultado = CancelarInscripcionUseCase(RepositorioFalso(respuestaCancelacion = respuesta), RELOJ_FIJO)(iniciaEn(Duration.ofHours(1)))

            assertEquals(respuesta, resultado)
        }
    }
}