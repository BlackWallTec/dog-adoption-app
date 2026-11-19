package com.app.android.data.dataSource.dataSourcesVoluntariado

import com.app.android.data.dtos.dtosVoluntariado.ProgramasResponseDto
import retrofit2.http.GET

interface ProgramaApiService {

    // El filtro de programas vencidos lo aplica el backend
    @GET("api/voluntarios/cliente/programas")
    suspend fun obtenerProgramas(): ProgramasResponseDto

    // Solo aquellos en los que el usuario está inscrito
    @GET("api/voluntarios/cliente/mis-programas")
    suspend fun obtenerMisProgramas(): ProgramasResponseDto
}