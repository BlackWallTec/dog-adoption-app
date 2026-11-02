package com.app.android.presentation.viewmodel.viewModelSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.AdoptionForm
import com.app.android.domain.entities.entitiesSolicitudes.AdoptionFormErrors

// Todo lo que el viewmodel va a necesitar para el formulario de adopción

data class AdoptionFormUiState(
    val form: AdoptionForm = AdoptionForm(),
    val errors: AdoptionFormErrors = AdoptionFormErrors(),
    val triedToSubmit: Boolean = false, // si ya presiono enviar
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val submitError: String? = null,
)
