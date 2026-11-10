package com.app.android.data.dataSource.dataSourcesSolicitudes

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStorage @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun save(accessToken: String, refreshToken: String, expiresAtMillis: Long) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .putLong(KEY_EXPIRES_AT, expiresAtMillis)
            .apply()
    }

    fun accessToken(): String? = prefs.getString(KEY_ACCESS, null)

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_EXPIRES_AT = "expires_at"
    }
}