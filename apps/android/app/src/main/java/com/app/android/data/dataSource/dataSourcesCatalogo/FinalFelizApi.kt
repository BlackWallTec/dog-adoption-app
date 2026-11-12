package com.app.android.data.dataSource.dataSourcesCatalogo

import com.app.android.data.dtos.dtosCatalogo.FinalesFelicesResponseDto
import retrofit2.http.GET

interface FinalFelizApi {
    @GET("api/catalogo/cliente/finales-felices")
    suspend fun getFinalesFelices(): FinalesFelicesResponseDto
}
