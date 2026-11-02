package com.app.android.data.repoImp.repoImpVoluntariado

import com.app.android.data.dataSource.dataSourcesVoluntariado.InscripcionApi
import com.app.android.data.dataSource.dataSourcesVoluntariado.safeApiCall
import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.repoInterface.repoIntVoluntariado.InscripcionRepository
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InscripcionRepositoryImpl @Inject constructor(
    private val api: InscripcionApi,
    private val gson: Gson,
) : InscripcionRepository {
    override suspend fun inscribirse(idPrograma: String): Resultado<Inscripcion> =
        safeApiCall(gson) {
            val respuesta = api.inscribirse(idPrograma)
            checkNotNull(respuesta.data) { "La respuesta no trae data" }.toDomain()
        }
}