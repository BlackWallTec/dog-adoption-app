package com.app.android.data.dataSource.dataSourcesSolicitudes

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface DocumentApi {

    @Multipart
    @POST("api/solicitudes/{id}/documentos")
    suspend fun uploadDocument(
        @Path("id") solicitudId: String,
        @Part("tipo") tipo: RequestBody,
        @Part archivo: MultipartBody.Part,
    ): DocumentoDto
}