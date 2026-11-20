package com.app.android.TestCatalogo

import com.app.android.domain.entities.entitiesCatalogo.ShareContent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareContentTest {

    @Test
    fun noMuestraDatosDesconocidos() {
        val contenido = ShareContent(
            nombre = "Kira",
            edad = null,
            sexo = "",
            descripcion = "",
            enlace = "qarino://perro/123"
        )

        val texto = contenido.texto()

        assertFalse(texto.contains("null"))
        assertFalse(texto.contains("Edad:"))
        assertFalse(texto.contains("Sexo:"))
        assertFalse(texto.contains("Descripción:"))
        assertTrue(texto.contains("qarino://perro/123"))
    }

    @Test
    fun muestraDatosDisponibles() {
        val contenido = ShareContent(
            nombre = "Kira",
            edad = 3,
            sexo = "Hembra",
            descripcion = "Perro amigable",
            enlace = "qarino://perro/123"
        )

        val texto = contenido.texto()

        assertTrue(texto.contains("Edad: 3"))
        assertTrue(texto.contains("Sexo: Hembra"))
        assertTrue(texto.contains("Descripción: Perro amigable"))
    }
}
