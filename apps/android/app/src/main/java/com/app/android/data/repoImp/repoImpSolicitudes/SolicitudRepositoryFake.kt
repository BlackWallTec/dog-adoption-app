package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
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

    private suspend fun simularRed() = delay(500)
}