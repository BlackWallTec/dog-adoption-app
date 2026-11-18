package com.app.android.voluntariado

import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.presentation.viewmodel.viewModelVoluntariado.AccionPrograma
import com.app.android.presentation.viewmodel.viewModelVoluntariado.toUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * PRUEBAS DE Programa.toUi(ahora): la entidad convertida en lo que la tarjeta pinta.
 *
 * Siempre usamos la hora fija `AHORA` (Herramientas.kt): así un programa "futuro" no se
 * vuelve "ya comenzó" la próxima semana.
 */
class ProgramaUiTest {
    // ------------------------------------------------------------------------------
    // TEXTO DEL CUPO
    // ------------------------------------------------------------------------------

    @Test
    fun `textoCupo - muestra ocupados, maximo y disponibles`() {
        val ui = programaValido().copy(cupoMaximo = 15, cupoDisponible = 7).toUi(AHORA)

        assertEquals("8/15 lugares ocupados · 7 disponibles", ui.textoCupo)
    }

    @Test
    fun `textoCupo - sin limite`() {
        val ui = programaValido().copy(cupoMaximo = null, cupoDisponible = null).toUi(AHORA)

        assertEquals("Sin límite de lugares", ui.textoCupo)
    }

    @Test
    fun `textoCupo - lleno y vacio`() {
        assertEquals(
            "10/10 lugares ocupados · 0 disponibles",
            programaValido().copy(cupoMaximo = 10, cupoDisponible = 0).toUi(AHORA).textoCupo,
        )
        assertEquals(
            "0/10 lugares ocupados · 10 disponibles",
            programaValido().copy(cupoMaximo = 10, cupoDisponible = 10).toUi(AHORA).textoCupo,
        )
    }

    @Test
    fun `textoCupo - con limite pero sin cupoDisponible muestra todo libre`() {
        val ui = programaValido().copy(cupoMaximo = 10, cupoDisponible = null).toUi(AHORA)

        assertEquals("0/10 lugares ocupados · 10 disponibles", ui.textoCupo)
    }

    // ------------------------------------------------------------------------------
    // BARRA DE PROGRESO
    // ------------------------------------------------------------------------------

    @Test
    fun `progresoCupo - sin limite la barra queda vacia`() {
        val ui = programaValido().copy(cupoMaximo = null, cupoDisponible = null).toUi(AHORA)

        assertEquals(0f, ui.progresoCupo, 0f)
    }

    @Test
    fun `progresoCupo - cupo cero la barra queda llena`() {
        val ui = programaValido().copy(cupoMaximo = 0, cupoDisponible = 0).toUi(AHORA)

        assertEquals(1f, ui.progresoCupo, 0f)
    }

    @Test
    fun `progresoCupo - lleno, a medias y vacio`() {
        assertEquals(1f, programaValido().copy(cupoMaximo = 10, cupoDisponible = 0).toUi(AHORA).progresoCupo, 0f)
        assertEquals(8f / 15, programaValido().copy(cupoMaximo = 15, cupoDisponible = 7).toUi(AHORA).progresoCupo, 1e-6f)
        assertEquals(0f, programaValido().copy(cupoMaximo = 10, cupoDisponible = 10).toUi(AHORA).progresoCupo, 0f)
    }

    @Test
    fun `progresoCupo - siempre entre 0 y 1 aunque los datos vengan raros`() {
        for (disponibles in listOf(-100, -1, 11, 1000)) {
            val progreso = programaValido().copy(cupoMaximo = 10, cupoDisponible = disponibles).toUi(AHORA).progresoCupo

            assertTrue("disponibles=$disponibles progreso=$progreso", progreso in 0f..1f)
        }
    }

    // ------------------------------------------------------------------------------
    // BOTÓN
    // ------------------------------------------------------------------------------

    @Test
    fun `boton - disponible permite inscribirse`() {
        val ui = programaValido().toUi(AHORA)

        assertEquals("Inscribirme", ui.textoBoton)
        assertTrue(ui.botonHabilitado)
    }

    @Test
    fun `boton - cada motivo de bloqueo tiene su texto y queda deshabilitado`() {
        val casos =
            listOf(
                // Inscrito en un programa ya iniciado: ya no se puede cancelar (HU37)
                programaValido().copy(usuarioInscrito = true, fechaInicio = AHORA) to "Inscrito",
                programaValido().copy(cupoDisponible = 0) to "Sin lugares",
                programaValido().copy(fechaInicio = AHORA) to "Ya comenzó",
                programaValido().copy(estado = EstadoPrograma.NO_DISPONIBLE) to "No disponible",
            )

        for ((programa, texto) in casos) {
            val ui = programa.toUi(AHORA)

            assertEquals(texto, ui.textoBoton)
            assertFalse(texto, ui.botonHabilitado)
        }
    }

    @Test
    fun `boton - inscrito gana sobre sin lugares`() {
        // Un inscrito en un programa que ya se llenó debe poder cancelar (HU37), no ver "Sin lugares".
        val ui = programaValido().copy(usuarioInscrito = true, cupoDisponible = 0).toUi(AHORA)

        assertEquals("Cancelar", ui.textoBoton)
        assertTrue(ui.botonHabilitado)
        assertEquals(AccionPrograma.CANCELAR, ui.accion)
    }

    @Test
    fun `boton - HU37 cancelar solo antes de que inicie el programa`() {
        val inscrito = programaValido().copy(usuarioInscrito = true)

        // Un instante antes de iniciar: se puede cancelar
        val antes = inscrito.toUi(inscrito.fechaInicio.minusNanos(1))
        assertEquals("Cancelar", antes.textoBoton)
        assertEquals(AccionPrograma.CANCELAR, antes.accion)

        // Justo al iniciar: ya no
        val alIniciar = inscrito.toUi(inscrito.fechaInicio)
        assertEquals("Inscrito", alIniciar.textoBoton)
        assertFalse(alIniciar.botonHabilitado)
        assertEquals(AccionPrograma.INSCRIBIRSE, alIniciar.accion)
    }

    @Test
    fun `boton - la hora que se pasa decide si ya comenzo`() {
        val programa = programaValido()

        assertEquals("Inscribirme", programa.toUi(programa.fechaInicio.minusSeconds(1)).textoBoton)
        assertEquals("Ya comenzó", programa.toUi(programa.fechaInicio).textoBoton)
    }

    // ------------------------------------------------------------------------------
    // RESTO DE LA TARJETA
    // ------------------------------------------------------------------------------

    @Test
    fun `datos - id, tipo, titulo y horario en la zona del refugio`() {
        // programaValido(): inicio y fin a las 18:00 UTC = 12:00 en Ciudad de México.
        val ui = programaValido().toUi(AHORA)

        assertEquals("programa-1", ui.id)
        assertEquals("Paseo", ui.tipo)
        assertEquals("Paseo de perritos", ui.titulo)
        assertEquals("12:00 – 12:00 h", ui.horario)
        assertTrue(ui.fecha, ui.fecha.first().isUpperCase())
        assertTrue(ui.fecha, ui.fecha.endsWith("2026"))
    }

    @Test
    fun `nulos - sin ubicacion usa la del refugio`() {
        assertEquals("Refugio", programaValido().toUi(AHORA).ubicacion)
        assertEquals("Refugio Qariño · Querétaro", programaValido().copy(ubicacion = null).toUi(AHORA).ubicacion)
    }
}
