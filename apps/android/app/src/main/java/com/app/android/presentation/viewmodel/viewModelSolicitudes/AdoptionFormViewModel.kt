package com.app.android.presentation.viewmodel.viewModelSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.AdoptionForm
import com.app.android.domain.entities.entitiesSolicitudes.AdoptionFormErrors
import com.app.android.domain.useCases.useCasesSolicitudes.SubmitAdoptionRequestUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.ValidateAdoptionFormUseCase
import javax.inject.Inject

// Maquina de estados de ViewModel para AdoptionFormScreen

data class AdoptionFormUiState(
    val form: AdoptionForm = AdoptionForm(),
    val errors: AdoptionFormErrors = AdoptionFormErrors(),
    val triedToSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val submitError: Boolean = false,
)

class AdoptionFormViewModel
    @Inject
    constructor(
        private val validateForm: ValidateAdoptionFormUseCase,
        private val submitRequest: SubmitAdoptionRequestUseCase,
    )
