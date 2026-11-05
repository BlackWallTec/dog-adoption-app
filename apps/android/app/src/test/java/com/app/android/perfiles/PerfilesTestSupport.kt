package com.app.android.perfiles

import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.*
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.AlmacenSesion

internal class AlmacenSesionMemoria : AlmacenSesion {
    private var value: Sesion? = null
    override val actual get() = value
    override fun cargar() = value
    override fun guardar(sesion: Sesion) { value = sesion }
    override fun limpiar() { value = null }
}

internal open class ApiPerfilesStub : UsuarioAPIservice {
    override suspend fun login(body: LoginRequestDTO): LoginResponseDTO = error("No configurado en esta prueba")
    override suspend fun registrar(body: RegistroRequestDTO): LoginResponseDTO = error("No configurado en esta prueba")
    override suspend fun sesion(): SesionResponseDTO = error("No configurado en esta prueba")
    override suspend fun perfil(): PerfilResponseDTO = error("No configurado en esta prueba")
    override suspend fun logout() = LogoutResponseDTO(true)
}

internal val registroValido = Registro(
    nombre = "Ana", apellidos = "Prueba", email = "perfiles.prueba@gmail.com", telefono = "5512345678",
    fechaNacimiento = "2000-06-15", calle = "Calle de prueba", numeroExterior = "0", codigoPostal = "00000",
    passwd = "clave-prueba-123", confirmPasswd = "clave-prueba-123", privacidad = true,
)
