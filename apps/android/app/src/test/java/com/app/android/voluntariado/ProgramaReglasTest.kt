package com.app.android.voluntariado

import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.entities.entitiesVoluntariado.conInscripcionConfirmada
import com.app.android.domain.entities.entitiesVoluntariado.validarInscripcion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

/*
 * BIENVENIDO: ESTE ES EL MEJOR ARCHIVO PARA EMPEZAR
 *
 * Una prueba unitaria revisa una pieza pequeña de código, sola y sin internet.
 * Casi todas siguen tres pasos, conocidos como "AAA":
 *   1. Arrange (preparar): armamos los datos.
 *   2. Act (actuar): ejecutamos el código que queremos probar.
 *   3. Assert (afirmar): comprobamos que el resultado sea el esperado.
 *
 * `@Test` le dice a JUnit "esta función es una prueba".
 * `assertEquals(esperado, real)` hace fallar la prueba si los valores son distintos.
 *
 * Aquí probamos las reglas de `ProgramaReglas.kt` y `Programa.kt` con 4 técnicas:
 *   - RANGOS: agrupar los valores en familias y probar todas las familias.
 *   - NULOS/VACÍOS: ¿qué pasa si falta el dato?
 *   - VALORES LÍMITE: los errores suelen esconderse en la frontera (`<` contra `<=`).
 *   - ESTRUCTURAL: que cada decisión tome los dos caminos (verdadero y falso).
 */
class ProgramaReglasTest {
    // ------------------------------------------------------------------------------
    // 1. COBERTURA DE RANGOS
    // Para el cupo hay tres familias: negativos y cero (bloquean), y positivos (pasan).
    // ------------------------------------------------------------------------------

    @Test
    fun `rangos - cupo negativo o cero siempre bloquea con SinCupo`() {
        // Prueba de estrés: en lugar de un solo valor, recorremos mil.
        for (cupo in -1000..0) {
            val programa = programaValido().copy(cupoDisponible = cupo)

            // El primer texto es un mensaje de ayuda: si falla, dice con qué cupo fue.
            assertEquals("cupo=$cupo", VoluntariadoError.SinCupo, programa.validarInscripcion(AHORA))
        }
    }

    @Test
    fun `rangos - cupo positivo siempre permite inscribirse`() {
        for (cupo in 1..1000) {
            val programa = programaValido().copy(cupoDisponible = cupo)

            // `null` significa "no hay ningún motivo para bloquear", es decir, se puede.
            assertNull("cupo=$cupo", programa.validarInscripcion(AHORA))
        }
    }

    @Test
    fun `rangos - fechas de inicio pasadas bloquean y futuras permiten`() {
        val diasDeDistancia = listOf(1L, 7L, 30L, 365L, 3650L)

        for (dias in diasDeDistancia) {
            val pasado = programaValido().copy(fechaInicio = AHORA.minus(Duration.ofDays(dias)))
            val futuro = programaValido().copy(fechaInicio = AHORA.plus(Duration.ofDays(dias)))

            assertEquals("hace $dias días", VoluntariadoError.ProgramaIniciado, pasado.validarInscripcion(AHORA))
            assertNull("en $dias días", futuro.validarInscripcion(AHORA))
        }
    }

    @Test
    fun `rangos - textos de estado reconocidos e irreconocibles`() {
        // Familia 1: "Disponible", sin importar mayúsculas ni espacios alrededor.
        for (texto in listOf("Disponible", "disponible", "DISPONIBLE", "  Disponible  ", "\tdIsPoNiBlE\n")) {
            assertEquals("texto='$texto'", EstadoPrograma.DISPONIBLE, EstadoPrograma.desdeTexto(texto))
        }

        // Familia 2: cualquier otro texto, incluso uno muy parecido.
        for (texto in listOf("Cerrado", "No disponible", "Disponibles", "Dispon ible", "123")) {
            assertEquals("texto='$texto'", EstadoPrograma.NO_DISPONIBLE, EstadoPrograma.desdeTexto(texto))
        }
    }

    @Test
    fun `rangos - confirmar inscripcion descuenta un lugar en cupos positivos`() {
        for (cupo in 1..1000) {
            val confirmado = programaValido().copy(cupoDisponible = cupo).conInscripcionConfirmada()

            assertEquals("cupo=$cupo", cupo - 1, confirmado.cupoDisponible)
            assertTrue(confirmado.usuarioInscrito)
        }
    }

