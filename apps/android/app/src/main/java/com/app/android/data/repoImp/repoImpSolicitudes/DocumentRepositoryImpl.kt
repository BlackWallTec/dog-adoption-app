package com.app.android.data.repoImp.repoImpSolicitudes

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
import android.util.Log

//This file prepares the the local information to send it to the backend
//It implements the interface from domain and works a little bit with the errors
//With context & dispatcher.io manages the asynchronous execution of the graphic interface 
//main function
class DocumentRepositoryImpl @Inject constructor(
    //we want to get the api
    private val api: DocumentApi,
    //we want to get the context to open photos
    @ApplicationContext private val context: Context,
) : DocumentRepository {
    //we have a function to send the data that we recieved from the user
    override suspend fun uploadDocument(
        //we send the id
        solicitudId: String,
        //type od document
        type: DocumentType,
        //and the file
        file: PickedFile,
        //Calls the auxiliary function to convert the file into bytes
    ): Result<UploadedDocument> = withContext(Dispatchers.IO) {
        val bytes = leerBytes(file.uri)
            ?: return@withContext Result.failure(
                IllegalStateException("No se pudo leer el archivo seleccionado."),
            )

        // Opens the channel of lecture of the bytes of the docuemnt so that it can be processed
        val tipo = type.name.toRequestBody("text/plain".toMediaType())
        val archivo = MultipartBody.Part.createFormData(
            "archivo",
            file.name,
            bytes.toRequestBody(file.mimeType.toMediaType()),
        )

        try {
            val dto = api.uploadDocument(solicitudId, tipo, archivo)
            Result.success(UploadedDocument(type, dto.nombreArchivo, dto.tamanoBytes ?: 0L))
        } catch (e: HttpException) {
            Result.failure(IllegalStateException(e.mensajeDelServidor()))
        } catch (e: IOException) {
            Log.e("DocumentRepo", "Fallo de red", e)
            Result.failure(IllegalStateException("No se pudo conectar con el servidor."))
        }
    }

    //Function to read the bytes of the document
    private fun leerBytes(uri: String): ByteArray? = try {
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
    } catch (e: IOException) {
        null
    } catch (e: SecurityException) {
        null
    }

    //We send the message of mistake if it is the case that everything is not connected
    private fun HttpException.mensajeDelServidor(): String {
        val cuerpo = response()?.errorBody()?.string()
        val mensaje = runCatching {
            JsonParser.parseString(cuerpo).asJsonObject.get("message").asString
        }.getOrNull()
        return mensaje ?: "Error del servidor (${code()})."
    }
}