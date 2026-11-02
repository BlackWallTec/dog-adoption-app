package com.app.android.presentation.view.viewsSolicitudes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.domain.entities.entitiesSolicitudes.AdoptionForm
import com.app.android.presentation.view.atomicDesign.page.CompromisoScreen
import com.app.android.presentation.view.atomicDesign.page.ConfirmarScreen
import com.app.android.presentation.view.atomicDesign.page.DatosGeneralesScreen
import com.app.android.presentation.view.atomicDesign.page.TuHogarScreen
import com.app.android.presentation.viewmodel.viewModelSolicitudes.AdoptionFormUiState

// Declarar que screen se va a ver

// Las diferentes páginas que tienen que enviar o recibir información

// Añadir Documentos al enlazar
enum class AdoptionStep { DatosGenerales, TuHogar, Compromiso, Confirmar }

@Composable
fun AdoptionFormContent(
    state: AdoptionFormUiState,
    onFormChange: (AdoptionForm) -> Unit,
    onNextClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Guardamos las views en el estado step
    when (state.step) {
        AdoptionStep.DatosGenerales -> {
            DatosGeneralesScreen(
                form = state.form,
                errors = state.errors,
                onFormChange = onFormChange,
                onNextClick = onNextClick,
                onBackClick = onBackClick,
                modifier = modifier,
            )
        }

        AdoptionStep.TuHogar -> {
            TuHogarScreen(
                form = state.form,
                errors = state.errors,
                onFormChange = onFormChange,
                onNextClick = onNextClick,
                onBackClick = onBackClick,
                modifier = modifier,
            )
        }

        AdoptionStep.Compromiso -> {
            CompromisoScreen(
                form = state.form,
                errors = state.errors,
                onFormChange = onFormChange,
                onNextClick = onNextClick,
                onBackClick = onBackClick,
                modifier = modifier,
            )
        }

        /* AdoptionStep.Documentos -> {
            DocumentosScreen(
                form = state.form,
                errors = state.errors,
                onFormChange = onFormChange,
                onNextClick = onNextClick,
                onBackClick = onBackClick,
                modifier = modifier,
            )
        }
         */

        AdoptionStep.Confirmar -> {
            ConfirmarScreen(
                form = state.form,
                isSubmitting = state.isSubmitting,
                submitError = state.submitError,
                onSubmitClick = onSubmitClick,
                onBackClick = onBackClick,
                modifier = modifier,
            )
        }
    }
}
