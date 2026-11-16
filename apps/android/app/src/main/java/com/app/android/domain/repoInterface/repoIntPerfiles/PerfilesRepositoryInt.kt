package com.app.android.domain.repoInterface.repoIntPerfiles

import com.app.android.domain.entities.entitiesPerfiles.CodigoPostal
import com.app.android.domain.entities.entitiesPerfiles.Perfil
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Usuario

// Todos los errores se lanzan como ErrorApi
interface PerfilesRepositoryInt {
    // POST /login. Guarda el token cifrado y devuelve la sesión. */
    suspend fun login(correo: String, contrasena: String): Sesion

    // POST /register (crea cuenta Cliente). No inicia sesión. */
    suspend fun register(registro: Registro): Usuario

    // GET /profile (usa el token guardado). */
    suspend fun profile(): Perfil

    // GET /me (usa el token guardado).
    suspend fun me(): Usuario

    // GET /codigos-postales?cp=
    suspend fun codigosPostales(cp: String): List<CodigoPostal>

    // Borra el token guardado. El backend no guarda estado de sesión móvil
    suspend fun logout()

    // Lee token guardado y llama me(), null si no hay token o venció (y entonces lo borraría).
    suspend fun sesionActual(): Sesion?
}