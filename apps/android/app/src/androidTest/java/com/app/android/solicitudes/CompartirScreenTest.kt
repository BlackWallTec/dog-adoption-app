
package com.app.android.solicitudes

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.app.android.presentation.view.viewsCatalogo.ShareMenuContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CompartirScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun probarBotonCompartir() {

        var compartirPresionado = false

        composeRule.setContent {
            MaterialTheme {
                ShareMenuContent(
                    onCompartir = {
                        compartirPresionado = true
                    },
                    onCopiar = {}
                )
            }
        }

        composeRule
            .onNodeWithText("Compartir")
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(compartirPresionado)
        }
    }

    @Test
    fun probarBotonCopiarEnlace() {

        var copiarPresionado = false

        composeRule.setContent {
            MaterialTheme {
                ShareMenuContent(
                    onCompartir = {},
                    onCopiar = {
                        copiarPresionado = true
                    }
                )
            }
        }

        composeRule
            .onNodeWithText("Copiar enlace")
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(copiarPresionado)
        }
    }
}
