package com.app.android.data.dataSource.dataSourcesVoluntariado

import retrofit2.http.POST
import retrofit2.http.Path

interface InscripcionApi {
    @POST("voluntarios/programas/{idPrograma}/inscripciones")
    suspend fun inscribirse(
        @Path("idPrograma") idPrograma: String,
    ): RespuestaApiDto<InscripcionDto>
}