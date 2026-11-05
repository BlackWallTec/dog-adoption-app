package com.app.android.perfiles

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.app.android.data.session.AlmacenSesionSeguro
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerfilesSessionStorageTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val storage = AlmacenSesionSeguro(context)
    private val preferences get() = context.getSharedPreferences("perfiles_sesion", Context.MODE_PRIVATE)
    @After fun cleanup() { storage.limpiar() }

    @Test fun cifraLaSesionYLaRecuperaEnUnaInstanciaNueva() {
        val session = Sesion(Usuario("usuario-temporal", "storage@example.test", "Cliente"), "token-temporal-de-prueba")
        storage.guardar(session)
        val persisted = preferences.getString("sesion", "").orEmpty()
        assertFalse(persisted.contains(session.token))
        assertFalse(persisted.contains(session.usuario.email))
        assertEquals(session, AlmacenSesionSeguro(context).cargar())
        storage.limpiar()
        assertNull(AlmacenSesionSeguro(context).cargar())
    }

    @Test fun datosAlteradosNoSeRestauranComoSesionValida() {
        preferences.edit().putString("sesion", "valor-alterado").commit()
        assertNull(storage.cargar())
        assertNull(preferences.getString("sesion", null))
    }
}
