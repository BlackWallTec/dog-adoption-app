package com.app.android.domain.repoInterface.repoIntPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Sesion

interface AlmacenSesion {
    val actual: Sesion?
    fun cargar(): Sesion?
    fun guardar(sesion: Sesion)
    fun limpiar()
}
