package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.Solicitud
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import kotlinx.coroutines.delay
import java.util.UUID

// Repositorio Fake: cumple el contrato pero lo guarda todo en memorio (ahorita no bd)
// se borra todo al cerrar la app aguass

class SolicitudRepositoryFake : SolicitudRepositoryInt {
    // "Tablas" en memoria
    private val solicitudes = mutableListOf<Solicitud>()
    // este mutable map of es como un diccionario (le pasas el string aqui (id) y te da datos)
    private val datosGeneralesPorSolicitud = mutableMapOf<String, DatosGenerales>()

    // Perfil inventado (lo que regresaría la tabla usuario)
    private val perfilFake =
        DatosGenerales(
            nombreCompleto = "Sofía García López",
            fechaNacimiento = "1995-03-15",
            ocupacion = "", // no está en el perfil: (probar a ver si corre bien)
            domicilio = "Calle Roble 24, Col. Álamos, Querétaro, Qro. · C.P. 76160",
            municipio = "Querétaro",
            telefono = "442 123 4567",
            correo = "sofia.garcia@correo.com",
        )

    override suspend fun buscarBorrador(
        idUsuario: String,
        idPerro: String,
    ): Solicitud? {
        simularRed()
        return solicitudes.find {
            it.idUsuario == idUsuario &&
                    it.idPerro == idPerro &&
                    it.estado == EstadoSolicitud.BORRADOR
        }
    }

    override suspend fun crearSolicitud(
        idUsuario: String,
        idPerro: String,
    ): Solicitud {
        simularRed()
        val nueva =
            Solicitud(
                id = UUID.randomUUID().toString(), // un uuid como los de Supabase
                idUsuario = idUsuario,
                idPerro = idPerro,
                estado = EstadoSolicitud.BORRADOR,
                aceptoRequisitos = true,
            )
        solicitudes.add(nueva)
        return nueva
    }

    override suspend fun getPerfil(idUsuario: String): DatosGenerales {
        simularRed()
        return perfilFake
    }

    override suspend fun getDatosGenerales(idSolicitud: String): DatosGenerales? {
        simularRed()
        return datosGeneralesPorSolicitud[idSolicitud] // null si no hay
    }

    override suspend fun guardarDatosGenerales(
        idSolicitud: String,
        datos: DatosGenerales,
    ) {
        simularRed()
        datosGeneralesPorSolicitud[idSolicitud] = datos // guarda o reemplaza
    }

    // Espera medio segundo para que se vea el "cargando" como si fuera internet
    private suspend fun simularRed() = delay(500)
}