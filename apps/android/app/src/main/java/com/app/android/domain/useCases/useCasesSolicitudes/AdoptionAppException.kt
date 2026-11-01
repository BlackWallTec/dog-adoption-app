package com.app.android.domain.useCases.useCasesSolicitudes

// Definir los errores que la aplicación entiende (no el backend)
// Este se diferencia de result y de uiState porque
// Result (emit) dice en qué va la operación | Lo crea el UseCase
// UiState Lo que la pantalla necesita | Lo crea el view model
// Exception (throw) Dice que salio mal | Lo crea el repositorio
// No tenemos common entonces lo pongo aqui ya que se usara para el viewmodel
// Estos casos igual pueden llegar a ser de uso para todos
sealed class AdoptionAppException(
    message: String? = null,
) : Exception(message)
