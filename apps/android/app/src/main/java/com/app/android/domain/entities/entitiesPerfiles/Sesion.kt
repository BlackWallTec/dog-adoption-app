package com.app.android.domain.entities.entitiesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Usuario

// Para el token
data class Sesion(
    val usuario: Usuario,
    val token: String
)