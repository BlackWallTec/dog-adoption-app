package com.app.android.data.repoImp.repoImpVoluntariado

import com.app.android.data.dataSource.dataSourcesVoluntariado.DetalleProgramaApi
import com.app.android.data.dataSource.dataSourcesVoluntariado.safeApiCall
import com.app.android.domain.entities.entitiesVoluntariado.DetallePrograma
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.repoInterface.repoIntVoluntariado.DetalleProgramaRepository
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

// HU36 safeApiCall convierte el 404 PROGRAMA_NO_ENCONTRADO, la falta de red y los errores en Resultado.Error
@Singleton
class DetalleProgramaRepositoryImpl
    @Inject
    constructor(
        private val api: DetalleProgramaApi,
        private val gson: Gson,
    ) : DetalleProgramaRepository {
        override suspend fun obtenerDetalle(idPrograma: String): Resultado<DetallePrograma> =
            safeApiCall(gson) {
                val respuesta = api.obtenerDetalle(idPrograma)
                checkNotNull(respuesta.data) { "La respuesta no trae data" }.toDomain()
            }
    }
