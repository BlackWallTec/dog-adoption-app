package com.app.android.perfiles

import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.app.android.MainActivity
import java.util.UUID
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Se ejecuta expresamente con perfilesLive=true y un correo temporal único. */
@RunWith(AndroidJUnit4::class)
class PerfilesLiveTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val arguments get() = InstrumentationRegistry.getArguments()

    @Before fun requireLiveSetup() {
        assumeTrue(arguments.getString("perfilesLive") == "true")
        if (Build.VERSION.SDK_INT >= 37) {
            instrumentation.uiAutomation.grantRuntimePermission("com.app.android", "android.permission.ACCESS_LOCAL_NETWORK")
            compose.activityRule.scenario.recreate()
        }
    }
    private fun waitFor(tag: String) {
        compose.waitUntil(45000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun enter(tag: String, value: String) { compose.onNodeWithTag(tag).performScrollTo().performTextInput(value) }

    @Test fun registroYLoginRealesConSesionMovil() {
        val email = requireNotNull(arguments.getString("perfilesTestEmail"))
        val password = "Prueba-" + UUID.randomUUID().toString()
        waitFor("login_email")
        compose.onNodeWithText("Regístrate").performScrollTo().performClick()
        waitFor("register_nombre")
        for ((field, value) in listOf(
            "nombre" to "Prueba temporal", "apellidos" to "Perfiles móvil",
            "email" to email, "telefono" to "+520000000000", "fechaNacimiento" to "2000-06-15",
            "calle" to "Registro temporal de verificación", "numeroExterior" to "0", "codigoPostal" to "00000",
            "passwd" to password, "confirmPasswd" to password,
        )) enter("register_$field", value)
        closeSoftKeyboard()
        compose.onNodeWithTag("register_privacidad").performScrollTo().performClick()
        compose.onNodeWithTag("register_submit").performScrollTo().performClick()
        waitFor("account_email")
        compose.onNodeWithTag("account_email").assertTextContains(email)
        waitFor("account_name")
        compose.onNodeWithTag("account_name").assertTextContains("Prueba temporal")
        compose.activityRule.scenario.recreate()
        waitFor("account_email")
        compose.onNodeWithTag("account_logout").performScrollTo().performClick()
        waitFor("login_email")
        enter("login_email", email)
        enter("login_password", password)
        closeSoftKeyboard()
        compose.onNodeWithTag("login_submit").performScrollTo().performClick()
        waitFor("account_email")
        compose.onNodeWithTag("account_email").assertTextContains(email)
        compose.onNodeWithTag("account_logout").performScrollTo().performClick()
        waitFor("login_email")
    }
}
