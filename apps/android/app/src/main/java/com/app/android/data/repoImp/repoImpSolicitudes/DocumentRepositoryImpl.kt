package com.app.android.data.repoImp

import android.content.Context
import android.net.Uri
import com.app.android.data.dataSource.dataSourcesSolicitudes.DocumentApi
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.entities.entitiesSolicitudes.UploadedDocument
import com.app.android.domain.repoInterface.repoIntSolicitudes.DocumentRepository
import com.google.gson.JsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class DocumentRepositoryImpl @Inject constructor(
    private val api: DocumentApi,
    @ApplicationContext private val context: Context,
) : DocumentRepository {

    override suspend fun uploadDocument(
        solicitudId: String,
        type: DocumentType,
        file: PickedFile,
    ): Result<UploadedDocument> = withContext(Dispatchers.IO) {
        val bytes = leerBytes(file.uri)
            ?: return@withContext Result.failure(
                IllegalStateException("No se pudo leer el archivo seleccionado."),
            )

        // "INE" o "DOMICILIO": coincide con lo que valida el backend
        val tipo = type.name.toRequestBody("text/plain".toMediaType())
        val archivo = MultipartBody.Part.createFormData(
            "archivo",
            file.name,
            bytes.toRequestBody(file.mimeType.toMediaType()),
        )

        try {
            val dto = api.uploadDocument(solicitudId, tipo, archivo)
            Result.success(UploadedDocument(type, dto.nombreArchivo, dto.tamanoBytes))
        } catch (e: HttpException) {
            Result.failure(IllegalStateException(e.mensajeDelServidor()))
        } catch (e: IOException) {
            Result.failure(IllegalStateException("No se pudo conectar con el servidor."))
        }
    }

    private fun leerBytes(uri: String): ByteArray? = try {
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
    } catch (e: IOException) {
        null
    } catch (e: SecurityException) {
        null
    }

    // La API responde errores como {"message": "..."}; ese texto llega al Snackbar
    private fun HttpException.mensajeDelServidor(): String {
        val cuerpo = response()?.errorBody()?.string()
        val mensaje = runCatching {
            JsonParser.parseString(cuerpo).asJsonObject.get("message").asString
        }.getOrNull()
        return mensaje ?: "Error del servidor (${code()})."
    }
}