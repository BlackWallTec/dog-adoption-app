package com.app.android.voluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.useCases.useCasesVoluntariado.InscribirseProgramaUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * PRUEBAS DEL CASO DE USO
 *
 * El caso de uso solo toma una decisión:
 *   - ¿Hay un motivo para bloquear? Regresa el error y NO llama al servidor.
 *   - ¿No lo hay? Le pide al repositorio que haga la inscripción.
 *
 * `inscribirse` es una función `suspend`, y estas solo se pueden llamar desde una
 * corrutina. `runBlocking` crea una y espera a que termine, justo lo que queremos aquí.
 * El `<Unit>` es importante: JUnit exige que una prueba no regrese ningún valor.
 */
class InscribirseProgramaUseCaseTest {
    private fun casoDeUso(repositorio: RepositorioFalso) = InscribirseProgramaUseCase(repositorio, RELOJ_FIJO)

    @Test
    fun `estructural - sin bloqueos llama al repositorio con el id del programa`() = runBlocking<Unit> {
        val repositorio = RepositorioFalso()

        val resultado = casoDeUso(repositorio)(programaValido())

        assertEquals(Resultado.Exito(INSCRIPCION_OK), resultado)
        assertEquals(listOf("programa-1"), repositorio.llamadas.toList())
    }

    @Test
    fun `estructural - con bloqueo regresa el error sin llamar al repositorio`() = runBlocking<Unit> {
        val repositorio = RepositorioFalso()
        val programa = programaValido().copy(usuarioInscrito = true)

        val resultado = casoDeUso(repositorio)(programa)

        assertEquals(Resultado.Error(VoluntariadoError.YaInscrito), resultado)
        // Lo más importante: evitamos una petición al servidor que iba a fallar.
        assertTrue(repositorio.llamadas.isEmpty())
    }

    @Test
    fun `estructural - todo error del repositorio llega sin cambios`() = runBlocking<Unit> {
        for (error in TODOS_LOS_ERRORES) {
            val repositorio = RepositorioFalso(respuesta = Resultado.Error(error))

            val resultado = casoDeUso(repositorio)(programaValido())

            assertEquals(Resultado.Error(error), resultado)
        }
    }

    @Test
    fun `limites - el reloj decide si el programa ya inicio`() = runBlocking<Unit> {
        // Comprueba que el caso de uso usa el reloj que le damos, y no la hora real.
        val casos =
            listOf(
                AHORA.minusNanos(1) to false,
                AHORA to false,
                AHORA.plusNanos(1) to true,
            )

        for ((inicio, llegaAlServidor) in casos) {
            val repositorio = RepositorioFalso()

            casoDeUso(repositorio)(programaValido().copy(fechaInicio = inicio))

            assertEquals("inicio=$inicio", llegaAlServidor, repositorio.llamadas.isNotEmpty())
        }
    }

    @Test
    fun `rangos - el cupo decide si se llama al servidor`() = runBlocking<Unit> {
        for (cupo in -100..100) {
            val repositorio = RepositorioFalso()

            val resultado = casoDeUso(repositorio)(programaValido().copy(cupoDisponible = cupo))

            val esperado: Resultado<Inscripcion> =
                if (cupo <= 0) Resultado.Error(VoluntariadoError.SinCupo) else Resultado.Exito(INSCRIPCION_OK)
            assertEquals("cupo=$cupo", esperado, resultado)
        }
    }

    @Test
    fun `nulos - cupo null llega al servidor`() = runBlocking<Unit> {
        val repositorio = RepositorioFalso()

        casoDeUso(repositorio)(programaValido().copy(cupoDisponible = null))

        assertEquals(1, repositorio.llamadas.size)
    }

    @Test
    fun `vacios - un id vacio se envia tal cual y el servidor lo rechaza`() = runBlocking<Unit> {
        // Android no valida el id; la API responde 404 (ver sus pruebas). Aquí comprobamos
        // que el caso de uso no lo cambia ni lo pierde en el camino.
        val repositorio = RepositorioFalso()

        casoDeUso(repositorio)(programaValido().copy(id = ""))

        assertEquals(listOf(""), repositorio.llamadas.toList())
    }
}
