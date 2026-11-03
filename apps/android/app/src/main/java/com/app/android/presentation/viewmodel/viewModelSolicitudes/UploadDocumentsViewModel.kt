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

@HiltViewModel
class UploadDocumentsViewModel @Inject constructor(
    private val uploadDocument: UploadDocumentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadDocumentsUiState())
    val uiState: StateFlow<UploadDocumentsUiState> = _uiState.asStateFlow()

    // 1. Guarda el archivo ÚNICAMENTE de forma local (Sin petición de red)
    fun onFileSelected(type: DocumentType, file: PickedFile) {
        updateSlot(type) {
            DocumentSlot(
                fileName = file.name,
                fileInfo = "${file.sizeBytes.toReadableSize()} · Listo para subir",
                pickedFile = file // Guardamos la referencia para enviarla al presionar Continuar
            )
        }
    }

    // 2. Se llama al dar clic en el botón "Continuar"
    fun onContinuarClicked(solicitudId: String, onSuccess: () -> Unit) {
        val slots = _uiState.value.slots

        // Marcamos todos los slots como cargando en la UI
        _uiState.update { state ->
            state.copy(
                slots = slots.mapValues { it.value.copy(isUploading = true) },
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                // Subimos cada documento acumulado
                for ((type, slot) in slots) {
                    val file = slot.pickedFile ?: continue

                    val result = uploadDocument(solicitudId, type, file)
                    if (result.isFailure) {
                        throw result.exceptionOrNull() ?: Exception("Error al subir ${type.name}")
                    }
                }

                // Finalizado con éxito -> Restablecemos estado y llamamos al callback de navegación
                _uiState.update { state ->
                    state.copy(slots = slots.mapValues { it.value.copy(isUploading = false) })
                }
                onSuccess()

            } catch (error: Exception) {
                // Si algo falla, quitamos el indicador de carga y mostramos el snackbar
                _uiState.update { state ->
                    state.copy(
                        slots = slots.mapValues { it.value.copy(isUploading = false) },
                        errorMessage = error.message ?: "No se pudieron subir los documentos"
                    )
                }
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun updateSlot(type: DocumentType, transform: (DocumentSlot) -> DocumentSlot) {
        _uiState.update { state ->
            state.copy(slots = state.slots + (type to transform(state.slots.getValue(type))))
        }
    }

    private fun Long.toReadableSize(): String =
        if (this >= 1_048_576) "%.1f MB".format(this / 1_048_576.0) else "${this / 1024} KB"
}