package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.data.dataSource.dataSourcesSolicitudes.BorradorLocalDataSource
import com.app.android.data.dataSource.dataSourcesSolicitudes.SolicitudApiService
import com.app.android.data.dtos.dtosSolicitudes.CrearSolicitudRequestDto
import com.app.android.data.dtos.dtosSolicitudes.GuardarDatosGeneralesRequestDto
import com.app.android.data.dtos.dtosSolicitudes.toDomain
import com.app.android.data.dtos.dtosSolicitudes.toRequestDto
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.Solicitud
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

// Repositorio de verdad, cumple el mismo contrato que el fake, pero con el backend de verdad
class SolicitudRepositoryImpl
    @Inject
    constructor(
        private val api: SolicitudApiService,
        private val borrador: BorradorLocalDataSource,
    ) : SolicitudRepositoryInt {
        override suspend fun buscarBorrador(
            idUsuario: String,
            idPerro: String,
        ): Solicitud? {
            val respuesta = api.fetchDraftRemote(idUsuario, idPerro)
            return respuesta.data?.toDomain() // data null = no hay borradorr
        }

        override suspend fun crearSolicitud(
            idUsuario: String,
            idPerro: String,
        ): Solicitud {
            val respuesta = api.createRequestRemote(CrearSolicitudRequestDto(idUsuario, idPerro))
            return respuesta.data?.toDomain()
                ?: throw Exception(respuesta.message ?: "El servidor no regresó la solicitud creada")
        }

        override suspend fun getPerfil(idUsuario: String): DatosGenerales {
            val respuesta = api.fetchProfileRemote(idUsuario)
            return respuesta.data?.toDomain()
                ?: throw Exception(respuesta.message ?: "No se encontró el perfil")
        }

        override suspend fun getDatosGenerales(idSolicitud: String): DatosGenerales? {
            val respuesta = api.fetchGeneralDataRemote(idSolicitud)
            return respuesta.data?.toDomain()
        }

        override suspend fun guardarDatosGenerales(
            idSolicitud: String,
            datos: DatosGenerales,
        ) {
            // Csolo se manda la ocupación (lo demas es preloaded)
            val respuesta =
                api.updateGeneralDataRemote(
                    idSolicitud,
                    GuardarDatosGeneralesRequestDto(ocupacion = datos.ocupacion),
                )
            if (respuesta.success != true) {
                throw Exception(respuesta.message ?: "No se pudieron guardar los datos")
            }
        }

        // Paso 3
        // Llamada al borrador local para recibir la información
        override suspend fun getTuHogar(idSolicitud: String): TuHogar? = borrador.getTuHogar(idSolicitud)

        override suspend fun guardarTuHogar(
            idSolicitud: String,
            datos: TuHogar,
        ) {
            borrador.guardarTuHogar(idSolicitud, datos)
        }

        // Paso 4
        override suspend fun getCompromiso(idSolicitud: String): Compromiso? = borrador.getCompromiso(idSolicitud)

        override suspend fun guardarCompromiso(
            idSolicitud: String,
            datos: Compromiso,
        ) {
            borrador.guardarCompromiso(idSolicitud, datos)
        }
    }
