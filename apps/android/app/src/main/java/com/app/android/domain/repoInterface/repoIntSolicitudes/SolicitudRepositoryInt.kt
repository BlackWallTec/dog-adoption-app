package com.app.android.domain.repoInterface.repoIntSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
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

    // Las solicitudes ya enviadas del cliente en sesión, de la más reciente a la más antigua.
    // No recibe idUsuario: la identidad sale de la sesión, así nadie puede pedir las de otro.
    suspend fun getMisSolicitudes(): List<SolicitudCliente>

    // Andy: suspend fun getDocumentosUsuario(idUsuario: String): List<DocumentoSubido>
    // Lucca: suspend fun enviarSolicitud(borrador: BorradorSolicitud): SolicitudResumen
}
