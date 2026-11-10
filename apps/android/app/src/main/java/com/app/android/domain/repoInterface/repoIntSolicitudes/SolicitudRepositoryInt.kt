package com.app.android.domain.repoInterface.repoIntSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen

// CONTRATO: lo que se le pide al servidor sobre solicitudes.
// Antes de "Enviar solicitud" solo hay readOnlys; nada escribe en la BD.
// (El borrador en memoria es otro contrato: BorradorRepositoryInt)
interface SolicitudRepositoryInt {
    // ¿Ya tiene este usuario una solicitud activa (no rechazada) para este perro?
    // null = no tiene, puede empezar
    suspend fun getSolicitudActiva(
        idUsuario: String,
        idPerro: String,
    ): SolicitudResumen?

    // Datos del perfil, para precargar Datos generales
    suspend fun getPerfil(idUsuario: String): DatosGenerales

    // Andy: suspend fun getDocumentosUsuario(idUsuario: String): List<DocumentoSubido>
    // Lucca: suspend fun enviarSolicitud(borrador: BorradorSolicitud): SolicitudResumen
}
