package com.app.android.perfiles

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.android.domain.entities.entitiesPerfiles.validarRegistro
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.viewsPerfiles.RegistroScreen
import com.app.android.presentation.viewmodel.viewModelPerfiles.RegistroUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerfilesUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cancelarSigueDisponibleDuranteElEnvio() {
        var cancellations = 0
        compose.setContent { AndroidTheme { RegistroScreen(RegistroUiState(isLoading = true), { _, _ -> }, {}, {}, { cancellations++ }) } }
        compose.onNodeWithTag("register_cancel").assertIsEnabled().performClick()
        assertEquals(1, cancellations)
    }

    @Test fun registroVacioMuestraErroresYEnfocaElPrimerCampo() {
        val state = mutableStateOf(RegistroUiState())
        compose.setContent { AndroidTheme { RegistroScreen(state.value, { _, _ -> }, {}, {
            state.value = state.value.copy(campos = validarRegistro(state.value.datos))
        }, {}) } }
        compose.onNodeWithTag("register_submit").performScrollTo().performClick()
        compose.onNodeWithTag("register_nombre").assertIsFocused()
        compose.onNodeWithText("Completa este campo (máximo 100 caracteres).").assertIsDisplayed()
        compose.onNodeWithTag("register_cancel").assertIsDisplayed()
    }
}
