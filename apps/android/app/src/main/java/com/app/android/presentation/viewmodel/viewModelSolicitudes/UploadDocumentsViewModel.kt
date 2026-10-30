package com.app.android.presentation.viewmodel.viewModelSolicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.useCases.useCasesSolicitudes.UploadDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//What does this class do?
//It recieves the event, meaning the document that the user chose
//Based on the upload if ti was succesfull or not, it updates the state based on this upload
@HiltViewModel
class UploadDocumentsViewModel  @Inject constructor (
    private val uploadDocument: UploadDocumentUseCase
): ViewModel(){
    //it can only be changed by the view Model
    private val _uiState = MutableStateFlow(UploadDocumentsUiState())
    //Variable that only the view looks
    val uiState: StateFlow<UploadDocumentsUiState> = _uiState.asStateFlow()

    //Event that comes through the view
    fun onFileSelected (type: DocumentType, file: PickedFile) {
        updateSlot(type) {it.copy(isUploading = true)}

        viewModelScope.launch {
            uploadDocument(type, file)
                //When the upload has been succesfull
                .onSuccess { doc ->
                    updateSlot(type) {
                        DocumentSlot(
                            fileName = doc.fileName,
                            fileInfo = "${doc.sizeBytes.toReadableSize()} · Carga completa"
                        )
                    }
                }
                //When it fails
                .onFailure { error ->
                    updateSlot(type) { it.copy(isUploading = false) }
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "No se pudo subir el archivo")
                    }
                }
        }
    }
    //Function to show the errors when the page cannot be loaded
    fun onErrorShown() {
        _uiState.update {it.copy(errorMessage = null)}
    }

    //This function uploads the state of the event succesfull or not
    private fun updateSlot (type: DocumentType, transform: (DocumentSlot) -> DocumentSlot) {
        _uiState.update { state ->
            state.copy(slots = state.slots + (type to transform(state.slots.getValue(type))))
        }
    }
    //We get the size of the file
    private fun Long.toReadableSize(): String =
        if (this >= 1_048_576) ".1f MB".format(this/1_048_576.0) else "${this/1024} KB"
}