package com.app.android.presentation.viewmodel.viewModelSolicitudes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.DocumentoLocal
import com.app.android.domain.entities.entitiesSolicitudes.DocumentosSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.useCases.useCasesSolicitudes.GetDocumentosUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GuardarDocumentosUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.UploadDocumentUseCase
import com.app.android.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadDocumentsViewModel
    @Inject
    constructor(
        private val getDocumentosUseCase: GetDocumentosUseCase,
        private val guardarDocumentosUseCase: GuardarDocumentosUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(UploadDocumentsUiState())
        val uiState: StateFlow<UploadDocumentsUiState> = _uiState.asStateFlow()

        init {
            cargarDocumentosGuardados()
        }

        private fun cargarDocumentosGuardados() {
            try {
                val docs = getDocumentosUseCase()
                _uiState.update { state ->
                    val newSlots = state.slots.toMutableMap()

                    docs.identificacion?.let { doc ->
                        newSlots[DocumentType.INE] = doc.toSlot()
                    }
                    docs.comprobanteDomicilio?.let { doc ->
                        newSlots[DocumentType.DOMICILIO] = doc.toSlot()
                    }
                    state.copy(slots = newSlots)
                }
            } catch (_: Exception) {
                // Si no hay borrador o esta vacio
            }
        }

        // 1. Guarda el archivo ÚNICAMENTE de forma local (Sin petición de red)
        fun onFileSelected(
            type: DocumentType,
            file: PickedFile,
        ) {
            updateSlot(type) {
                DocumentSlot(
                    fileUri = Uri.parse(file.uri),
                    fileName = file.name,
                    fileInfo = "${file.sizeBytes.toReadableSize()} · Listo para subir",
                    pickedFile = file, // Guardamos la referencia para enviarla al presionar Continuar
                )
            }
        }

        // Presionar continuar valida y guarda en el BorradorRepositoryInt
        fun onContinuarClicked(onSuccess: () -> Unit) {
            val slots = _uiState.value.slots
            val ineFile = slots[DocumentType.INE]?.pickedFile
            val domFile = slots[DocumentType.DOMICILIO]?.pickedFile

            val documentos =
                DocumentosSolicitud(
                    identificacion =
                        ineFile?.let {
                            DocumentoLocal(
                                uri = it.uri,
                                nombre = it.name,
                                tamanoBytes = it.sizeBytes,
                                mimeType = it.mimeType,
                            )
                        },
                    comprobanteDomicilio =
                        domFile?.let {
                            DocumentoLocal(
                                uri = it.uri,
                                nombre = it.name,
                                tamanoBytes = it.sizeBytes,
                                mimeType = it.mimeType,
                            )
                        },
                )
            val errores = guardarDocumentosUseCase(documentos)

            if (errores.hayErrores) {
                val mensajeError =
                    listOfNotNull(
                        errores.identificacion,
                        errores.comprobanteDomicilio,
                    ).joinToString("\n")
                _uiState.update { it.copy(errorMessage = mensajeError) }
            } else {
                onSuccess()
            }
        }

        fun onErrorShown() {
            _uiState.update { it.copy(errorMessage = null) }
        }

        private fun updateSlot(
            type: DocumentType,
            transform: (DocumentSlot) -> DocumentSlot,
        ) {
            _uiState.update { state ->
                state.copy(slots = state.slots + (type to transform(state.slots.getValue(type))))
            }
        }

        private fun DocumentoLocal.toSlot(): DocumentSlot =
            DocumentSlot(
                fileUri = Uri.parse(uri),
                fileName = nombre,
                fileInfo = "${tamanoBytes.toReadableSize()} · Guardado",
                pickedFile = PickedFile(uri = uri, name = nombre, sizeBytes = tamanoBytes, mimeType = mimeType),
            )

        private fun Long.toReadableSize(): String = if (this >= 1_048_576) "%.1f MB".format(this / 1_048_576.0) else "${this / 1024} KB"
    }
