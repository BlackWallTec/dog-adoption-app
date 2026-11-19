package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.data.dataSource.dataSourcesSolicitudes.SolicitudApiService
import com.app.android.data.dtos.dtosSolicitudes.toDomain
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

// REPOSITORIO REAL: cumple el contrato usando el backend
class SolicitudRepositoryImpl
    @Inject
    constructor(
        private val api: SolicitudApiService,
    ) : SolicitudRepositoryInt {
        override suspend fun getSolicitudActiva(
            idUsuario: String,
            idPerro: String,
        ): SolicitudResumen? {
            val respuesta = api.fetchActiveRemote(idUsuario, idPerro)
            return respuesta.data?.toDomain() // data null = no tiene solicitud activa
        }

        override suspend fun getPerfil(idUsuario: String): DatosGenerales {
            val respuesta = api.fetchProfileRemote(idUsuario)
            // si no hay perfil
            return respuesta.data?.toDomain()
                ?: throw Exception(respuesta.message ?: "No se encontró el perfil")
        }

        override suspend fun getMisSolicitudes(): List<SolicitudCliente> {
            val respuesta = api.fetchMisSolicitudesRemote()
            // si esta vacía
            return respuesta.data?.map { it.toDomain() }
                ?: throw Exception(respuesta.message ?: "No se encontraron solicitudes")
        }
    }
