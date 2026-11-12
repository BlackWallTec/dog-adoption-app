package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import kotlinx.coroutines.delay

// REPOSITORIO FAKE: mismo contrato, datos inventados en memoria.
// Para tests y para trabajar sin backend.
class SolicitudRepositoryFake : SolicitudRepositoryInt {
    // Solicitudes activas inventadas. Llave = "idUsuario|idPerro"
    private val activas = mutableMapOf<String, SolicitudResumen>()

    private val perfilFake =
        DatosGenerales(
            nombreCompleto = "Sofía García López",
            fechaNacimiento = "1995-03-15",
            ocupacion = "", // no está en el perfil: el usuario la escribe
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
                documentoRechazado = false,
                comentarioAdmin = null,
            ),
            SolicitudCliente(
                idSolicitud = "2",
                folio = "QA-00000002",
                fechaSolicitud = "2026-10-10T16:00:00Z",
                estado = EstadoSolicitud.PENDIENTE_REVISION,
                nombrePerro = "Luna",
                fotoUrl = null,
                documentoRechazado = true,
                comentarioAdmin = "La foto de tu INE sale borrosa, súbela de nuevo por favor.",
            ),
            SolicitudCliente(
                idSolicitud = "3",
                folio = "QA-00000003",
                fechaSolicitud = "2026-09-28T12:15:00Z",
                estado = EstadoSolicitud.APROBADA,
                nombrePerro = null, // el perro ya no está en el catálogo
                fotoUrl = null,
                documentoRechazado = false,
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

    override suspend fun getSolicitudActiva(
        idUsuario: String,
        idPerro: String,
    ): SolicitudResumen? {
        simularRed()
        return activas["$idUsuario|$idPerro"]
    }

    override suspend fun getPerfil(idUsuario: String): DatosGenerales {
        simularRed()
        return perfilFake
    }

    override suspend fun getMisSolicitudes(): List<SolicitudCliente> {
        simularRed()
        return misSolicitudesFake
    }

    private suspend fun simularRed() = delay(500)
}
