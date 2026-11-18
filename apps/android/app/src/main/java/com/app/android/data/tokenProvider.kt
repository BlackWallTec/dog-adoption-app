package com.app.android.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface TokenProvider {
    fun token(): String?

    
    /** true si quedó guardado. */
    fun save(token: String): Boolean

    fun clear()
}

// Token del JWT cifrado con EncryptedSharedPreferences, llave en Android Keystore
@Singleton
class EncryptedTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : TokenProvider {

    private val prefs: SharedPreferences? by lazy { abrir() }

    override fun token(): String? =
        checkNotNull(prefs) { "No se pudo abrir el almacenamiento de sesión" }.getString(KEY_TOKEN, null)

    // commit() (síncrono) para saber si realmente se guardó
    override fun save(token: String): Boolean =
        try { prefs?.edit()?.putString(KEY_TOKEN, token)?.commit() == true } catch (_: Exception) { false }

    override fun clear() {
        check(prefs?.edit()?.remove(KEY_TOKEN)?.commit() == true) { "No se pudo borrar la sesión" }
    }

    private fun crear(): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        ARCHIVO,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    // Si el archivo o la llave del Keystore se corrompen, se recrea (el usuario vuelve a iniciar sesión).
    private fun abrir(): SharedPreferences? = try {
        crear()
    } catch (_: Exception) {
        context.deleteSharedPreferences(ARCHIVO)
        try { crear() } catch (_: Exception) { null }
    }

    private companion object {
        const val ARCHIVO = "perfiles_sesion_cifrada"
        const val KEY_TOKEN = "token"
    }
}
