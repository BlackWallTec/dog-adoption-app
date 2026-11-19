package com.app.android.donativos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.viewsPerfiles.CuentaScreen
import com.app.android.presentation.viewmodel.viewModelPerfiles.SesionUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// HU45: el donante entra a sus datos fiscales desde Configuración
class AccesoDatosFiscalesTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun mostrarConfiguracion(
        onDatosFiscales: () -> Unit = {},
        onMisSolicitudes: () -> Unit = {},
    ) {
        val sesion = Sesion(usuario = Usuario(id = "prueba", rol = "Cliente"), token = "")
        composeRule.setContent {
            AndroidTheme {
                CuentaScreen(
                    state = SesionUiState(sesion = sesion, comprobando = false),
                    onRetry = {},
                    onCatalog = {},
                    onLogout = {},
                    onLogin = {},
                    onMisSolicitudes = onMisSolicitudes,
                    onDatosFiscales = onDatosFiscales,
                )
            }
        }
    }

    @Test
    fun muestra_el_acceso_a_datos_fiscales() {
        mostrarConfiguracion()
        composeRule.onNodeWithText("Datos fiscales").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Información para tus CFDI").assertIsDisplayed()
    }

    @Test
    fun al_tocar_datos_fiscales_navega() {
        var abrio = false
        mostrarConfiguracion(onDatosFiscales = { abrio = true })
        composeRule.onNodeWithText("Datos fiscales").performScrollTo().performClick()
        assertTrue(abrio)
    }

    @Test
    fun al_tocar_mis_solicitudes_navega() {
        var abrio = false
        mostrarConfiguracion(onMisSolicitudes = { abrio = true })
        composeRule.onNodeWithText("Mis solicitudes").performScrollTo().performClick()
        assertTrue(abrio)
    }
}
