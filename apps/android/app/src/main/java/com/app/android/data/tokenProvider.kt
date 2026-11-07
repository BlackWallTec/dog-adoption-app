package com.app.android.data

import javax.inject.Inject

interface TokenProvider {
    fun token (): String?
}

class TemporaryTokenProvider @Inject constructor(): TokenProvider{
    override fun token(): String? = ""

}

