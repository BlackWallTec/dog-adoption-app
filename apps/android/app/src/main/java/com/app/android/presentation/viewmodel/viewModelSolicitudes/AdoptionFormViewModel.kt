package com.app.android.presentation.viewmodel.viewModelSolicitudes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesSolicitudes.AdoptionForm
import com.app.android.domain.entities.entitiesSolicitudes.AdoptionRequest
import com.app.android.domain.useCases.useCasesSolicitudes.AdoptionAppException
import com.app.android.domain.useCases.useCasesSolicitudes.Result
import com.app.android.domain.useCases.useCasesSolicitudes.SubmitAdoptionRequestUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.ValidateAdoptionFormUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Maquina de estados de ViewModel para AdoptionFormScreen

class AdoptionFormViewModel
    @Inject
    constructor(
        private val validateForm: ValidateAdoptionFormUseCase,
        private val submitRequest: SubmitAdoptionRequestUseCase,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        // El id del perro que se va a adoptar
        private val dogId: String = checkNotNull(savedStateHandle["dogId"])
        private val _uiState = MutableStateFlow(AdoptionFormUiState())
        val uiState: StateFlow<AdoptionFormUiState> = _uiState.asStateFlow()

        // Evento: el usuario escribió algo en un campo
        fun onFormChange(newForm: AdoptionForm) {
            _uiState.update { state ->
                state.copy(
                    form = newForm,
                    // después del primer intento de envio, los errores se corrigen en vivo
                    errors = if (state.triedToSubmit) validateForm(newForm) else state.errors,
                    submitError = null,
                )
            }
        }

        // Evento: el usuario presionó el botón de enviar solicitud
        fun onSubmitClick() {
            val current = _uiState.value
            if (current.isSubmitting) return // evita doble envio

            val errors = validateForm(current.form)
            if (errors.hasErrors()) {
                _uiState.update {
                    it.copy(
                        errors = errors,
                        triedToSubmit = true,
                    )
                }
                return // no se envia nada
            }
            viewModelScope.launch {
                submitRequest(AdoptionRequest(dogId, current.form)).collect { result ->
                    _uiState.update { state ->
                        when (result) {
                            is Result.Loading -> {
                                state.copy(
                                    isSubmitting = true,
                                    triedToSubmit = true,
                                    submitError = null,
                                )
                            }

                            is Result.Success -> {
                                state.copy(
                                    isSubmitting = false,
                                    isSubmitted = true,
                                )
                            }

                            is Result.Error -> {
                                state.copy(
                                    isSubmitting = false,
                                    submitError = mensajeDeError(result.exception),
                                )
                            }
                        }
                    }
                }
            }
        }

        // El mensaje técnico va al log; el mensaje humano a la pantalla
        private fun mensajeDeError(e: Throwable): String =
            when (e) {
                is AdoptionAppException.NoConnection -> "Sin conexión. Revisa tu internet e intentalo de nuevo."
                is AdoptionAppException.UnAuthorized -> "Tu sesión expiró. Inicia sesión de nuevo."
                is AdoptionAppException.Rejected -> "Revisa los datos de tu formulario"
                is AdoptionAppException.Server -> "Hubo un problema con el servidor. Intentelo más tarde."
                else -> "No pudimos enviar tu solicitud."
            }
    }
