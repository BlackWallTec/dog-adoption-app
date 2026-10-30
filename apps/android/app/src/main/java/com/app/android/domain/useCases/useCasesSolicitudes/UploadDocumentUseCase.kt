package com.app.android.domain.useCases.useCasesSolicitudes

//imports of the files for the data recieved and sent
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.entities.entitiesSolicitudes.UploadedDocument
import com.app.android.domain.repoInterface.repoIntSolicitudes.DocumentRepository

import javax.inject.Inject

//@Inject for Hilt
class UploadDocumentUseCase @Inject constructor(
    //it cannot be replaced
    private val repository: DocumentRepository
) {
    suspend operator fun invoke (
        type: DocumentType,
        file: PickedFile
    ): Result<UploadedDocument>{
        //if the type of file is not on the list
        if (file.mimeType !in ALLOWED_TYPES) {
            return Result.failure(
                IllegalArgumentException("Formato no permitido. Usa PDF, JPG o PNG")
            )
        }
        //case in whcich the file is bigger
        if (file.sizeBytes > MAX_SIZE_BYTES) {
            return Result.failure (
                IllegalArgumentException ("El archivo supera los 5MB")
            )
        }
        return repository.uploadDocument(type, file)
    }
    private companion object {
        val ALLOWED_TYPES = setOf ("application/pdf", "image/jpeg", "image/png")
        const val MAX_SIZE_BYTES = 5L * 1024 * 1024
    }
}