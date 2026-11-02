package com.app.android.domain.entities.entitiesPerfiles

// Para el arreglo de usuario
data class Usuario(
    val id: String,
    val email: String,
    val rol: String
)

// Para el token
data class Sesion(
    val usuario: Usuario,
    val token: String
)

// ESTO ÚLTIMO ME LO RECOMENDÓ LA IA
class CredencialesInvalidasException : Exception("Credenciales inválidas")