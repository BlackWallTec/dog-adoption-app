package com.app.android.presentation.view.viewsSolicitudes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.domain.entities.entitiesSolicitudes.AdoptionForm

// Declarar que screen se va a ver

// Las diferentes páginas que tienen que enviar o recibir información ROUTES

// Añadir lo de los demás al enlazar
enum class AdoptionStep { TuHogar, Compromiso, Confirmar }

@Composable
fun AdoptionFormContent(
    state: AdoptionFormUiState,
    onFormChange: (AdoptionForm) -> Unit,
    onNextClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onBackClick: () -> Unit, // regresar al anterior
    modifier: Modifier = Modifier,
) {
    // Guardamos las views en el estado step
    when (state.step) {
        AdoptionStep.TuHogar -> {
            TuHogarRoute(
                onBackClick = onBackClick,
                onSubmitted = onNextClick,
                form = state.form,
                errors = state.errors,
                onFormChange = onFormChange,
                onNextClick = onNextClick,
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
