package com.app.android.domain.entities.entitiesPerfiles

import java.util.Calendar
import java.util.TimeZone

// Datos del formulario de registro (POST /register crea una cuenta Cliente).
data class Registro(
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val calle: String = "",
    val numeroExterior: String = "",
    // El backend lo valida como string: debe enviarse siempre (puede ser "").
    val numeroInterior: String = "",
    // id de la colonia elegida (id_codigo_postal), no el número postal.
    val idCodigoPostal: String = "",
    val contrasena: String = "",
    val confirmContrasena: String = "",
    val privacidad: Boolean = false,
)

/** Igual que shared/perfiles/registro.validation.js: quita espacios y un "+" inicial. */
fun limpiarTelefono(telefono: String): String = telefono.trim().replace(Regex("^\\+"), "")

private val NOMBRE          = Regex("^[\\p{L}\\p{M} '-]{1,100}$")
private val APELLIDOS       = Regex("^[\\p{L}\\p{M} '-]{1,150}$")
private val CALLE           = Regex("^[\\p{L}\\p{M}\\p{N} .,'/#-]{1,250}$")
private val CORREO          = Regex(
    "^(?=.{1,254}$)[^\\s@]+@(gmail\\.com|outlook\\.com|yahoo\\.com|hotmail\\.com|icloud\\.com)$",
    RegexOption.IGNORE_CASE,
)
private val TELEFONO        = Regex("^(\\d{10}|\\d{12})$")
private val NUMERO_EXTERIOR = Regex("^\\d{1,9}$")
private val NUMERO_INTERIOR = Regex("^\\s*\\d{0,9}\\s*$")
private val FECHA           = Regex("^\\d{4}-\\d{2}-\\d{2}$")

// En JS "." no cuenta \n, \r, \u2028 ni \u2029; la longitud se mide en unidades UTF-16.
private fun contrasenaValida(c: String) =
    c.length in 8..72 && c.none { it == '\n' || it == '\r' || it == '\u2028' || it == '\u2029' }

private fun diasDelMes(anio: Int, mes: Int): Int = when (mes) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11            -> 30
    else -> if ((anio % 4 == 0 && anio % 100 != 0) || anio % 400 == 0) 29 else 28
}

// Fecha de hoy en UTC, con nombre de campos legibles.
private data class FechaHoy(val anio: Int, val mes: Int, val dia: Int)

private fun hoyUtc(): FechaHoy {
    val c = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    return FechaHoy(
        anio = c.get(Calendar.YEAR),
        mes  = c.get(Calendar.MONTH) + 1,
        dia  = c.get(Calendar.DAY_OF_MONTH),
    )
}

/**
 * Réplica de validateRegistration (shared/perfiles/registro.validation.js).
 * Devuelve un mapa campo -> mensaje con las llaves del backend (vacío si todo es válido).
 */
fun validarRegistro(r: Registro, hoy: FechaHoy = hoyUtc()): Map<String, String> {
    val errores = linkedMapOf<String, String>()

    if (!NOMBRE.matches(r.nombre.trim()))
        errores["nombre"] = "Nombre inválido (1 a 100 letras)."
    if (!APELLIDOS.matches(r.apellidos.trim()))
        errores["apellidos"] = "Apellidos inválidos (1 a 150 letras)."
    if (!CORREO.matches(r.correo.trim()))
        errores["correo"] = "Usa un correo de Gmail, Outlook, Yahoo, Hotmail o iCloud (máx. 254 caracteres)."
    if (!TELEFONO.matches(limpiarTelefono(r.telefono)))
        errores["telefono"] = "El teléfono debe tener 10 o 12 dígitos."

    val fecha = r.fechaNacimiento.trim()
    if (fecha.isEmpty()) {
        errores["fecha_nacimiento"] = "Ingresa tu fecha de nacimiento."
    } else {
        val anio = fecha.take(4).toIntOrNull()
        val mes  = fecha.drop(5).take(2).toIntOrNull()
        val dia  = fecha.drop(8).take(2).toIntOrNull()
        val valida = FECHA.matches(fecha) && anio != null && mes != null && dia != null &&
            anio > 0 && mes in 1..12 && dia in 1..diasDelMes(anio, mes)

        if (!valida) {
            errores["fecha_nacimiento"] = "Fecha de nacimiento inválida (AAAA-MM-DD)."
        } else {
            val cumplePendiente = (mes!! * 100 + dia!!) > (hoy.mes * 100 + hoy.dia)
            val edad = hoy.anio - anio!! - if (cumplePendiente) 1 else 0
            if (edad < 18)
                errores["fecha_nacimiento"] = "No tienes la mayoría de edad."
        }
    }

    if (!CALLE.matches(r.calle.trim()))
        errores["calle"] = "Calle inválida (1 a 250 caracteres)."
    if (!NUMERO_EXTERIOR.matches(r.numeroExterior.trim()))
        errores["numero_exterior"] = "El número exterior debe tener de 1 a 9 dígitos."
    if (!NUMERO_INTERIOR.matches(r.numeroInterior))
        errores["numero_interior"] = "El número interior debe tener hasta 9 dígitos."
    if (r.idCodigoPostal.isBlank())
        errores["id_codigo_postal"] = "Selecciona tu colonia."
    if (!contrasenaValida(r.contrasena))
        errores["contrasena"] = "La contraseña debe tener de 8 a 72 caracteres."
    if (r.confirmContrasena.isEmpty() || r.confirmContrasena != r.contrasena)
        errores["confirmContrasena"] = "Las contraseñas no coinciden."
    if (!r.privacidad)
        errores["privacidad"] = "Acepta el aviso de privacidad."

    return errores
}