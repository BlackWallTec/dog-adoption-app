package com.app.android.solicitudes

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.IndicadorPasos
import com.app.android.presentation.view.atomicDesign.page.DatosGeneralesScreen
import com.app.android.presentation.view.atomicDesign.page.RequisitosScreen
import com.app.android.presentation.view.ui.theme.AndroidTheme
import org.junit.Rule
import org.junit.Test

// HU02: Como adoptante quiero ver en qué paso de la solicitud voy
class HU02ProgresoSolicitudTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun el_indicador_muestra_el_paso_y_su_nombre() {
        composeRule.setContent {
            AndroidTheme { IndicadorPasos(pasoActual = 2) }
        }
        composeRule.onNodeWithText("Paso 3 de 6 · Tu hogar").assertExists()
    }

    @Test
    fun un_paso_fuera_de_rango_se_queda_en_el_ultimo() {
        composeRule.setContent {
            AndroidTheme { IndicadorPasos(pasoActual = 10) }
        }
        composeRule.onNodeWithText("Paso 6 de 6 · Confirmar").assertExists()
    }

    @Test
    fun requisitos_es_el_paso_1() {
        composeRule.setContent {
            AndroidTheme {
                RequisitosScreen(
                    aceptado = false,
                    onAceptadoChange = {},
                    cargando = false,
                    errorMensaje = null,
                    onBackClick = {},
                    onContinuarClick = {},
                )
            }
        }
        composeRule.onNodeWithText("Paso 1 de 6 · Requisitos").assertExists()
    }

    @Test
    fun datos_generales_es_el_paso_2() {
        composeRule.setContent {
            AndroidTheme {
                DatosGeneralesScreen(
                    cargando = false,
                    errorCarga = null,
                    datos = datosDePrueba,
                    edad = 31,
                    errorOcupacion = null,
                    onOcupacionChange = {},
                    guardando = false,
                    errorGuardar = null,
                    onReintentar = {},
                    onBackClick = {},
                    onContinuarClick = {},
                )
            }
        }
        composeRule.onNodeWithText("Paso 2 de 6 · Datos generales").assertExists()
    }

    private val datosDePrueba =
        DatosGenerales(
            nombreCompleto = "Sofía García López",
            fechaNacimiento = "1995-03-15",
            ocupacion = "",
            domicilio = "Calle Roble 24, Col. Álamos, Querétaro",
            municipio = "Querétaro",
            telefono = "442 123 4567",
            correo = "sofia@correo.com",
        )
}