package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudParaEditar
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.entities.entitiesSolicitudes.UploadedDocument
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import kotlinx.coroutines.delay

class SolicitudRepositoryFake : SolicitudRepositoryInt {
    private val activas = mutableMapOf<String, SolicitudResumen>()

    private val perfilFake =
        DatosGenerales(
            nombreCompleto = "Sofía García López",
            fechaNacimiento = "1995-03-15",
            ocupacion = "",
            domicilio = "Calle Roble 24, Col. Álamos, Querétaro, Qro. · C.P. 76160",
            municipio = "Querétaro",
            telefono = "442 123 4567",
            correo = "sofia.garcia@correo.com",
        )

    // Solicitudes inventadas para trabajar sin backend (incluye un perro que ya no está)
    private val misSolicitudesFake =
        listOf(
            SolicitudCliente(
                idSolicitud = "1",
                folio = "QA-00000001",
                fechaSolicitud = "2026-10-12T18:30:00Z",
                estado = EstadoSolicitud.PENDIENTE_REVISION,
                nombrePerro = "Bruno",
                fotoUrl = null,
                edicionHabilitada = false,
                comentarioAdmin = null,
            ),
            SolicitudCliente(
                idSolicitud = "2",
                folio = "QA-00000002",
                fechaSolicitud = "2026-10-10T16:00:00Z",
                estado = EstadoSolicitud.RECHAZADA,
                nombrePerro = "Luna",
                fotoUrl = null,
                edicionHabilitada = true,
                comentarioAdmin = "La foto de tu INE sale borrosa, súbela de nuevo por favor.",
            ),
            SolicitudCliente(
                idSolicitud = "3",
                folio = "QA-00000003",
                fechaSolicitud = "2026-09-28T12:15:00Z",
                estado = EstadoSolicitud.APROBADA,
                nombrePerro = null, // el perro ya no está en el catálogo
                fotoUrl = null,
                edicionHabilitada = false,
                comentarioAdmin = null,
            ),
        )

    // Para tests: simula que ese usuario ya tiene una solicitud para ese perro
    fun agregarActiva(
        idUsuario: String,
        idPerro: String,
        solicitud: SolicitudResumen,
    ) {
        activas["$idUsuario|$idPerro"] = solicitud
    }

    override suspend fun getSolicitudActiva(idPerro: String): SolicitudResumen? {
        simularRed()
        return activas.entries.firstOrNull { it.key.endsWith("|$idPerro") }?.value
    }

    override suspend fun getPerfil(): DatosGenerales {
        simularRed()
        return perfilFake
    }

    override suspend fun getMisSolicitudes(): List<SolicitudCliente> {
        simularRed()
        return misSolicitudesFake
    }

    override suspend fun crearOReutilizarBorrador(borrador: BorradorSolicitud): String {
        simularRed()
        return "sol-fake-123"
    }

    override suspend fun subirDocumento(
        solicitudId: String,
        type: DocumentType,
        file: PickedFile,
        bytes: ByteArray,
    ): UploadedDocument {
        simularRed()
        return UploadedDocument(type = type, fileName = file.name, sizeBytes = file.sizeBytes)
    }

    override suspend fun finalizarSolicitud(solicitudId: String) {
        simularRed()
    }

    private suspend fun simularRed() = delay(500)

    override suspend fun getSolicitudParaEditar(idSolicitud: String): SolicitudParaEditar =
        throw NotImplementedError("El Fake no simula la edición todavía")

    override suspend fun reenviarSolicitud(
        idSolicitud: String,
        borrador: BorradorSolicitud,
    ): Unit = throw NotImplementedError("El Fake no simula el reenvío todavía")
}
