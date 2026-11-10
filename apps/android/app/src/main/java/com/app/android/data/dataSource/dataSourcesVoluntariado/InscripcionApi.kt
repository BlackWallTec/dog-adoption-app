package com.app.android.data.dataSource.dataSourcesVoluntariado

import com.app.android.data.dtos.dtosVoluntariado.InscripcionDto
import com.app.android.data.dtos.dtosVoluntariado.RespuestaApiDto
import retrofit2.http.POST
import retrofit2.http.Path

interface InscripcionApi {

    @POST("api/voluntarios/programas/{idPrograma}/inscripciones")

    suspend fun inscribirse(
        @Path("idPrograma") idPrograma: String,
    ): RespuestaApiDto<InscripcionDto>
}