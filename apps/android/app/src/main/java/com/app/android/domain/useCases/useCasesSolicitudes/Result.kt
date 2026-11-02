package com.app.android.domain.useCases.useCasesSolicitudes

// Archivo result para el módulo de solicitudes de adopción
// Queda aclarar si lo vamos a utilizar entre todos o no

//  El Result lo necesito para que el viewModel y
//  el usecase se comuniquen

sealed class Result<out T> {
    data class Success<out T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val exception: Throwable,
    ) : Result<Nothing>()

    data object Loading : Result<Nothing>()
}
