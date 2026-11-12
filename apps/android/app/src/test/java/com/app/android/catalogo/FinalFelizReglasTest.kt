package com.app.android.catalogo

import com.app.android.data.dtos.dtosCatalogo.FinalFelizDto
import com.app.android.data.dtos.dtosCatalogo.toDomain
import com.app.android.domain.entities.entitiesCatalogo.FinalFeliz
import com.app.android.domain.entities.entitiesCatalogo.MAX_FINALES_EN_CARRUSEL
import com.app.android.domain.entities.entitiesCatalogo.paraCarrusel
import com.app.android.presentation.view.atomicDesign.organism.fechaAdopcionLegible
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalFelizReglasTest {
    @Test
    fun `rangos - solo adoptado y adoptada entran al carrusel`() {
        val estados =
            listOf(
                "Adoptado" to true,
                "adoptada" to true,
                "  ADOPTADO  " to true,
                "Disponible" to false,
                "En proceso de adopción" to false,
                "" to false,
                "Adoptados" to false,
            )

        for ((estado, entra) in estados) {
            val resultado = listOf(final(estado = estado)).paraCarrusel()
            assertEquals(estado, entra, resultado.isNotEmpty())
        }
    }

    @Test
    fun `nulos y vacios - sin foto, sin historia, sin id o sin publicar no entra`() {
        val rechazados =
            listOf(
                final(foto = ""),
                final(foto = "   "),
                final(historia = ""),
                final(historia = "   "),
                final(id = ""),
                final(id = "   "),
                final(publicado = false),
            )

        for (item in rechazados) {
            assertTrue(item.toString(), listOf(item).paraCarrusel().isEmpty())
        }
    }

    @Test
    fun `nulos - el dto con campos null no se publica y limpia el texto`() {
        val dominio =
            FinalFelizDto(
                idPerro = "  perro-1  ",
                nombre = null,
                fotoHogarUrl = "  https://foto  ",
                historia = "  Encontró hogar.  ",
                fechaAdopcion = "   ",
                estado = " Adoptado ",
                publicado = null,
            ).toDomain()

        assertEquals("perro-1", dominio.idPerro)
        assertEquals("Sin nombre", dominio.nombre)
        assertEquals("https://foto", dominio.fotoHogarUrl)
        assertEquals("Encontró hogar.", dominio.historia)
        assertNull(dominio.fechaAdopcion)
        assertEquals("Adoptado", dominio.estado)
        assertEquals(false, dominio.publicado)
        assertTrue(listOf(dominio).paraCarrusel().isEmpty())
    }

    @Test
    fun `limite - conserva las 20 adopciones mas recientes`() {
        val finales =
            (0..MAX_FINALES_EN_CARRUSEL).map { indice ->
                final(
                    id = "perro-$indice",
                    fecha = "2024-01-${(indice + 1).toString().padStart(2, '0')}",
                )
            }

        val carrusel = finales.paraCarrusel()

        assertEquals(MAX_FINALES_EN_CARRUSEL, carrusel.size)
        assertEquals("perro-$MAX_FINALES_EN_CARRUSEL", carrusel.first().idPerro)
        assertTrue(carrusel.none { it.idPerro == "perro-0" })
    }

    @Test
    fun `estructural - el duplicado conserva la primera historia y la fecha vacia va al final`() {
        val primero = final(id = "mismo", historia = "Primera historia.", fecha = null)
        val repetido = final(id = " mismo ", historia = "Segunda historia.", fecha = "2026-08-01")
        val reciente = final(id = "reciente", fecha = "2026-09-01")
        val antigua = final(id = "antigua", fecha = "2020-01-01")
        val sinFecha = final(id = "sin-fecha", fecha = null)

        val carrusel = listOf(sinFecha, antigua, primero, repetido, reciente).paraCarrusel()

        assertEquals(listOf("reciente", "antigua", "sin-fecha", "mismo"), carrusel.map { it.idPerro })
        assertEquals("Primera historia.", carrusel.first { it.idPerro == "mismo" }.historia)
    }

    @Test
    fun `fecha - un dia iso se lee en espanol y un texto raro se conserva`() {
        assertNull(fechaAdopcionLegible(null))
        assertNull(fechaAdopcionLegible("   "))
        assertEquals("15 de marzo de 2026", fechaAdopcionLegible("2026-03-15"))
        assertEquals("15 de marzo de 2026", fechaAdopcionLegible("2026-03-15T18:30:00"))
        assertEquals("ayer", fechaAdopcionLegible("ayer"))
    }

    private fun final(
        id: String = "perro-1",
        nombre: String = "Luna",
        foto: String = "https://foto",
        historia: String = "Ya tiene hogar.",
        fecha: String? = "2026-03-01",
        estado: String = "Adoptado",
        publicado: Boolean = true,
    ) = FinalFeliz(
        idPerro = id,
        nombre = nombre,
        fotoHogarUrl = foto,
        historia = historia,
        fechaAdopcion = fecha,
        estado = estado,
        publicado = publicado,
    )
}
