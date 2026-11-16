package com.app.android.data

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


// Establece el header para todas las peticiones
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val b = chain.request().newBuilder().header("x-client", "mobile")
        tokenProvider.token()?.takeIf { it.isNotBlank() }?.let { b.header("Authorization", "Bearer $it") }
        return chain.proceed(b.build())
    }
}