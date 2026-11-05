package com.app.android.domain.repoInterface.repoIntSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.Solicitud
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar

// CONTRATO: que se puede hacer con las solicitudes, sin decir como
// Lo implementa SolicitudRepositoryFake (ahorita que no esta la BD) o SolicitudRepositoryImpl (cuando este el  backend).

interface SolicitudRepositoryInt {
    // Busca si el usuario ya tiene una solicitud en borrador para ese perro si no = null
    suspend fun buscarBorrador(
        idUsuario: String,
        idPerro: String,
    ): Solicitud?

    suspend fun getSolicitudActiva(
        idUsuario: String,
        idPerro: String,
    ): SolicitudResumen?

    // Crea una solicitud nueva (estado BORRADOR)
    suspend fun crearSolicitud(
        idUsuario: String,
        idPerro: String,
    ): Solicitud

    // Consigue datos del perfil del usuario (para precargar la pantalla con los datos)
    suspend fun getPerfil(idUsuario: String): DatosGenerales

    // Datos generales ya guardados en esa solicitud.
    // null = todavía no ha guardado este paso
    suspend fun getDatosGenerales(idSolicitud: String): DatosGenerales?

    // Guarda los datos generales de esa solicitud
    suspend fun guardarDatosGenerales(
        idSolicitud: String,
        datos: DatosGenerales,
    )

    // Paso 3: TuHogar - Respuestas
    // null = todavía no ha guardado este paso
    suspend fun getTuHogar(idSolicitud: String): TuHogar?

    // Guarda las respuestas de tu hogar
    suspend fun guardarTuHogar(
        idSolicitud: String,
        datos: TuHogar,
    )

    // Respuestas del paso 4: Compromiso
    // null = todavía no ha guardado este paso
    suspend fun getCompromiso(idSolicitud: String): Compromiso?

    // Guarda las respuestas del paso 4: Compromiso
    suspend fun guardarCompromiso(
        idSolicitud: String,
        datos: Compromiso,
    )

    suspend fun enviarSolicitud(borrador: BorradorSolicitud): SolicitudResumen
}
