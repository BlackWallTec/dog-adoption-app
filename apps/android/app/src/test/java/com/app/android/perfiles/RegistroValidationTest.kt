package com.app.android.perfiles

import com.app.android.domain.entities.entitiesPerfiles.normalizado
import com.app.android.domain.entities.entitiesPerfiles.validarRegistro
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

class RegistroValidationTest {
    private fun today(year: Int = 2026, month: Int = 11, day: Int = 5) = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear(); set(year, month - 1, day)
    }
    @Test fun `normaliza nombres y correo pero conserva exactamente la contrasena`() {
        val datos = registroValido.copy(email = " ANA@GMAIL.COM ", nombre = " Ana ", passwd = " con espacios ", confirmPasswd = " con espacios ")
        assertTrue(validarRegistro(datos, today()).isEmpty())
        assertEquals("ana@gmail.com", datos.normalizado().email)
        assertEquals("Ana", datos.normalizado().nombre)
        assertEquals(" con espacios ", datos.normalizado().passwd)
    }
    @Test fun `rechaza menores y fechas imposibles pero admite cumplir dieciocho hoy`() {
        for ((date, valid) in listOf("2008-11-05" to true, "2008-11-06" to false, "2999-01-01" to false, "2000-02-30" to false, "2000-02-29" to true, "0000-01-01" to false)) {
            assertEquals(date, valid, validarRegistro(registroValido.copy(fechaNacimiento = date), today())["fechaNacimiento"] == null)
        }
        val leap = registroValido.copy(fechaNacimiento = "2008-02-29")
        assertNotNull(validarRegistro(leap, today(2026, 2, 28))["fechaNacimiento"])
        assertNull(validarRegistro(leap, today(2026, 3, 1))["fechaNacimiento"])
    }
    @Test fun `correo telefono direccion y aceptacion se validan antes de enviar`() {
        val datos = registroValido.copy(email = "ana@otro.test", telefono = "123", calle = "", numeroExterior = "-1", codigoPostal = "0000", privacidad = false)
        val errores = validarRegistro(datos, today())
        assertTrue(errores.keys.containsAll(listOf("email", "telefono", "calle", "numeroExterior", "codigoPostal", "privacidad")))
    }
    @Test fun `no trunca contrasenas multibyte ni acepta confirmaciones distintas`() {
        assertNotNull(validarRegistro(registroValido.copy(passwd = "á".repeat(37)), today())["passwd"])
        assertNotNull(validarRegistro(registroValido.copy(confirmPasswd = "diferente"), today())["confirmPasswd"])
        assertNotNull(validarRegistro(registroValido.copy(passwd = "corta"), today())["passwd"])
    }
}
