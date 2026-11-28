package com.app.android.domain.repoInterface.repoIntSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudParaEditar
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.entities.entitiesSolicitudes.UploadedDocument

interface SolicitudRepositoryInt {
    suspend fun getSolicitudActiva(idPerro: String): SolicitudResumen?

    // Datos del perfil, para precargar Datos generales
    // Las solicitudes ya enviadas del cliente en sesión, de la más reciente a la más antigua.
    // No recibe idUsuario: la identidad sale de la sesión, así nadie puede pedir las de otro.
    suspend fun getMisSolicitudes(): List<SolicitudCliente>

    suspend fun getPerfil(): DatosGenerales

    suspend fun crearOReutilizarBorrador(borrador: BorradorSolicitud): String

    suspend fun subirDocumento(
        solicitudId: String,
        type: DocumentType,
        file: PickedFile,
        bytes: ByteArray,
    ): UploadedDocument

    suspend fun finalizarSolicitud(solicitudId: String)

    // HU09

    suspend fun getSolicitudParaEditar(idSolicitud: String): SolicitudParaEditar

    suspend fun reenviarSolicitud(
        idSolicitud: String,
        borrador: BorradorSolicitud,
    )
}

// Error que avisa que la solicitud ya no se puede editar
// viene de la Impl.
class NoEditableException(
    override val message: String,
) : Exception(message)