    // ------------------------------------------------------------------------------
    // 2. COBERTURA DE VALORES NULOS / VACÍOS
    // Son la causa de muchísimos errores en producción. Aquí `null` en el cupo significa
    // "sin límite", así que NO debe confundirse con "sin lugares".
    // ------------------------------------------------------------------------------

    @Test
    fun `nulos - cupo disponible null significa sin limite y permite inscribirse`() {
        val programa = programaValido().copy(cupoMaximo = null, cupoDisponible = null)

        assertNull(programa.validarInscripcion(AHORA))
    }

    @Test
    fun `nulos - confirmar inscripcion con cupo null lo deja en null`() {
        val confirmado = programaValido().copy(cupoDisponible = null).conInscripcionConfirmada()

        assertNull(confirmado.cupoDisponible) // sigue siendo "sin límite"
        assertTrue(confirmado.usuarioInscrito)
    }

    @Test
    fun `nulos - descripcion y ubicacion opcionales no afectan la validacion`() {
        val programa = programaValido().copy(descripcion = null, ubicacion = null)

        assertNull(programa.validarInscripcion(AHORA))
    }

    @Test
    fun `vacios - textos vacios no afectan la validacion`() {
        // La regla no revisa estos campos. La prueba documenta ese comportamiento:
        // quien rechaza un id vacío es la API (ver las pruebas de apps/api).
        val programa =
            programaValido().copy(id = "", nombre = "", tipo = "", descripcion = "", ubicacion = "")

        assertNull(programa.validarInscripcion(AHORA))
    }

    @Test
    fun `nulos y vacios - estado sin texto se considera no disponible`() {
        for (texto in listOf(null, "", " ", "    ", "\t\n")) {
            assertEquals("texto='$texto'", EstadoPrograma.NO_DISPONIBLE, EstadoPrograma.desdeTexto(texto))
        }
    }

    // ------------------------------------------------------------------------------
    // 3. ANÁLISIS DE VALORES LÍMITE
    // La frontera del cupo está entre 0 y 1. Probamos justo antes, en ella y justo después.
    // Si alguien cambiara `<= 0` por `< 0`, la prueba del cupo 0 lo descubriría.
    // ------------------------------------------------------------------------------

    @Test
    fun `limites - cupo alrededor de cero`() {
        // Una "tabla de casos": cada par es (cupo, resultado esperado).
        val casos =
            listOf(
                Int.MIN_VALUE to VoluntariadoError.SinCupo, // el Int más chico que existe
                -1 to VoluntariadoError.SinCupo,
                0 to VoluntariadoError.SinCupo, // última frontera que bloquea
                1 to null, // primera que permite: el último lugar disponible
                2 to null,
                Int.MAX_VALUE to null, // el Int más grande que existe
            )

        for ((cupo, esperado) in casos) {
            val programa = programaValido().copy(cupoDisponible = cupo)

            assertEquals("cupo=$cupo", esperado, programa.validarInscripcion(AHORA))
        }
    }

    @Test
    fun `limites - fecha de inicio alrededor del instante actual`() {
        // Un nanosegundo es la unidad más pequeña que maneja Instant.
        val casos =
            listOf(
                Instant.MIN to VoluntariadoError.ProgramaIniciado, // la fecha más antigua posible
                AHORA.minusNanos(1) to VoluntariadoError.ProgramaIniciado,
                AHORA to VoluntariadoError.ProgramaIniciado, // empieza justo ahora: ya es tarde
                AHORA.plusNanos(1) to null, // falta un instante: aún se puede
                Instant.MAX to null, // la fecha más lejana posible
            )

        for ((inicio, esperado) in casos) {
            val programa = programaValido().copy(fechaInicio = inicio)

            assertEquals("inicio=$inicio", esperado, programa.validarInscripcion(AHORA))
        }
    }

    @Test
    fun `limites - confirmar inscripcion nunca deja el cupo por debajo de cero`() {
        val casos =
            listOf(
                2 to 1,
                1 to 0, // se ocupa el último lugar
                0 to 0, // sin lugares no se resta: no puede quedar en -1
                -1 to 0,
                Int.MAX_VALUE to Int.MAX_VALUE - 1,
            )

        for ((antes, despues) in casos) {
            val confirmado = programaValido().copy(cupoDisponible = antes).conInscripcionConfirmada()

            assertEquals("cupo=$antes", despues, confirmado.cupoDisponible)
        }
    }

