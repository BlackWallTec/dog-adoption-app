package com.app.android.data.dataSource.dataSourcesDonativos

import com.app.android.data.dtos.dtosDonativos.ApiResponseDatosFiscalesDto
import com.app.android.data.dtos.dtosDonativos.ApiResponseDonativosSimpleDto
import com.app.android.data.dtos.dtosDonativos.GuardarDatosFiscalesRequestDto
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DonativosApiService {
    // GET /api/donativos/cliente/datos-fiscales/:idUsuario
    @GET("api/donativos/cliente/datos-fiscales/{idUsuario}")
    suspend fun fetchDatosFiscalesRemote(
        @Path("idUsuario") idUsuario: String,
    ): ApiResponseDatosFiscalesDto

    // PUT /api/donativos/cliente/datos-fiscales/:idUsuario
    @PUT("api/donativos/cliente/datos-fiscales/{idUsuario}")
    suspend fun updateDatosFiscalesRemote(
        @Path("idUsuario") idUsuario: String,
        @Body body: GuardarDatosFiscalesRequestDto,
    ): ApiResponseDonativosSimpleDto

    // PUT /api/donativos/cliente/datos-fiscales/:idUsuario/constancia
    // el archivo va crudo, el Content-Type dice si es pdf, jpg o png
    @PUT("api/donativos/cliente/datos-fiscales/{idUsuario}/constancia")
    suspend fun uploadConstanciaRemote(
        @Path("idUsuario") idUsuario: String,
        @Body archivo: RequestBody,
    ): ApiResponseDonativosSimpleDto

    // POST /api/donativos/cliente/donativos/:idUsuario (HU46)
    // el comprobante va crudo, el Content-Type dice si es pdf, jpg o png
    @POST("api/donativos/cliente/donativos/{idUsuario}")
    suspend fun createDonativoRemote(
        @Path("idUsuario") idUsuario: String,
        @Query("monto") monto: String,
        @Query("referencia") referencia: String?,
        @Body comprobante: RequestBody,
    ): ApiResponseDonativosSimpleDto
}
