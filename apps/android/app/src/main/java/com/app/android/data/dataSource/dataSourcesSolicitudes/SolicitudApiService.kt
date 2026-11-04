package com.app.android.data.dataSource.dataSourcesSolicitudes

import com.app.android.data.dtos.dtosSolicitudes.ApiResponseCompromisoDto
import com.app.android.data.dtos.dtosSolicitudes.ApiResponseDatosGeneralesDto
import com.app.android.data.dtos.dtosSolicitudes.ApiResponseSimpleDto
import com.app.android.data.dtos.dtosSolicitudes.ApiResponseSolicitudDto
import com.app.android.data.dtos.dtosSolicitudes.ApiResponseTuHogarDto
import com.app.android.data.dtos.dtosSolicitudes.CrearSolicitudRequestDto
import com.app.android.data.dtos.dtosSolicitudes.GuardarCompromisoRequestDto
import com.app.android.data.dtos.dtosSolicitudes.GuardarDatosGeneralesRequestDto
import com.app.android.data.dtos.dtosSolicitudes.GuardarTuHogarRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SolicitudApiService {
    // GET /api/solicitudes/cliente/borrador?idUsuario=...&idPerro=...
    @GET("api/solicitudes/cliente/borrador")
    suspend fun fetchDraftRemote(
        @Query("idUsuario") idUsuario: String, // se agrega como ?idUsuario=...
        @Query("idPerro") idPerro: String, // se agrega como &idPerro=...
    ): ApiResponseSolicitudDto

    // POST /api/solicitudes/cliente
    @POST("api/solicitudes/cliente")
    suspend fun createRequestRemote(
        @Body body: CrearSolicitudRequestDto, // se manda como JSON en el body
    ): ApiResponseSolicitudDto

    // GET /api/solicitudes/cliente/perfil/:idUsuario
    @GET("api/solicitudes/cliente/perfil/{idUsuario}")
    suspend fun fetchProfileRemote(
        @Path("idUsuario") idUsuario: String,
    ): ApiResponseDatosGeneralesDto

    // GET /api/solicitudes/cliente/:idSolicitud/datos-generales
    @GET("api/solicitudes/cliente/{idSolicitud}/datos-generales")
    suspend fun fetchGeneralDataRemote(
        @Path("idSolicitud") idSolicitud: String,
    ): ApiResponseDatosGeneralesDto

    // PUT /api/solicitudes/cliente/:idSolicitud/datos-generales
    @PUT("api/solicitudes/cliente/{idSolicitud}/datos-generales")
    suspend fun updateGeneralDataRemote(
        @Path("idSolicitud") idSolicitud: String,
        @Body body: GuardarDatosGeneralesRequestDto,
    ): ApiResponseSimpleDto

    // GET /api/solicitudes/cliente/:idSolicitud/tu-hogar
    @GET("api/solicitudes/cliente/{idSolicitud}/tu-hogar")
    suspend fun fetchHomeRemote(
        @Path("idSolicitud") idSolicitud: String,
    ): ApiResponseTuHogarDto

    // PUT /api/solicitudes/cliente/:idSolicitud/tu-hogar
    @PUT("api/solicitudes/cliente/{idSolicitud}/tu-hogar")
    suspend fun updateHomeRemote(
        @Path("idSolicitud") idSolicitud: String,
        @Body body: GuardarTuHogarRequestDto,
    ): ApiResponseSimpleDto

    // GET /api/solicitudes/cliente/:idSolicitud/compromiso
    @GET("api/solicitudes/cliente/{idSolicitud}/compromiso")
    suspend fun fetchCommitmentRemote(
        @Path("idSolicitud") idSolicitud: String,
    ): ApiResponseCompromisoDto

    // PUT /api/solicitudes/cliente/:idSolicitud/compromiso
    @PUT("api/solicitudes/cliente/{idSolicitud}/compromiso")
    suspend fun updateCommitmentRemote(
        @Path("idSolicitud") idSolicitud: String,
        @Body body: GuardarCompromisoRequestDto,
    ): ApiResponseSimpleDto
}
