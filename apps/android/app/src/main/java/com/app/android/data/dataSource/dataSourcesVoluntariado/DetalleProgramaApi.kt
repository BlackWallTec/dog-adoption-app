package com.app.android.data.dataSource.dataSourcesVoluntariado

import com.app.android.data.dtos.dtosVoluntariado.DetalleProgramaResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

// HU36 detalle publico de un programa y no requiere sesion
interface DetalleProgramaApi {
    @GET("api/voluntarios/cliente/programas/{idPrograma}")
    suspend fun obtenerDetalle(
        @Path("idPrograma") idPrograma: String,
    ): DetalleProgramaResponseDto
}
