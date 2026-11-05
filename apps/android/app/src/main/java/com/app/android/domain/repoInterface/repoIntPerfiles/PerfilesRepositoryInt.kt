package com.app.android.domain.repoInterface.repoIntPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.app.android.domain.entities.entitiesPerfiles.Perfil

// LOGIN - Aquí empieza el recorrido, con el contrato de DOMINIO de qué puede pedir la aplicación; en este caso devuelve una sesión (entidad, para persistencia en el dispositivo)

interface PerfilesRepositoryInt {
    // Sólo contrato
    suspend fun login(email: String, passwd: String): Sesion
    suspend fun registrar(datos: Registro): Sesion
    suspend fun restaurarSesion(): Sesion?
    suspend fun consultarPerfil(): Perfil
    suspend fun cerrarSesion(notificarServidor: Boolean = true)
}
