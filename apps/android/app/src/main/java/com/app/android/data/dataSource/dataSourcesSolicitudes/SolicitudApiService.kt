package com.app.android.data.dataSource.dataSourcesSolicitudes

import com.app.android.data.dtos.dtosSolicitudes.ApiResponseDatosGeneralesDto
import com.app.android.data.dtos.dtosSolicitudes.ApiResponseSolicitudActivaDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Peticiones al backend de Solicitudes.
// Antes de "Enviar solicitud" SOLO hay GET (lecturas).
interface SolicitudApiService {
    // GET /api/solicitudes/cliente/activa?idUsuario=...&idPerro=...
    @GET("api/solicitudes/cliente/activa")
    suspend fun fetchActiveRemote(
        @Query("idUsuario") idUsuario: String,
        @Query("idPerro") idPerro: String,
    ): ApiResponseSolicitudActivaDto

    // GET /api/solicitudes/cliente/perfil/:idUsuario
    @GET("api/solicitudes/cliente/perfil/{idUsuario}")
    suspend fun fetchProfileRemote(
        @Path("idUsuario") idUsuario: String,
    ): ApiResponseDatosGeneralesDto

    // Andy: fetchUserDocumentsRemote (GET /documentos/:idUsuario)
    // Lucca: enviarSolicitudRemote (POST multipart)
}