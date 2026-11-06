package com.app.android.solicitudes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.app.android.presentation.view.atomicDesign.page.RequisitosScreen
import com.app.android.presentation.view.ui.theme.AndroidTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// HU11: Como adoptante quiero ver los requisitos antes de hacer una solicitud
class HU11RequisitosTest {
    // La "regla" que dibuja composables dentro del test
    @get:Rule
    val composeRule = createComposeRule()

    // Dibuja RequisitosScreen con un checkbox que sí funciona (como el preview)
    private fun mostrarPantalla(onContinuar: () -> Unit = {}) {
        composeRule.setContent {
            var aceptado by remember { mutableStateOf(false) }
            AndroidTheme {
                RequisitosScreen(
                    aceptado = aceptado,
                    onAceptadoChange = { aceptado = it },
                    cargando = false,
                    errorMensaje = null,
                    onBackClick = {},
                    onContinuarClick = onContinuar,
                )
            }
        }
    }

    @Test
    fun muestra_los_cuatro_requisitos_y_el_aviso_de_esterilizacion() {
        mostrarPantalla()

        composeRule.onNodeWithText("Requisitos").assertExists()
        composeRule.onNodeWithText("Ser mayor de edad").assertExists()
        composeRule.onNodeWithText("Identificación oficial").assertExists()
        composeRule.onNodeWithText("Comprobante de domicilio").assertExists()
        composeRule.onNodeWithText("Compromiso de cuidado y seguimiento").assertExists()
        composeRule.onNodeWithText("IMPORTANTE: La esterilización es requisito.").assertExists()
    }

    @Test
    fun continuar_esta_desactivado_hasta_aceptar_los_requisitos() {
        var continuoPicado = false
        mostrarPantalla(onContinuar = { continuoPicado = true })

        // 1. Sin aceptar: el botón está desactivado
        composeRule.onNodeWithText("Continuar").performScrollTo().assertIsNotEnabled()

        // 2. Palomea el checkbox
        composeRule.onNode(isToggleable()).performScrollTo().performClick()

        // 3. Ya se puede picar Continuar
        composeRule.onNodeWithText("Continuar").assertIsEnabled().performClick()
        assertTrue(continuoPicado)
    }
}