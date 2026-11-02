package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.domain.entities.entitiesSolicitudes.AdoptionForm
import com.app.android.presentation.viewmodel.viewModelSolicitudes.AdoptionFormUiState

@Composable
fun AdoptionFormContent(
    state: AdoptionFormUiState,
    onFormChange: (AdoptionForm) -> Unit,
    onSubmitClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Definición del formulario de adopción
    val form = state.form

    // Paso 1 del formulario: "Datos generales"
    Column {
    }
}
