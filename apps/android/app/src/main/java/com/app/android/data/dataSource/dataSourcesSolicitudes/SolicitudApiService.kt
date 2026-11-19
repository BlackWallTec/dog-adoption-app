package com.app.android.data.dataSource.dataSourcesSolicitudes

import com.app.android.data.dtos.dtosSolicitudes.ApiResponseDatosGeneralesDto
import com.app.android.data.dtos.dtosSolicitudes.ApiResponseMisSolicitudesDto
import com.app.android.data.dtos.dtosSolicitudes.ApiResponseSolicitudActivaDto
import com.app.android.data.dtos.dtosSolicitudes.ApiResponseSimpleDto
import com.app.android.data.dtos.dtosSolicitudes.CrearSolicitudRequestDto
import com.app.android.data.dtos.dtosSolicitudes.CrearSolicitudResponseDto
import com.app.android.data.dtos.dtosSolicitudes.DocumentoUploadResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface SolicitudApiService {
    @GET("api/solicitudes/cliente/activa")
    suspend fun fetchActiveRemote(
        @Query("idPerro") idPerro: String,
    ): ApiResponseSolicitudActivaDto

    @GET("api/solicitudes/cliente/perfil")
    suspend fun fetchProfileRemote(): ApiResponseDatosGeneralesDto
    
    // GET /api/solicitudes/cliente/mis-solicitudes
    @GET("api/solicitudes/cliente/mis-solicitudes")
    suspend fun fetchMisSolicitudesRemote(): ApiResponseMisSolicitudesDto

    @POST("api/solicitudes")
    suspend fun crearOReutilizarBorrador(
        @Body request: CrearSolicitudRequestDto,
    ): CrearSolicitudResponseDto

    @Multipart
    @POST("api/solicitudes/{id}/documentos")
    suspend fun uploadDocument(
        @Path("id") solicitudId: String,
        @Part("tipo") tipo: RequestBody,
        @Part archivo: MultipartBody.Part,
    ): DocumentoUploadResponseDto

    @POST("api/solicitudes/{id}/enviar")
    suspend fun finalizarSolicitud(
        @Path("id") solicitudId: String,
    ): ApiResponseSimpleDto
}
