package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.entities.entitiesSolicitudes.UploadedDocument
import com.app.android.domain.repoInterface.repoIntSolicitudes.DocumentRepository
import jakarta.inject.Inject
import kotlinx.coroutines.delay

//It simulates the upload and then it is replaced by the RetriFit document
//it pretends to upload the file it works to see the app working without backend
class FakeDocumentRepository @Inject constructor(): DocumentRepository{
    override suspend fun uploadDocument(
        type: DocumentType,
        file: PickedFile
    ): Result<UploadedDocument> {
        //it simulates the time in the network
        delay(500)
        return Result.success(UploadedDocument(type, file.name, file.sizeBytes))
    }

}