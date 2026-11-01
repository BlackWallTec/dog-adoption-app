package com.app.android.presentation.viewmodel.viewModelSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DocumentType

//State of one card
data class DocumentSlot(
    val fileName: String? = null,
    val fileInfo: String = "",
    val isUploading: Boolean = false
) {
    val isLoaded: Boolean get() = fileName != null
}

//State of the whole screen
data class UploadDocumentsUiState(
    val slots: Map<DocumentType, DocumentSlot> =
        DocumentType.entries.associateWith{ DocumentSlot ()},
    val errorMessage: String? = null
) {
    //It takes the "picture" of the screen
    val isUploading: Boolean get() = slots.values.any {it.isUploading}
    val canContinue: Boolean get() = slots.values.all {it.isLoaded} && !isUploading
}
