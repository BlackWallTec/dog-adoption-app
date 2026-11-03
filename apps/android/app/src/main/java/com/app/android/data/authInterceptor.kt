package com.app.android.data

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor (
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept (chain: Interceptor.Chain): Response {
        val token = tokenProvider.token()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Autorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}