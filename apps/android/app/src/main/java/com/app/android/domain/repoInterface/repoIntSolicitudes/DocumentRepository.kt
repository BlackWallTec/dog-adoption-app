package com.app.android.domain.repoInterface.repoIntSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.entities.entitiesSolicitudes.UploadedDocument

//interface defines a function without body
//suspend: freezes the screen while the file is being uploaded
interface DocumentRepository {
    suspend fun uploadDocument(
        type: DocumentType,
        file: PickedFile
        //instead of throwig exception, it returns a box with exit or fail
    ): Result<UploadedDocument>
}