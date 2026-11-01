package com.app.android.domain.entities.entitiesPerfil

data class Usuario(
    val id_usuario: String,
    val rol: String,
    val nombre: String,
    val apellidos: String,
    val contrasena: String,
    val fecha_nacimiento: String,
    val fecha_registro: String,
)
