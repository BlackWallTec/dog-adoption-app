package com.app.android.domain.entities.entitiesPerfiles

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class Registro(
    val nombre: String = "", val apellidos: String = "", val email: String = "",
    val telefono: String = "", val fechaNacimiento: String = "", val calle: String = "",
    val numeroExterior: String = "", val codigoPostal: String = "",
    val passwd: String = "", val confirmPasswd: String = "", val privacidad: Boolean = false,
)

fun Registro.normalizado() = copy(
    nombre = nombre.trim(), apellidos = apellidos.trim(), email = email.trim().lowercase(Locale.ROOT),
    telefono = telefono.trim(), fechaNacimiento = fechaNacimiento.trim(), calle = calle.trim(),
    numeroExterior = numeroExterior.trim(), codigoPostal = codigoPostal.trim(),
)

/** Mismas reglas que Express; el servidor vuelve a validarlas antes de insertar. */
fun validarRegistro(datos: Registro, hoy: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))): Map<String, String> {
    val d = datos.normalizado()
    val errores = linkedMapOf<String, String>()
    for ((campo, valor, limite) in listOf(Triple("nombre", d.nombre, 100), Triple("apellidos", d.apellidos, 150), Triple("calle", d.calle, 250))) {
        if (valor.isBlank() || valor.length > limite) errores[campo] = "Completa este campo (máximo $limite caracteres)."
    }
    if (d.email.length > 254 || !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(d.email)) {
        errores["email"] = "Ingresa un correo válido."
    } else if (d.email.substringAfter('@') !in setOf("gmail.com", "outlook.com", "yahoo.com", "hotmail.com", "icloud.com")) {
        errores["email"] = "Utiliza Gmail, Outlook, Yahoo, Hotmail o iCloud."
    }
    if (!Regex("^\\+?[\\d ()-]+$").matches(d.telefono) || d.telefono.count(Char::isDigit) !in 7..15) {
        errores["telefono"] = "Ingresa un teléfono de entre 7 y 15 dígitos."
    }
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val posicion = ParsePosition(0)
    val nacimiento = formato.parse(d.fechaNacimiento, posicion)
    if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(d.fechaNacimiento) || nacimiento == null || posicion.index != d.fechaNacimiento.length || formato.format(nacimiento) != d.fechaNacimiento) {
        errores["fechaNacimiento"] = "Ingresa una fecha válida con formato AAAA-MM-DD."
    } else {
        val fecha = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = nacimiento }
        var edad = hoy.get(Calendar.YEAR) - fecha.get(Calendar.YEAR)
        if (hoy.get(Calendar.MONTH) * 100 + hoy.get(Calendar.DAY_OF_MONTH) < fecha.get(Calendar.MONTH) * 100 + fecha.get(Calendar.DAY_OF_MONTH)) edad--
        if (edad < 18) errores["fechaNacimiento"] = "Debes ser mayor de edad para crear una cuenta."
    }
    if (!Regex("^\\d{1,9}$").matches(d.numeroExterior)) errores["numeroExterior"] = "Ingresa el número exterior, únicamente con números."
    if (!Regex("^\\d{5}$").matches(d.codigoPostal)) errores["codigoPostal"] = "Ingresa un código postal de 5 dígitos."
    if (d.passwd.length < 8) errores["passwd"] = "Usa al menos 8 caracteres."
    else if (d.passwd.toByteArray(Charsets.UTF_8).size > 72) errores["passwd"] = "La contraseña es demasiado larga; utiliza menos caracteres."
    if (d.confirmPasswd.isEmpty() || d.passwd != d.confirmPasswd) errores["confirmPasswd"] = "Las contraseñas deben coincidir."
    if (!d.privacidad) errores["privacidad"] = "Acepta el aviso de privacidad para crear tu cuenta."
    return errores
}

class RegistroInvalidoException(val campos: Map<String, String>) : Exception("Revisa los campos del registro")
