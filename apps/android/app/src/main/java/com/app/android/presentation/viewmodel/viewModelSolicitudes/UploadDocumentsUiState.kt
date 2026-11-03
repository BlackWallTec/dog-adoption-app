package com.app.android.presentation.viewmodel.viewModelSolicitudes

import android.net.Uri
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile

//State of one card
data class DocumentSlot(
    val fileUri: Uri? = null,
    val fileName: String? = null,
    val fileInfo: String = "",
    val isUploading: Boolean = false,
    val pickedFile: PickedFile? = null
) {
    val isLoaded: Boolean get() = fileName != null
}

//State of the whole screen
data class UploadDocumentsUiState(
    val slots: Map<DocumentType, DocumentSlot> =
        DocumentType.entries.associateWith{ DocumentSlot ()},
    val isSubmittingAll: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    //It takes the "picture" of the screen
    val isUploading: Boolean get() = slots.values.any {it.isUploading}
    val canContinue: Boolean get() = slots.values.all {it.isLoaded} && !isUploading
}
