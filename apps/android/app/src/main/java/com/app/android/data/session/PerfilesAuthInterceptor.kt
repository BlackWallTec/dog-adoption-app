package com.app.android.data.session

import com.app.android.domain.repoInterface.repoIntPerfiles.AlmacenSesion
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class PerfilesAuthInterceptor @Inject constructor(private val almacen: AlmacenSesion) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder().header("x-client", "mobile")
        val autenticando = request.url.encodedPath in setOf("/api/perfiles/login", "/api/perfiles/register")
        if (!autenticando) almacen.actual?.token?.let { builder.header("Authorization", "Bearer $it") }
        return chain.proceed(builder.build())
    }
}