    @Test
    fun `limites - el Int mas chico no desborda el cupo`() {
        // ¿Qué bug había? Un Int no puede bajar de Int.MIN_VALUE: al restarle 1 "da la vuelta"
        // y se convierte en el número más grande, así que un cupo negativo aparecía como
        // 2,147,483,647 lugares. La prueba de estrés lo descubrió.
        // ¿Qué valida ahora? Que el código revise el valor ANTES de restar: si ya es 0 o
        // menos, el cupo queda en 0 y la resta nunca ocurre.
        val confirmado = programaValido().copy(cupoDisponible = Int.MIN_VALUE).conInscripcionConfirmada()

        assertEquals(0, confirmado.cupoDisponible)
    }

    // ------------------------------------------------------------------------------
    // 4. COBERTURA ESTRUCTURAL (DECISIÓN / CONDICIÓN)
    // - Decisión: cada `if` o `when` debe salir verdadero Y falso.
    // - Condición: en `a && b`, cada parte (a y b) también debe tomar ambos valores.
    // ------------------------------------------------------------------------------

    @Test
    fun `estructural - cada rama del when produce su resultado`() {
        val base = programaValido()

        // Una prueba por cada línea del `when` de validarInscripcion:
        assertEquals(VoluntariadoError.YaInscrito, base.copy(usuarioInscrito = true).validarInscripcion(AHORA))
        assertEquals(
            VoluntariadoError.ProgramaNoDisponible,
            base.copy(estado = EstadoPrograma.NO_DISPONIBLE).validarInscripcion(AHORA),
        )
        assertEquals(VoluntariadoError.ProgramaIniciado, base.copy(fechaInicio = AHORA).validarInscripcion(AHORA))
        assertEquals(VoluntariadoError.SinCupo, base.copy(cupoDisponible = 0).validarInscripcion(AHORA))
        assertNull(base.validarInscripcion(AHORA)) // la rama `else`
    }

    @Test
    fun `estructural - las dos condiciones de la regla de cupo`() {
        // La línea es: cupoDisponible != null && cupoDisponible <= 0
        //
        //   | != null | <= 0        | decisión | caso    |
        //   |---------|-------------|----------|---------|
        //   | falso   | no se evalúa| falso    | null    |
        //   | cierto  | cierto      | cierto   | 0       |
        //   | cierto  | falso       | falso    | 1       |
        //
        // `&&` tiene "cortocircuito": si la izquierda es falsa, ya no evalúa la derecha.
        assertNull(programaValido().copy(cupoDisponible = null).validarInscripcion(AHORA))
        assertEquals(VoluntariadoError.SinCupo, programaValido().copy(cupoDisponible = 0).validarInscripcion(AHORA))
        assertNull(programaValido().copy(cupoDisponible = 1).validarInscripcion(AHORA))
    }

    @Test
    fun `estructural - todas las combinaciones respetan el orden de prioridad`() {
        // Estrés por combinaciones: 2 x 2 x 3 x 4 = 48 programas distintos.
        // Si un programa tiene varios problemas, se reporta el de la regla que va primero.
        val pasado = AHORA.minusSeconds(1)
        val futuro = AHORA.plusSeconds(1)
        var combinaciones = 0

        for (inscrito in listOf(true, false)) {
            for (estado in EstadoPrograma.entries) {
                for (inicio in listOf(pasado, AHORA, futuro)) {
                    for (cupo in listOf(null, -1, 0, 1)) {
                        val programa =
                            programaValido().copy(
                                usuarioInscrito = inscrito,
                                estado = estado,
                                fechaInicio = inicio,
                                cupoDisponible = cupo,
                            )

                        val esperado =
                            when {
                                inscrito -> VoluntariadoError.YaInscrito
                                estado == EstadoPrograma.NO_DISPONIBLE -> VoluntariadoError.ProgramaNoDisponible
                                inicio != futuro -> VoluntariadoError.ProgramaIniciado
                                cupo == -1 || cupo == 0 -> VoluntariadoError.SinCupo
                                else -> null
                            }

                        assertEquals("$programa", esperado, programa.validarInscripcion(AHORA))
                        combinaciones++
                    }
                }
            }
        }

        // Comprobamos que los ciclos sí corrieron: una prueba que no ejecuta nada siempre "pasa".
        assertEquals(48, combinaciones)
    }

    @Test
    fun `estructural - confirmar inscripcion conserva los demas datos`() {
        val original = programaValido()

        val confirmado = original.conInscripcionConfirmada()

        // `copy` solo debe cambiar dos campos, así que restauramos esos dos y comparamos.
        assertEquals(
            original,
            confirmado.copy(usuarioInscrito = false, cupoDisponible = original.cupoDisponible),
        )
    }
}
