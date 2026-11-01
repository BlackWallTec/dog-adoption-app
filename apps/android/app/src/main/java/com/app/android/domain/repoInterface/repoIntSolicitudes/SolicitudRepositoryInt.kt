package com.app.android.domain.repoInterface.repoIntSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.Solicitud

// CONTRATO: que se puede hacer con las solicitudes, sin decir como
// Lo implementa SolicitudRepositoryFake (ahorita que no esta la BD) o SolicitudRepositoryImpl (cuando este el  backend).

interface SolicitudRepositoryInt{
    // Busca si el usuario ya tiene una solicitud en borrador para ese perro si no = null
    suspend fun buscarBorrador(
        idUsuario: String,
        idPerro: String,
    ): Solicitud?

    // Crea una solicitud nueva (estado BORRADOR)
    suspend fun crearSolicitud(
        idUsuario: String,
        idPerro: String,
    ): Solicitud

    //Consigue datos del perfil del usuario (para precargar la pantalla con los datos)
    suspend fun getPerfil(idUsuario: String): DatosGenerales

    // Datos generales ya guardados en esa solicitud.
    // null = todavía no ha guardado este paso
    suspend fun getDatosGenerales(idSolicitud: String): DatosGenerales?

    // Guarda los datos generales de esa solicitud
    suspend fun guardarDatosGenerales(
        idSolicitud: String,
        datos: DatosGenerales,
    )
}
