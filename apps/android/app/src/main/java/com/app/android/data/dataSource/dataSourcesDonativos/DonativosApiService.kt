package com.app.android.data.dataSource.dataSourcesDonativos

import com.app.android.data.dtos.dtosDonativos.ApiResponseDatosFiscalesDto
import com.app.android.data.dtos.dtosDonativos.ApiResponseDonativosSimpleDto
import com.app.android.data.dtos.dtosDonativos.ApiResponseMisDonativosDto
import com.app.android.data.dtos.dtosDonativos.GuardarDatosFiscalesRequestDto
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DonativosApiService {
    // GET /api/donativos/cliente/datos-fiscales (el usuario sale del token)
    @GET("api/donativos/cliente/datos-fiscales")
    suspend fun fetchDatosFiscalesRemote(): ApiResponseDatosFiscalesDto

    // PUT /api/donativos/cliente/datos-fiscales
    @PUT("api/donativos/cliente/datos-fiscales")
    suspend fun updateDatosFiscalesRemote(
        @Body body: GuardarDatosFiscalesRequestDto,
    ): ApiResponseDonativosSimpleDto

    // PUT /api/donativos/cliente/datos-fiscales/constancia
    // el archivo va crudo, el Content-Type dice si es pdf, jpg o png
    @PUT("api/donativos/cliente/datos-fiscales/constancia")
    suspend fun uploadConstanciaRemote(
        @Body archivo: RequestBody,
    ): ApiResponseDonativosSimpleDto

    // POST /api/donativos/cliente/donativos (HU46, el usuario sale del token)
    // el comprobante va crudo, el Content-Type dice si es pdf, jpg o png
    @POST("api/donativos/cliente/donativos")
    suspend fun createDonativoRemote(
        @Query("monto") monto: String,
        @Query("referencia") referencia: String?,
        @Body comprobante: RequestBody,
    ): ApiResponseDonativosSimpleDto

    // GET /api/donativos/cliente/mis-donativos (HU49, el usuario sale del token)
    @GET("api/donativos/cliente/mis-donativos")
    suspend fun fetchMisDonativosRemote(): ApiResponseMisDonativosDto

    // POST /api/donativos/cliente/mis-donativos/:idDonativo/cfdi (HU49)
    @POST("api/donativos/cliente/mis-donativos/{idDonativo}/cfdi")
    suspend fun createSolicitudCfdiRemote(
        @Path("idDonativo") idDonativo: String,
    ): ApiResponseDonativosSimpleDto
}
