package com.app.android.data.session

import android.annotation.SuppressLint
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.AlmacenSesion
import com.google.gson.Gson
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Solo guarda la sesión cifrada. La contraseña nunca se escribe al dispositivo. */
// commit permite comprobar el guardado antes de aceptar la sesión; el repositorio usa Dispatchers.IO.
@SuppressLint("ApplySharedPref", "UseKtx")
class AlmacenSesionSeguro(context: Context) : AlmacenSesion {
    private val preferencias = context.getSharedPreferences("perfiles_sesion", Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val gson = Gson()
    @Volatile private var cache: Sesion? = null
    override val actual: Sesion? get() = cache

    private fun clave(): SecretKey {
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build())
        }.generateKey()
    }

    @Synchronized override fun guardar(sesion: Sesion) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, clave())
        val encrypted = cipher.doFinal(gson.toJson(sesion).toByteArray(Charsets.UTF_8))
        val encoded = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP)
        check(preferencias.edit().putString("sesion", encoded).commit()) { "No se pudo guardar la sesión" }
        cache = sesion
    }

    @Synchronized override fun cargar(): Sesion? {
        val encoded = preferencias.getString("sesion", null) ?: return null.also { cache = null }
        return try {
            val parts = encoded.split(':', limit = 2)
            check(parts.size == 2)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
            val json = cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)).toString(Charsets.UTF_8)
            val sesion = gson.fromJson(json, Sesion::class.java)
            check(!sesion.token.isNullOrBlank() && !sesion.usuario.id.isNullOrBlank() && !sesion.usuario.email.isNullOrBlank() && !sesion.usuario.rol.isNullOrBlank())
            sesion.also { cache = it }
        } catch (_: Exception) {
            limpiar()
            null
        }
    }

    @Synchronized override fun limpiar() {
        cache = null
        // Invalida también el cifrado anterior aunque un respaldo antiguo conserve el archivo.
        if (keyStore.containsAlias(ALIAS)) keyStore.deleteEntry(ALIAS)
        preferencias.edit().remove("sesion").commit()
    }

    private companion object { const val ALIAS = "qarino_perfiles_sesion_v1" }
}
